package ciyin.designsystem.material3

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import ciyin.designsystem.component.scaffold.NomiScaffold
import ciyin.designsystem.component.scaffold.NomiScaffoldColors
import ciyin.designsystem.component.text.NomiText
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.theme.DarkMode
import ciyin.designsystem.theme.NomiTheme
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Material 3 脚手架适配：槽位顺序、内容让位与显式颜色透传。
 */
@OptIn(ExperimentalTestApi::class)
class Material3ScaffoldTest {
    /**
     * 主体内容位于顶栏与底栏之间，不被原生悬浮栏覆盖。
     *
     * @return 当前平台的 Compose 测试执行结果。
     */
    @Test
    fun contentSitsBetweenTopAndBottomBars() = runComposeUiTest {
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Material3, darkMode = DarkMode.Light) {
                NomiScaffold(
                    topBar = {
                        Box(Modifier.fillMaxWidth().height(48.dp).testTag("top"))
                    },
                    bottomBar = {
                        Box(Modifier.fillMaxWidth().height(48.dp).testTag("bottom"))
                    },
                ) {
                    Box(Modifier.fillMaxSize().testTag("body"))
                }
            }
        }
        val top = onNodeWithTag("top").getUnclippedBoundsInRoot()
        val body = onNodeWithTag("body").getUnclippedBoundsInRoot()
        val bottom = onNodeWithTag("bottom").getUnclippedBoundsInRoot()
        assertTrue(top.bottom <= body.top, "顶栏必须位于主体内容之上")
        assertTrue(body.bottom <= bottom.top, "底栏必须位于主体内容之下")
    }

    /**
     * 显式容器色与内容色（含透明）直接交给原生 Scaffold，不做能力校验也不补默认值。
     *
     * @return 当前平台的 Compose 测试执行结果。
     */
    @Test
    fun explicitColorsAreAcceptedWithoutValidation() = runComposeUiTest {
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Material3, darkMode = DarkMode.Light) {
                NomiScaffold(
                    modifier = Modifier.testTag("scaffold"),
                    colors = NomiScaffoldColors(
                        containerColor = Color.Transparent,
                        contentColor = Color(0xFF203040),
                    ),
                    topBar = { NomiText("top") },
                    bottomBar = { NomiText("bottom") },
                ) {
                    NomiText("body")
                }
            }
        }
        onNodeWithTag("scaffold").assertExists()
        onNodeWithText("top").assertExists()
        onNodeWithText("body").assertExists()
        onNodeWithText("bottom").assertExists()
    }
}
