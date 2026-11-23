package ciyin.designsystem.material3

import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import ciyin.designsystem.component.button.NomiButtonColors
import ciyin.designsystem.component.button.NomiIconButton
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.theme.DarkMode
import ciyin.designsystem.theme.NomiTheme
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Material 3 图标按钮适配：原生点击语义、禁用状态与稀疏颜色。
 */
@OptIn(ExperimentalTestApi::class)
class Material3IconButtonTest {
    /**
     * 原生图标按钮响应点击，禁用后不可操作。
     */
    @Test
    fun iconButtonForwardsClicksAndDisabledState() = runComposeUiTest {
        var enabled by mutableStateOf(true)
        var clicks = 0
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Material3, darkMode = DarkMode.Light) {
                NomiIconButton(
                    onClick = { clicks++ },
                    modifier = Modifier.testTag("icon"),
                    enabled = enabled,
                ) {}
            }
        }
        val icon = onNodeWithTag("icon")
        icon.performClick()
        runOnIdle { assertEquals(1, clicks) }
        runOnIdle { enabled = false }
        icon.assertIsNotEnabled()
    }

    /**
     * 未指定颜色时沿用原生图标按钮色板，显式值覆盖启用与禁用内容色。
     */
    @Test
    fun contentColorsFollowNativeIconButtonDefaults() = runComposeUiTest {
        var useExplicit by mutableStateOf(false)
        var enabled by mutableStateOf(true)
        var observed = Color.Unspecified
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Material3, darkMode = DarkMode.Light) {
                CompositionLocalProvider(LocalContentColor provides Color.Red) {
                    NomiIconButton(
                        onClick = {},
                        enabled = enabled,
                        colors = if (useExplicit) {
                            NomiButtonColors(
                                contentColor = Color.Blue,
                                disabledContentColor = Color.Green,
                            )
                        } else {
                            NomiButtonColors()
                        },
                    ) {
                        observed = LocalContentColor.current
                    }
                }
            }
        }
        runOnIdle { assertEquals(Color.Red, observed) }
        runOnIdle { useExplicit = true }
        runOnIdle { assertEquals(Color.Blue, observed) }
        runOnIdle { enabled = false }
        runOnIdle { assertEquals(Color.Green, observed) }
    }
}
