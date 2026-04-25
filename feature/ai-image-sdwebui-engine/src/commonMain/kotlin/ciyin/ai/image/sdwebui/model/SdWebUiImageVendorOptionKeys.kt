package ciyin.ai.image.sdwebui.model

/**
 * SD WebUI 生图引擎在 [ciyin.ai.core.image.ImageRequest.vendorOptions] 中使用的键名集中定义，
 * 业务侧与单测应引用本对象，避免散落魔法字符串。
 */
object SdWebUiImageVendorOptionKeys {

    private const val PREFIX = "sdwebui."

    /** `alwayson_scripts` 透传，值为 JsonObject，键为脚本名。 */
    val alwaysOnScripts: String = PREFIX + "alwaysonScripts"

    /**
     * 文生图时附加的 WebUI 专有字段（与 [SdWebUiText2ImageExtras] 同构 JSON），
     * 在 [ciyin.ai.core.image.ImageRequest] 映射完成后按字段覆盖到 `txt2img` 请求。
     */
    val txt2imgExtras: String = PREFIX + "txt2img.extras"

    /**
     * 图生图 / 局部重绘时附加字段（与 [SdWebUiImg2ImgExtras] 同构 JSON），
     * 在 [ciyin.ai.core.image.ImageRequest] 映射完成后按字段覆盖到 `img2img` 请求。
     */
    val img2imgExtras: String = PREFIX + "img2img.extras"
}
