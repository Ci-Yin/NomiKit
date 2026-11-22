package ciyin.designsystem.nomi.divider

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import ciyin.designsystem.component.divider.NomiDividerOrientation
import ciyin.designsystem.component.divider.NomiDividerRenderer
import ciyin.designsystem.component.divider.NomiDividerSpec
import ciyin.designsystem.theme.NomiTheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.VerticalDivider

/**
 * Nomi 自身分割线实现；粗细与颜色读取 Nomi 主题令牌。
 */
internal object DefaultNomiDividerRenderer : NomiDividerRenderer {
    /**
     * 渲染分割线。
     *
     * @param orientation 分割线方向，决定使用水平还是垂直分割线。
     * @param modifier 应用于分割线根节点的修饰符。
     * @param thickness 显式粗细；null 使用 Nomi 细线令牌。
     * @param color 显式颜色；Unspecified 使用 Nomi 分割线令牌，Transparent 是有效覆盖。
     * @param nativeSpec 分割线专属参数；Nomi 外观完全由 [NomiTheme] 令牌决定，本实现忽略它。
     */
    @Composable
    override fun Divider(
        orientation: NomiDividerOrientation,
        modifier: Modifier,
        thickness: Dp?,
        color: Color,
        nativeSpec: NomiDividerSpec?,
    ) {
        val resolvedThickness = thickness ?: NomiTheme.sizes.strokes.thin
        val resolvedColor = if (color == Color.Unspecified) NomiTheme.colorScheme.divider else color
        when (orientation) {
            NomiDividerOrientation.Horizontal -> HorizontalDivider(
                modifier = modifier,
                thickness = resolvedThickness,
                color = resolvedColor,
            )

            NomiDividerOrientation.Vertical -> VerticalDivider(
                modifier = modifier,
                thickness = resolvedThickness,
                color = resolvedColor,
            )
        }
    }
}
