package ciyin.designsystem.nomi.button

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import ciyin.designsystem.component.button.NomiButtonColors
import ciyin.designsystem.component.button.NomiIconButton
import ciyin.designsystem.nomi.Default
import ciyin.designsystem.nomi.LocalNomiNativeContentColor
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.theme.NomiTheme
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Nomi 图标按钮适配：方形触摸目标、点击与禁用语义、内容色继承。
 */
@OptIn(ExperimentalTestApi::class)
class NomiIconButtonTest {
    /**
     * 默认使用 48dp 方形最小触摸目标。
     */
    @Test
    fun defaultSizeIsSquareMinimumTouchTarget() = runComposeUiTest {
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Default) {
                NomiIconButton(
                    onClick = {},
                    modifier = Modifier.testTag("icon"),
                ) {}
            }
        }
        onNodeWithTag("icon").assertWidthIsEqualTo(48.dp)
        onNodeWithTag("icon").assertHeightIsEqualTo(48.dp)
    }

    /**
     * 启用时响应一次点击，禁用后不再接受操作。
     */
    @Test
    fun clickAndDisabledSemantics() = runComposeUiTest {
        var enabled by mutableStateOf(true)
        var clicks = 0
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Default) {
                NomiIconButton(
                    onClick = { clicks++ },
                    modifier = Modifier.testTag("icon"),
                    enabled = enabled,
                ) {}
            }
        }
        val icon = onNodeWithTag("icon")
        icon.assert(hasClickAction())
        icon.performClick()
        runOnIdle { assertEquals(1, clicks) }
        runOnIdle { enabled = false }
        icon.assertIsNotEnabled()
    }

    /**
     * 未指定内容色时继承当前槽位，显式覆盖时优先使用显式值。
     */
    @Test
    fun contentColorInheritsSlotAndExplicitOverride() = runComposeUiTest {
        var useExplicit by mutableStateOf(false)
        var observed = Color.Unspecified
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Default) {
                CompositionLocalProvider(LocalNomiNativeContentColor provides Color.Red) {
                    NomiIconButton(
                        onClick = {},
                        colors = if (useExplicit) {
                            NomiButtonColors(contentColor = Color.Blue)
                        } else {
                            NomiButtonColors()
                        },
                    ) {
                        val current = LocalNomiNativeContentColor.current
                        SideEffect { observed = current }
                    }
                }
            }
        }
        runOnIdle { assertEquals(Color.Red, observed) }
        runOnIdle { useExplicit = true }
        runOnIdle { assertEquals(Color.Blue, observed) }
    }
}
