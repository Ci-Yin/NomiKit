package ciyin.platform.time

import ciyin.io.File
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime


/**
 *
 * kotlin文件作用描述
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2025/10/26 15:21
 */

/** 聚合数据日期格式（示例：05/31） */
const val JuheFormatPattern = "MM/dd"

/** 标准日期格式（示例：2025-10-31） */
const val FormatPattern = "yyyy-MM-dd"

/** 日期时间格式（示例：2025-10-31 14:23:59） */
const val DateAndTimePattern = "yyyy-MM-dd HH:mm:ss"

/** 日期时间+星期格式（示例：2025-10-31 14:23:59 星期五） */
const val DateAndTimePatternWithWeek = "yyyy-MM-dd HH:mm:ss EEEE"

/** 简短日期格式（同标准格式，保留字段以兼容旧代码） */
const val FormatPatternShort = "yyyy-MM-dd"

fun currentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()

/**
 * 自定义格式化
 * @param pattern 格式模式，支持:
 *   - yyyy: 年份（4位）
 *   - MM: 月份（2位）
 *   - dd: 日期（2位）
 *   - HH: 小时（2位）
 *   - mm: 分钟（2位）
 *   - ss: 秒（2位）
 *   - SSS: 毫秒（3位）
 */
fun LocalDateTime.format(pattern: String = DateAndTimePatternWithWeek): String {
    return pattern
        .replace("yyyy", year.pad(4))
        .replace("MM", month.number.pad())
        .replace("dd", day.pad())
        .replace("HH", hour.pad())
        .replace("mm", minute.pad())
        .replace("ss", second.pad())
        .replace("SSS", (nanosecond / 1_000_000).pad(3))
}

/**
 * 时间格式化扩展函数
 */

// ==================== LocalDateTime 扩展 ====================

/**
 * 格式化为 HH:mm:ss
 */
fun LocalDateTime.toTimeString(): String =
    "${hour.pad()}:${minute.pad()}:${second.pad()}"

/**
 * 格式化为 HH:mm:ss.SSS
 */
fun LocalDateTime.toTimeStringWithMillis(): String {
    val millis = (nanosecond / 1_000_000).pad(3)
    return "${toTimeString()}.$millis"
}

/**
 * 格式化为 yyyy-MM-dd
 */
fun LocalDateTime.toDateString(): String =
    "${year.pad(4)}-${month.number.pad()}-${day.pad()}"

/**
 * 格式化为 yyyy-MM-dd HH:mm:ss
 */
fun LocalDateTime.toDateTimeString(): String =
    "${toDateString()} ${toTimeString()}"

/**
 * 格式化为 yyyy-MM-dd HH:mm:ss.SSS
 */
fun LocalDateTime.toDateTimeStringWithMillis(): String =
    "${toDateString()} ${toTimeStringWithMillis()}"

/**
 * 格式化为 ISO 8601 格式: yyyy-MM-ddTHH:mm:ss
 */
fun LocalDateTime.toIsoString(): String =
    "${toDateString()}T${toTimeString()}"

/**
 * 格式化为 ISO 8601 格式（带毫秒）: yyyy-MM-ddTHH:mm:ss.SSS
 */
fun LocalDateTime.toIsoStringWithMillis(): String =
    "${toDateString()}T${toTimeStringWithMillis()}"

// ==================== Clock 扩展 ====================

/**
 * 获取当前系统时区的 LocalDateTime
 */
@OptIn(ExperimentalTime::class)
fun Clock.nowLocal(): LocalDateTime =
    now().toLocalDateTime(TimeZone.currentSystemDefault())

/**
 * 获取当前时间: HH:mm:ss
 */
@OptIn(ExperimentalTime::class)
fun Clock.currentTimeString(): String =
    nowLocal().toTimeString()

/**
 * 获取当前时间（带毫秒）: HH:mm:ss.SSS
 */
@OptIn(ExperimentalTime::class)
fun Clock.currentTimeStringWithMillis(): String =
    nowLocal().toTimeStringWithMillis()

/**
 * 获取当前日期: yyyy-MM-dd
 */
@OptIn(ExperimentalTime::class)
fun Clock.currentDateString(): String =
    nowLocal().toDateString()

/**
 * 获取当前日期时间: yyyy-MM-dd HH:mm:ss
 */
@OptIn(ExperimentalTime::class)
fun Clock.currentDateTimeString(): String =
    nowLocal().toDateTimeString()

/**
 * 获取当前日期时间（带毫秒）: yyyy-MM-dd HH:mm:ss.SSS
 */
@OptIn(ExperimentalTime::class)
fun Clock.currentDateTimeStringWithMillis(): String =
    nowLocal().toDateTimeStringWithMillis()

// ==================== 辅助函数 ====================

/**
 * 整数补零
 */
private fun Int.pad(length: Int = 2): String =
    toString().padStart(length, '0')

/**
 * Long 补零（用于毫秒等）
 */
private fun Long.pad(length: Int = 2): String =
    toString().padStart(length, '0')

/**
 * 获取已格式化的文件最后修改时间
 *
 * @return 格式化后的时间
 */
@OptIn(ExperimentalTime::class)
val File.formatLastModified: String
    get() = Clock.System.nowLocal().format()