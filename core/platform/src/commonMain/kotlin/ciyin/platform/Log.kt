package ciyin.platform

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * 日志记录对象类，提供不同类型的日志记录功能。
 */
object Log {

    /**
     * 信息日志类型。
     */
    private const val TYPE_INFO = 1

    /**
     * 警告日志类型。
     */
    private const val TYPE_WARN = 2

    /**
     * 调试日志类型。
     */
    private const val TYPE_DEBUG = 3

    /**
     * 错误日志类型。
     */
    private const val TYPE_ERROR = 4

    /**
     * 未知日志类型。
     */
    private const val TYPE_WER = 5

    private val _log = MutableStateFlow("")
    val log = _log.asStateFlow()

    /**
     * 内部日志记录函数。
     *
     * @param type 日志类型。
     * @param tag 日志标签。
     * @param logs 日志消息。
     */
    @OptIn(ExperimentalTime::class)
    private fun log(type: Int, tag: String, logs: Any?) = try {
        val t = when (type) {
            TYPE_INFO -> "INFO"
            TYPE_WARN -> "WARN"
            TYPE_DEBUG -> "DEBUG"
            TYPE_ERROR -> "ERROR"
            else -> "WER"
        }

        val time = Clock.System.nowLocal().format("HH:mm:ss")

        val msg = "[$time] [$t] [$tag] $logs \n"
        _log.update { msg }
        /*GlobalUiState.appLog = GlobalUiState.appLog.let {
            // 使用 Regex.escape() 来正确转义所有特殊字符
            val escapedLogs = Regex.escape(logs.toString())
            val regexMsg = "\\[\\d+:\\d+:\\d+] \\[$t] \\[$tag] ${escapedLogs.toString().replace("\\", "\\\\")} \n"
            val regexReceive = "\\[\\d+:\\d+:\\d+] \\[$t] \\[$tag] \\.+\n"
            if (it.matchIn(regexMsg + regexReceive)) {
                it.replace(Regex(regexReceive + regexMsg), "[$time] [$t] [$tag] ...\n$msg")
            } else if (it.matchIn(regexMsg)) {
                "${it}[$time] [$t] [$tag] ...\n$msg"
            } else {
                "$it$msg"
            }
        }*/
        print(msg)
    } catch (e: Exception) {
        e.printStackTrace()
    }

    /**
     * 记录信息日志。
     *
     * @param tag 日志标签。
     * @param msg 日志消息。
     */
    fun info(tag: String, vararg msg: Any?) {
        for (m in msg) {
            //log.trace(m.toString())
            log(TYPE_INFO, tag, m)
        }
    }

    /**
     * 记录警告日志。
     *
     * @param tag 日志标签。
     * @param msg 日志消息。
     */
    fun warn(tag: String, vararg msg: Any?) {
        for (m in msg) {
            //log.warn(m.toString())
            log(TYPE_WARN, tag, m)
        }
    }

    /**
     * 记录调试日志。
     *
     * @param tag 日志标签。
     * @param msg 日志消息。
     */
    fun debug(tag: String, vararg msg: Any?) {
        for (m in msg) {
            //log.debug(m.toString())
            log(TYPE_DEBUG, tag, m)
        }
    }

    /**
     * 记录错误日志。
     *
     * @param tag 日志标签。
     * @param msg 日志消息。
     */
    fun error(tag: String, vararg msg: Any?) {
        for (m in msg) {
            //log.error(m.toString())
            log(TYPE_ERROR, tag, m)
        }
    }

    /**
     * 记录未知日志。
     *
     * @param tag 日志标签。
     * @param msg 日志消息。
     */
    fun wer(tag: String, vararg msg: Any?) {
        for (m in msg) {
            //log.trace(m.toString())
            log(TYPE_WER, tag, m)
        }
    }

}

