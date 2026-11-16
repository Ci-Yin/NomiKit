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
import ciyin.designsystem.theme.defaultLightNomiColorScheme
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
     * A-B-A 不继承中间系统默认，显式完整色板请求合并并恢复，完整八角色排版保留原生祖先实例。
     */
    @Test
    fun aToBToAPreservesNativeBaselineAndExplicitRequests() = runComposeUiTest {
        var outerPrimary by mutableStateOf(Color.Cyan)
        var expectedTypography: Typography? = null
        var outerTypography: Typography? = null
        var childTypography: Typography? = null
        var restoredBase = Color.Unspecified
        var nestedBase = Color.Unspecified
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
                    overrides = NomiThemeOverrides(
                        colorScheme = defaultLightNomiColorScheme().copy(primary = outerPrimary)
                    ),
                ) {
                    outerTypography = FluentTheme.typography
                    NomiThemeOverride(designSystem = NomiDesignSystem.Material3) {
                        NomiThemeOverride(designSystem = NomiDesignSystem.Fluent) {
                            childTypography = FluentTheme.typography
                            childCorners = FluentTheme.cornerRadius
                            nestedBase = FluentTheme.colors.shades.base
                            NomiThemeOverride(
                                overrides = NomiThemeOverrides(
                                    colorScheme = defaultLightNomiColorScheme()
                                        .copy(primary = Color.Magenta)
                                )
                            ) {
                                assertEquals(Color.Magenta, FluentTheme.colors.shades.base)
                            }
                            restoredBase = FluentTheme.colors.shades.base
                        }
                    }
                }
            }
        }
        for (primary in listOf(Color.Cyan, Color.Green)) {
            runOnIdle { outerPrimary = primary }
            waitForIdle()
            assertEquals(primary, nestedBase)
            assertEquals(primary, restoredBase)
            assertSame(expectedTypography, outerTypography)
            assertSame(expectedTypography, childTypography)
            assertEquals(expectedCorners, childCorners)
        }
    }

    /**
     * 显式原生配置高于默认基线，请求中的完整色板按 primary 再覆盖强调色阶，不重置完整排版。
     */
    @Test
    fun typedConfigurationAndRequestOverridesHaveStablePriority() = runComposeUiTest {
        var requestPrimary by mutableStateOf<Color?>(null)
        var configuredTypography: Typography? = null
        val expectedColors = defaultLightNomiColorScheme()
            .copy(primary = Color.Blue)
            .toFluentColors(isDark = false)
        var actualTypography: Typography? = null
        var actualColors: Colors? = null
        var expectedCorners: CornerRadius? = null
        var actualCorners: CornerRadius? = null
        setContent {
            val base = FluentTheme.typography
            val typography = base.withBody(TextStyle(fontSize = 26.sp, lineHeight = 34.sp))
            configuredTypography = typography
            val corners = FluentTheme.cornerRadius
            expectedCorners = corners
            val designSystem = rememberFluentDesignSystem(
                colors = expectedColors,
                typography = typography,
                cornerRadius = corners
            )
            NomiTheme(
                designSystem = designSystem,
                darkMode = DarkMode.Light,
                overrides = NomiThemeOverrides(
                    colorScheme = requestPrimary?.let {
                        defaultLightNomiColorScheme().copy(primary = it)
                    }
                ),
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
        runOnIdle { requestPrimary = Color.Red }
        waitForIdle()
        assertEquals(Color.Red, actualColors?.shades?.base)
        assertSame(configuredTypography, actualTypography)
        runOnIdle { requestPrimary = null }
        waitForIdle()
        assertSame(expectedColors, actualColors)
    }

    /**
     * 完整中立色板只按 primary 重建强调色阶，显式 typed 色板的深色模式不被请求改写。
     */
    @Test
    fun neutralPaletteRebuildsAccentWhileKeepingTypedPaletteMode() = runComposeUiTest {
        val configured = darkColors()
        var requestPrimary by mutableStateOf<Color?>(null)
        var observed: Colors? = null
        setContent {
            val designSystem = rememberFluentDesignSystem(colors = configured)
            NomiTheme(
                designSystem = designSystem,
                darkMode = DarkMode.Light,
                overrides = NomiThemeOverrides(
                    colorScheme = requestPrimary?.let {
                        defaultLightNomiColorScheme().copy(primary = it)
                    }
                ),
            ) { observed = FluentTheme.colors }
        }
        waitForIdle()
        assertSame(configured, observed)
        runOnIdle { requestPrimary = Color.Red }
        waitForIdle()
        val actual = requireNotNull(observed)
        assertEquals(Color.Red, actual.shades.base)
        assertEquals(configured.darkMode, actual.darkMode)
        runOnIdle { requestPrimary = null }
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
