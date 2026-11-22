package ciyin.designsystem.component.surface

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import ciyin.designsystem.component.NomiBorder
import ciyin.designsystem.component.card.NomiCardSpec

/**
 * 容器与非点击卡片分别派发，避免用统一表面配方代替各系统原生 Card。
 */
@Stable
interface NomiSurfaceRenderer {
    /**
     * 使用原生表面默认值，只应用调用方显式指定的参数。
     *
     * @param modifier 应用于表面根节点的修饰符。
     * @param shape 表面轮廓；null 使用当前设计系统的默认形状。
     * @param colors 容器色与内容色的逐字段覆盖；Unspecified 保留原生值，Transparent 是有效覆盖。
     * @param border Default 保留原生描边，None 移除描边，Stroke 指定描边。
     * @param nativeSpec 表面专属参数；null 或未携带本系统类型时全部沿用原生默认。
     * @param content 表面内组合的内容，继承其内容颜色，不提供额外布局 Scope。
     */
    @Composable
    fun Surface(
        modifier: Modifier,
        shape: Shape?,
        colors: NomiSurfaceColors,
        border: NomiBorder,
        nativeSpec: NomiSurfaceSpec?,
        content: @Composable () -> Unit,
    )

    /**
     * 使用原生非点击卡片，只应用显式覆盖；实现须遵守声明的卡片颜色与描边能力。
     *
     * @param modifier 应用于非点击卡片根节点的修饰符。
     * @param shape 卡片轮廓；null 保留当前设计系统的原生形状。
     * @param colors 容器色与内容色的逐字段覆盖；Unspecified 保留原生值，Transparent 是有效覆盖。
     * @param border Default 保留原生描边，None 移除描边，Stroke 指定描边。
     * @param contentPadding 卡片内容内边距；null 保留原生间距。
     * @param nativeSpec 卡片专属参数；null 或未携带本系统类型时全部沿用原生默认。
     * @param content 在卡片 ColumnScope 中组合的内容，继承卡片内容样式。
     */
    @Composable
    fun Card(
        modifier: Modifier,
        shape: Shape?,
        colors: NomiSurfaceColors,
        border: NomiBorder,
        contentPadding: PaddingValues?,
        nativeSpec: NomiCardSpec?,
        content: @Composable ColumnScope.() -> Unit,
    )
}
