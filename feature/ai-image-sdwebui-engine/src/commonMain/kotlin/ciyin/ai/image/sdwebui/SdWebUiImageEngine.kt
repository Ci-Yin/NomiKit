package ciyin.ai.image.sdwebui

import ciyin.ai.core.capability.AiCapability
import ciyin.ai.core.capability.ImageCapability
import ciyin.ai.core.engine.EngineId
import ciyin.ai.core.engine.EngineRuntime
import ciyin.ai.core.engine.ImageEngine
import ciyin.ai.core.error.AiEngineError
import ciyin.ai.core.image.ImageEvent
import ciyin.ai.core.image.ImageModelInfo
import ciyin.ai.core.image.ImageRequest
import ciyin.ai.core.image.ImageSource
import ciyin.ai.image.sdwebui.mapper.invoke
import ciyin.ai.image.sdwebui.mapper.toAiEngineError
import ciyin.sdwebui.SdWebUi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * 基于 `feature/sdwebui` 的 [ImageEngine] 适配实现。
 *
 * 该类是一个薄适配层：
 * - 生成前把 [ImageRequest] 映射为 `txt2img` / `img2img` 过程；
 * - 生成后按需要串接 RemBG / Upscale；
 * - 所有错误统一折叠到 `ai-core` 的 [AiEngineError]。
 *
 * @property id 引擎实例 ID。
 * @property sdWebUi 已构造好的底层 SDK 入口。
 */
class SdWebUiImageEngine(
    override val id: EngineId,
    private val sdWebUi: SdWebUi,
) : ImageEngine {

    /**
     * 便利构造：由配置直接构造底层 [SdWebUi]。
     */
    constructor(config: SdWebUiImageEngineConfig) : this(
        id = config.id,
        sdWebUi = SdWebUi.Builder()
            .host(config.host)
            .port(config.port)
            .useHttps(config.useHttps)
            .build(),
    )

    override val provider: String = "sdwebui"

    override val runtime: EngineRuntime = EngineRuntime.RemoteSelfHosted

    override val capabilities: Set<AiCapability> = setOf(
        ImageCapability.TextToImage,
        ImageCapability.ImageToImage,
        ImageCapability.Inpainting,
        ImageCapability.ControlNet,
        ImageCapability.FaceDetailer,
        ImageCapability.FaceSwap,
        ImageCapability.BackgroundRemoval,
        ImageCapability.Upscale,
    )

    override fun generate(request: ImageRequest): Flow<ImageEvent> = flow {
        emit(ImageEvent.Started)
        sdWebUi(request)
            .onSuccess { emit(ImageEvent.Completed(it)) }
            .onFailure { emit(ImageEvent.Failed(it.toAiEngineError())) }
    }

    override suspend fun listModels(): Result<List<ImageModelInfo>> =
        sdWebUi.stableDiffusion.getModels().map { models ->
            models.map { model ->
                ImageModelInfo(
                    engineId = id,
                    model = model.title,
                    displayName = model.name.takeIf { it != model.title },
                    capabilities = capabilities.filterIsInstance<ImageCapability>().toSet(),
                )
            }
        }

    override suspend fun validate(request: ImageRequest): Result<Unit> = runCatching {
        val steps = request.steps
        val cfgScale = request.cfgScale
        require(request.batch > 0) { "batch 必须大于 0" }
        require(request.size.width > 0 && request.size.height > 0) { "输出尺寸必须大于 0" }
        require(steps == null || steps > 0) { "steps 必须大于 0" }
        require(cfgScale == null || cfgScale > 0f) { "cfgScale 必须大于 0" }

        request.controls.forEach { control ->
            when (control) {
                is ciyin.ai.core.image.ImageControl.ControlNet -> Unit
                is ciyin.ai.core.image.ImageControl.IPAdapter ->
                    error("SdWebUiImageEngine 暂不支持 IPAdapter")
            }
        }

        when (val source = request.source) {
            ImageSource.TextToImage -> Unit
            is ImageSource.ImageToImage -> require(source.sourceImage.isNotEmpty()) { "图生图输入图不能为空" }
            is ImageSource.Inpainting -> {
                require(source.sourceImage.isNotEmpty()) { "重绘原图不能为空" }
                require(source.mask.isNotEmpty()) { "重绘蒙版不能为空" }
            }
        }
    }
}
