package ciyin.designsystem.fluent

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ciyin.designsystem.theme.DarkMode
import ciyin.designsystem.theme.NomiTheme
import ciyin.designsystem.theme.defaultNomiShapes
import ciyin.designsystem.theme.defaultNomiTypography
import io.github.composefluent.lightColors
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * 验证中立到原生的映射函数与预置映射，不经过组合环境。
 */
class FluentThemeMappingTest {
    /**
     * 原生色板按已解析明暗选择，System 由应用层解析，这里按浅色处理。
     */
    @Test
    fun nativePalettesFollowResolvedDarkMode() {
        assertEquals(false, nativeFluentColors(false).darkMode)
        assertEquals(true, nativeFluentColors(true).darkMode)
        assertEquals(false, nativeFluentColors(false).darkMode)
    }

    /**
     * 只替换强调色阶：底色与三阶深浅色由新主色生成，其它角色与 typed 明暗保持不变。
     */
    @Test
    fun withPrimaryOnlyRebuildsAccentRamp() {
        val native = lightColors()
        val accent = Color(0xFF123456)
        val primary = native.withPrimary(accent)
        assertEquals(accent, primary.shades.base)
        assertEquals(native.darkMode, primary.darkMode)
        assertEquals(native.background.solid.base, primary.background.solid.base)
        assertEquals(native.background.card.default, primary.background.card.default)
        assertEquals(native.text.text.primary, primary.text.text.primary)
        assertEquals(native.stroke.control.default, primary.stroke.control.default)
        assertEquals(native.system.critical, primary.system.critical)
        assertTrue(primary.shades.light1 != accent)
        assertTrue(primary.shades.dark3 != accent)
    }

    /**
     * 中立十五角色中确定的八个角色映射到原生八角色，其余角色不参与原生排版。
     */
    @Test
    fun neutralTypographyMapsEightFluentRoles() {
        val typography = defaultNomiTypography().copy(
            bodySmall = TextStyle(fontSize = 11.sp),
            bodyMedium = TextStyle(fontSize = 12.sp),
            labelLarge = TextStyle(fontSize = 13.sp),
            bodyLarge = TextStyle(fontSize = 14.sp),
            titleLarge = TextStyle(fontSize = 15.sp),
            headlineMedium = TextStyle(fontSize = 16.sp),
            headlineLarge = TextStyle(fontSize = 17.sp),
            displayLarge = TextStyle(fontSize = 18.sp),
        )
        val native = typography.toFluentTypography()
        assertEquals(typography.bodySmall, native.caption)
        assertEquals(typography.bodyMedium, native.body)
        assertEquals(typography.labelLarge, native.bodyStrong)
        assertEquals(typography.bodyLarge, native.bodyLarge)
        assertEquals(typography.titleLarge, native.subtitle)
        assertEquals(typography.headlineMedium, native.title)
        assertEquals(typography.headlineLarge, native.titleLarge)
        assertEquals(typography.displayLarge, native.display)
    }

    /**
     * 九级中立圆角只取三个代表档位映射为三档原生圆角。
     */
    @Test
    fun neutralShapesMapThreeCornerRadii() {
        val shapes = defaultNomiShapes().copy(
            tiny = RoundedCornerShape(9.dp),
            small = RoundedCornerShape(11.dp),
            large = RoundedCornerShape(13.dp),
        )
        val corners = shapes.toFluentCornerRadius()
        assertEquals(9.dp, corners.intersectionEdge)
        assertEquals(11.dp, corners.control)
        assertEquals(13.dp, corners.overlay)
    }

    /**
     * 默认形状映射得到 2dp 控件交界、8dp 控件与 16dp 浮层三档原生圆角。
     */
    @Test
    fun defaultShapesMapToNativeCornerRadii() {
        val corners = defaultNomiShapes().toFluentCornerRadius()
        assertEquals(2.dp, corners.intersectionEdge)
        assertEquals(8.dp, corners.control)
        assertEquals(16.dp, corners.overlay)
    }

    /**
     * 三个预置扩展只声明各自负责的原生组，未涉及的组保持 null 以回退到原生基线。
     */
    @Test
    fun presetExtensionsDeclareOnlyTheirOwnGroups() {
        val theme = NomiTheme.dark()
        val native = theme.toNativeFluentTheme()
        assertEquals(true, native.isDark)
        assertNull(native.colors)
        assertNull(native.typography)
        assertNull(native.cornerRadius)
        assertTrue(native.useAcrylicPopup)
        assertTrue(native.compactMode)

        val primary = theme.toPrimaryFluentTheme()
        assertEquals(true, primary.isDark)
        assertEquals(theme.colorScheme.primary, requireNotNull(primary.colors).shades.base)
        assertTrue(requireNotNull(primary.colors).darkMode)
        assertNull(primary.typography)
        assertNull(primary.cornerRadius)

        val full = theme.toFullFluentTheme()
        assertEquals(theme.colorScheme.primary, requireNotNull(full.colors).shades.base)
        assertEquals(theme.typography.bodyMedium, requireNotNull(full.typography).body)
        assertEquals(theme.typography.bodySmall, requireNotNull(full.typography).caption)
        assertEquals(16.dp, requireNotNull(full.cornerRadius).overlay)
    }

    /**
     * 主题规格默认启用 acrylic 弹层与紧凑密度，可分别覆盖。
     */
    @Test
    fun themeSpecDefaultsToNativeAcrylicAndCompactMode() {
        val defaults = FluentTheme()
        assertTrue(defaults.useAcrylicPopup)
        assertTrue(defaults.compactMode)

        val custom = defaults.copy(useAcrylicPopup = false, compactMode = false)
        assertFalse(custom.useAcrylicPopup)
        assertFalse(custom.compactMode)
    }
}
