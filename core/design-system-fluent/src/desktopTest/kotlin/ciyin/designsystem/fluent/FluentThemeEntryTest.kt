package ciyin.designsystem.fluent

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
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Fluent 主题入口基于 [NomiThemeOverrides] 的映射、继承及降级边界。
 */
@OptIn(ExperimentalTestApi::class)
class FluentThemeEntryTest {
    /**
     * Fluent 直接回调五组结果与原请求来源，不把原生默认回填为明确覆盖。
     */
    @Test
    fun providerPublishesFlatResultsAndExplicitSources() = runComposeUiTest {
        val typography = defaultNomiTypography().copy(bodyMedium = TextStyle(fontSize = 23.sp))
        val spacings = defaultSpacings().copy(large = 27.dp)
        val sizes = defaultNomiSizes()
        val overrides = NomiThemeOverrides(
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
    fun explicitTypographyAndLayoutGroupsKeepExactReadModel() = runComposeUiTest {
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
                designSystem = NomiDesignSystem.Fluent,
                overrides = NomiThemeOverrides(
                    typography = input,
                    spacings = spacing,
                    sizes = sizes,
                ),
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
     * 嵌套根保留明确排版而重新选原生颜色，Override 则保留明确的完整色板。
     */
    @Test
    fun nestedRootRechoosesColorsWhileOverrideInheritsPalette() = runComposeUiTest {
        val input = defaultNomiTypography().copy(bodyMedium = TextStyle(fontSize = 24.sp))
        val palette = defaultLightNomiColorScheme().copy(primary = Color.Red)
        var nestedColors: Colors? = null
        var nestedBody: TextStyle? = null
        var overriddenBase = Color.Unspecified
        setContent {
            NomiTheme(
                darkMode = DarkMode.Light,
                designSystem = NomiDesignSystem.Fluent,
                overrides = NomiThemeOverrides(typography = input, colorScheme = palette),
            ) {
                NomiTheme {
                    nestedColors = FluentTheme.colors
                    nestedBody = FluentTheme.typography.body
                }
                NomiThemeOverride { overriddenBase = FluentTheme.colors.shades.base }
            }
        }
        assertEquals(lightColors().shades.base, requireNotNull(nestedColors).shades.base)
        assertEquals(input.bodyMedium, nestedBody)
        assertEquals(Color.Red, overriddenBase)
    }

    /**
     * Fluent 没有完整色板入口：完整色板只取 primary 作为强调色，其余字段忽略。
     */
    @Test
    fun completeColorSchemeFallsBackToPrimaryAccent() = runComposeUiTest {
        val scheme = defaultLightNomiColorScheme().copy(primary = Color(0xFF123456))
        var accent = Color.Unspecified
        setContent {
            NomiTheme(
                designSystem = NomiDesignSystem.Fluent,
                overrides = NomiThemeOverrides(colorScheme = scheme),
            ) {
                accent = FluentTheme.colors.shades.base
            }
        }
        runOnIdle { assertEquals(Color(0xFF123456), accent) }
    }

    /**
     * 九级圆角无法表示为原生 Dp CornerRadius：忽略覆盖并正常渲染。
     */
    @Test
    fun completeShapesAreIgnored() = runComposeUiTest {
        var rendered = false
        setContent {
            NomiTheme(
                designSystem = NomiDesignSystem.Fluent,
                overrides = NomiThemeOverrides(shapes = defaultNomiShapes()),
            ) {
                rendered = true
            }
        }
        runOnIdle { assertTrue(rendered) }
    }
}