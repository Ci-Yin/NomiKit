package ciyin.designsystem.nomi.card

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import ciyin.designsystem.component.NomiBorder
import ciyin.designsystem.component.surface.NomiSurfaceColors
import ciyin.designsystem.theme.NomiTheme
import ciyin.designsystem.nomi.surface.DefaultNomiSurfaceRenderer

/**
 * Nomi 自身的非点击卡片配方，其默认内边距不向其他设计系统传播。
 *
 * @param modifier 应用于卡片外层表面的修饰符。
 * @param shape 卡片轮廓；null 使用当前 Nomi 主题的 large 形状。
 * @param colors 表面颜色覆盖；Unspecified 回退到 Nomi surface/onSurface，Transparent 是有效覆盖。
 * @param border Stroke 绘制指定描边；Default 与 None 均不绘制 Nomi 默认描边。
 * @param contentPadding 内容内边距；null 使用当前 Nomi 主题的 large 间距。
 * @param content 在 ColumnScope 中组合的卡片内容，继承 Nomi 内容色及显式 slot 增量。
 */
@Composable
internal fun DefaultNomiCard(
    modifier: Modifier,
    shape: Shape?,
    colors: NomiSurfaceColors,
    border: NomiBorder,
    contentPadding: PaddingValues?,
    content: @Composable ColumnScope.() -> Unit,
) {
    DefaultNomiSurfaceRenderer.Surface(
        modifier = modifier,
        shape = shape ?: NomiTheme.shapes.large,
        colors = colors,
        border = border,
    ) {
        Column(
            modifier = Modifier.padding(contentPadding ?: PaddingValues(NomiTheme.spacings.large)),
            content = content,
        )
    }
}
