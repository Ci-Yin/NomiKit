package ciyin.ai.core.error

/**
 * AI 引擎层统一错误模型。
 *
 * 该错误模型仅描述"调用引擎过程中可能出现的失败"，**不**描述任何业务语义。
 * 上层（`app:shared/data`）需要在 `RepositoryImpl` 中将 [AiEngineError] 映射为业务通用的
 * `DataError`（参考 `.docs/contributing/layered.md` 的错误流转规则），再由 `domain` 层
 * 翻译为场景错误（如"请重新登录""配额已用尽"等）。
 *
 * 设计说明：
 * - **不**直接依赖 `app:shared` 的 `DataError`：`feature` 层是通用技术能力层，按
 *   `architecture.md` 不允许反向依赖业务层；
 * - **不**用 `Throwable` 子类作为传输模型：避免堆栈信息在 `Flow` / 跨进程传递时丢失或膨胀，
 *   仅在确实要 `throw` 时（罕见）借助 [AiEngineException] 包装。
 */
sealed interface AiEngineError {

    /** 网络层失败：超时、连接拒绝、DNS、SSL 等。 */
    data class Network(val cause: Throwable?, val message: String?) : AiEngineError

    /** 鉴权失败：token 缺失 / 过期 / 配额耗尽（HTTP 401 / 403 等）。 */
    data class Unauthorized(val providerMessage: String?) : AiEngineError

    /** 限流：HTTP 429 或厂商自定义限流响应。 */
    data class RateLimited(val retryAfterMs: Long?, val providerMessage: String?) : AiEngineError

    /** 上游协议错误：响应格式不符合期望、解析失败、缺字段等。 */
    data class Protocol(val message: String, val cause: Throwable? = null) : AiEngineError

    /** 引擎拒绝执行（内容过滤 / safety / NSFW 等）。 */
    data class Refused(val reason: String) : AiEngineError

    /** 请求不被引擎支持（capability 缺失 / 参数非法 / 模型未加载）。 */
    data class Unsupported(val message: String) : AiEngineError

    /**
     * 用户主动取消。
     *
     * `CancellationException` **不会**走这里——遇到 CE 应原样向上抛，由协程框架处理。
     * 仅当业务侧通过显式 API（如 `engine.cancel()` 假想接口）取消时才发送本事件。
     */
    data object Cancelled : AiEngineError

    /** 未分类失败兜底，避免有失败被忽略而 Flow 无终止事件。 */
    data class Unknown(val cause: Throwable?, val message: String?) : AiEngineError
}
