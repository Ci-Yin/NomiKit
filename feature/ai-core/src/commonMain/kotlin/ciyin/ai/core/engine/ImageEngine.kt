package ciyin.ai.core.engine

import ciyin.ai.core.image.ImageEvent
import ciyin.ai.core.image.ImageModelInfo
import ciyin.ai.core.image.ImageRequest
import kotlinx.coroutines.flow.Flow

/**
 * 生图能力引擎。
 *
 * 即便底层 SDK 是同步返回，也强制走 [Flow] 以统一进度 / 预览 / 失败建模。
 * 这样未来一旦底层支持 progress polling 或队列任务，调用方代码完全不用改。
 */
interface ImageEngine : AiEngine {

    /**
     * 发起一次生图请求并以事件流形式返回。
     *
     * 实现需保证：
     * - 至少发送 `ImageEvent.Started`，并以 `ImageEvent.Completed` 或 `ImageEvent.Failed` 结尾；
     * - 中间过程允许穿插 `ImageEvent.Progress` / `ImageEvent.Preview`；
     * - 与 [ChatEngine.stream] 同样：cancel 时清理上游；引擎层错误转 `Failed`，禁止抛 RuntimeException。
     */
    fun generate(request: ImageRequest): Flow<ImageEvent>

    /**
     * 列出当前引擎可用的所有生图模型。
     *
     * 与 [ChatEngine.listModels] 语义一致；返回 [Result] 由调用方决定如何降级。
     */
    suspend fun listModels(): Result<List<ImageModelInfo>>

    /**
     * 在不实际发起调用的前提下校验 [request] 是否能被本引擎执行。
     */
    suspend fun validate(request: ImageRequest): Result<Unit>
}
