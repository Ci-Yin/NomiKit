package ciyin.designsystem.material3

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import ciyin.designsystem.theme.DarkMode
import ciyin.designsystem.theme.defaultDarkNomiColorScheme
import ciyin.designsystem.theme.defaultLightNomiColorScheme
import ciyin.designsystem.theme.defaultNomiShapes
import ciyin.designsystem.theme.defaultNomiTypography
import kotlin.test.Test
import kotlin.test.assertEquals
import androidx.compose.material3.darkColorScheme as materialDarkColorScheme
import androidx.compose.material3.lightColorScheme as materialLightColorScheme

/**
 * 验证中立令牌到 Material 3 的完整映射及原生排版到中立排版的转换。
 */
class Material3ConversionsTest {
    /**
     * 十五个排版角色必须逐个参与映射与逆转换，不能遗漏小标签或标题。
     */
    @Test
    fun typographyRoundTripPreservesEveryPublicRole() {
        val original = defaultNomiTypography().copy(
            displayLarge = TextStyle(fontSize = 61.sp),
            displayMedium = TextStyle(fontSize = 49.sp),
            displaySmall = TextStyle(fontSize = 39.sp),
            headlineLarge = TextStyle(fontSize = 35.sp),
            headlineMedium = TextStyle(fontSize = 31.sp),
            headlineSmall = TextStyle(fontSize = 27.sp),
            titleLarge = TextStyle(fontSize = 25.sp),
            titleMedium = TextStyle(fontSize = 21.sp),
            titleSmall = TextStyle(fontSize = 19.sp),
            bodyLarge = TextStyle(fontSize = 18.sp),
            bodyMedium = TextStyle(fontSize = 17.sp),
            bodySmall = TextStyle(fontSize = 15.sp),
            labelLarge = TextStyle(fontSize = 14.sp),
            labelMedium = TextStyle(fontSize = 13.sp),
            labelSmall = TextStyle(fontSize = 11.sp),
        )
        assertEquals(original, original.toMaterial3Typography().toNomiTypography())
    }

    /**
     * 五个表面等级必须保持精确映射，未覆盖的专属角色沿用原生默认。
     */
    @Test
    fun colorsPreserveLevelsAndNativeDefaults() {
        val colors = defaultLightNomiColorScheme().copy(
            primary = Color.Red,
            surface = Color.Green,
            surfaceHigh = Color.Blue,
            surfaceHigher = Color.Yellow,
            surfaceLow = Color.Cyan,
            surfaceLower = Color.Magenta,
        )
        val material = colors.toMaterial3ColorScheme(isDark = false)
        assertEquals(colors.primary, material.primary)
        assertEquals(colors.surface, material.surface)
        assertEquals(colors.surface, material.surfaceContainer)
        assertEquals(colors.surfaceHigh, material.surfaceContainerHigh)
        assertEquals(colors.surfaceHigher, material.surfaceContainerHighest)
        assertEquals(colors.surfaceLow, material.surfaceContainerLow)
        assertEquals(colors.surfaceLower, material.surfaceContainerLowest)
        assertEquals(materialLightColorScheme().tertiary, material.tertiary)
        assertEquals(materialLightColorScheme().outlineVariant, material.outlineVariant)
        val dark = defaultDarkNomiColorScheme().toMaterial3ColorScheme(isDark = true)
        assertEquals(materialDarkColorScheme().tertiary, dark.tertiary)
        assertEquals(materialDarkColorScheme().primaryContainer, dark.primaryContainer)
    }

    /**
     * Material 最大标准圆角仍映射中立 huge。
     */
    @Test
    fun shapesKeepLegacyMapping() {
        val shapes = defaultNomiShapes()
        val material = shapes.toMaterial3Shapes()
        assertEquals(shapes.extraSmall, material.extraSmall)
        assertEquals(shapes.small, material.small)
        assertEquals(shapes.medium, material.medium)
        assertEquals(shapes.large, material.large)
        assertEquals(shapes.huge, material.extraLarge)
    }
}
