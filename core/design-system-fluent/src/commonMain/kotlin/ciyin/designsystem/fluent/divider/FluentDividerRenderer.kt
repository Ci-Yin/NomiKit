package ciyin.designsystem.fluent.divider

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import ciyin.designsystem.component.divider.NomiDividerOrientation
import ciyin.designsystem.component.divider.NomiDividerRenderer
import ciyin.designsystem.component.divider.NomiDividerSpec
import ciyin.designsystem.fluent.rememberFluentWarning
import io.github.composefluent.component.ListItemSeparator

/**
 * 原生 Fluent 分割线适配。
 *
 * 上游只有列表分隔线原语，固定 1dp 与描边色，且没有纵向原语；显式粗细、颜色与纵向方向都忽略并
 * 记录一次警告，不用自绘线条冒充原生分割线。
 */
internal object FluentDividerRenderer : NomiDividerRenderer {
    /**
     * 渲染原生列表分隔线；无法承载的请求降级并记录警告。
     *
     * @param orientation 分割线方向；纵向没有原生原语，降级为不渲染。
     * @param modifier 应用于分隔线的修饰符。
     * @param thickness 显式粗细；上游没有对应入口，忽略并记录警告。
     * @param color 显式颜色；上游没有对应入口，忽略并记录警告。
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
        rememberFluentWarning(thickness != null, thickness) {
            "Fluent 分割线没有粗细参数，已忽略 thickness=$thickness"
        }
        rememberFluentWarning(color != Color.Unspecified, color) {
            "Fluent 分割线没有颜色参数，已忽略 color=$color"
        }
        if (orientation == NomiDividerOrientation.Vertical) {
            rememberFluentWarning(true, orientation) {
                "Fluent 没有纵向分割线原语，已忽略 Vertical 方向"
            }
            return
        }
        ListItemSeparator(modifier = modifier)
    }
}
