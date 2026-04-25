package ciyin.ai.image.sdwebui.model

import ciyin.sdwebui.payload.script.ScriptPayload
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 图生图 / 局部重绘（`sdapi/v1/img2img`）在 [ciyin.ai.core.image.ImageRequest] 与 [ciyin.ai.core.image.ImageSource] 之外的 WebUI 请求体字段子集。
 *
 * JSON 键与 [ciyin.sdwebui.payload.Image2ImagePayload] 一致（[SerialName]），但**已省略**由通用层表达的字段，请勿在本结构中重复传递：
 *
 * - **文/图共用**：`prompt`、`negative_prompt`、`width`、`height`、`batch_size`、`steps`、`cfg_scale`、`seed`；
 * - **图生图**：`init_images`（由 [ImageSource.ImageToImage] / [ImageSource.Inpainting] 的底图编码为 Base64 列表）、`denoising_strength`（由 `ImageSource` 提供）；
 * - **局部重绘**：`mask`（由 [ImageSource.Inpainting.mask] 编码）。
 *
 * ### 使用方式
 * 编码为 `JsonElement` 后放入 [ImageRequest.vendorOptions] 的 [SdWebUiImageVendorOptionKeys.img2imgExtras]。
 * 引擎在完成 `init_images` / `prompt` / 尺寸与重绘强度等基础映射**之后**（含 inpaint 的 `mask`）再按字段覆盖。
 *
 * ### 可空语义
 * 与 [SdWebUiText2ImageExtras] 相同：`null` 且 JSON 缺省该键则不覆盖；显式 JSON 键则按值覆盖。
 *
 * ### 与 `alwaysOnScripts` 的关系
 * [alwaysonScripts] 与 [SdWebUiImageVendorOptionKeys.alwaysOnScripts] 及 [ImageRequest.controls] 映射结果**叠加**，同名脚本键以后写入为准。
 *
 * @param resizeMode WebUI `resize_mode`：缩放/裁剪模式整型枚举（与 AUTOMATIC1111 UI 选项一致）。
 * @param maskBlur WebUI `mask_blur`：遮罩边缘模糊半径（像素）；对 inpaint 有效。
 * @param inpaintingFill WebUI `inpainting_fill`：局部重绘填充方式整型枚举。
 * @param inpaintFullRes WebUI `inpaint_full_res`：是否仅对遮罩区域以全分辨率重绘。
 * @param inpaintFullResPadding WebUI `inpaint_full_res_padding`：全分辨率 inpaint 时遮罩扩展边距。
 * @param inpaintingMaskInvert WebUI `inpainting_mask_invert`：遮罩反转方式整型枚举。
 * @param initialNoiseMultiplier WebUI `initial_noise_multiplier`：图生图初始噪声倍率。
 * @param styles WebUI `styles`：风格标签列表。
 * @param subseed WebUI `subseed`：variation 子种子。
 * @param subseedStrength WebUI `subseed_strength`：子种子强度。
 * @param seedResizeFromH WebUI `seed_resize_from_h`：种子缩放参考高度。
 * @param seedResizeFromW WebUI `seed_resize_from_w`：种子缩放参考宽度。
 * @param nIter WebUI `n_iter`：批次数。
 * @param imageCfgScale WebUI `image_cfg_scale`：图生图专用图像 CFG（如 InstructPix2Pix 等管线）。
 * @param restoreFaces WebUI `restore_faces`：面部修复。
 * @param tiling WebUI `tiling`：平铺无缝纹理。
 * @param doNotSaveSamples WebUI `do_not_save_samples`：不在服务端保存样本文件。
 * @param eta WebUI `eta`：采样 ETA 噪声系数。
 * @param sChurn WebUI `s_churn`：随机调度器 churn。
 * @param sTmax WebUI `s_tmax`：随机调度器 `t_max`。
 * @param sTmin WebUI `s_tmin`：随机调度器 `t_min`。
 * @param sNoise WebUI `s_noise`：随机调度器噪声。
 * @param overrideSettings WebUI `override_settings`：临时覆盖 WebUI 设置。
 * @param overrideSettingsRestoreAfterwards WebUI `override_settings_restore_afterwards`：请求结束后是否恢复设置。
 * @param samplerName WebUI `sampler_name`：采样器名称；与 [samplerIndex] 同时存在时按构建器顺序先后覆盖。
 * @param samplerIndex WebUI `sampler_index`：采样器索引字符串。
 * @param includeInitImages WebUI `include_init_images`：是否在响应中回传初始图 Base64。
 * @param scriptName WebUI `script_name`：主脚本名称。
 * @param scriptArgs WebUI `script_args`：主脚本参数。
 * @param sendImages WebUI `send_images`：响应是否包含生成图 Base64。
 * @param saveImages WebUI `save_images`：是否在服务端保存生成图。
 * @param alwaysonScripts WebUI `alwayson_scripts`：常驻扩展脚本映射；语义见类说明。
 */
@Serializable
data class SdWebUiImg2ImgExtras(
    val resizeMode: Int? = null,
    val maskBlur: Int? = null,
    val inpaintingFill: Int? = null,
    val inpaintFullRes: Boolean? = null,
    val inpaintFullResPadding: Int? = null,
    val inpaintingMaskInvert: Int? = null,
    val initialNoiseMultiplier: Int? = null,
    val styles: List<String>? = null,
    val subseed: Int? = null,
    val subseedStrength: Int? = null,
    val seedResizeFromH: Int? = null,
    val seedResizeFromW: Int? = null,
    val nIter: Int? = null,
    val imageCfgScale: Float? = null,
    val restoreFaces: Boolean? = null,
    val tiling: Boolean? = null,
    val doNotSaveSamples: Boolean? = null,
    val eta: Float? = null,
    val sChurn: Int? = null,
    val sTmax: Int? = null,
    val sTmin: Int? = null,
    val sNoise: Int? = null,
    val overrideSettings: Map<String, String>? = null,
    val overrideSettingsRestoreAfterwards: Boolean? = null,
    val samplerName: String? = null,
    val samplerIndex: String? = null,
    val includeInitImages: Boolean? = null,
    val scriptName: String? = null,
    val scriptArgs: List<String>? = null,
    val sendImages: Boolean? = null,
    val saveImages: Boolean? = null,
    val alwaysonScripts: Map<String, ScriptPayload>? = null,
)
