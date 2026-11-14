package ciyin.designsystem.fluent.card

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import ciyin.designsystem.component.NomiBorder
import ciyin.designsystem.component.surface.NomiSurfaceColors
import io.github.composefluent.FluentTheme
import io.github.composefluent.surface.Card as NativeCard

/**
 * 原生静态 Card；明确拒绝上游不支持的颜色与描边，绝不伪造点击 Card。
 *
 * @param modifier 原生非点击 Card 的外层修饰符。
 * @param shape 显式形状；null 使用 FluentTheme.shapes.overlay。
 * @param colors 必须全部为 Unspecified；包括 Transparent 在内的任何显式颜色均不支持。
 * @param border 仅允许 Default 保留原生描边；None 与 Stroke 均不支持。
 * @param contentPadding null 不给内层 Column 添加内边距，非 null 仅作用于该内容布局。
 * @param content ColumnScope 内容，继承原生 Card 内容环境，不新增中立显式 slot。
 * @throws IllegalArgumentException 存在显式 colors 或 border 不是 Default 时抛出，应改用 NomiSurface。
 */
@Composable
internal fun FluentCard(
    modifier: Modifier,
    shape: Shape?,
    colors: NomiSurfaceColors,
    border: NomiBorder,
    contentPadding: PaddingValues?,
    content: @Composable ColumnScope.() -> Unit,
) {
    require(colors == NomiSurfaceColors()) { "fluent NomiCard 不支持 colors 覆盖；请使用 NomiSurface" }
    require(border == NomiBorder.Default) { "fluent NomiCard 不支持 border 覆盖；请使用 NomiSurface" }
    NativeCard(modifier = modifier, shape = shape ?: FluentTheme.shapes.overlay) {
        Column(
            modifier = if (contentPadding == null) Modifier else Modifier.padding(contentPadding),
            content = content,
        )
    }
}
