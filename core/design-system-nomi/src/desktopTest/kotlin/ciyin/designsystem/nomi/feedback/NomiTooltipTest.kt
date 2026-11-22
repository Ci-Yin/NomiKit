package ciyin.designsystem.nomi.feedback

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
import ciyin.designsystem.nomi.Default
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.theme.DarkMode
import ciyin.designsystem.theme.NomiTheme
import kotlin.test.Test

/**
 * Nomi 提示浮层实现：容器与内容配色读取 Nomi 令牌。
 */
@OptIn(ExperimentalTestApi::class)
class NomiTooltipTest {
    /**
     * 锚点内容正常渲染，浮层状态不暴露给调用方。
     *
     * @return 当前平台的 Compose 测试执行结果。
     */
    @Test
    fun anchorContentRendersWithoutExposingState() = runComposeUiTest {
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Default, darkMode = DarkMode.Light) {
                NomiTooltip(tooltip = { NomiText("提示内容") }) {
                    Box(Modifier.size(48.dp).testTag("anchor"))
                }
            }
        }
        onNodeWithTag("anchor", useUnmergedTree = true).assertExists()
    }
}
