package ciyin.ai.image.sdwebui

import ciyin.ai.core.capability.AiCapability
import ciyin.ai.core.capability.ImageCapability
import ciyin.ai.core.engine.EngineId
import ciyin.ai.core.engine.EngineRuntime
import ciyin.ai.core.engine.ImageEngine
import ciyin.ai.core.error.AiEngineError
import ciyin.ai.core.image.GeneratedImage
import ciyin.ai.core.image.ImageEvent
import ciyin.ai.core.image.ImageModelInfo
import ciyin.ai.core.image.ImageRequest
import ciyin.ai.core.image.ImageSource
import ciyin.ai.image.sdwebui.mapper.fromSdWebUiBase64
import ciyin.ai.image.sdwebui.mapper.invoke
import ciyin.ai.image.sdwebui.mapper.toAiEngineError
import ciyin.sdwebui.SdWebUi
import ciyin.sdwebui.response.ProgressResponse
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.coroutines.cancellation.CancellationException

/**
 * 基于 `feature/sdwebui` 的 [ImageEngine] 适配实现。
 *
 * 该类是一个薄适配层：
 * - 生成前把 [ImageRequest] 映射为 `txt2img` / `img2img` 过程；
 * - 生成进行中轮询 `sdapi/v1/progress`，发出 [ImageEvent.Progress] 与 [ImageEvent.Preview]（依赖 WebUI 开启实时预览时才有 `current_image`）；
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
        try {
            coroutineScope {
                var lastPreviewContentHash: Int? = null
                val generation = async { sdWebUi(request) }
                while (!generation.isCompleted) {
                    sdWebUi.stableDiffusion.getProgress().getOrNull()?.let { prog ->
                        emit(
                            ImageEvent.Progress(
                                progress = prog.progress.coerceIn(0f, 1f),
                                message = formatProgressMessage(prog),
                            ),
                        )
                        val b64 = prog.currentImage?.takeIf { it.isNotBlank() } ?: return@let
                        val bytes = runCatching { b64.fromSdWebUiBase64() }
                            .getOrNull()
                            ?.takeIf { it.isNotEmpty() }
                            ?: return@let
                        val h = bytes.contentHashCode()
                        if (h == lastPreviewContentHash) return@let
                        lastPreviewContentHash = h
                        val image = GeneratedImage(
                            bytes = bytes,
                            mimeType = mimeTypeForDecodedPreview(bytes),
                        )
                        emit(ImageEvent.Preview(image))
                    }
                    delay(ProgressPollIntervalMs)
                }
                generation.await().fold(
                    onSuccess = { emit(ImageEvent.Completed(it)) },
                    onFailure = { emit(ImageEvent.Failed(it.toAiEngineError())) },
                )
            }
        } catch (e: CancellationException) {
            throw e
        }
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

    private companion object {
        /** 与 WebUI 轮询 `sdapi/v1/progress` 相近的间隔（毫秒）。 */
        private const val ProgressPollIntervalMs = 1000L
    }
}

private fun formatProgressMessage(prog: ProgressResponse): String? {
    val st = prog.state
    val stepPart =
        if (st.samplingSteps > 0) "采样 ${st.samplingStep}/${st.samplingSteps}" else null
    val info = prog.textInfo?.takeIf { it.isNotBlank() }
    return when {
        stepPart != null && info != null -> "$stepPart · $info"
        stepPart != null -> stepPart
        else -> info
    }
}

/**
 * SD WebUI 实时预览多为 JPEG，成品图多为 PNG；按魔数区分 MIME，便于上层解码。
 */
private fun mimeTypeForDecodedPreview(bytes: ByteArray): String =
    if (bytes.size >= 2 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte()) {
        "image/jpeg"
    } else {
        "image/png"
    }
