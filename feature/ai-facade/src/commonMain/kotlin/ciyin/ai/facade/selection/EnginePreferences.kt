package ciyin.ai.facade.selection

/**
 * 默认偏好与降级策略提供者，由调用方实现并注入到 `DefaultAiChat` / `DefaultAiImage`。
 *
 * 设计取舍：
 * - Facade 层不自行持久化，不读取 DataStore / Room / 任何业务存储，避免反向依赖；
 * - 多套调用方可以各自提供不同的偏好来源，而 Facade 实现保持复用；
 * - 所有方法都是 `suspend`，因为业务侧实现可能需要从 DataStore 或网络读取配置。
 */
interface EnginePreferences {

    /** 默认聊天引擎路由描述；返回 [ChatEngineSpec.Default] 等价于“由 Facade 自行兜底”。 */
    suspend fun defaultChatSpec(): ChatEngineSpec

    /** 默认生图引擎路由描述。 */
    suspend fun defaultImageSpec(): ImageEngineSpec

    /** 聊天调用的降级策略；返回 `FallbackPolicy()` 即“重试 1 次但不降级”。 */
    suspend fun chatFallback(): FallbackPolicy

    /** 生图调用的降级策略。 */
    suspend fun imageFallback(): FallbackPolicy
}
