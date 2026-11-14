package ciyin.designsystem.fluent

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ciyin.designsystem.theme.NomiThemeOverrides
import ciyin.designsystem.theme.defaultSpacings
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * 验证 Fluent adapter 接收的主题覆盖是稀疏合并，而不是投影反向回填。
 */
class FluentThemeMappingTest {
    /**
     * 子覆盖只改变显式传入字段，未传字段保留父覆盖。
     */
    @Test
    fun themeOverridesMergeSparsely() {
        val parent = NomiThemeOverrides(
            accentColor = Color.Red,
            spacings = defaultSpacings().copy(large = 20.dp),
        )
        val child = NomiThemeOverrides(accentColor = Color.Blue)
        val merged = parent.merge(child)
        assertEquals(Color.Blue, merged.accentColor)
        assertEquals(parent.spacings, merged.spacings)
    }

    /**
     * 空覆盖保持原生默认意图，不制造 adapter 自己的颜色或尺寸补丁。
     */
    @Test
    fun emptyOverridesRemainEmpty() {
        assertEquals(Color.Unspecified, NomiThemeOverrides().accentColor)
        assertEquals(null, NomiThemeOverrides().spacings)
        assertEquals(null, NomiThemeOverrides().sizes)
    }
}
