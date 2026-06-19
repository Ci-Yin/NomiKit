package ciyin.ai.image.sdwebui

/**
 * SD WebUI 引擎暴露给上层使用的 LoRA 条目。
 *
 * @property name LoRA 名称。
 * @property alias LoRA 别名。
 * @property path LoRA 文件路径。
 * @property metadata LoRA 元数据对象的紧凑 JSON 字符串。
 */
data class SdWebUiLoraInfo(
    val name: String,
    val alias: String,
    val path: String,
    val metadata: String,
)
