package ciyin.ai.core.chat

import kotlinx.serialization.json.JsonObject

/**
 * 工具（function calling）定义。
 *
 * 与 OpenAI tools / Anthropic tools / Gemini function_declarations 等主流协议保持同构。
 * 业务侧给出 [name] / [description] / [parametersJsonSchema]，引擎适配层按各自协议封装。
 *
 * @property name 工具名，要求满足主流厂商对 function name 的约束（字母数字下划线，长度 ≤ 64）。
 * @property description 工具用途的自然语言描述，模型据此决定是否调用。
 * @property parametersJsonSchema 标准 JSON Schema，描述参数结构。
 *           保留为 [JsonObject] 而非自定义模型，是为了让调用方可以直接复用现成 schema。
 */
data class ChatToolSpec(
    val name: String,
    val description: String,
    val parametersJsonSchema: JsonObject,
)
