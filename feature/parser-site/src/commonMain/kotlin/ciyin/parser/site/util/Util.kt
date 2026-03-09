package ciyin.parser.site.util

import ciyin.lang.matchIn
import ciyin.platform.time.toInstant

/** 将字符串转换为时间戳。*/
internal fun String.toTimestamp(): Long {
    return when {
        matchIn("^\\d+$") -> toLong()
        matchIn("""([+-]\d{2})(\d{2})$""") -> toInstant().epochSeconds
        else -> 0L
    }
}

