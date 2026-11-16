package ciyin.designsystem.material3

import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ciyin.designsystem.nomi.Default
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.theme.NomiColorScheme
import ciyin.designsystem.theme.NomiTheme
import ciyin.designsystem.theme.NomiThemeOverride
import ciyin.designsystem.theme.NomiThemeOverrides
import ciyin.designsystem.theme.DarkMode
import ciyin.designsystem.theme.Spacings
import ciyin.designsystem.theme.defaultDarkNomiColorScheme
import ciyin.designsystem.theme.defaultSpacings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

/**
 * 原生主题基线、稀疏覆盖与读模型的严格回归，不以同一包装器作为原生参照。
 */
@OptIn(ExperimentalTestApi::class)
class Material3NativeThemeTest {
    /**
     * 浅深色均取独立原生工厂，排版与圆角保留完整原生对象语义。
     *
     * @return 本回归的跨平台测试完成结果；Web 由测试运行器等待其 Promise，JVM/Native 同步完成。
     */
    @Test
    fun defaultsMatchNativeThemeAndNeverInheritForeignBaseline() = runComposeUiTest {
        var dark by mutableStateOf(false)
        var colors: ColorScheme? = null
        var typography: Typography? = null
        var shapes: Shapes? = null
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(primary = Color.Red),
                typography = Typography(bodyLarge = TextStyle(fontSize = 41.sp)),
                shapes = Shapes(medium = CutCornerShape(9.dp)),
            ) {
                NomiTheme(
                    designSystem = NomiDesignSystem.Material3,
                    darkMode = if (dark) DarkMode.Dark else DarkMode.Light,
                ) {
                    colors = MaterialTheme.colorScheme
                    typography = MaterialTheme.typography
                    shapes = MaterialTheme.shapes
                    assertEquals(MaterialTheme.colorScheme.primary, NomiTheme.colorScheme.primary)
                    assertEquals(MaterialTheme.typography.toNomiTypography(), NomiTheme.typography)
                }
            }
        }
        for (isDark in listOf(false, true)) {
            runOnIdle { dark = isDark }
            waitForIdle()
            val expected = if (isDark) darkColorScheme() else lightColorScheme()
            assertEquals(expected.primary, colors?.primary)
            assertEquals(expected.surface, colors?.surface)
            assertEquals(expected.tertiary, colors?.tertiary)
            assertEquals(Typography(), typography)
            assertEquals(Shapes(), shapes)
        }
    }

    /**
     * 局部只合并明确间距，不继承外层 Nomi 的默认排版、色板与形状；退出后回到父系统解析结果。
     *
     * @return 本回归的跨平台测试完成结果；Web 由测试运行器等待其 Promise，JVM/Native 同步完成。
     */
    @Test
    fun overridesRemainSparseAcrossSystemsAndRestoreParent() = runComposeUiTest {
        val spacings = defaultSpacings().copy(large = 27.dp)
        var childPrimary = Color.Unspecified
        var childTypography: Typography? = null
        var restoredColors: NomiColorScheme? = null
        var restoredSpacings: Spacings? = null
        setContent {
            NomiTheme(
                designSystem = NomiDesignSystem.Default,
                darkMode = DarkMode.Dark,
                overrides = NomiThemeOverrides(spacings = spacings),
            ) {
                NomiThemeOverride(designSystem = NomiDesignSystem.Material3) {
                    childPrimary = MaterialTheme.colorScheme.primary
                    childTypography = MaterialTheme.typography
                    assertEquals(darkColorScheme().primary, MaterialTheme.colorScheme.primary)
                    assertEquals(darkColorScheme().secondary, MaterialTheme.colorScheme.secondary)
                    assertEquals(spacings, NomiTheme.spacings)
                    assertEquals(Shapes(), MaterialTheme.shapes)
                }
                restoredColors = NomiTheme.colorScheme
                restoredSpacings = NomiTheme.spacings
            }
        }
        assertEquals(darkColorScheme().primary, childPrimary)
        assertEquals(Typography(), childTypography)
        assertEquals(defaultDarkNomiColorScheme(), restoredColors)
        assertEquals(spacings, restoredSpacings)
    }

    /**
     * 无请求颜色覆盖时，适配器配置的完整原生色板、排版与形状实例及逐字段值全部保留，
     * 不按模式回退原生默认，也不重建排版对象。
     *
     * @return 本回归的跨平台测试完成结果；Web 由测试运行器等待其 Promise，JVM/Native 同步完成。
     */
    @Test
    fun explicitNativeConfigurationPreservesInstances() = runComposeUiTest {
        val colors = lightColorScheme(primary = Color.Blue, tertiary = Color.Yellow)
        val typography = Typography(bodyMedium = TextStyle(fontSize = 29.sp))
        val shapes = Shapes()
        var actualTypography: Typography? = null
        var actualShapes: Shapes? = null
        setContent {
            val designSystem = rememberMaterial3DesignSystem(
                colorScheme = colors,
                typography = typography,
                shapes = shapes,
            )
            NomiTheme(
                designSystem = designSystem,
                darkMode = DarkMode.Dark,
                overrides = NomiThemeOverrides(),
            ) {
                actualTypography = MaterialTheme.typography
                actualShapes = MaterialTheme.shapes
                assertEquals(colors.primary, MaterialTheme.colorScheme.primary)
                assertEquals(colors.onPrimary, MaterialTheme.colorScheme.onPrimary)
                assertEquals(colors.tertiary, MaterialTheme.colorScheme.tertiary)
                assertEquals(colors.background, MaterialTheme.colorScheme.background)
            }
        }
        assertSame(typography, actualTypography)
        assertSame(shapes, actualShapes)
    }

    /**
     * 不可表示的主题级切角必须明确拒绝，不能静默伪装成圆角投影。
     */
    @Test
    fun nonRoundedThemeShapeFailsFast() {
        assertFailsWith<IllegalArgumentException> {
            runComposeUiTest {
                setContent {
                    rememberMaterial3DesignSystem(shapes = Shapes(medium = CutCornerShape(9.dp)))
                }
            }
        }
    }
}
