package ciyin.ui.foundation.widget.scrollbar.usage

import androidx.compose.foundation.gestures.Orientation
import kotlin.test.Test
import kotlin.test.assertEquals

/** 竖向列表容器滚动条方向契约测试。 */
class ScrollbarUsageOrientationTest {

    /** Full-height 网格滚动条必须沿竖直轴工作。 */
    @Test
    fun fullHeightGridScrollbarUsesVerticalOrientation() {
        assertEquals(
            expected = Orientation.Vertical,
            actual = fullHeightGridScrollbarOrientation,
        )
    }
}
