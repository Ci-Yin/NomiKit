package ciyin.ai.image.sdwebui.model

import ciyin.serialization.json.toJsonElement
import kotlinx.serialization.json.JsonElement

/**
 * 以 [SdWebUiText2ImageExtras] 为接收者构建 extras（通常在其上链式 [copy]），
 * 并返回可直接用于 [ciyin.ai.core.image.ImageRequest.vendorOptions] 的 **键** 与 **值** 对。
 *
 * 示例：
 * ```
 * val (key, element) = buildSdWebUiText2ImageExtras {
 *     copy(
 *         samplerName = "Euler a",
 *         hiRes = SdWebUiText2ImageHiresFix(enableHr = true, hrScale = 2),
 *     )
 * }
 * vendorOptions = mapOf(key to element)
 * ```
 */
fun buildSdWebUiText2ImageExtras(
    configure: SdWebUiText2ImageExtras.() -> SdWebUiText2ImageExtras,
): Pair<String, JsonElement> =
    SdWebUiImageVendorOptionKeys.txt2imgExtras to SdWebUiText2ImageExtras().configure()
        .toJsonElement()

/**
 * 同上，对应 [SdWebUiImg2ImgExtras] 与 [SdWebUiImageVendorOptionKeys.img2imgExtras]。
 */
fun buildSdWebUiImg2ImgExtras(
    configure: SdWebUiImg2ImgExtras.() -> SdWebUiImg2ImgExtras,
): Pair<String, JsonElement> =
    SdWebUiImageVendorOptionKeys.img2imgExtras to SdWebUiImg2ImgExtras().configure().toJsonElement()
