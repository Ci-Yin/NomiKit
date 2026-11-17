package ciyin.designsystem.material3

import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
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
import ciyin.designsystem.theme.DarkMode
import ciyin.designsystem.theme.LocalNomiTheme
import ciyin.designsystem.theme.NomiTheme
import ciyin.designsystem.theme.Spacings
import ciyin.designsystem.theme.defaultSpacings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import androidx.compose.material3.darkColorScheme as nativeDarkColorScheme
import androidx.compose.material3.lightColorScheme as nativeLightColorScheme

/**
 * 原生主题基线、自定义映射与跨设计系统稀疏切换的严格回归。
 */
@OptIn(ExperimentalTestApi::class)
class Material3NativeThemeTest {
    /**
     * 浅深色均取独立原生色板；未映射的排版与形状继承外层 MaterialTheme。
     *
     * @return 当前平台的 Compose 测试执行结果。
     */
    @Test
    fun defaultsUseNativePaletteAndInheritCurrentTypographyAndShapes() = runComposeUiTest {
        var dark by mutableStateOf(false)
        val designSystem = NomiDesignSystem.Material3
        val outerTypography = Typography(bodyLarge = TextStyle(fontSize = 41.sp))
        val outerShapes = Shapes(medium = CutCornerShape(9.dp))
        setContent {
            MaterialTheme(
                colorScheme = nativeLightColorScheme(primary = Color.Red),
                typography = outerTypography,
                shapes = outerShapes,
            ) {
                NomiTheme(
                    darkMode = if (dark) DarkMode.Dark else DarkMode.Light,
                    designSystem = designSystem,
                ) {
                    val expected = if (dark) nativeDarkColorScheme() else nativeLightColorScheme()
                    assertEquals(expected.primary, MaterialTheme.colorScheme.primary)
                    assertEquals(expected.surface, MaterialTheme.colorScheme.surface)
                    assertEquals(expected.tertiary, MaterialTheme.colorScheme.tertiary)
                    assertEquals(outerTypography, MaterialTheme.typography)
                    assertEquals(outerShapes, MaterialTheme.shapes)
                }
            }
        }
        for (isDark in listOf(false, true)) {
            runOnIdle { dark = isDark }
            waitForIdle()
        }
    }

    /**
     * 自定义映射显式提供的原生实例按引用安装，不写入中立主题。
     *
     * @return 当前平台的 Compose 测试执行结果。
     */
    @Test
    fun customMapKeepsExplicitNativeInstances() = runComposeUiTest {
        val colors = nativeLightColorScheme(primary = Color.Blue, tertiary = Color.Yellow)
        val nativeTypography = Typography(bodyMedium = TextStyle(fontSize = 29.sp))
        val nativeShapes = Shapes()
        val map: NomiTheme.() -> Material3Theme = {
            Material3Theme(
                isDark = isDark,
                colorScheme = colors,
                typography = nativeTypography,
                shapes = nativeShapes,
            )
        }
        var actualTypography: Typography? = null
        var actualShapes: Shapes? = null
        setContent {
            NomiTheme(
                darkMode = DarkMode.Dark,
                designSystem = NomiDesignSystem.Material3(map),
            ) {
                actualTypography = MaterialTheme.typography
                actualShapes = MaterialTheme.shapes
                assertEquals(colors.primary, MaterialTheme.colorScheme.primary)
                assertEquals(colors.onPrimary, MaterialTheme.colorScheme.onPrimary)
                assertEquals(colors.tertiary, MaterialTheme.colorScheme.tertiary)
                assertEquals(colors.background, MaterialTheme.colorScheme.background)
            }
        }
        assertSame(nativeTypography, actualTypography)
        assertSame(nativeShapes, actualShapes)
    }

    /**
     * 从 Nomi 默认系统切到 Material 3 时只继承入口主题的度量，色板取 Material 3 默认，
     * 未映射的排版与形状继承当前 MaterialTheme；退出后恢复父层主题。
     *
     * @return 当前平台的 Compose 测试执行结果。
     */
    @Test
    fun sparseSwitchAcrossSystemsKeepsMeasuresAndRestoresParent() = runComposeUiTest {
        val spacings = defaultSpacings().copy(large = 27.dp)
        val parentTheme = NomiTheme.dark().copy(spacings = spacings)
        var childPrimary = Color.Unspecified
        var childTypography: Typography? = null
        var childShapes: Shapes? = null
        var childSpacings: Spacings? = null
        var restoredSpacings: Spacings? = null
        setContent {
            NomiTheme(theme = parentTheme, designSystem = NomiDesignSystem.Default) {
                NomiTheme(
                    theme = LocalNomiTheme.current,
                    designSystem = NomiDesignSystem.Material3
                ) {
                    childPrimary = MaterialTheme.colorScheme.primary
                    childTypography = MaterialTheme.typography
                    childShapes = MaterialTheme.shapes
                    childSpacings = NomiTheme.spacings
                    assertEquals(nativeDarkColorScheme().primary, MaterialTheme.colorScheme.primary)
                    assertEquals(
                        nativeDarkColorScheme().secondary,
                        MaterialTheme.colorScheme.secondary,
                    )
                    assertEquals(Typography(), MaterialTheme.typography)
                    assertEquals(Shapes(), MaterialTheme.shapes)
                    assertEquals(spacings, NomiTheme.spacings)
                }
                restoredSpacings = NomiTheme.spacings
            }
        }
        assertEquals(nativeDarkColorScheme().primary, childPrimary)
        assertEquals(Typography(), childTypography)
        assertEquals(Shapes(), childShapes)
        assertEquals(spacings, childSpacings)
        assertEquals(spacings, restoredSpacings)
    }
}
