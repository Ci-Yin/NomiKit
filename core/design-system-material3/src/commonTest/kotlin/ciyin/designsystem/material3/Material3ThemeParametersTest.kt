package ciyin.designsystem.material3

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.SideEffect
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
import ciyin.designsystem.theme.NomiTheme
import ciyin.designsystem.theme.NomiThemeOverride
import ciyin.designsystem.theme.NomiThemeOverrides
import ciyin.designsystem.theme.defaultLightNomiColorScheme
import ciyin.designsystem.theme.defaultNomiShapes
import ciyin.designsystem.theme.defaultNomiSizes
import ciyin.designsystem.theme.defaultNomiTypography
import ciyin.designsystem.theme.defaultSpacings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import androidx.compose.material3.darkColorScheme as nativeDarkColorScheme
import androidx.compose.material3.lightColorScheme as nativeLightColorScheme

/**
 * 直接五组主题参数与原生基线的映射、继承和无损读模型回归。
 */
@OptIn(ExperimentalTestApi::class)
class Material3ThemeParametersTest {
    /**
     * 父层完整色板与子层强调色合法合并，透明强调色也同时作用于原生主题和中立结果。
     *
     * @return 当前平台的 Compose 测试完成结果。
     */
    @Test
    fun inheritedPaletteAndChildAccentStayAligned() = runComposeUiTest {
        val palette =
            defaultLightNomiColorScheme().copy(primary = Color.Red, warning = Color.Yellow)
        val seen = mutableListOf<Color>()
        setContent {
            NomiTheme(
                colorScheme = palette,
                darkMode = DarkMode.Light,
                designSystem = NomiDesignSystem.Material3,
            ) {
                assertSame(palette, NomiTheme.colorScheme)
                NomiThemeOverride(overrides = NomiThemeOverrides(accentColor = Color.Transparent)) {
                    assertEquals(Color.Transparent, NomiTheme.colorScheme.primary)
                    assertEquals(Color.Transparent, MaterialTheme.colorScheme.primary)
                    assertEquals(palette.copy(primary = Color.Transparent), NomiTheme.colorScheme)
                    seen += NomiTheme.colorScheme.primary
                    NomiThemeOverride(colorScheme = palette.copy(primary = Color.Blue)) {
                        assertEquals(Color.Blue, NomiTheme.colorScheme.primary)
                        assertEquals(Color.Blue, MaterialTheme.colorScheme.primary)
                    }
                    assertEquals(Color.Transparent, NomiTheme.colorScheme.primary)
                }
                assertSame(palette, NomiTheme.colorScheme)
                assertEquals(Color.Red, MaterialTheme.colorScheme.primary)
            }
        }
        assertEquals(listOf(Color.Transparent), seen)
    }

    /**
     * 五组直接参数全部映射，同时保留没有原生对应角色的完整中立字段。
     *
     * @return 本回归的跨平台测试完成结果；Web 由测试运行器等待其 Promise，JVM/Native 同步完成。
     */
    @Test
    fun directGroupsMapToNativeAndPreserveCompleteProjection() = runComposeUiTest {
        val colors = defaultLightNomiColorScheme().copy(
            primary = Color.Cyan,
            warning = Color.Magenta,
            onWarning = Color.Green,
            textCaption = Color.Yellow,
        )
        val typography = defaultNomiTypography().copy(bodyMedium = TextStyle(fontSize = 29.sp))
        val shapes = defaultNomiShapes().copy(
            tiny = RoundedCornerShape(3.dp),
            huge = RoundedCornerShape(31.dp)
        )
        val spacings = defaultSpacings().copy(large = 27.dp)
        val sizes = defaultNomiSizes().let { it.copy(strokes = it.strokes.copy(thin = 3.dp)) }
        setContent {
            NomiTheme(
                darkMode = DarkMode.Light,
                colorScheme = colors,
                typography = typography,
                shapes = shapes,
                spacings = spacings,
                sizes = sizes,
                designSystem = NomiDesignSystem.Material3,
            ) {
                assertSame(colors, NomiTheme.colorScheme)
                assertSame(typography, NomiTheme.typography)
                assertSame(shapes, NomiTheme.shapes)
                assertEquals(spacings, NomiTheme.spacings)
                assertEquals(sizes, NomiTheme.sizes)
                assertEquals(colors.primary, MaterialTheme.colorScheme.primary)
                assertEquals(typography.bodyMedium, MaterialTheme.typography.bodyMedium)
                assertEquals(shapes.huge, MaterialTheme.shapes.extraLarge)
            }
        }
    }

    /**
     * Material 3 适配器返回的扁平结果保留请求模式、实际明暗状态和显式覆盖元数据。
     *
     * @return 本回归的跨平台测试完成结果；Web 由测试运行器等待其 Promise，JVM/Native 同步完成。
     */
    @Test
    fun flatResolvedThemeRetainsRequestMetadata() = runComposeUiTest {
        val overrides = NomiThemeOverrides(accentColor = Color.Cyan)
        val request = NomiThemeRequest(
            requestedMode = DarkMode.Dark,
            isDark = true,
            overrides = overrides,
        )
        var published: ResolvedNomiTheme? = null
        setContent {
            NomiDesignSystem.Material3.ProvideTheme(request = request) { resolved ->
                assertEquals(MaterialTheme.colorScheme.primary, resolved.colorScheme.primary)
                SideEffect { published = resolved }
            }
        }
        val actual = requireNotNull(published)
        assertEquals(DarkMode.Dark, actual.requestedMode)
        assertEquals(true, actual.isDark)
        assertSame(overrides, actual.overrides)
        assertEquals(Color.Cyan, actual.colorScheme.primary)
    }

