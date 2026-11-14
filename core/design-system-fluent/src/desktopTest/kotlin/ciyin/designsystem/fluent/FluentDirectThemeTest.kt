package ciyin.designsystem.fluent

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.runtime.NomiThemeRequest
import ciyin.designsystem.runtime.ResolvedNomiTheme
import ciyin.designsystem.theme.DarkMode
import ciyin.designsystem.theme.NomiSizes
import ciyin.designsystem.theme.NomiTheme
import ciyin.designsystem.theme.NomiThemeOverride
import ciyin.designsystem.theme.NomiThemeOverrides
import ciyin.designsystem.theme.NomiTypography
import ciyin.designsystem.theme.Spacings
import ciyin.designsystem.theme.defaultLightNomiColorScheme
import ciyin.designsystem.theme.defaultNomiShapes
import ciyin.designsystem.theme.defaultNomiSizes
import ciyin.designsystem.theme.defaultNomiTypography
import ciyin.designsystem.theme.defaultSpacings
import io.github.composefluent.Colors
import io.github.composefluent.FluentTheme
import io.github.composefluent.Typography
import io.github.composefluent.lightColors
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * 旧式直接主题参数在 Fluent 的映射、继承及明确不支持边界。
 */
@OptIn(ExperimentalTestApi::class)
class FluentDirectThemeTest {
    /**
     * Fluent 直接回调五组结果与原请求来源，不把原生默认回填为明确覆盖。
     */
    @Test
    fun providerPublishesFlatResultsAndExplicitSources() = runComposeUiTest {
        val typography = defaultNomiTypography().copy(bodyMedium = TextStyle(fontSize = 23.sp))
        val spacings = defaultSpacings().copy(large = 27.dp)
        val sizes = defaultNomiSizes()
        val overrides = NomiThemeOverrides(
            accentColor = Color.Magenta,
            typography = typography,
            spacings = spacings,
            sizes = sizes,
        )
        val request = NomiThemeRequest(
            requestedMode = DarkMode.Dark,
            isDark = true,
            overrides = overrides,
        )
        var published: ResolvedNomiTheme? = null
        var nativeColors: Colors? = null
        var nativeBody: TextStyle? = null
        setContent {
            NomiDesignSystem.Fluent.ProvideTheme(request = request) { resolved ->
                published = resolved
                nativeColors = FluentTheme.colors
                nativeBody = FluentTheme.typography.body
            }
        }
        runOnIdle {
            val result = requireNotNull(published)
            val native = requireNotNull(nativeColors)
            assertEquals(DarkMode.Dark, result.requestedMode)
            assertTrue(result.isDark)
            assertSame(overrides, result.overrides)
            assertSame(typography, result.typography)
            assertSame(spacings, result.spacings)
            assertSame(sizes, result.sizes)
            assertEquals(native.shades.base, result.colorScheme.primary)
            assertEquals(native.background.layer.default, result.colorScheme.surface)
            assertEquals(typography.bodyMedium, nativeBody)
            assertEquals(null, result.overrides.colorScheme)
            assertEquals(null, result.overrides.shapes)
        }
    }

