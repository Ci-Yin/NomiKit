package ciyin.ai.core.error

/**
 * 把 [AiEngineError] 包装成 [RuntimeException]，仅用于"必须以异常形式出现"的边界场景。
 *
 * 默认情况下 `ChatEngine` / `ImageEngine` 都通过 `Flow` 的 `Failed` 事件传递错误，
 * **不**应使用本异常。仅在以下场景可考虑：
 * - 同步 API（如 `validate(...)` 返回 `Result.Failure`，调用方想 `getOrThrow`）；
 * - 与必须 throw 的旧代码桥接。
 *
 * @param error 被包装的引擎错误模型。
 */
class AiEngineException(val error: AiEngineError) : RuntimeException(error.toString())