    /**
     * 新根继承明确排版、形状及页面度量，但颜色根据当前模式重新选择，不继承父 accent 或色板。
     *
     * @return 本回归的跨平台测试完成结果；Web 由测试运行器等待其 Promise，JVM/Native 同步完成。
     */
    @Test
    fun nestedRootInheritsExplicitGroupsButReselectsPalette() = runComposeUiTest {
        val typography = defaultNomiTypography().copy(bodyLarge = TextStyle(fontSize = 31.sp))
        val shapes = defaultNomiShapes().copy(large = RoundedCornerShape(23.dp))
        val spacings = defaultSpacings().copy(large = 25.dp)
        val sizes = defaultNomiSizes()
        setContent {
            NomiTheme(
                darkMode = DarkMode.Light,
                colorScheme = defaultLightNomiColorScheme().copy(primary = Color.Red),
                typography = typography,
                shapes = shapes,
                spacings = spacings,
                sizes = sizes,
                designSystem = NomiDesignSystem.Material3,
            ) {
                NomiThemeOverride(overrides = NomiThemeOverrides(accentColor = Color.Magenta)) {
                    NomiTheme(darkMode = DarkMode.Dark) {
                        assertEquals(
                            nativeDarkColorScheme().primary,
                            MaterialTheme.colorScheme.primary
                        )
                        assertEquals(
                            nativeDarkColorScheme().surface,
                            MaterialTheme.colorScheme.surface
                        )
                        assertSame(typography, NomiTheme.typography)
                        assertSame(shapes, NomiTheme.shapes)
                        assertEquals(spacings, NomiTheme.spacings)
                        assertEquals(sizes, NomiTheme.sizes)
                    }
                    assertEquals(Color.Magenta, MaterialTheme.colorScheme.primary)
                }
                assertEquals(Color.Red, MaterialTheme.colorScheme.primary)
            }
        }
    }

    /**
     * 完整颜色覆盖只改已映射角色，原生配置的额外角色及排版专属角色保留。
     *
     * @return 本回归的跨平台测试完成结果；Web 由测试运行器等待其 Promise，JVM/Native 同步完成。
     */
    @Test
    fun completeOverridesPreserveUnmappedNativeConfiguration() = runComposeUiTest {
        val nativeColors = nativeLightColorScheme(
            tertiary = Color.Red,
            tertiaryContainer = Color.Green,
            inverseSurface = Color.Blue,
            primaryContainer = Color.Yellow,
        )
        val nativeTypography = Typography(bodySmall = TextStyle(fontSize = 19.sp))
        val colors =
            defaultLightNomiColorScheme().copy(primary = Color.Cyan, success = Color.Magenta)
        val typography = defaultNomiTypography().copy(displaySmall = TextStyle(fontSize = 37.sp))
        setContent {
            val designSystem = rememberMaterial3DesignSystem(
                colorScheme = nativeColors,
                typography = nativeTypography
            )
            NomiTheme(designSystem = designSystem, colorScheme = colors, typography = typography) {
                val actual = MaterialTheme.colorScheme
                assertEquals(Color.Cyan, actual.primary)
                assertEquals(nativeColors.tertiary, actual.tertiary)
                assertEquals(nativeColors.tertiaryContainer, actual.tertiaryContainer)
                assertEquals(nativeColors.inverseSurface, actual.inverseSurface)
                assertEquals(nativeColors.primaryContainer, actual.primaryContainer)
                // Typography.equals 覆盖包括十五个内部强调角色在内的完整原生对象。
                assertEquals(
                    typography.toMaterial3Typography(base = nativeTypography),
                    MaterialTheme.typography
                )
                assertSame(colors, NomiTheme.colorScheme)
                assertSame(typography, NomiTheme.typography)
            }
        }
    }

    /**
     * 子 accent 仅叠加父完整颜色 primary，子完整颜色又清除父 accent；其它语义字段及退出恢复不丢失。
     *
     * @return 本回归的跨平台测试完成结果；Web 由测试运行器等待其 Promise，JVM/Native 同步完成。
     */
    @Test
    fun localOverridesKeepCompleteGroupsAndRestoreParent() = runComposeUiTest {
        val parentColors =
            defaultLightNomiColorScheme().copy(primary = Color.Red, warning = Color.Cyan)
        val childColors =
            defaultLightNomiColorScheme().copy(primary = Color.Blue, warning = Color.Magenta)
        val parentTypography = defaultNomiTypography()
        val childTypography = parentTypography.copy(labelSmall = TextStyle(fontSize = 17.sp))
        setContent {
            NomiTheme(
                colorScheme = parentColors,
                typography = parentTypography,
                designSystem = NomiDesignSystem.Material3,
            ) {
                NomiThemeOverride(overrides = NomiThemeOverrides(accentColor = Color.Green)) {
                    assertEquals(parentColors.copy(primary = Color.Green), NomiTheme.colorScheme)
                    assertEquals(Color.Green, MaterialTheme.colorScheme.primary)
                    NomiThemeOverride(colorScheme = childColors, typography = childTypography) {
                        assertSame(childColors, NomiTheme.colorScheme)
                        assertSame(childTypography, NomiTheme.typography)
                        assertEquals(Color.Blue, MaterialTheme.colorScheme.primary)
                    }
                    assertSame(parentTypography, NomiTheme.typography)
                    assertEquals(parentColors.copy(primary = Color.Green), NomiTheme.colorScheme)
                }
                assertSame(parentColors, NomiTheme.colorScheme)
            }
        }
    }
}
