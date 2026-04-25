package ciyin.ai.image.sdwebui.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * AUTOMATIC1111 文生图中 **Hi-res fix（高分辨率修复）** 与 **HR 二阶段** 相关参数在 [SdWebUiText2ImageExtras] 中的分组。
 *
 * ### 在 JSON 中的位置
 * 作为 [SdWebUiText2ImageExtras] 的 [SdWebUiText2ImageExtras.hiresFix] 嵌套在键 **`hi_res`** 下；各成员键名与 WebUI `sdapi/v1/txt2img` 请求体一致（见各 [SerialName]）。
 * 引擎在解码 [SdWebUiImageVendorOptionKeys.txt2imgExtras] 时，若 extras 根级出现与 WebUI 相同的**扁平**键（`enable_hr`、`hr_scale` 等），会先归并到 `hi_res` 再反序列化。
 *
 * ### 可空语义
 * 字段为 `null` 且 JSON 缺省该键时，不覆盖已从 [ciyin.ai.core.image.ImageRequest] 与基础 mapper 写入的 `txt2img` 构建器状态。
 *
 * @param enable WebUI `enable_hr`：是否启用 Hi-res fix。
 * @param upscaler WebUI `hr_upscaler`：二阶段放大器名称（如 `Latent`、具体 upscaler 名）。
 * @param denoisingStrength WebUI `denoising_strength`：Hi-res / 二阶段相关去噪强度（与主采样 `denoising_strength` 在 txt2img 中的语义以 WebUI 为准）。
 * @param secondPassSteps WebUI `hr_second_pass_steps`：二阶段额外采样步数。
 * @param scale WebUI `hr_scale`：放大倍数。
 * @param resizeX WebUI `hr_resize_x`：目标宽度（像素）。
 * @param resizeY WebUI `hr_resize_y`：目标高度（像素）。
 */
@Serializable
data class SdWebUiText2ImageHiresFix(
    val enable: Boolean? = true,
    val upscaler: String? = null,
    val denoisingStrength: Float? = null,
    val secondPassSteps: Int? = null,
    val scale: Int? = null,
    val resizeX: Int? = null,
    val resizeY: Int? = null,
)
