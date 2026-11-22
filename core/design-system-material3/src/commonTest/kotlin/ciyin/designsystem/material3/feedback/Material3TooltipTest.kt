package ciyin.designsystem.material3

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import ciyin.designsystem.component.feedback.NomiTooltip
import ciyin.designsystem.component.text.NomiText
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.theme.DarkMode
import ciyin.designsystem.theme.NomiTheme
import kotlin.test.Test

/**
 * Material 3 提示浮层适配：调用方内容作为锚点，浮层状态由原生实现持有。
 */
@OptIn(ExperimentalTestApi::class)
class Material3TooltipTest {
    /**
     * 锚点内容正常渲染，提示内容由原生浮层按需显示。
     *
     * @return 当前平台的 Compose 测试执行结果。
     */
    @Test
    fun anchorContentRendersWithoutExposingState() = runComposeUiTest {
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Material3, darkMode = DarkMode.Light) {
                NomiTooltip(tooltip = { NomiText("提示内容") }) {
                    Box(Modifier.size(48.dp).testTag("anchor"))
                }
            }
        }
        onNodeWithTag("anchor", useUnmergedTree = true).assertExists()
    }
}
