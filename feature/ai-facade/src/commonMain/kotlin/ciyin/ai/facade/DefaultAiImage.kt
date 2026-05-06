package ciyin.ai.facade

import ciyin.ai.core.capability.ImageCapability
import ciyin.ai.core.engine.EngineId
import ciyin.ai.core.engine.ImageEngine
import ciyin.ai.core.image.ImageEvent
import ciyin.ai.core.image.ImageModelInfo
import ciyin.ai.core.image.ImagePostProcessor
import ciyin.ai.core.image.ImageRequest
import ciyin.ai.core.image.ImageSource
import ciyin.ai.core.registry.ImageEngineSelector
import ciyin.ai.facade.internal.EngineAttempt
import ciyin.ai.facade.internal.InvocationIds
import ciyin.ai.facade.internal.buildAttempts
import ciyin.ai.facade.internal.collectWithFallback
import ciyin.ai.facade.observability.AiInvocationListener
import ciyin.ai.facade.selection.EnginePreferences
import ciyin.ai.facade.selection.ImageEngineSpec
import kotlinx.coroutines.flow.Flow
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
    private val selector: ImageEngineSelector,
    private val preferences: EnginePreferences,
    private val listeners: List<AiInvocationListener> = emptyList(),
) : AiImage {

    override fun generate(
        request: ImageRequest,
        spec: ImageEngineSpec
    ): Flow<ImageEvent> = flow {
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
            errorOf = { event -> (event as? ImageEvent.Failed)?.error },
            isCompleted = { event -> event is ImageEvent.Completed },
            uncaughtFailureEvent = { err -> ImageEvent.Failed(err) },
        )
    }

    override suspend fun models(spec: ImageEngineSpec): List<ImageModelInfo> {
        val resolved = resolveRequestedSpec(spec)
        val deduped = LinkedHashMap<String, ImageModelInfo>()
        enginesForModelListing(resolved).forEach { engine ->
            engine.models().forEach { model ->
                deduped.getOrPut(model.model.lowercase()) { model }
            }
        }
        return deduped.values.toList()
    }

    private fun enginesForModelListing(resolved: ImageEngineSpec): List<ImageEngine> =
        when (resolved) {
            ImageEngineSpec.Default -> selector.all()
            is ImageEngineSpec.Explicit -> {
                listOf(
                    selector.select(
                        preferredId = resolved.engineId,
                    ),
                )
            }

            is ImageEngineSpec.ByCapability -> {
                if (resolved.required.isEmpty()) {
                    selector.all()
                } else {
                    selector.all().filter { engine ->
                        engine.capabilities.containsAll(resolved.required)
                    }
                }
            }
        }

    private suspend fun resolveRequestedSpec(spec: ImageEngineSpec): ImageEngineSpec = when (spec) {
        ImageEngineSpec.Default -> {
            when (val preferred = preferences.defaultImageSpec()) {
                ImageEngineSpec.Default -> ImageEngineSpec.ByCapability(emptySet())
                else -> preferred
            }
        }

        else -> spec
    }

    private fun resolveAttempt(
        spec: ImageEngineSpec,
        request: ImageRequest,
    ): EngineAttempt<ImageEngine, ImageEvent> = when (spec) {
        is ImageEngineSpec.Default -> {
            val engine = selector.select()
            engine.toAttempt(model = request.model, request = request)
        }

        is ImageEngineSpec.Explicit -> {
            val engine = selector.select(preferredId = spec.engineId)
            engine.toAttempt(model = request.model ?: spec.model, request = request)
        }

        is ImageEngineSpec.ByCapability -> {
            val engine = selector.select(required = spec.required)
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
        val engine = selector.all().firstOrNull { it.id == engineId } ?: return null
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