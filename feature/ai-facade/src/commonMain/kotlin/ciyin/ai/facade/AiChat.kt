package ciyin.ai.facade

import ciyin.ai.core.chat.ChatEvent
import ciyin.ai.core.chat.ChatModelInfo
import ciyin.ai.core.chat.ChatRequest
import ciyin.ai.facade.selection.ChatEngineSpec
import ciyin.ai.facade.selection.EnginePreferences
import kotlinx.coroutines.flow.Flow

/**
 * 聊天能力的统一入口。
 *
 * 上层（业务侧 Repository / UseCase / 跨模块工具）只与本接口交互；
 * 具体走哪个引擎、用哪个模型、是否降级、要不要重试、怎么打日志计费——全部由实现内部决定。
 *
 * 实现需保证：
 * - 同一个 [ChatRequest] 在不同模型下应**行为可比**；差异化字段走 [ChatRequest.vendorOptions]；
 * - 失败按 `FallbackPolicy` 决定是否切到备用引擎；
 * - 调用过程通过 `AiInvocationListener` 暴露可观测信号；
 * - **不**自行持久化用户偏好；偏好通过 [EnginePreferences] 接口由调用方注入。
 *
 * 默认实现见 [ciyin.ai.facade.impl.chat.DefaultAiChat]。
 */
interface AiChat {

    /**
     * 显式指定模型流式聊天。
     *
     * @param spec 引擎路由描述，参见 [ChatEngineSpec]。
     * @param request 通用聊天请求。
     */
    fun stream(request: ChatRequest, spec: ChatEngineSpec = ChatEngineSpec.Default): Flow<ChatEvent>

    /**
     * 列出全部已注册引擎的可用聊天模型，供 UI 展示与选择。
     *
     * 实现应做"按引擎顺序拼接 + 去重 + 错误降级"：单家失败不应导致整体失败，
     * 至少返回成功拉取到的部分；若全部引擎均失败则返回空列表。
     */
    suspend fun models(): List<ChatModelInfo>
}
