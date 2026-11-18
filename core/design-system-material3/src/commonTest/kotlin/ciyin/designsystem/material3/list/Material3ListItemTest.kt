package ciyin.designsystem.material3.list

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import ciyin.designsystem.component.list.NomiListItem
import ciyin.designsystem.component.list.NomiListItemSpec
import ciyin.designsystem.component.text.NomiText
import ciyin.designsystem.material3.Material3
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.theme.DarkMode
import ciyin.designsystem.theme.NomiTheme
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Material 3 列表项适配：原生槽位、点击行为与静态行边界。
 */
@OptIn(ExperimentalTestApi::class)
class Material3ListItemTest {
    /**
     * 可点击行渲染标题、副标题与两个内容槽，点击回调命中一次。
     *
     * @return 当前平台的 Compose 测试执行结果。
     */
    @Test
    fun clickableRowRendersSlotsAndDispatchesClick() = runComposeUiTest {
        var clicks = 0
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Material3, darkMode = DarkMode.Light) {
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
     * 静态行不附加点击语义，未提供的槽不进入组合。
     *
     * @return 当前平台的 Compose 测试执行结果。
     */
    @Test
    fun staticRowHasNoClickActionAndOmittedSlotsStayAbsent() = runComposeUiTest {
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Material3, darkMode = DarkMode.Light) {
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
     * 携带专属参数时 overline 槽进入原生渲染，同时携带的其它系统参数被静默忽略。
     *
     * @return 当前平台的 Compose 测试执行结果。
     */
    @Test
    fun nativeSpecFeedsOverlineAndIgnoresForeignSpec() = runComposeUiTest {
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Material3, darkMode = DarkMode.Light) {
                NomiListItem(
                    title = { NomiText("标题") },
                    modifier = Modifier.testTag("spec"),
                    nativeSpec = Material3ListItemSpec(overlineContent = { NomiText("分组") }) +
                            ForeignListItemSpec(),
                )
            }
        }
        onNodeWithTag("spec").assertExists()
        onNodeWithText("分组").assertExists()
        onNodeWithText("标题").assertExists()
    }
}

/**
 * 其它设计系统的测试专属参数，用于验证跨系统静默忽略。
 */
private class ForeignListItemSpec : NomiListItemSpec
