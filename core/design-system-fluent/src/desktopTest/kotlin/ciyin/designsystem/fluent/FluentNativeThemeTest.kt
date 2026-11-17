package ciyin.designsystem.fluent

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
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
import ciyin.designsystem.component.NomiDesignSystemComponents
import ciyin.designsystem.component.card.NomiCard
import ciyin.designsystem.component.surface.NomiSurface
import ciyin.designsystem.component.text.NomiText
import ciyin.designsystem.runtime.LocalNomiContentColor
import ciyin.designsystem.runtime.LocalNomiTextStyle
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.runtime.NomiDesignSystemId
import ciyin.designsystem.theme.DarkMode
import ciyin.designsystem.theme.NomiTheme
import io.github.composefluent.Colors
import io.github.composefluent.CornerRadius
import io.github.composefluent.ExperimentalFluentApi
import io.github.composefluent.Typography
import io.github.composefluent.component.ButtonDefaults
import io.github.composefluent.darkColors
import io.github.composefluent.lightColors
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import io.github.composefluent.FluentTheme as NativeFluentTheme
import io.github.composefluent.component.Button as NativeButton

/**
 * 原生 Fluent 基线捕获、映射优先级及嵌套原生 slot 的严格回归。
 */
@OptIn(ExperimentalTestApi::class, ExperimentalFluentApi::class)
class FluentNativeThemeTest {
    /**
     * A-B-A 切换复用当前原生主题的排版与圆角，而不是首次进入时捕获的基线。
     */
    @Test
    fun aToBToAReusesCurrentNativeThemeInsteadOfCapturedBaseline() = runComposeUiTest {
        var override: Typography? = null
        var outerTypography: Typography? = null
        var innerTypography: Typography? = null
        var baselineCorners: CornerRadius? = null
        var innerCorners: CornerRadius? = null
        setContent {
            val upstream = NativeFluentTheme.typography
            NativeFluentTheme(
                colors = lightColors(),
                typography = upstream.withBody(TextStyle(fontSize = 11.sp, lineHeight = 15.sp)),
            ) {
                baselineCorners = NativeFluentTheme.cornerRadius
                val covered = NativeFluentTheme.typography
                    .withBody(TextStyle(fontSize = 25.sp, lineHeight = 33.sp))
                override = covered
                NomiTheme(
                    darkMode = DarkMode.Light,
                    designSystem = NomiDesignSystem.Fluent {
                        FluentTheme(isDark = isDark, typography = covered)
                    },
                ) {
                    outerTypography = NativeFluentTheme.typography
                    NomiTheme(darkMode = DarkMode.Light, designSystem = PassthroughSystem) {
                        NomiTheme(
                            darkMode = DarkMode.Light,
                            designSystem = NomiDesignSystem.Fluent
                        ) {
                            innerTypography = NativeFluentTheme.typography
                            innerCorners = NativeFluentTheme.cornerRadius
                        }
                    }
                }
            }
        }
        runOnIdle {
            assertEquals(25.sp, requireNotNull(outerTypography).body.fontSize)
            assertEquals(requireNotNull(override).body, requireNotNull(innerTypography).body)
            assertEquals(
                requireNotNull(baselineCorners).overlay,
                requireNotNull(innerCorners).overlay,
            )
        }
    }

    /**
     * 显式 typed 色板原样安装并保留自身明暗，未指定的排版与圆角回退到捕获的基线。
     */
    @Test
    fun typedPaletteKeepsItsOwnDarkModeWhileTypographyFallsBackToBaseline() = runComposeUiTest {
        val configured = darkColors()
        var baseline: Typography? = null
        var observedColors: Colors? = null
        var observedTypography: Typography? = null
        setContent {
            baseline = NativeFluentTheme.typography
            NomiTheme(
                darkMode = DarkMode.Light,
                designSystem = NomiDesignSystem.Fluent {
                    FluentTheme(isDark = isDark, colors = configured)
                },
            ) {
                observedColors = NativeFluentTheme.colors
                observedTypography = NativeFluentTheme.typography
            }
        }
        runOnIdle {
            val colors = requireNotNull(observedColors)
            assertSame(configured, colors)
            assertEquals(true, colors.darkMode)
            assertEquals(
                requireNotNull(baseline).body,
                requireNotNull(observedTypography).body,
            )
        }
    }

    /**
     * 切换已解析明暗模式时替换原生色板，但基线排版保持首次捕获的实例。
     */
    @Test
    fun switchingResolvedModeSwapsPaletteWithoutResamplingBaseline() = runComposeUiTest {
        var dark by mutableStateOf(false)
        var nativeColors: Colors? = null
        var nativeTypography: Typography? = null
        setContent {
            val upstream = NativeFluentTheme.typography
                .withBody(TextStyle(fontSize = 12.sp, lineHeight = 16.sp))
            NativeFluentTheme(colors = lightColors(), typography = upstream) {
                NomiTheme(
                    theme = if (dark) NomiTheme.dark() else NomiTheme.light(),
                    designSystem = NomiDesignSystem.Fluent,
                ) {
                    nativeColors = NativeFluentTheme.colors
                    nativeTypography = NativeFluentTheme.typography
                }
            }
        }
        runOnIdle {
            assertEquals(false, requireNotNull(nativeColors).darkMode)
            assertEquals(12.sp, requireNotNull(nativeTypography).body.fontSize)
        }
        runOnIdle { dark = true }
        waitForIdle()
        runOnIdle {
            assertEquals(true, requireNotNull(nativeColors).darkMode)
            assertEquals(12.sp, requireNotNull(nativeTypography).body.fontSize)
        }
    }

    /**
     * 默认 Card/Surface 不把其原生正文色写为显式中立色，嵌套原生禁用按钮仍能接管颜色和字号。
     */
    @Test
    fun containersDoNotShadowNestedNativeDisabledButton() = runComposeUiTest {
        var expectedBody = TextStyle.Default
        setContent {
            val nativeTypography = NativeFluentTheme.typography
                .withBody(TextStyle(fontSize = 24.sp, lineHeight = 32.sp))
            expectedBody = nativeTypography.body
            NomiTheme(
                darkMode = DarkMode.Light,
                designSystem = NomiDesignSystem.Fluent {
                    FluentTheme(isDark = isDark, typography = nativeTypography)
                },
            ) {
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
 * 测试用「其它设计系统」边界：不安装原生主题，只把内容放回当前原生环境。
 *
 * 用于构造 A-B-A 切换，让上一步 Fluent 的覆盖仍留在原生环境中。
 */
private object PassthroughSystem : NomiDesignSystem {
    /**
     * 测试标识，与 Fluent 本身区分。
     */
    override val id = NomiDesignSystemId("fluent-test-passthrough")

    /**
     * 沿用 Fluent 组件族；本对象只承担主题边界。
     */
    override val components: NomiDesignSystemComponents = NomiDesignSystem.Fluent.components

    /**
     * 不安装原生主题，直接组合内容。
     *
     * @param theme 本层中立主题；本对象不读取也不改写它。
     * @param content 在父原生环境中组合的子树。
     */
    @Composable
    override fun ProvideTheme(theme: NomiTheme, content: @Composable () -> Unit) = content()
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
