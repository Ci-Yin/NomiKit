package ciyin.designsystem.fluent

import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.unit.sp
import ciyin.designsystem.component.card.NomiCard
import ciyin.designsystem.component.surface.NomiSurface
import ciyin.designsystem.component.text.NomiText
import ciyin.designsystem.material3.Material3
import ciyin.designsystem.runtime.LocalNomiContentColor
import ciyin.designsystem.runtime.LocalNomiTextStyle
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.theme.DarkMode
import ciyin.designsystem.theme.NomiTheme
import ciyin.designsystem.theme.NomiThemeOverride
import ciyin.designsystem.theme.NomiThemeOverrides
import io.github.composefluent.Colors
import io.github.composefluent.CornerRadius
import io.github.composefluent.ExperimentalFluentApi
import io.github.composefluent.FluentTheme
import io.github.composefluent.Typography
import io.github.composefluent.component.ButtonDefaults
import io.github.composefluent.darkColors
import io.github.composefluent.lightColors
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import io.github.composefluent.component.Button as NativeButton

/**
 * 原生 Fluent 祖先基线、配置优先级及嵌套原生 slot 的严格回归。
 */
@OptIn(ExperimentalTestApi::class, ExperimentalFluentApi::class)
class FluentNativeThemeTest {
    /**
     * A-B-A 不继承中间系统默认，显式 accent 请求合并并恢复，完整八角色排版保留原生祖先实例。
     */
    @Test
    fun aToBToAPreservesNativeBaselineAndExplicitRequests() = runComposeUiTest {
        var outerAccent by mutableStateOf(Color.Cyan)
        var expectedTypography: Typography? = null
        var outerTypography: Typography? = null
        var childTypography: Typography? = null
        var restoredAccent = Color.Unspecified
        var nestedAccent = Color.Unspecified
        var expectedCorners: CornerRadius? = null
        var childCorners: CornerRadius? = null
        setContent {
            val upstream = FluentTheme.typography
            FluentTheme(colors = lightColors(), typography = upstream) {
                expectedTypography = FluentTheme.typography
                expectedCorners = FluentTheme.cornerRadius
                NomiTheme(
                    designSystem = NomiDesignSystem.Fluent,
                    darkMode = DarkMode.Light,
                    overrides = NomiThemeOverrides(accentColor = outerAccent),
                ) {
                    outerTypography = FluentTheme.typography
                    NomiThemeOverride(designSystem = NomiDesignSystem.Material3) {
                        NomiThemeOverride(designSystem = NomiDesignSystem.Fluent) {
                            childTypography = FluentTheme.typography
                            childCorners = FluentTheme.cornerRadius
                            nestedAccent = FluentTheme.colors.shades.base
                            NomiThemeOverride(overrides = NomiThemeOverrides(accentColor = Color.Magenta)) {
                                assertEquals(Color.Magenta, FluentTheme.colors.shades.base)
                            }
                            restoredAccent = FluentTheme.colors.shades.base
                        }
                    }
                }
            }
        }
        for (accent in listOf(Color.Cyan, Color.Green)) {
            runOnIdle { outerAccent = accent }
            waitForIdle()
            assertEquals(accent, nestedAccent)
            assertEquals(accent, restoredAccent)
            assertSame(expectedTypography, outerTypography)
            assertSame(expectedTypography, childTypography)
            assertEquals(expectedCorners, childCorners)
        }
    }

    /**
     * 显式原生配置高于默认基线，显式请求 accent 再覆盖颜色配置，不重置完整排版。
     */
    @Test
    fun typedConfigurationAndRequestOverridesHaveStablePriority() = runComposeUiTest {
        var accent by mutableStateOf(Color.Unspecified)
        var configuredTypography: Typography? = null
        var expectedColors: Colors? = null
        var actualTypography: Typography? = null
        var actualColors: Colors? = null
        var expectedCorners: CornerRadius? = null
        var actualCorners: CornerRadius? = null
        setContent {
            val base = FluentTheme.typography
            val typography = base.withBody(TextStyle(fontSize = 26.sp, lineHeight = 34.sp))
            configuredTypography = typography
            val colors = NomiThemeOverrides(accentColor = Color.Blue).toFluentColors(isDark = false)
            expectedColors = colors
            val corners = FluentTheme.cornerRadius
            expectedCorners = corners
            val designSystem = rememberFluentDesignSystem(
                colors = colors,
                typography = typography,
                cornerRadius = corners
            )
            NomiTheme(
                designSystem = designSystem,
                darkMode = DarkMode.Light,
                overrides = NomiThemeOverrides(accentColor = accent),
            ) {
                actualTypography = FluentTheme.typography
                actualColors = FluentTheme.colors
                actualCorners = FluentTheme.cornerRadius
                assertEquals(typography.body, NomiTheme.typography.bodyMedium)
                assertEquals(typography.caption, NomiTheme.typography.bodySmall)
                assertEquals(typography.display, NomiTheme.typography.displayLarge)
                assertEquals(typography.titleLarge, NomiTheme.typography.headlineLarge)
            }
        }
        waitForIdle()
        assertSame(expectedColors, actualColors)
        assertSame(configuredTypography, actualTypography)
        assertEquals(expectedCorners, actualCorners)
        runOnIdle { accent = Color.Red }
        waitForIdle()
        assertEquals(Color.Red, actualColors?.shades?.base)
        assertSame(configuredTypography, actualTypography)
        runOnIdle { accent = Color.Unspecified }
        waitForIdle()
        assertSame(expectedColors, actualColors)
    }

