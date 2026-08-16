package ciyin.ui.foundation.widget.scrollbar

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** 通用滚动条运行时手势配置与取消清理的 Compose 回归测试。 */
@OptIn(ExperimentalTestApi::class)
class ScrollbarUiTest {

    /** 横向滚动条必须按指针的 x 坐标推进拇指进度。 */
    @Test
    fun horizontalDragAdvancesWithPointerX() = runComposeUiTest {
        val movements = mutableListOf<Float>()

        setContent {
            val state = remember { ScrollbarState() }
            Scrollbar(
                modifier = Modifier.testTag(ScrollbarTag).size(width = 240.dp, height = 40.dp),
                isShowTrack = true,
                isSupperSmall = true,
                orientation = Orientation.Horizontal,
                state = state,
                onThumbMoved = { moved, _ -> movements += moved },
                thumb = { Box(modifier = Modifier.fillMaxSize()) },
            )
        }

        onNodeWithTag(ScrollbarTag).performTouchInput {
            down(position = Offset(x = 20f, y = 20f))
            moveTo(position = Offset(x = 200f, y = 20f))
        }
        waitForIdle()

        assertTrue(movements.any { moved -> moved > 0.5f }, "横向拖动未推进 x 轴进度：$movements")
    }

    /** 运行时由只读切换为可交互后应立即允许真实拖动。 */
    @Test
    fun runtimeInteractiveChangeRestartsPointerInput() = runComposeUiTest {
        var isInteractive by mutableStateOf(false)
        var dragStarts = 0

        setContent {
            val state = remember { ScrollbarState() }
            Scrollbar(
                modifier = Modifier.testTag(ScrollbarTag).size(width = 40.dp, height = 240.dp),
                isShowTrack = true,
                isSupperSmall = true,
                isInteractive = isInteractive,
                orientation = Orientation.Vertical,
                state = state,
                onDragStart = { dragStarts += 1 },
                thumb = { Box(modifier = Modifier.fillMaxSize()) },
            )
        }

        onNodeWithTag(ScrollbarTag).performTouchInput {
            down(position = Offset(x = 20f, y = 20f))
            moveTo(position = Offset(x = 20f, y = 180f))
            up()
        }
        assertEquals(0, dragStarts)

        runOnIdle { isInteractive = true }
        waitForIdle()
        onNodeWithTag(ScrollbarTag).performTouchInput {
            down(position = Offset(x = 20f, y = 20f))
            moveTo(position = Offset(x = 20f, y = 180f))
            up()
        }

        assertEquals(1, dragStarts)
    }

    /** 拖动取消必须结束外层拖动态并移除位置摘要。 */
    @Test
    fun dragCancelEndsOuterDraggingSummary() = runComposeUiTest {
        var isInteractive by mutableStateOf(true)
        var isDragging by mutableStateOf(false)
        val draggingHistory = mutableListOf<Boolean>()

        setContent {
            val state = remember { ScrollbarState() }
            Box {
                Scrollbar(
                    modifier = Modifier.testTag(ScrollbarTag).size(width = 40.dp, height = 240.dp),
                    isShowTrack = true,
                    isSupperSmall = true,
                    isInteractive = isInteractive,
                    orientation = Orientation.Vertical,
                    state = state,
                    onDragStart = {
                        isDragging = true
                        draggingHistory += true
                    },
                    onDragEnd = {
                        isDragging = false
                        draggingHistory += false
                    },
                    thumb = { Box(modifier = Modifier.fillMaxSize()) },
                )
                if (isDragging) {
                    Text(SummaryText)
                }
            }
        }

        onNodeWithTag(ScrollbarTag).performTouchInput {
            down(position = Offset(x = 20f, y = 20f))
            moveTo(position = Offset(x = 20f, y = 180f))
        }
        onNodeWithText(SummaryText).assertExists()
        runOnIdle { isInteractive = false }
        waitForIdle()

        assertTrue(draggingHistory.contains(true))
        onNodeWithText(SummaryText).assertDoesNotExist()
    }

    /** 测试使用的稳定语义与摘要文本。 */
    private companion object {
        /** 滚动条测试节点标签。 */
        const val ScrollbarTag = "scrollbar"

        /** 拖动期间显示的位置摘要。 */
        const val SummaryText = "第 20 项"
    }
}
