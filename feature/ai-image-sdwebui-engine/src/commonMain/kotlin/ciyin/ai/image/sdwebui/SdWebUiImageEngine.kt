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
import ciyin.platform.logger
import ciyin.sdwebui.SdWebUi
import ciyin.sdwebui.response.ProgressResponse
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

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

    private val logger = logger("AiImage.StableDiffusion")

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
        logger.d { "开始生图 engineId=${id.value} ${request.toSdWebUiImageEngineLogSummary()}" }
        coroutineScope {
            var lastPreviewContentHash: Int? = null
            val generation = async { sdWebUi(request) }
            while (!generation.isCompleted) {
                // `getProgress()` 底层 HTTP 在超时等场景会抛异常；若冒泡会打断整个 Flow，永远走不到 await().fold 的 Failed。
                val prog = try {
                    sdWebUi.stableDiffusion.getProgress().getOrNull()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Throwable) {
                    logger.v { "生图进度轮询 HTTP 异常 engineId=${id.value} ${e::class.simpleName} ${e.message}" }
                    null
                }
                prog?.let { p ->
                    val ratio = p.progress.coerceIn(0f, 1f)
                    val msg = formatProgressMessage(p)
                    logger.v { "生图进度轮询 engineId=${id.value} ratio=$ratio message=$msg" }
                    emit(
                        ImageEvent.Progress(
                            progress = ratio,
                            message = msg,
                        ),
                    )
                    val b64 = p.currentImage?.takeIf { it.isNotBlank() } ?: return@let
                    val bytes = runCatching { b64.fromSdWebUiBase64() }
                        .getOrNull()
                        ?.takeIf { it.isNotEmpty() }
                        ?: return@let
                    val h = bytes.contentHashCode()
                    if (h == lastPreviewContentHash) return@let
                    lastPreviewContentHash = h
                    val mime = mimeTypeForDecodedPreview(bytes)
                    logger.v { "生图实时预览 engineId=${id.value} bytes=${bytes.size} mime=$mime contentHash=$h" }
                    val image = GeneratedImage(
                        bytes = bytes,
                        mimeType = mime,
                    )
                    emit(ImageEvent.Preview(image))
                }
                delay(ProgressPollIntervalMs)
            }
            generation.await().fold(
                onSuccess = { result ->
                    logger.d {
                        "生图完成 engineId=${id.value} 张数=${result.images.size} " +
                                result.images.mapIndexed { i, img ->
                                    "#$i bytes=${img.bytes.size} mime=${img.mimeType}"
                                }.joinToString(" ") +
                                " info 键=${result.info.keys.joinToString(",")}"
                    }
                    emit(ImageEvent.Completed(result))
                },
                onFailure = { t ->
                    logger.e(t) {
                        "生图失败 engineId=${id.value} ${request.toSdWebUiImageEngineLogSummary()} cause=${t.message}"
                    }
                    emit(ImageEvent.Failed(t.toAiEngineError()))
                },
            )
        }
    }

    override suspend fun models(): List<ImageModelInfo> {
        logger.d { "拉取 SD WebUI 模型列表 engineId=${id.value}" }
        return sdWebUi.stableDiffusion.getModels().fold(
            onSuccess = { models ->
                val list = models.map { model ->
                    ImageModelInfo(
                        engineId = id,
                        model = model.title,
                        displayName = model.name.takeIf { it != model.title },
                        capabilities = capabilities.filterIsInstance<ImageCapability>().toSet(),
                    )
                }
                logger.d { "模型列表成功 engineId=${id.value} 数量=${list.size}" }
                list
            },
            onFailure = { e ->
                logger.e(e) { "模型列表失败 engineId=${id.value} cause=${e.message}" }
                emptyList()
            },
        )
    }

    /**
     * 拉取 SD WebUI 当前可用的 LoRA 列表。
     *
     * 底层请求失败时直接抛出原始异常，调用方可在业务层按需要转译为场景错误。
     */
    suspend fun loras(): List<SdWebUiLoraInfo> {
        logger.d { "拉取 SD WebUI LoRA 列表 engineId=${id.value}" }
        return sdWebUi.stableDiffusion.getLoras()
            .getOrThrow()
            .map { lora ->
                SdWebUiLoraInfo(
                    name = lora.name,
                    alias = lora.alias,
                    path = lora.path,
                    metadata = lora.metadata,
                )
            }
            .also { loras ->
                logger.d { "LoRA 列表成功 engineId=${id.value} 数量=${loras.size}" }
            }
    }

    override suspend fun validate(request: ImageRequest): Result<Unit> {
        logger.d { "校验生图请求 engineId=${id.value} ${request.toSdWebUiImageEngineLogSummary()}" }
        return runCatching {
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
        }.onSuccess {
            logger.d { "校验通过 engineId=${id.value}" }
        }.onFailure { e ->
            logger.w(e) { "校验未通过 engineId=${id.value} cause=${e.message}" }
        }
    }

    /**
     * 单行摘要：便于排查；不输出完整图像字节，提示词截断并压成一行。
     */
    private fun ImageRequest.toSdWebUiImageEngineLogSummary(): String {
        val mode = when (val src = source) {
            ImageSource.TextToImage -> "文生图"
            is ImageSource.ImageToImage ->
                "图生图 src=${src.sourceImage.size}B denoise=${src.denoisingStrength}"

            is ImageSource.Inpainting ->
                "局部重绘 src=${src.sourceImage.size}B mask=${src.mask.size}B denoise=${src.denoisingStrength}"
        }
        val promptOneLine = prompt.replace("\r\n", " ").replace('\n', ' ').replace('\r', ' ')
        val promptTail =
            if (promptOneLine.length > 120) promptOneLine.take(120) + "…" else promptOneLine
        val vendorKeys = vendorOptions.keys.takeIf { it.isNotEmpty() }?.joinToString(",") ?: "无"
        return "model=${model ?: "默认"} mode=$mode " +
                "${size.width}×${size.height} batch=$batch " +
                "steps=${steps ?: "默认"} cfg=${cfgScale ?: "默认"} seed=${seed ?: "随机"} " +
                "controls=${controls.size} postProcessors=${postProcessors.size} " +
                "vendorKeys=[$vendorKeys] " +
                "negLen=${negativePrompt?.length ?: 0} prompt(${prompt.length})=$promptTail"
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

    private companion object {
        /** 与 WebUI 轮询 `sdapi/v1/progress` 相近的间隔（毫秒）。 */
        private const val ProgressPollIntervalMs = 1000L
    }
}