    /**
     * 仅覆盖 accent 不改变显式原生色板的深色模式与非强调色角色。
     */
    @Test
    fun accentOverridePreservesConfiguredPaletteModeAndNeutralRoles() = runComposeUiTest {
        val configured = darkColors()
        var accent by mutableStateOf(Color.Unspecified)
        var observed: Colors? = null
        setContent {
            val designSystem = rememberFluentDesignSystem(colors = configured)
            NomiTheme(
                designSystem = designSystem,
                darkMode = DarkMode.Light,
                overrides = NomiThemeOverrides(accentColor = accent),
            ) { observed = FluentTheme.colors }
        }
        waitForIdle()
        assertSame(configured, observed)
        runOnIdle { accent = Color.Red }
        waitForIdle()
        val actual = requireNotNull(observed)
        assertEquals(Color.Red, actual.shades.base)
        assertEquals(configured.darkMode, actual.darkMode)
        // accentAcrylic 按定义依赖 Shades；这里只要求非强调色背景保持不变。
        assertEquals(configured.background.card, actual.background.card)
        assertEquals(configured.background.solid, actual.background.solid)
        assertEquals(configured.background.layer, actual.background.layer)
        assertEquals(configured.background.mica, actual.background.mica)
        assertEquals(configured.background.acrylic, actual.background.acrylic)
        assertEquals(configured.background.smoke, actual.background.smoke)
        assertEquals(configured.text.text, actual.text.text)
        assertEquals(configured.control, actual.control)
        assertEquals(configured.controlAlt, actual.controlAlt)
        assertEquals(configured.stroke, actual.stroke)
        assertEquals(configured.system, actual.system)
        runOnIdle { accent = Color.Unspecified }
        waitForIdle()
        assertSame(configured, observed)
    }

    /**
     * 默认 Card/Surface 不把其原生正文色写为显式中立色，嵌套原生禁用按钮仍能接管颜色和字号。
     */
    @Test
    fun containersDoNotShadowNestedNativeDisabledButton() = runComposeUiTest {
        var expectedBody = TextStyle.Default
        setContent {
            val typography =
                FluentTheme.typography.withBody(TextStyle(fontSize = 24.sp, lineHeight = 32.sp))
            expectedBody = typography.body
            val designSystem = rememberFluentDesignSystem(typography = typography)
            NomiTheme(designSystem = designSystem, darkMode = DarkMode.Light) {
                Column {
                    NomiCard {
                        assertEquals(Color.Unspecified, LocalNomiContentColor.current)
                        assertEquals(TextStyle.Default, LocalNomiTextStyle.current)
                        val defaults = ButtonDefaults.buttonColors()
                        NativeButton(
                            onClick = {},
                            disabled = true,
                            buttonColors = ButtonDefaults.buttonColors(
                                disabled = defaults.disabled.copy(
                                    contentColor = Color.Magenta
                                )
                            ),
                        ) { NomiText("嵌套按钮", modifier = Modifier.testTag("card-native")) }
                    }
                    NomiSurface {
                        val defaults = ButtonDefaults.buttonColors()
                        NativeButton(
                            onClick = {},
                            disabled = true,
                            buttonColors = ButtonDefaults.buttonColors(
                                disabled = defaults.disabled.copy(
                                    contentColor = Color.Magenta
                                )
                            ),
                        ) { NomiText("嵌套按钮", modifier = Modifier.testTag("surface-native")) }
                    }
                }
            }
        }
        for (tag in listOf("card-native", "surface-native")) {
            val style =
                onNodeWithTag(tag, useUnmergedTree = true).fluentTextLayout().layoutInput.style
            assertEquals(Color.Magenta, style.color)
            assertEquals(expectedBody.fontSize, style.fontSize)
            assertEquals(expectedBody.lineHeight, style.lineHeight)
        }
    }
}

/**
 * 从实际原生排版复制全部八角色，只修改测试需要的正文，避免重建默认数值。
 *
 * @receiver 测试当前实际原生排版，提供未修改的七个角色。
 * @param body 替换原生 body 角色的完整文本样式。
 * @return 新的八角色排版，接收者不变。
 */
private fun Typography.withBody(body: TextStyle): Typography = Typography(
    caption = caption,
    body = body,
    bodyStrong = bodyStrong,
    bodyLarge = bodyLarge,
    subtitle = subtitle,
    title = title,
    titleLarge = titleLarge,
    display = display,
)
