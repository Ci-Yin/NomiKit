package ciyin.ai.core.chat

/**
 * 跨引擎通用的聊天调用选项。
 *
 * 仅覆盖**所有主流厂商都稳定支持**的字段；任何"只有某一两家有"的字段都不放这里，
 * 应通过 [ChatRequest.vendorOptions] 透传。
 *
 * 所有字段的 `null` 意为"沿用引擎/模型默认值"，**不**强制下传给上游。
 *
 * @property temperature 采样温度。常见取值 `0.0..2.0`，越高越发散。
 * @property topP 核采样阈值。常见取值 `0.0..1.0`，与 [temperature] 二选一即可。
 * @property maxOutputTokens 模型回复最大 token 数。`null` 表示不限。
 * @property stop 停止词列表。命中任一即停止生成；多数厂商上限 4 项。
 * @property seed 采样种子，用于实验复现。`null` 表示随机。
 * @property stream 是否使用流式输出。即便为 `false`，引擎实现仍以 `Flow` 形式暴露
 *           一组 `Started → Completed/Failed` 事件，**不**改变接口形态。
 */
data class ChatOptions(
    val temperature: Float? = null,
    val topP: Float? = null,
    val maxOutputTokens: Int? = null,
    val stop: List<String> = emptyList(),
    val seed: Long? = null,
    val stream: Boolean = true,
)
