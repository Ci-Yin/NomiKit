package ciyin.designsystem.material3

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import ciyin.designsystem.component.scaffold.NomiTopAppBar
import ciyin.designsystem.component.scaffold.NomiTopAppBarColors
import ciyin.designsystem.component.text.NomiText
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.theme.DarkMode
import ciyin.designsystem.theme.NomiTheme
import kotlin.test.Test

/**
 * Material 3 顶栏适配：三个槽位渲染与显式颜色接受。
 */
@OptIn(ExperimentalTestApi::class)
class Material3TopAppBarTest {
    /**
     * 标题、导航图标与操作槽都进入原生顶栏。
     *
     * @return 当前平台的 Compose 测试执行结果。
     */
    @Test
    fun allSlotsAreRendered() = runComposeUiTest {
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Material3, darkMode = DarkMode.Light) {
                NomiTopAppBar(
                    title = { NomiText("标题", modifier = Modifier.testTag("title")) },
                    navigationIcon = {
                        NomiText(
                            "返回",
                            modifier = Modifier.testTag("navigation")
                        )
                    },
                    actions = { NomiText("操作", modifier = Modifier.testTag("action")) },
                    colors = NomiTopAppBarColors(containerColor = Color(0xFF102030)),
                )
            }
        }
        onNodeWithTag("title").assertExists()
        onNodeWithTag("navigation").assertExists()
        onNodeWithTag("action").assertExists()
    }

    /**
     * 未提供导航图标时仍渲染标题，显式透明标题色被接受。
     *
     * @return 当前平台的 Compose 测试执行结果。
     */
    @Test
    fun missingNavigationIconStillRendersTitle() = runComposeUiTest {
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Material3, darkMode = DarkMode.Light) {
                NomiTopAppBar(
                    title = { NomiText("标题", modifier = Modifier.testTag("title")) },
                    colors = NomiTopAppBarColors(titleContentColor = Color.Transparent),
                )
            }
        }
        onNodeWithTag("title").assertExists()
    }
}
