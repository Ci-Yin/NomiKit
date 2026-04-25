package ciyin.ai.core.capability

/**
 * 引擎能力声明的根 sealed 接口。
 *
 * 所有"某个引擎能做某件事"的描述都属于其某个子接口（[ChatCapability] / [ImageCapability]…）。
 * 选择 `sealed interface` 而非 `enum` 是为了：
 * 1. 子接口之间不互相干扰（生图能力新增不影响聊天）；
 * 2. 允许后续以 `data object` 形式扩展（如 `Streaming` / `ToolCalling`）；
 * 3. 业务层能用 `is ChatCapability` 等做编译期穷举判断。
 */
sealed interface AiCapability
