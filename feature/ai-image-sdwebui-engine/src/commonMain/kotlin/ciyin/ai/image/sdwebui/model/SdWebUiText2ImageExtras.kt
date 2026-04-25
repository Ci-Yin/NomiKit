package ciyin.ai.image.sdwebui.model

import ciyin.sdwebui.payload.script.ScriptPayload
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 文生图（`sdapi/v1/txt2img`）在 [ciyin.ai.core.image.ImageRequest] 之外的 WebUI 请求体字段子集。
 *
 * 与完整 [ciyin.sdwebui.payload.Text2ImagePayload] 对齐的 JSON 键名（[SerialName]），但**已省略**由 [ImageRequest] 统一表达的字段，避免重复与分叉：
 * `prompt`、`negative_prompt`、`seed`、`steps`、`cfg_scale`、`width`、`height`、`batch_size`。
 *
 * ### Hi-res 字段形态
 * [hiresFix] 在 JSON 中序列化为嵌套对象，键名为 **`hi_res`**。
 * 解码时引擎会将 WebUI 常见的**顶层扁平**键（`enable_hr`、`denoising_strength`、`hr_scale` 等）
 * 自动归并到 `hi_res` 后再反序列化，因此从 WebUI 复制的扁平片段亦可直接使用。
 *
 * ### 使用方式
 * 将实例通过 `Json.encodeToJsonElement(SdWebUiText2ImageExtras.serializer(), …)` 编码后，
 * 放入 [ImageRequest.vendorOptions] 中键 [SdWebUiImageVendorOptionKeys.txt2imgExtras] 对应项。
 * 引擎在 `ciyin.ai.image.sdwebui.mapper` 中完成 [ImageRequest] → `txt2img` 基础映射**之后**，按字段覆盖到构建器。
 *
 * ### 可空语义
 * 各参数为 `null` 且 JSON **缺省该键**时：不覆盖已从 [ImageRequest] 写入的值。
 * 若 JSON 显式给出键（含空列表、空对象），则按解码结果覆盖。
 *
 * ### 与 `alwaysOnScripts` 的关系
 * [alwaysonScripts] 与 [SdWebUiImageVendorOptionKeys.alwaysOnScripts] 及 [ImageRequest.controls] 映射结果**叠加**（后写入的脚本同名键覆盖前者）。
 *
 * @param styles 嵌入提示的风格标签列表。
 * @param subseed variation 子种子，与 WebUI 子种子 UI 一致。
 * @param subseedStrength 子种子影响强度。
 * @param seedResizeFromH 种子缩放参考高度（像素）。
 * @param seedResizeFromW 种子缩放参考宽度（像素）。
 * @param samplerName 采样器显示名；若与 [samplerIndex] 同时给出，先应用名称再应用索引（与 [ciyin.sdwebui.process.Text2Image.Builder] 行为一致）。
 * @param nIter 批次数（每批张数仍由 [ImageRequest.batch] 决定）。
 * @param restoreFaces 是否启用面部修复。
 * @param tiling 是否生成平铺无缝纹理。
 * @param doNotSaveSamples 为真不在服务端磁盘保存单张样本。
 * @param doNotSaveGrid 为真不保存预览网格图。
 * @param eta 部分采样器的 ETA / 噪声系数。
 * @param sChurn 随机调度器 churn（与 WebUI 高级设置一致）。
 * @param sTmax 随机调度器 `t_max`。
 * @param sTmin 随机调度器 `t_min`。
 * @param sNoise 随机调度器附加噪声。
 * @param overrideSettings 请求期间临时覆盖的 WebUI 设置键值。
 * @param overrideSettingsRestoreAfterwards 请求结束后是否恢复被覆盖项。
 * @param comments 随请求携带的注释键值（由 WebUI 定义用途）。
 * @param firstphaseWidth HR 一阶段宽度（像素）；仍位于 extras 顶层，与 WebUI 一致。
 * @param firstphaseHeight HR 一阶段高度（像素）。
 * @param hiresFix Hi-res fix 与 HR 二阶段相关子字段（见 [SdWebUiText2ImageHiresFix]）。
 * @param samplerIndex 采样器索引字符串；可与 [samplerName] 独立指定以匹配 WebUI API。
 * @param scriptName 主脚本（非 always-on）名称。
 * @param scriptArgs 主脚本参数列表。
 * @param sendImages 是否在响应中返回生成图 Base64；客户端一般应保持由引擎默认的 `true`，仅在明确需要时覆盖。
 * @param saveImages 是否在服务端保存生成图。
 * @param alwaysonScripts 常驻扩展脚本映射，值为 [ScriptPayload]；与顶层 `alwaysOnScripts` vendor 键合并时同名键以后者为准。
 */
@Serializable
data class SdWebUiText2ImageExtras(
    val styles: List<String>? = null,
    val subseed: Int? = null,
    val subseedStrength: Int? = null,
    val seedResizeFromH: Int? = null,
    val seedResizeFromW: Int? = null,
    val samplerName: String? = null,
    val nIter: Int? = null,
    val restoreFaces: Boolean? = null,
    val tiling: Boolean? = null,
    val doNotSaveSamples: Boolean? = null,
    val doNotSaveGrid: Boolean? = null,
    val eta: Float? = null,
    val sChurn: Int? = null,
    val sTmax: Int? = null,
    val sTmin: Int? = null,
    val sNoise: Int? = null,
    val overrideSettings: Map<String, String>? = null,
    val overrideSettingsRestoreAfterwards: Boolean? = null,
    val comments: Map<String, String>? = null,
    val firstphaseWidth: Int? = null,
    val firstphaseHeight: Int? = null,
    val hiresFix: SdWebUiText2ImageHiresFix? = null,
    val samplerIndex: String? = null,
    val scriptName: String? = null,
    val scriptArgs: List<String>? = null,
    val sendImages: Boolean? = null,
    val saveImages: Boolean? = null,
    val alwaysonScripts: Map<String, ScriptPayload>? = null,
)
