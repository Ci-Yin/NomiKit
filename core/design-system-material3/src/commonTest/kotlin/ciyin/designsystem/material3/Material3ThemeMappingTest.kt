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
import ciyin.designsystem.theme.DarkMode
import ciyin.designsystem.theme.LocalNomiTheme
import ciyin.designsystem.theme.NomiTheme
import ciyin.designsystem.theme.defaultDarkNomiColorScheme
import ciyin.designsystem.theme.defaultLightNomiColorScheme
import ciyin.designsystem.theme.defaultNomiShapes
import ciyin.designsystem.theme.defaultNomiSizes
import ciyin.designsystem.theme.defaultNomiTypography
import ciyin.designsystem.theme.defaultSpacings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import androidx.compose.material3.darkColorScheme as nativeDarkColorScheme
import androidx.compose.material3.lightColorScheme as nativeLightColorScheme

/**
 * 锁定三种预置扩展、工厂身份与原生安装基线。
 */
@OptIn(ExperimentalTestApi::class)
class Material3ThemeMappingTest {
    /**
     * Native 只传递已解析明暗模式，中立主题的其余组不进入原生 spec。
     */
    @Test
    fun nativePresetOnlyCarriesResolvedMode() {
        val theme = NomiTheme.dark().copy(
            colorScheme = defaultLightNomiColorScheme().copy(primary = Color.Red),
            typography = defaultNomiTypography().copy(bodyMedium = TextStyle(fontSize = 29.sp)),
            shapes = defaultNomiShapes().copy(medium = RoundedCornerShape(11.dp)),
        )
        val spec = theme.toNativeMaterial3Theme()
        assertEquals(true, spec.isDark)
        assertNull(spec.colorScheme)
        assertNull(spec.typography)
        assertNull(spec.shapes)
    }

    /**
     * Primary 只写入主色，onPrimary、tertiary、surface 等角色保留对应明暗的原生默认。
     */
    @Test
    fun primaryPresetOnlyReplacesPrimaryRole() {
        val light = NomiTheme.light().copy(
            colorScheme = defaultLightNomiColorScheme().copy(
                primary = Color.Red,
                onPrimary = Color.Green,
            ),
        ).toPrimaryMaterial3Theme()
        assertEquals(false, light.isDark)
        val lightColors = requireNotNull(light.colorScheme)
        assertEquals(Color.Red, lightColors.primary)
        assertEquals(nativeLightColorScheme().onPrimary, lightColors.onPrimary)
        assertEquals(nativeLightColorScheme().tertiary, lightColors.tertiary)
        assertEquals(nativeLightColorScheme().surface, lightColors.surface)
        assertNull(light.typography)
        assertNull(light.shapes)

        val dark = NomiTheme.dark()
            .copy(colorScheme = defaultDarkNomiColorScheme().copy(primary = Color.Cyan))
            .toPrimaryMaterial3Theme()
        assertEquals(true, dark.isDark)
        val darkColors = requireNotNull(dark.colorScheme)
        assertEquals(Color.Cyan, darkColors.primary)
        assertEquals(nativeDarkColorScheme().onPrimary, darkColors.onPrimary)
        assertNull(dark.typography)
        assertNull(dark.shapes)
    }

    /**
     * Full 整组映射颜色、排版与形状，未映射的原生角色保留原生默认。
     */
    @Test
    fun fullPresetMapsEverySupportedGroup() {
        val colors = defaultLightNomiColorScheme().copy(primary = Color.Cyan)
        val typography = defaultNomiTypography().copy(bodyMedium = TextStyle(fontSize = 29.sp))
        val shapes = defaultNomiShapes().copy(huge = RoundedCornerShape(31.dp))
        val spec = NomiTheme.light()
            .copy(colorScheme = colors, typography = typography, shapes = shapes)
            .toFullMaterial3Theme()
        assertEquals(false, spec.isDark)
        val mappedColors = requireNotNull(spec.colorScheme)
        assertEquals(Color.Cyan, mappedColors.primary)
        assertEquals(colors.surface, mappedColors.surfaceContainer)
        assertEquals(colors.surfaceHigh, mappedColors.surfaceContainerHigh)
        assertEquals(colors.outline, mappedColors.outline)
        assertEquals(nativeLightColorScheme().tertiary, mappedColors.tertiary)
        val mappedTypography = requireNotNull(spec.typography)
        assertEquals(typography.bodyMedium, mappedTypography.bodyMedium)
        assertEquals(typography.labelSmall, mappedTypography.labelSmall)
        val mappedShapes = requireNotNull(spec.shapes)
        assertEquals(shapes.extraSmall, mappedShapes.extraSmall)
        assertEquals(shapes.huge, mappedShapes.extraLarge)
    }

    /**
     * 无参属性与默认函数返回共享单例，带映射函数返回同一标识下的独立实例。
     */
    @Test
    fun material3FactoriesExposeStableIdentity() {
        val shared = NomiDesignSystem.Material3
        assertSame(shared, NomiDesignSystem.Material3)
        assertEquals("material3", shared.id.value)
        assertSame(Material3Components, shared.components)

        val mapped = NomiDesignSystem.Material3 { toPrimaryMaterial3Theme() }
        assertEquals("material3", mapped.id.value)
        assertSame(Material3Components, mapped.components)
    }

    /**
     * Native 安装 Material 3 原生默认主题，入口主题值原样发布，不做中立投影。
     *
     * @return 当前平台的 Compose 测试执行结果。
     */
    @Test
    fun nativeFactoryInstallsNativeDefaultsWithoutProjectingTheme() = runComposeUiTest {
        val theme = NomiTheme.light().copy(
            colorScheme = defaultLightNomiColorScheme().copy(primary = Color.Red),
            typography = defaultNomiTypography().copy(bodyMedium = TextStyle(fontSize = 29.sp)),
            shapes = defaultNomiShapes().copy(medium = RoundedCornerShape(11.dp)),
            spacings = defaultSpacings().copy(large = 27.dp),
            sizes = defaultNomiSizes().let { it.copy(strokes = it.strokes.copy(thin = 3.dp)) },
        )
        val designSystem = NomiDesignSystem.Material3
        setContent {
            NomiTheme(theme = theme, designSystem = designSystem) {
                assertEquals(nativeLightColorScheme().primary, MaterialTheme.colorScheme.primary)
                assertEquals(nativeLightColorScheme().surface, MaterialTheme.colorScheme.surface)
                assertEquals(Typography(), MaterialTheme.typography)
                assertEquals(Shapes(), MaterialTheme.shapes)
                assertEquals(theme, LocalNomiTheme.current)
            }
        }
    }
}
