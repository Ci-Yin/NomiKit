package ciyin.ui.foundation.uitl

import co.touchlab.kermit.Severity
import co.touchlab.kermit.Severity.Assert
import co.touchlab.kermit.Severity.Debug
import co.touchlab.kermit.Severity.Error
import co.touchlab.kermit.Severity.Info
import co.touchlab.kermit.Severity.Verbose
import co.touchlab.kermit.Severity.Warn
import com.freeletics.flowredux2.FlowReduxStateMachineFactory
import com.freeletics.flowredux2.Logger
import co.touchlab.kermit.Logger as KermitLogger

/**
 * FlowRedux2 日志适配器，将 FlowRedux2 的日志输出委托给 Kermit 日志库。
 *
 * 该适配器实现了 [Logger] 接口，
 * 并将日志级别映射到 Kermit 的 [Severity]，实现统一的日志管理。
 *
 * @property kermitLogger 用于实际日志输出的 Kermit Logger 实例。
 */
class KermitFlowReduxLoggerAdapter(
    private val kermitLogger: KermitLogger = KermitLogger
) : Logger {
    /**
     * 最低日志级别，低于此级别的日志将被忽略。
     * 默认为 Kermit 的最小日志级别。
     */
    override var minLevel: Logger.Level = kermitLogger.config.minSeverity.toFlowReduxLevel()

    /**
     * 输出日志消息到 Kermit 日志库。
     *
     * @param tag 日志标签，用于标识日志来源。
     * @param level 日志级别。
     * @param message 日志消息内容，可为空。
     * @param throwable 关联的异常信息，可为空。
     */
    override fun log(tag: String, level: Logger.Level, message: String?, throwable: Throwable?) {
        val severity = level.toKermitSeverity()
        kermitLogger.log(
            severity,
            tag,
            throwable,
            message ?: throwable?.message ?: "Exception occurred"
        )
    }


}

fun <S : Any, A : Any> FlowReduxStateMachineFactory<S, A>.installKermitLogger(
    kermitLogger: KermitLogger = KermitLogger,
    name: String = this::class.simpleName!!
) {
    installLogger(KermitFlowReduxLoggerAdapter(kermitLogger = kermitLogger), name = name)
}

inline fun <reified T> FlowReduxStateMachineFactory<*, *>.installKermitLogger(
    vararg extraTags: String = emptyArray(),
    kermitLogger: KermitLogger = KermitLogger
) {
    val extraTags = if (extraTags.isEmpty()) "" else extraTags.joinToString(", ", "[", "]")
    installKermitLogger(kermitLogger, name = "${T::class.simpleName}$extraTags")
}

/**
 * 将 FlowRedux2 的日志级别转换为 Kermit 的 Severity。
 *
 * @return 对应的 Kermit [Severity]。
 */
private fun Logger.Level.toKermitSeverity(): Severity = when (this) {
    Logger.Level.Verbose -> Verbose
    Logger.Level.Debug -> Debug
    Logger.Level.Info -> Info
    Logger.Level.Warn -> Warn
    Logger.Level.Error -> Error
}

private fun Severity.toFlowReduxLevel(): Logger.Level = when (this) {
    Verbose -> Logger.Level.Verbose
    Debug -> Logger.Level.Debug
    Info -> Logger.Level.Info
    Warn -> Logger.Level.Warn
    Error -> Logger.Level.Error
    Assert -> Logger.Level.Error
}