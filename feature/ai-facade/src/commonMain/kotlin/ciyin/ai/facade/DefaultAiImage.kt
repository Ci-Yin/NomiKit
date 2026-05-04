package ciyin.ai.facade

import ciyin.ai.core.capability.ImageCapability
import ciyin.ai.core.engine.EngineId
import ciyin.ai.core.engine.ImageEngine
import ciyin.ai.core.image.ImageEvent
import ciyin.ai.core.image.ImageEvent.Completed
import ciyin.ai.core.image.ImageEvent.Failed
import ciyin.ai.core.image.ImageModelInfo
import ciyin.ai.core.image.ImagePostProcessor
import ciyin.ai.core.image.ImageRequest
import ciyin.ai.core.image.ImageSource
import ciyin.ai.core.registry.EngineSelector
import ciyin.ai.facade.internal.EngineAttempt
import ciyin.ai.facade.internal.InvocationIds
import ciyin.ai.facade.internal.buildAttempts
import ciyin.ai.facade.internal.collectWithFallback
import ciyin.ai.facade.observability.AiInvocationListener
import ciyin.ai.facade.selection.EnginePreferences
import ciyin.ai.facade.selection.ImageModelSpec
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

/**
 * [AiImage] 的默认实现。
 *
 * 职责与 [DefaultAiChat] 对称：负责引擎选择、降级与观测，但不承担任何业务语义。
 *
 * @property selector 引擎选择器。
 * @property preferences 默认模型与降级策略提供者。
 * @property listeners 调用观测监听器列表。
 */
class DefaultAiImage(
    private val selector: EngineSelector,
    private val preferences: EnginePreferences,
    private val listeners: List<AiInvocationListener> = emptyList(),
) : AiImage {

    override fun generate(request: ImageRequest): Flow<ImageEvent> = flow {
        emitAll(generate(ImageModelSpec.Default, request))
    }

    override fun generate(spec: ImageModelSpec, request: ImageRequest): Flow<ImageEvent> = flow {
        val resolvedSpec = resolveRequestedSpec(spec)
        val fallbackPolicy = preferences.imageFallback()
        val primaryAttempt = resolveAttempt(resolvedSpec, request)
        val attempts = buildAttempts(
            primary = primaryAttempt,
            primaryId = primaryAttempt.engine.id,
            backupIds = fallbackPolicy.backupEngines,
            resolve = { backupId -> resolveBackupAttempt(backupId, request) },
        )
        collectWithFallback(
            attempts = attempts,
            policy = fallbackPolicy,
            invocationId = InvocationIds.next(),
            capability = request.primaryCapability(),
            listeners = listeners,
            engineIdOf = { it.id },
            errorOf = { event -> (event as? Failed)?.error },
            isCompleted = { event -> event is Completed },
            uncaughtFailureEvent = { err -> Failed(err) },
        )
    }

    override suspend fun listAvailableModels(): Result<List<ImageModelInfo>> {
        val failures = mutableListOf<Throwable>()
        val deduped = LinkedHashMap<String, ImageModelInfo>()

        selector.allImage().forEach { engine ->
            engine.listModels()
                .onSuccess { models ->
                    models.forEach { model ->
                        deduped.getOrPut(model.model.lowercase()) { model }
                    }
                }
                .onFailure { failures += it }
        }

        if (deduped.isNotEmpty()) {
            return Result.success(deduped.values.toList())
        }
        return Result.failure(
            failures.lastOrNull() ?: IllegalStateException("没有任何生图引擎返回可用模型"),
        )
    }

    private suspend fun resolveRequestedSpec(spec: ImageModelSpec): ImageModelSpec = when (spec) {
        ImageModelSpec.Default -> {
            when (val preferred = preferences.defaultImageSpec()) {
                ImageModelSpec.Default -> ImageModelSpec.ByCapability(emptySet())
                else -> preferred
            }
        }

        else -> spec
    }

    private fun resolveAttempt(
        spec: ImageModelSpec,
        request: ImageRequest,
    ): EngineAttempt<ImageEngine, ImageEvent> = when (spec) {
        is ImageModelSpec.Default -> {
            val engine = selector.selectImage()
            engine.toAttempt(model = request.model, request = request)
        }

        is ImageModelSpec.Explicit -> {
            val engine = selector.selectImage(preferredId = spec.engineId)
            engine.toAttempt(model = spec.model ?: request.model, request = request)
        }

        is ImageModelSpec.ByCapability -> {
            val engine = selector.selectImage(required = spec.required)
            engine.toAttempt(model = request.model, request = request)
        }
    }

    /**
     * 解析一个备用生图引擎尝试。
     *
     * 与 [DefaultAiChat.resolveBackupAttempt] 同理：备用引擎只保留请求上已有的模型名，
     * 不复用主引擎的显式模型覆盖。
     */
    private fun resolveBackupAttempt(
        engineId: EngineId,
        request: ImageRequest,
    ): EngineAttempt<ImageEngine, ImageEvent>? {
        val engine = selector.allImage().firstOrNull { it.id == engineId } ?: return null
        return engine.toAttempt(model = request.model, request = request)
    }

    private fun ImageEngine.toAttempt(
        model: String?,
        request: ImageRequest,
    ): EngineAttempt<ImageEngine, ImageEvent> = EngineAttempt(
        engine = this,
        model = model,
        stream = { generate(request.withModel(model)) },
    )

    private fun ImageRequest.withModel(model: String?): ImageRequest =
        if (this.model == model) this else copy(model = model)

    private fun ImageRequest.primaryCapability(): ImageCapability =
        when (source) {
            ImageSource.TextToImage -> {
                when {
                    postProcessors.any { it is ImagePostProcessor.BackgroundRemoval } ->
                        ImageCapability.BackgroundRemoval

                    postProcessors.any { it is ImagePostProcessor.Upscale } ->
                        ImageCapability.Upscale

                    postProcessors.any { it is ImagePostProcessor.FaceSwap } ->
                        ImageCapability.FaceSwap

                    postProcessors.any { it is ImagePostProcessor.FaceDetailer } ->
                        ImageCapability.FaceDetailer

                    controls.isNotEmpty() -> ImageCapability.ControlNet
                    else -> ImageCapability.TextToImage
                }
            }

            is ImageSource.ImageToImage -> ImageCapability.ImageToImage
            is ImageSource.Inpainting -> ImageCapability.Inpainting
        }
}