    /**
     * 八个原生角色按既定映射生效，中立十五角色及布局组完整保留。
     */
    @Test
    fun directTypographyAndLayoutGroupsKeepExactReadModel() = runComposeUiTest {
        val input = defaultNomiTypography().copy(
            displayLarge = TextStyle(fontSize = 61.sp),
            displayMedium = TextStyle(fontSize = 53.sp),
            headlineLarge = TextStyle(fontSize = 39.sp),
            headlineMedium = TextStyle(fontSize = 29.sp),
            titleLarge = TextStyle(fontSize = 23.sp),
            bodyLarge = TextStyle(fontSize = 19.sp),
            bodyMedium = TextStyle(fontSize = 17.sp),
            bodySmall = TextStyle(fontSize = 13.sp),
            labelLarge = TextStyle(fontSize = 15.sp),
        )
        val spacing = defaultSpacings().copy(large = 31.dp)
        val sizes = defaultNomiSizes().copy(
            layoutConstraints = defaultNomiSizes().layoutConstraints.copy(cardMinWidth = 310.dp)
        )
        var actual: Typography? = null
        var projectedTypography: NomiTypography? = null
        var projectedSpacings: Spacings? = null
        var projectedSizes: NomiSizes? = null
        setContent {
            NomiTheme(
                darkMode = DarkMode.Light,
                typography = input,
                spacings = spacing,
                sizes = sizes,
                designSystem = NomiDesignSystem.Fluent,
            ) {
                actual = FluentTheme.typography
                projectedTypography = NomiTheme.typography
                projectedSpacings = NomiTheme.spacings
                projectedSizes = NomiTheme.sizes
            }
        }
        val native = requireNotNull(actual)
        assertEquals(input.bodySmall, native.caption)
        assertEquals(input.bodyMedium, native.body)
        assertEquals(input.labelLarge, native.bodyStrong)
        assertEquals(input.bodyLarge, native.bodyLarge)
        assertEquals(input.titleLarge, native.subtitle)
        assertEquals(input.headlineMedium, native.title)
        assertEquals(input.headlineLarge, native.titleLarge)
        assertEquals(input.displayLarge, native.display)
        assertSame(input, projectedTypography)
        assertSame(spacing, projectedSpacings)
        assertSame(sizes, projectedSizes)
    }

    /**
     * 嵌套根保留明确排版而重新选原生颜色，Override 则保留明确强调色。
     */
    @Test
    fun nestedRootRechoosesColorsWhileOverrideInheritsAccent() = runComposeUiTest {
        val input = defaultNomiTypography().copy(bodyMedium = TextStyle(fontSize = 24.sp))
        var nestedColors: Colors? = null
        var nestedBody: TextStyle? = null
        var overriddenAccent = Color.Unspecified
        setContent {
            NomiTheme(
                darkMode = DarkMode.Light,
                typography = input,
                designSystem = NomiDesignSystem.Fluent,
                overrides = NomiThemeOverrides(accentColor = Color.Red),
            ) {
                NomiTheme {
                    nestedColors = FluentTheme.colors
                    nestedBody = FluentTheme.typography.body
                }
                NomiThemeOverride { overriddenAccent = FluentTheme.colors.shades.base }
            }
        }
        assertEquals(lightColors().shades.base, requireNotNull(nestedColors).shades.base)
        assertEquals(input.bodyMedium, nestedBody)
        assertEquals(Color.Red, overriddenAccent)
    }

    /**
     * Fluent 没有完整二十七字段色板入口，不可只取 primary 然后默默丢弃其它字段。
     */
    @Test
    fun completeColorSchemeIsRejected() {
        reject("colorScheme") {
            NomiTheme(
                designSystem = NomiDesignSystem.Fluent,
                colorScheme = defaultLightNomiColorScheme()
            ) {}
        }
    }

    /**
     * 九级任意圆角无法表示为原生 Dp CornerRadius，明确拒绝。
     */
    @Test
    fun completeShapesAreRejected() {
        reject("shapes") {
            NomiTheme(
                designSystem = NomiDesignSystem.Fluent,
                shapes = defaultNomiShapes()
            ) {}
        }
    }

    /**
     * 每个非法请求独立建立组合环境，避免二次 setContent 掩盖目标异常。
     *
     * @param parameter 预期异常消息包含的不支持参数名。
     * @param content 在新 Compose 环境中执行的非法主题调用，应抛出 IllegalArgumentException。
     */
    private fun reject(parameter: String, content: @Composable () -> Unit) {
        val failure = assertFailsWith<IllegalArgumentException> {
            runComposeUiTest { setContent(composable = content) }
        }
        assertTrue(failure.message.orEmpty().contains(parameter))
    }
}
