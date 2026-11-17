package ciyin.designsystem.material3

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.theme.NomiTheme
import ciyin.designsystem.theme.defaultDarkNomiColorScheme
import ciyin.designsystem.theme.defaultLightNomiColorScheme
import ciyin.designsystem.theme.defaultNomiShapes
import ciyin.designsystem.theme.defaultNomiTypography
import kotlin.test.Test
import kotlin.test.assertEquals
import androidx.compose.material3.darkColorScheme as nativeDarkColorScheme

/**
 * 主题值经映射函数安装到 Material 3 后的原生组与嵌套恢复回归。
 */
@OptIn(ExperimentalTestApi::class)
class Material3ThemeParametersTest {
    /**
     * Primary 只改主色，原生 onPrimary、tertiary 与 surface 保持默认。
     *
     * @return 当前平台的 Compose 测试执行结果。
     */
    @Test
    fun primaryMapOnlyRewritesPrimaryRole() = runComposeUiTest {
        val theme = NomiTheme.dark().copy(
            colorScheme = defaultDarkNomiColorScheme().copy(
                primary = Color.Red,
                onPrimary = Color.Green,
            ),
        )
        val designSystem = NomiDesignSystem.Material3 { toPrimaryMaterial3Theme() }
        setContent {
            NomiTheme(theme = theme, designSystem = designSystem) {
                assertEquals(Color.Red, MaterialTheme.colorScheme.primary)
                assertEquals(nativeDarkColorScheme().onPrimary, MaterialTheme.colorScheme.onPrimary)
                assertEquals(nativeDarkColorScheme().tertiary, MaterialTheme.colorScheme.tertiary)
                assertEquals(nativeDarkColorScheme().surface, MaterialTheme.colorScheme.surface)
                assertEquals(Typography(), MaterialTheme.typography)
                assertEquals(Shapes(), MaterialTheme.shapes)
                assertEquals(true, NomiTheme.isDark)
            }
        }
    }

    /**
     * Full 把可映射颜色、十五个排版角色与五个形状等级写入原生主题；
     * warning 等中立扩展与非标准形状等级不进入原生组。
     *
     * @return 当前平台的 Compose 测试执行结果。
     */
    @Test
    fun fullMapMapsGroupsIntoNativeTheme() = runComposeUiTest {
        val colors = defaultLightNomiColorScheme().copy(
            primary = Color.Cyan,
            onPrimary = Color.Magenta,
        )
        val typography = defaultNomiTypography().copy(bodyMedium = TextStyle(fontSize = 29.sp))
        val shapes = defaultNomiShapes().copy(
            tiny = RoundedCornerShape(3.dp),
            huge = RoundedCornerShape(31.dp),
        )
        val theme = NomiTheme.light().copy(
            colorScheme = colors,
            typography = typography,
            shapes = shapes,
        )
        val nativeTypography = typography.toMaterial3Typography()
        val designSystem = NomiDesignSystem.Material3 { toFullMaterial3Theme() }
        setContent {
            NomiTheme(theme = theme, designSystem = designSystem) {
                assertEquals(colors.primary, MaterialTheme.colorScheme.primary)
                assertEquals(colors.onPrimary, MaterialTheme.colorScheme.onPrimary)
                assertEquals(colors.surface, MaterialTheme.colorScheme.surface)
                assertEquals(colors.surfaceHigh, MaterialTheme.colorScheme.surfaceContainerHigh)
                assertEquals(nativeTypography, MaterialTheme.typography)
                assertEquals(shapes.extraSmall, MaterialTheme.shapes.extraSmall)
                assertEquals(shapes.huge, MaterialTheme.shapes.extraLarge)
            }
        }
    }

    /**
     * 嵌套入口整组替换原生主题；退出后恢复父层。
     *
     * @return 当前平台的 Compose 测试执行结果。
     */
    @Test
    fun nestedEntryReplacesWholeGroupsAndRestoresParent() = runComposeUiTest {
        val designSystem = NomiDesignSystem.Material3 { toFullMaterial3Theme() }
        val parentColors = defaultLightNomiColorScheme().copy(primary = Color.Red)
        val parentTypography =
            defaultNomiTypography().copy(labelSmall = TextStyle(fontSize = 17.sp))
        val parentTheme = NomiTheme.light().copy(
            colorScheme = parentColors,
            typography = parentTypography,
        )
        val childColors = defaultLightNomiColorScheme().copy(primary = Color.Blue)
        val childTypography = defaultNomiTypography().copy(titleLarge = TextStyle(fontSize = 23.sp))
        val childTheme = parentTheme.copy(
            colorScheme = childColors,
            typography = childTypography,
        )
        setContent {
            NomiTheme(theme = parentTheme, designSystem = designSystem) {
                NomiTheme(theme = childTheme, designSystem = designSystem) {
                    assertEquals(Color.Blue, MaterialTheme.colorScheme.primary)
                    assertEquals(childTypography.titleLarge, MaterialTheme.typography.titleLarge)
                }
                assertEquals(Color.Red, MaterialTheme.colorScheme.primary)
                assertEquals(parentTypography, NomiTheme.typography)
            }
        }
    }

    /**
     * 嵌套入口切换映射时颜色重选深色原生默认；未映射的排版与形状继承父层原生主题。
     *
     * @return 当前平台的 Compose 测试执行结果。
     */
    @Test
    fun nestedEntrySwitchingMapReselectsPaletteAndInheritsTypography() = runComposeUiTest {
        val designSystem = NomiDesignSystem.Material3 { toFullMaterial3Theme() }
        val parentTheme = NomiTheme.light().copy(
            colorScheme = defaultLightNomiColorScheme().copy(primary = Color.Red),
            typography = defaultNomiTypography().copy(bodyLarge = TextStyle(fontSize = 31.sp)),
        )
        setContent {
            NomiTheme(theme = parentTheme, designSystem = designSystem) {
                NomiTheme(
                    theme = parentTheme.copy(isDark = true),
                    designSystem = NomiDesignSystem.Material3,
                ) {
                    assertEquals(nativeDarkColorScheme().primary, MaterialTheme.colorScheme.primary)
                    assertEquals(nativeDarkColorScheme().surface, MaterialTheme.colorScheme.surface)
                    assertEquals(
                        parentTheme.typography.toMaterial3Typography(),
                        MaterialTheme.typography
                    )
                    assertEquals(parentTheme.shapes.toMaterial3Shapes(), MaterialTheme.shapes)
                    assertEquals(true, NomiTheme.isDark)
                }
                assertEquals(Color.Red, MaterialTheme.colorScheme.primary)
                assertEquals(parentTheme.typography, NomiTheme.typography)
            }
        }
    }
}
