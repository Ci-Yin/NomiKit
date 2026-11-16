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
import ciyin.designsystem.fluent.rememberFluentWarning
import io.github.composefluent.FluentTheme
import io.github.composefluent.surface.Card as NativeCard

/**
 * 原生静态 Card；上游不支持的颜色与描边忽略并记录警告，绝不伪造点击 Card。
 *
 * @param modifier 原生非点击 Card 的外层修饰符。
 * @param shape 显式形状；null 使用 FluentTheme.shapes.overlay。
 * @param colors 无原生入口；忽略覆盖并保留原生外观，需要颜色覆盖时改用 NomiSurface。
 * @param border 无原生入口；忽略覆盖并保留原生描边，需要描边覆盖时改用 NomiSurface。
 * @param contentPadding null 不给内层 Column 添加内边距，非 null 仅作用于该内容布局。
 * @param content ColumnScope 内容，继承原生 Card 内容环境，不新增中立显式 slot。
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
    rememberFluentWarning(colors != NomiSurfaceColors(), colors) {
        "NomiCard 的 colors 覆盖无原生入口，已保留原生默认；需要颜色覆盖时改用 NomiSurface"
    }
    rememberFluentWarning(border != NomiBorder.Default, border) {
        "NomiCard 的 border 覆盖无原生入口，已保留原生默认；需要描边覆盖时改用 NomiSurface"
    }
    NativeCard(modifier = modifier, shape = shape ?: FluentTheme.shapes.overlay) {
        Column(
            modifier = if (contentPadding == null) Modifier else Modifier.padding(contentPadding),
            content = content,
        )
    }
}
