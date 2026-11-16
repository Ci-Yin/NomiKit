package ciyin.designsystem.fluent

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import ciyin.platform.logger

/**
 * Fluent 适配器日志；降级告警统一从这里输出。
 */
internal val fluentLogger = logger(tag = "NomiFluent")

/**
 * 无法表达的覆盖首次出现时记录一次警告，避免逐帧刷屏。
 *
 * @param overridden 本次调用是否真的携带了该覆盖。
 * @param key 覆盖值；同值重组不重复记录。
 * @param message 警告内容，只在需要记录时求值。
 */
@Composable
internal fun rememberFluentWarning(
    overridden: Boolean,
    key: Any?,
    message: () -> String,
) {
    if (!overridden) return
    remember(key) { fluentLogger.w(message()) }
}
