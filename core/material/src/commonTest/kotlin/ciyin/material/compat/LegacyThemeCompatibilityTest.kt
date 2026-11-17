package ciyin.material.compat

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ciyin.designsystem.material3.Material3
import ciyin.designsystem.material3.toFullMaterial3Theme
import ciyin.designsystem.material3.toMaterial3Typography
import ciyin.designsystem.runtime.LocalNomiDesignSystem
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.theme.NomiTheme
import ciyin.designsystem.theme.defaultLightNomiColorScheme
import ciyin.designsystem.theme.defaultNomiShapes
import ciyin.designsystem.theme.defaultNomiSizes
import ciyin.designsystem.theme.defaultNomiTypography
import ciyin.designsystem.theme.defaultSpacings
import ciyin.material.theme.AppTheme
import ciyin.material.theme.DarkMode
import ciyin.material.theme.DarkMode.Dark
import ciyin.material.theme.DarkMode.Light
import ciyin.material.theme.Spacings
import ciyin.material.theme.darkColorScheme
import ciyin.material.theme.lightColorScheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

/**
 * 验证旧主题完整参数、嵌套继承、原生排版身份及新主题根下的旧只读入口。
 */
@OptIn(ExperimentalTestApi::class)
class LegacyThemeCompatibilityTest {
    /**
     * 验证旧主题完整命名参数可用，且原生 Material 环境保留指定排版实例与已解析明暗。
     *
     * @return [runComposeUiTest] 的平台测试结果；JVM/Native 等待执行完成，Web 返回表示完成状态的 Promise。
     */
    @Test
    fun fullLegacySignaturePreservesOriginalTypography() = runComposeUiTest {
        val typography = Typography(bodyMedium = TextStyle(fontSize = 23.sp))
        val colors = lightColorScheme(primary = Color.Red)
        val shapes = defaultNomiShapes()
        val spacings = defaultSpacings().copy(large = 19.dp)
        val sizes = defaultNomiSizes()
        var observedTypography: Typography? = null
        var nativeTypography: Typography? = null
        var observedDarkMode: DarkMode? = null
        var observedSpacings: Spacings? = null
        setContent {
            AppTheme(
                darkMode = Dark,
                colorScheme = colors,
                typography = typography,
                shapes = shapes,
                spacings = spacings,
                sizes = sizes,
                content = {
                    observedTypography = AppTheme.typography
                    nativeTypography = MaterialTheme.typography
                    observedDarkMode = AppTheme.darkMode
                    observedSpacings = AppTheme.spacings
                    assertEquals(Color.Red, AppTheme.colorScheme.primary)
                    assertEquals(NomiTheme.colorScheme, AppTheme.colorScheme)
                    assertEquals(shapes, AppTheme.shapes)
                    assertEquals(sizes, AppTheme.sizes)
                    assertEquals(
                        NomiDesignSystem.Material3.id,
                        LocalNomiDesignSystem.current?.id,
                    )
                },
            )
        }
        assertSame(typography, observedTypography)
        assertSame(typography, nativeTypography)
        assertEquals(Dark, observedDarkMode)
        assertEquals(spacings, observedSpacings)
    }

    /**
     * 验证没有主题包装时旧模式为已解析的浅色，默认排版与中立默认令牌转换结果一致。
     *
     * @return [runComposeUiTest] 的平台测试结果；JVM/Native 等待执行完成，Web 返回表示完成状态的 Promise。
     */
    @Test
    fun unwrappedLegacyDefaultsRemainUnchanged() = runComposeUiTest {
        var mode: DarkMode? = null
        var typography: Typography? = null
        setContent {
            mode = AppTheme.darkMode
            typography = AppTheme.typography
        }
        assertEquals(Light, mode)
        assertEquals(defaultNomiTypography().toMaterial3Typography(), typography)
    }

    /**
     * 验证嵌套旧主题省略颜色时重选默认深色，同时继承父级模式、间距与原生排版实例。
     *
     * @return [runComposeUiTest] 的平台测试结果；JVM/Native 等待执行完成，Web 返回表示完成状态的 Promise。
     */
    @Test
    fun nestedDefaultsKeepLegacySelectionBehavior() = runComposeUiTest {
        val typography = Typography(bodyMedium = TextStyle(fontSize = 21.sp))
        val spacings = defaultSpacings().copy(large = 21.dp)
        var observedTypography: Typography? = null
        var observedColor = Color.Unspecified
        setContent {
            AppTheme(
                darkMode = Dark,
                colorScheme = darkColorScheme(primary = Color.Red),
                typography = typography,
                spacings = spacings,
            ) {
                AppTheme {
                    observedTypography = AppTheme.typography
                    observedColor = AppTheme.colorScheme.primary
                    assertEquals(Dark, AppTheme.darkMode)
                    assertEquals(spacings, AppTheme.spacings)
                }
            }
        }
        assertSame(typography, observedTypography)
        assertEquals(darkColorScheme().primary, observedColor)
    }

