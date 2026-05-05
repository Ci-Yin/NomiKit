package ciyin.ai.core.support

import ciyin.ai.core.capability.ImageCapability
import ciyin.ai.core.engine.EngineId
import ciyin.ai.core.engine.EngineRuntime
import ciyin.ai.core.engine.ImageEngine
import ciyin.ai.core.image.ImageEvent
import ciyin.ai.core.image.ImageModelInfo
import ciyin.ai.core.image.ImageRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * 测试用的最小 [ImageEngine] 实现，作用与 [FakeChatEngine] 同构。
 */
internal class FakeImageEngine(
    override val id: EngineId,
    override val provider: String = "fake",
    override val runtime: EngineRuntime = EngineRuntime.RemoteSelfHosted,
    override val capabilities: Set<ImageCapability> = emptySet(),
) : ImageEngine {
    override fun generate(request: ImageRequest): Flow<ImageEvent> = flowOf()
    override suspend fun models(): List<ImageModelInfo> = emptyList()
    override suspend fun validate(request: ImageRequest): Result<Unit> = Result.success(Unit)
}
