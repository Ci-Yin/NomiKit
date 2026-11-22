package ciyin.designsystem.material3.divider

import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import ciyin.designsystem.component.divider.NomiDividerOrientation
import ciyin.designsystem.component.divider.NomiDividerRenderer
import ciyin.designsystem.component.divider.NomiDividerSpec

/**
 * 原生 Material 3 分割线适配。
 *
 * 水平与垂直方向都使用原生分割线；未指定粗细与颜色时回落原生默认值。
 */
internal object Material3DividerRenderer : NomiDividerRenderer {
    /**
     * 渲染原生分割线。
     *
     * @param orientation 分割线方向，决定使用原生水平还是垂直分割线。
     * @param modifier 应用于分割线根节点的修饰符。
     * @param thickness 显式粗细；null 回落到原生默认粗细。
     * @param color 显式颜色；Unspecified 回落到原生描边色，Transparent 是有效覆盖。
     * @param nativeSpec 分割线专属参数；当前没有实现，原样接受并忽略。
     */
    @Composable
    override fun Divider(
        orientation: NomiDividerOrientation,
        modifier: Modifier,
        thickness: Dp?,
        color: Color,
        nativeSpec: NomiDividerSpec?,
    ) {
        val resolvedThickness = thickness ?: DividerDefaults.Thickness
        val resolvedColor = if (color == Color.Unspecified) DividerDefaults.color else color
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
