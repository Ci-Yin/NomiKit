package ciyin.ai.core.image

import kotlinx.serialization.json.JsonElement

/**
 * 跨引擎通用的生图请求。
 *
 * 设计原则：
 * - [source] 决定模式（文生图 / 图生图 / 局部重绘）；
 * - [controls] 是结构化 control 列表（ControlNet / IPAdapter 等），通用层只描述意图，
 *   具体如何映射到底层 alwayson script 由 engine 适配层负责；
 * - [postProcessors] 是后处理流水线（FaceDetailer / RemBG / Upscale），同上；
 * - 厂商专有字段（如 SD WebUI 的某个新插件、ComfyUI 的特殊节点参数）走 [vendorOptions]。
 *
 * @property model 模型名（如 `"sd_xl_base_1.0"`）；`null` 表示沿用引擎当前激活的模型。
 * @property prompt 正向提示词。
 * @property negativePrompt 负向提示词；`null` 等价于空字符串。
 * @property source 生图模式，参见 [ImageSource]。
 * @property size 输出尺寸。
 * @property batch 一次请求生成的图像数量。
 * @property steps 采样步数；`null` 表示沿用引擎默认。
 * @property cfgScale CFG scale；`null` 表示沿用引擎默认。
 * @property seed 采样种子；`null` 表示随机。
 * @property controls 结构化条件控制列表（可为空）。
 * @property postProcessors 后处理流水线（按列表顺序串行执行）。
 * @property vendorOptions 厂商专有参数，键名建议加厂商前缀以避免冲突，
 *           如 `"sdwebui.alwaysonScripts"` / `"comfyui.workflowOverride"`。
 */
data class ImageRequest(
    val model: String? = null,
    val prompt: String,
    val negativePrompt: String? = null,
    val source: ImageSource = ImageSource.TextToImage,
    val size: ImageSize = ImageSize(1024, 1024),
    val batch: Int = 1,
    val steps: Int? = null,
    val cfgScale: Float? = null,
    val seed: Long? = null,
    val controls: List<ImageControl> = emptyList(),
    val postProcessors: List<ImagePostProcessor> = emptyList(),
    val vendorOptions: Map<String, JsonElement> = emptyMap(),
)
