package ciyin.ai.core.image

/**
 * 一次生图调用的最终结果。
 *
 * 由 [ImageEvent.Completed] 携带向上传递。
 *
 * @property images 本次生成的全部图像（[ImageRequest.batch] 张）。
 * @property info 引擎附带的元信息（如 SD WebUI 的 `info` JSON 字符串、参数回显等），
 *           原样保留为 `Map<String, String>` 以便上层展示或持久化，**不**做强类型解析。
 */
data class ImageResult(
    val images: List<GeneratedImage>,
    val info: Map<String, String> = emptyMap(),
)
