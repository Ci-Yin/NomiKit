package ciyin.ui.foundation.extension

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.click
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** 多平台滚动状态监听器的真实桌面鼠标输入回归测试。 */
@OptIn(ExperimentalTestApi::class)
class ScrollStateChangedUiTest {

    /** 鼠标滚轮向内容末端和起点滚动时应分别报告 Top 与 Bottom。 */
    @Test
    fun mouseWheelReportsBothScrollDirections() = runComposeUiTest {
        val directions = mutableListOf<Direction>()

        setContent {
            val scrollState = rememberScrollState()
            ScrollListenerFixture(
                scrollState = scrollState,
                onDirection = directions::add,
            )
        }

        onNodeWithTag(ScrollTargetTag).performMouseInput { scroll(120f) }
        waitForIdle()
        onNodeWithTag(ScrollTargetTag).performMouseInput { scroll(-120f) }
        waitForIdle()

        assertEquals(listOf(Direction.Top, Direction.Bottom), directions.distinct())
    }

    /** 普通鼠标点击不应被误报为拖拽或滚动方向。 */
    @Test
    fun ordinaryMouseClickDoesNotReportScrollDirection() = runComposeUiTest {
        val directions = mutableListOf<Direction>()

        setContent {
            val scrollState = rememberScrollState()
            ScrollListenerFixture(
                scrollState = scrollState,
                onDirection = directions::add,
            )
        }

        onNodeWithTag(ScrollTargetTag).performMouseInput { click() }
        waitForIdle()

        assertTrue(directions.isEmpty())
    }

    /**
     * 渲染带生产滚动监听器的可滚动测试容器。
     *
     * @param scrollState 被监听的真实滚动状态
     * @param onDirection 用户滚动方向回调
     */
    @Composable
    private fun ScrollListenerFixture(
        scrollState: ScrollState,
        onDirection: (Direction) -> Unit,
    ) {
        Box(
            modifier = Modifier
                .testTag(ScrollTargetTag)
                .size(100.dp)
                .verticalScroll(scrollState)
                .onScrollStateChanged(scrollState) { status, direction, _ ->
                    if (status != ScrollStatus.Idle) {
                        onDirection(direction)
                    }
                },
        ) {
            Box(modifier = Modifier.width(100.dp).height(500.dp))
        }
    }

    /** 测试滚动目标的稳定语义标签。 */
    private companion object {
        const val ScrollTargetTag = "scroll-state-changed-target"
    }
}
