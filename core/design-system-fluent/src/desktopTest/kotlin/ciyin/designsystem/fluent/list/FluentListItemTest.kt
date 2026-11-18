package ciyin.designsystem.fluent.list

import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.text.TextStyle
import ciyin.designsystem.component.list.NomiListItem
import ciyin.designsystem.component.list.NomiListItemSpec
import ciyin.designsystem.component.text.NomiText
import ciyin.designsystem.fluent.Fluent
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.theme.NomiTheme
import io.github.composefluent.FluentTheme
import io.github.composefluent.LocalContentColor
import io.github.composefluent.LocalTextStyle
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Fluent 列表项适配：卡片行点击、静态行边界与原生文本环境。
 */
@OptIn(ExperimentalTestApi::class)
class FluentListItemTest {
    /**
     * 可点击行渲染标题、副标题与两个内容槽，点击回调命中一次。
     */
    @Test
    fun clickableRowRendersSlotsAndDispatchesClick() = runComposeUiTest {
        var clicks = 0
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Fluent) {
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
     * 静态行使用无点击重载，不响应点击。
     */
    @Test
    fun staticRowHasNoClickAction() = runComposeUiTest {
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Fluent) {
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
     * 标题槽使用原生 bodyStrong，尾部槽使用原生 caption 与 secondary 内容色。
     */
    @Test
    fun titleAndTrailingUseNativeTextEnvironment() = runComposeUiTest {
        var seenTitleStyle: TextStyle = TextStyle.Default
        var seenTrailingColor: Color = Color.Unspecified
        var expectedBodyStrong: TextStyle = TextStyle.Default
        var expectedTrailingColor: Color = Color.Unspecified
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Fluent) {
                expectedBodyStrong = FluentTheme.typography.bodyStrong
                expectedTrailingColor = FluentTheme.colors.text.text.secondary
                NomiListItem(
                    title = {
                        val style = LocalTextStyle.current
                        SideEffect { seenTitleStyle = style }
                        NomiText("标题")
                    },
                    subtitle = { NomiText("副标题") },
                    trailing = {
                        val color = LocalContentColor.current
                        SideEffect { seenTrailingColor = color }
                        NomiText("尾部")
                    },
                )
            }
        }
        runOnIdle {
            assertEquals(expectedBodyStrong.fontSize, seenTitleStyle.fontSize)
            assertEquals(expectedBodyStrong.fontWeight, seenTitleStyle.fontWeight)
            assertEquals(expectedBodyStrong.lineHeight, seenTitleStyle.lineHeight)
            assertEquals(expectedTrailingColor, seenTrailingColor)
        }
    }

    /**
     * 携带专属参数时 dropdown 槽进入原生尾部区域，静态行仍不获得点击语义。
     */
    @Test
    fun nativeSpecFeedsDropdownSlot() = runComposeUiTest {
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Fluent) {
                NomiListItem(
                    title = { NomiText("标题") },
                    modifier = Modifier.testTag("with-dropdown"),
                    nativeSpec = FluentListItemSpec(dropdown = { NomiText("箭头") }),
                )
            }
        }
        onNodeWithTag("with-dropdown").assertHasNoClickAction()
        onNodeWithText("箭头").assertExists()
    }

    /**
     * 其它设计系统的列表项参数被静默忽略，行仍渲染默认外观且不抛异常。
     */
    @Test
    fun foreignNativeSpecIsIgnored() = runComposeUiTest {
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Fluent) {
                NomiListItem(
                    title = { NomiText("标题") },
                    modifier = Modifier.testTag("foreign"),
                    nativeSpec = ForeignListItemSpec(),
                )
            }
        }
        onNodeWithTag("foreign").assertExists()
        onNodeWithText("标题").assertExists()
    }
}

/**
 * 其它设计系统的测试专属参数，用于验证跨系统静默忽略。
 */
private class ForeignListItemSpec : NomiListItemSpec