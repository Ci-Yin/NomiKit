package ciyin.ai.core.support

import ciyin.ai.core.capability.ChatCapability
import ciyin.ai.core.chat.ChatEvent
import ciyin.ai.core.chat.ChatModelInfo
import ciyin.ai.core.chat.ChatRequest
import ciyin.ai.core.engine.ChatEngine
import ciyin.ai.core.engine.EngineId
import ciyin.ai.core.engine.EngineRuntime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * 测试用的最小 [ChatEngine] 实现。
 *
 * 仅承载 [id] / [provider] / [runtime] / [capabilities] 等治理信息，
 * `stream` / `models` / `validate` 在 ai-core 单元测试中均不会被调用，
 * 因此用最简单的占位实现。
 */
internal class FakeChatEngine(
    override val id: EngineId,
    override val provider: String = "fake",
    override val runtime: EngineRuntime = EngineRuntime.RemoteCloud,
    override val capabilities: Set<ChatCapability> = emptySet(),
) : ChatEngine {
    override fun stream(request: ChatRequest): Flow<ChatEvent> = flowOf()
    override suspend fun models(): List<ChatModelInfo> = emptyList()
    override suspend fun validate(request: ChatRequest): Result<Unit> = Result.success(Unit)
}
