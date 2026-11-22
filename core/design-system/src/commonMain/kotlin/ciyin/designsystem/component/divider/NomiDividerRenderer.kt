package ciyin.designsystem.component.divider

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

/**
 * 分割线原生渲染契约。
 *
 * 实现负责使用当前设计系统的原生分割线，并保留原生粗细与描边色；无法表达显式粗细或颜色时
 * 忽略该值并记录一次警告，不重绘伪造外观。
 */
@Stable
interface NomiDividerRenderer {
    /**
     * 渲染一条原生分割线。
     *
     * @param orientation 分割线方向；实现按方向选择对应的原生入口。
     * @param modifier 应用于分割线根节点的修饰符。
     * @param thickness 显式粗细；null 保留原生粗细。
     * @param color 显式颜色；Unspecified 保留原生描边色，Transparent 是有效覆盖。
     * @param nativeSpec 分割线专属参数；null，或未携带本系统定义的类型时，全部沿用原生默认。
     */
    @Composable
    fun Divider(
        orientation: NomiDividerOrientation,
        modifier: Modifier,
        thickness: Dp?,
        color: Color,
        nativeSpec: NomiDividerSpec?,
    )
}
