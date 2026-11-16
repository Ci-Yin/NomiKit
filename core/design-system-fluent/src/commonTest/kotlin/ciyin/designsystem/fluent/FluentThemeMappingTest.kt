package ciyin.designsystem.fluent

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ciyin.designsystem.theme.NomiThemeOverrides
import ciyin.designsystem.theme.defaultLightNomiColorScheme
import ciyin.designsystem.theme.defaultNomiTypography
import ciyin.designsystem.theme.defaultSpacings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

/**
 * 验证 Fluent adapter 接收的主题覆盖是稀疏合并，而不是投影反向回填。
 */
class FluentThemeMappingTest {
    /**
     * 子覆盖只改变显式传入组，未传组保留父覆盖。
     */
    @Test
    fun themeOverridesMergeSparsely() {
        val parent = NomiThemeOverrides(spacings = defaultSpacings().copy(large = 20.dp))
        val child = NomiThemeOverrides(typography = defaultNomiTypography())
        val merged = parent.merge(child)
        assertSame(child.typography, merged.typography)
        assertSame(parent.spacings, merged.spacings)
    }

    /**
     * 空覆盖保持原生默认意图，不制造 adapter 自己的颜色或尺寸补丁。
     */
    @Test
    fun emptyOverridesRemainEmpty() {
        assertEquals(null, NomiThemeOverrides().colorScheme)
        assertEquals(null, NomiThemeOverrides().spacings)
        assertEquals(null, NomiThemeOverrides().sizes)
    }

    /**
     * 完整中立色板只取 primary 生成强调色阶，其余中立颜色字段不参与 Fluent 映射。
     */
    @Test
    fun neutralPaletteOnlyDerivesPrimaryShades() {
        val palette = defaultLightNomiColorScheme().copy(primary = Color.Magenta)
        val colors = palette.toFluentColors(isDark = true)
        assertEquals(Color.Magenta, colors.shades.base)
        assertEquals(true, colors.darkMode)
    }
}
