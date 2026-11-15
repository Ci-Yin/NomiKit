package ciyin.designsystem.nomi.list

import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import ciyin.designsystem.component.list.NomiListItem
import ciyin.designsystem.component.text.NomiText
import ciyin.designsystem.nomi.Default
import ciyin.designsystem.runtime.LocalNomiTextStyle
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.theme.NomiTheme
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Nomi 列表项实现：点击行为、静态行边界与令牌文本环境。
 */
@OptIn(ExperimentalTestApi::class)
class DefaultNomiListItemTest {
    /**
     * 可点击行渲染标题、副标题与两个内容槽，点击回调命中一次。
     */
    @Test
    fun clickableRowRendersSlotsAndDispatchesClick() = runComposeUiTest {
        var clicks = 0
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Default) {
                NomiListItem(
                    title = { NomiText("标题") },
                    modifier = Modifier.testTag("item"),
                    onClick = { clicks++ },
                    leading = { NomiText("前导") },
                    subtitle = { NomiText("副标题") },
                    trailing = { NomiText("尾部") },
                )
            }
        }
        onNodeWithText("标题").assertExists()
        onNodeWithText("前导").assertExists()
        onNodeWithText("副标题").assertExists()
        onNodeWithText("尾部").assertExists()
        onNodeWithTag("item").assertHasClickAction().performClick()
        runOnIdle { assertEquals(1, clicks) }
    }

    /**
     * 静态行不附加点击语义。
     */
    @Test
    fun staticRowHasNoClickAction() = runComposeUiTest {
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Default) {
                NomiListItem(
                    title = { NomiText("静态标题") },
                    modifier = Modifier.testTag("static"),
                )
            }
        }
        onNodeWithTag("static").assertHasNoClickAction()
        onNodeWithText("静态标题").assertExists()
    }

    /**
     * 标题槽使用 titleSmall 加 SemiBold 的 Nomi 原生标题样式。
     */
    @Test
    fun titleSlotUsesNomiTitleStyle() = runComposeUiTest {
        var seenStyle: TextStyle = TextStyle.Default
        var expectedStyle: TextStyle = TextStyle.Default
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Default) {
                expectedStyle = LocalNomiTextStyle.current.merge(
                    NomiTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                )
                NomiListItem(
                    title = {
                        val style = LocalNomiTextStyle.current
                        SideEffect { seenStyle = style }
                        NomiText("标题")
                    },
                )
            }
        }
        runOnIdle { assertEquals(expectedStyle, seenStyle) }
    }
}
