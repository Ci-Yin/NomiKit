package ciyin.designsystem.fluent.feedback

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
import ciyin.designsystem.fluent.Fluent
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.theme.DarkMode
import ciyin.designsystem.theme.NomiTheme
import kotlin.test.Test

/**
 * Fluent 提示浮层适配：锚点内容正常渲染，浮层由原生 TooltipBox 管理。
 */
@OptIn(ExperimentalTestApi::class)
class FluentTooltipTest {
    /**
     * 锚点内容渲染，不因实验 API 包装失败。
     *
     * @return 当前平台的 Compose 测试执行结果。
     */
    @Test
    fun anchorContentRendersWithoutExposingState() = runComposeUiTest {
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Fluent, darkMode = DarkMode.Light) {
                NomiTooltip(tooltip = { NomiText("提示内容") }) {
                    Box(Modifier.size(48.dp).testTag("anchor"))
                }
            }
        }
        onNodeWithTag("anchor", useUnmergedTree = true).assertExists()
    }
}
