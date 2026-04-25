package ciyin.ai.core.engine

import kotlin.jvm.JvmInline

/**
 * 引擎实例的唯一标识。
 *
 * 形式建议：`<provider>:<instance>`，例如：
 * - `"openai:default"`
 * - `"openai-compatible:openrouter-prod"`
 * - `"sdwebui:local-7860"`
 * - `"ollama:home-server"`
 *
 * `Registry` 用 [value] 作为查表 key；业务侧做配置存储时也以 [value] 形式持久化。
 *
 * @property value 全局唯一的字符串标识，由调用方保证唯一性。
 */
@JvmInline
value class EngineId(val value: String)
