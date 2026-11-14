package ciyin.designsystem.component.card

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import ciyin.designsystem.runtime.requireNomiDesignSystem
import ciyin.designsystem.component.NomiBorder
import ciyin.designsystem.component.surface.NomiSurfaceColors

/**
 * 非点击原生卡片入口；不预设统一表面、圆角、elevation 或内容内边距。
 *
 * @param modifier 应用于非点击卡片根节点的修饰符。
 * @param shape 卡片轮廓；null 保留当前设计系统的原生形状。
 * @param colors 容器色与内容色的逐字段覆盖；Unspecified 保留原生值，Transparent 是有效覆盖。
 * @param border Default 保留原生描边，None 移除描边，Stroke 指定描边。
 * @param contentPadding 卡片内容内边距；null 保留原生间距。
 * @param content 在卡片 ColumnScope 中组合的内容，继承卡片内容样式。
 * @throws IllegalArgumentException 当前设计系统不支持显式颜色或非 Default 描边时抛出；需要这些覆盖时可使用 NomiSurface。
 */
@Composable
fun NomiCard(
    modifier: Modifier = Modifier,
    shape: Shape? = null,
    colors: NomiSurfaceColors = NomiSurfaceColors(),
    border: NomiBorder = NomiBorder.Default,
    contentPadding: PaddingValues? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val designSystem = requireNomiDesignSystem()
    require(
        designSystem.capabilities.cardColors ||
                (colors.containerColor == Color.Unspecified && colors.contentColor == Color.Unspecified)
    ) {
        designSystem.id.value + " NomiCard 不支持 colors 覆盖；请使用 NomiSurface"
    }
    require(designSystem.capabilities.cardBorder || border == NomiBorder.Default) {
        designSystem.id.value + " NomiCard 不支持 border 覆盖；请使用 NomiSurface"
    }
    designSystem.components.surfaces.Card(
        modifier = modifier,
        shape = shape,
        colors = colors,
        border = border,
        contentPadding = contentPadding,
        content = content,
    )
}
