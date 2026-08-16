package ciyin.video.player.ui.progress

import kotlin.test.Test
import kotlin.test.assertEquals

/** 播放时间格式化测试。 */
class MediaProgressIndicatorTextTest {

    /** 分钟级时长使用两段式时间格式。 */
    @Test
    fun formatsMinuteDuration() {
        assertEquals("01:05 / 02:05", renderSeconds(current = 65, total = 125))
    }

    /** 小时级时长使用三段式时间格式。 */
    @Test
    fun formatsHourDuration() {
        assertEquals("01:01:01 / 02:02:12", renderSeconds(current = 3661, total = 7332))
    }
}
