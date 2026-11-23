package ciyin.designsystem.fluent.button

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.click
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runComposeUiTest
import ciyin.designsystem.component.button.NomiButtonColors
import ciyin.designsystem.component.button.NomiIconButton
import ciyin.designsystem.fluent.Fluent
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.theme.DarkMode
import ciyin.designsystem.theme.NomiTheme
import io.github.composefluent.LocalContentColor
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Fluent 图标按钮适配：原生 SubtleButton 图标形态的点击、禁用与内容色。
 */
@OptIn(ExperimentalTestApi::class)
class FluentIconButtonTest {
    /**
     * 原生按钮响应点击，禁用后不可操作。
     */
    @Test
    fun iconButtonForwardsClicksAndDisabledState() = runComposeUiTest {
        var enabled by mutableStateOf(true)
        var clicks = 0
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Fluent, darkMode = DarkMode.Light) {
                NomiIconButton(
                    onClick = { clicks++ },
                    modifier = Modifier.testTag("icon"),
                    enabled = enabled,
                ) {}
            }
        }
        val icon = onNodeWithTag("icon")
        icon.performTouchInput { click() }
        runOnIdle { assertEquals(1, clicks) }
        runOnIdle { enabled = false }
        icon.performTouchInput { click() }
        runOnIdle { assertEquals(1, clicks) }
    }

    /**
     * 显式内容色进入原生按钮内容环境。
     */
    @Test
    fun explicitContentColorReachesFluentContent() = runComposeUiTest {
        var observed = Color.Unspecified
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Fluent, darkMode = DarkMode.Light) {
                NomiIconButton(
                    onClick = {},
                    colors = NomiButtonColors(contentColor = Color.Magenta),
                ) {
                    observed = LocalContentColor.current
                }
            }
        }
        runOnIdle { assertEquals(Color.Magenta, observed) }
    }
}
