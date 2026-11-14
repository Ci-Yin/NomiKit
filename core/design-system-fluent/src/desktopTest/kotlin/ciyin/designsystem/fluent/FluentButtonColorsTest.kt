package ciyin.designsystem.fluent

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.text.TextStyle
import ciyin.designsystem.component.button.NomiButton
import ciyin.designsystem.component.button.NomiButtonColors
import ciyin.designsystem.component.button.NomiButtonVariant
import ciyin.designsystem.component.text.NomiText
import ciyin.designsystem.fluent.button.toFluentButtonColors
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.theme.DarkMode
import ciyin.designsystem.theme.NomiTheme
import io.github.composefluent.FluentTheme
import io.github.composefluent.LocalContentColor
import io.github.composefluent.component.ButtonColorScheme
import io.github.composefluent.component.ButtonDefaults
import io.github.composefluent.darkColors
import io.github.composefluent.lightColors
import kotlin.test.Test
import kotlin.test.assertEquals
import io.github.composefluent.component.Text as NativeText

/**
 * 原生五态颜色逐字段覆盖与文字颜色优先级，不用可见性断言替代色值。
 */
@OptIn(ExperimentalTestApi::class)
class FluentButtonColorsTest {
    /**
     * 全部变体在深浅色中逐通道覆盖，未指定通道及五态描边必须保留原生值。
     */
    @Test
    fun partialOverridesPreserveEveryNativeStateAndBorder() = runComposeUiTest {
        var variant by mutableStateOf<NomiButtonVariant?>(null)
        var dark by mutableStateOf(false)
        var override by mutableStateOf(NomiButtonColors())
        var native: ButtonColorScheme? = null
        var actual: ButtonColorScheme? = null
        var criticalFill = Color.Unspecified
        var criticalText = Color.Unspecified
        setContent {
            FluentTheme(colors = if (dark) darkColors() else lightColors()) {
                native = when (variant) {
                    NomiButtonVariant.Primary -> ButtonDefaults.accentButtonColors()
                    NomiButtonVariant.Subtle -> ButtonDefaults.subtleButtonColors()
                    NomiButtonVariant.Text -> ButtonDefaults.hyperlinkButtonColors()
                    else -> ButtonDefaults.buttonColors()
                }
                actual = override.toFluentButtonColors(variant)
                criticalFill = FluentTheme.colors.system.criticalBackground
                criticalText = FluentTheme.colors.system.critical
            }
        }
        val cases = listOf(
            NomiButtonColors(),
            NomiButtonColors(containerColor = Color.Transparent),
            NomiButtonColors(contentColor = Color.Magenta),
            NomiButtonColors(disabledContainerColor = Color.Yellow),
            NomiButtonColors(disabledContentColor = Color.Cyan),
            NomiButtonColors(
                containerColor = Color.Red,
                contentColor = Color.Green,
                disabledContainerColor = Color.Blue,
                disabledContentColor = Color.Transparent,
            ),
        )
        for (isDark in listOf(false, true)) {
            for (current in listOf(null) + NomiButtonVariant.entries) {
                for (colors in cases) {
                    runOnIdle { dark = isDark; variant = current; override = colors }
                    waitForIdle()
                    val source = native!!
                    val target = actual!!
                    val sourceStates = listOf(
                        source.default,
                        source.hovered,
                        source.pressed,
                        source.focused,
                        source.disabled
                    )
                    val targetStates = listOf(
                        target.default,
                        target.hovered,
                        target.pressed,
                        target.focused,
                        target.disabled
                    )
                    for (index in sourceStates.indices) {
                        val disabled = index == sourceStates.lastIndex
                        val base = sourceStates[index]
                        val mapped = targetStates[index]
                        val fill =
                            if (disabled) colors.disabledContainerColor else colors.containerColor
                        val foreground =
                            if (disabled) colors.disabledContentColor else colors.contentColor
                        val expectedFill = when {
                            fill != Color.Unspecified -> fill
                            !disabled && current == NomiButtonVariant.Destructive -> criticalFill
                            else -> base.fillColor
                        }
                        val expectedText = when {
                            foreground != Color.Unspecified -> foreground
                            !disabled && current == NomiButtonVariant.Destructive -> criticalText
                            else -> base.contentColor
                        }
                        assertEquals(
                            expectedFill,
                            mapped.fillColor,
                            "容器：$current / $index / $isDark"
                        )
                        assertEquals(
                            expectedText,
                            mapped.contentColor,
                            "文字：$current / $index / $isDark"
                        )
                        assertEquals(
                            base.borderBrush,
                            mapped.borderBrush,
                            "描边：$current / $index / $isDark"
                        )
                    }
                }
            }
        }
    }

    /**
     * 启禁用状态显式颜色优先于文字样式色；未指定时保持原生状态色，两个文字入口一致。
     */
    @Test
    fun stateColorThenTextStyleThenNativeColorInBothStates() = runComposeUiTest {
        var enabled by mutableStateOf(true)
        var colors by mutableStateOf(NomiButtonColors())
        var style by mutableStateOf<TextStyle?>(null)
        var defaultEnabled = Color.Unspecified
        var defaultDisabled = Color.Unspecified
        var slotColor = Color.Unspecified
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Fluent, darkMode = DarkMode.Light) {
                val native = ButtonDefaults.buttonColors()
                defaultEnabled = native.default.contentColor
                defaultDisabled = native.disabled.contentColor
                NomiButton(onClick = {}, enabled = enabled, colors = colors, textStyle = style) {
                    slotColor = LocalContentColor.current
                    NativeText("原生", modifier = Modifier.testTag("native-text"))
                    NomiText("中立", modifier = Modifier.testTag("app-text"))
                }
            }
        }
        for (currentEnabled in listOf(true, false)) {
            for (stateColor in listOf(Color.Unspecified, Color.Yellow, Color.Transparent)) {
                for (styleColor in listOf(Color.Unspecified, Color.Red)) {
                    runOnIdle {
                        enabled = currentEnabled
                        colors = if (currentEnabled) {
                            NomiButtonColors(
                                contentColor = stateColor,
                                disabledContentColor = Color.Cyan
                            )
                        } else {
                            NomiButtonColors(
                                contentColor = Color.Cyan,
                                disabledContentColor = stateColor
                            )
                        }
                        style =
                            if (styleColor == Color.Unspecified) null else TextStyle(color = styleColor)
                    }
                    waitForIdle()
                    val expected = when {
                        stateColor != Color.Unspecified -> stateColor
                        styleColor != Color.Unspecified -> styleColor
                        currentEnabled -> defaultEnabled
                        else -> defaultDisabled
                    }
                    assertEquals(expected, slotColor)
                    assertEquals(
                        expected,
                        onNodeWithTag(
                            "native-text",
                            useUnmergedTree = true
                        ).fluentTextLayout().layoutInput.style.color
                    )
                    assertEquals(
                        expected,
                        onNodeWithTag(
                            "app-text",
                            useUnmergedTree = true
                        ).fluentTextLayout().layoutInput.style.color
                    )
                }
            }
        }
    }
}