    /**
     * 验证旧门面显式排版不替换父主题的完整投影，退出后父主题保持不变。
     *
     * @return [runComposeUiTest] 的平台测试结果；JVM/Native 等待执行完成，Web 返回表示完成状态的 Promise。
     */
    @Test
    fun explicitLegacyTypographyIsIsolatedFromParentTheme() = runComposeUiTest {
        val parentTypography =
            defaultNomiTypography().copy(bodyMedium = TextStyle(fontSize = 41.sp))
        val parentShapes = defaultNomiShapes().copy(large = RoundedCornerShape(29.dp))
        val parentSpacings = defaultSpacings().copy(large = 31.dp)
        val originalTypography = Typography(bodyMedium = TextStyle(fontSize = 19.sp))
        val legacyShapes = defaultNomiShapes().copy(large = RoundedCornerShape(7.dp))
        var observed: Typography? = null
        var parentTypographyAfter = defaultNomiTypography()
        var parentShapesAfter = defaultNomiShapes()
        setContent {
            NomiTheme(
                theme = NomiTheme(
                    typography = parentTypography,
                    shapes = parentShapes,
                    spacings = parentSpacings,
                ),
                designSystem = NomiDesignSystem.Material3 { toFullMaterial3Theme() },
            ) {
                AppTheme(
                    typography = originalTypography,
                    shapes = legacyShapes,
                    colorScheme = lightColorScheme(primary = Color.Cyan),
                ) {
                    observed = AppTheme.typography
                    assertSame(originalTypography, MaterialTheme.typography)
                    assertEquals(legacyShapes, AppTheme.shapes)
                    assertEquals(parentSpacings, AppTheme.spacings)
                    assertEquals(Color.Cyan, AppTheme.colorScheme.primary)
                }
                parentTypographyAfter = NomiTheme.typography
                parentShapesAfter = NomiTheme.shapes
            }
        }
        assertSame(originalTypography, observed)
        assertEquals(parentTypography, parentTypographyAfter)
        assertEquals(parentShapes, parentShapesAfter)
    }

    /**
     * 验证旧主题省略参数时先读取父级排版、圆角与模式，并按模式重选默认色。
     *
     * @return [runComposeUiTest] 的平台测试结果；JVM/Native 等待执行完成，Web 返回表示完成状态的 Promise。
     */
    @Test
    fun omittedLegacyParametersReadParentTheme() = runComposeUiTest {
        val parentTypography =
            defaultNomiTypography().copy(bodyMedium = TextStyle(fontSize = 35.sp))
        val parentShapes = defaultNomiShapes().copy(
            large = RoundedCornerShape(21.dp),
        )
        var parentNativeTypography: Typography? = null
        var childNativeTypography: Typography? = null
        setContent {
            NomiTheme(
                theme = NomiTheme(
                    typography = parentTypography,
                    shapes = parentShapes,
                    isDark = true,
                ),
                designSystem = NomiDesignSystem.Material3 { toFullMaterial3Theme() },
            ) {
                parentNativeTypography = MaterialTheme.typography
                AppTheme {
                    childNativeTypography = AppTheme.typography
                    assertEquals(parentShapes, AppTheme.shapes)
                    assertEquals(darkColorScheme().primary, AppTheme.colorScheme.primary)
                    assertEquals(Dark, AppTheme.darkMode)
                }
            }
        }
        assertSame(parentNativeTypography, childNativeTypography)
    }

    /**
     * 验证无旧包装器时旧只读入口仍读取新 Material 3 根的主题投影及内层实际 Material 排版。
     *
     * @return [runComposeUiTest] 的平台测试结果；JVM/Native 等待执行完成，Web 返回表示完成状态的 Promise。
     */
    @Test
    fun legacyGettersReadNewMaterialRootWithoutBridgeGate() = runComposeUiTest {
        val spacings = defaultSpacings().copy(large = 29.dp)
        val baseTheme = NomiTheme(
            colorScheme = defaultLightNomiColorScheme().copy(primary = Color.Cyan),
            spacings = spacings,
            isDark = true,
        )
        val actualTypography = Typography(bodyMedium = TextStyle(fontSize = 31.sp))
        var observedTypography: Typography? = null
        setContent {
            NomiTheme(
                theme = baseTheme,
                designSystem = NomiDesignSystem.Material3 { toFullMaterial3Theme() },
            ) {
                MaterialTheme(typography = actualTypography) {
                    observedTypography = AppTheme.typography
                    assertEquals(Color.Cyan, AppTheme.colorScheme.primary)
                    assertEquals(NomiTheme.colorScheme, AppTheme.colorScheme)
                    assertEquals(spacings, AppTheme.spacings)
                    assertEquals(NomiTheme.shapes, AppTheme.shapes)
                    assertEquals(NomiTheme.sizes, AppTheme.sizes)
                    assertEquals(Dark, AppTheme.darkMode)
                }
            }
        }
        assertSame(actualTypography, observedTypography)
    }
}
