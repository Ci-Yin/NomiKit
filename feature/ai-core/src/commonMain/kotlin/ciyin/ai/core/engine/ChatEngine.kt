package ciyin.ai.core.engine

import ciyin.ai.core.chat.ChatModelInfo
import ciyin.ai.core.chat.ChatRequest
import ciyin.ai.core.chat.ChatEvent
import kotlinx.coroutines.flow.Flow

/**
 * 聊天能力引擎。
 *
 * 所有产出统一走 [Flow]，兼容远程流式（SSE）与本地 token streaming。
 * 即便底层 SDK 是同步的，实现也应当强制走 [Flow] 以便统一进度 / 事件 / 失败建模。
 */
interface ChatEngine : AiEngine {

    /**
     * 发起一次聊天请求并以事件流形式返回。
     *
     * 实现需保证：
     * - 至少发送 `ChatEvent.Started`，并以 `ChatEvent.Completed` 或 `ChatEvent.Failed` 结尾；
     * - 调用方主动 cancel 协程时，应及时停止上游请求并清理资源；
     * - 任何引擎层错误都应转换为 `ChatEvent.Failed(AiEngineError)` 发送，**禁止**直接抛出
     *   `RuntimeException` 让上层 Flow 整体崩溃。`CancellationException` 例外，应原样向上抛。
     */
    fun stream(request: ChatRequest): Flow<ChatEvent>

    /**
     * 列出当前引擎可用的所有聊天模型。
     *
     * 拉取失败时返回空列表（例如网络错误），由上层 Facade 做跨引擎聚合与展示降级。
     */
    suspend fun models(): List<ChatModelInfo>

    /**
     * 在不实际发起调用的前提下校验 [request] 是否能被本引擎执行。
     *
     * 用于尽早拒绝（如缺失 capability、参数非法），避免业务侧浪费调用配额。
     * 实现可以是纯本地校验，也可以做轻量的远程预检（不强制）。
     */
    suspend fun validate(request: ChatRequest): Result<Unit>
}
