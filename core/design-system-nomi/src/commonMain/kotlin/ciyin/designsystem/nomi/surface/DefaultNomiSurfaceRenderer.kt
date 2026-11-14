package ciyin.designsystem.nomi.surface

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.takeOrElse
import ciyin.designsystem.component.NomiBorder
import ciyin.designsystem.component.surface.NomiSurfaceColors
import ciyin.designsystem.component.surface.NomiSurfaceRenderer
import ciyin.designsystem.runtime.LocalNomiTextStyle
import ciyin.designsystem.runtime.ProvideNomiContentStyle
import ciyin.designsystem.theme.NomiTheme
import ciyin.designsystem.nomi.LocalNomiNativeContentColor
import ciyin.designsystem.nomi.card.DefaultNomiCard

/**
 * Nomi 表面族组装及自身表面实现，Card 的真实布局位于独立文件。
 */
internal object DefaultNomiSurfaceRenderer : NomiSurfaceRenderer {
    /**
     * 未指定时使用 Nomi 自身默认，并精确应用显式覆盖。
     *
     * @param modifier 应用于外层 Box 的修饰符，其后追加裁剪、背景及可选描边。
     * @param shape 表面轮廓；null 使用 Nomi medium 形状。
     * @param colors 颜色逐字段覆盖；Unspecified 回退到 Nomi surface/onSurface，Transparent 不回退。
     * @param border Stroke 绘制指定描边；Default 和 None 均不绘制描边。
     * @param content Box 内的组合内容，继承解析后的 Nomi 内容色；只有显式内容色发布到跨系统 slot。
     */
    @Composable
    override fun Surface(
        modifier: Modifier,
        shape: Shape?,
        colors: NomiSurfaceColors,
        border: NomiBorder,
        content: @Composable () -> Unit,
    ) {
        val resolvedShape = shape ?: NomiTheme.shapes.medium
        val containerColor = colors.containerColor.takeOrElse { NomiTheme.colorScheme.surface }
        val contentColor = colors.contentColor.takeOrElse { NomiTheme.colorScheme.onSurface }
        val drawing = modifier.clip(resolvedShape).background(containerColor, resolvedShape)
        val bordered = if (border is NomiBorder.Stroke) drawing.border(
            border.value,
            resolvedShape
        ) else drawing
        Box(modifier = bordered) {
            CompositionLocalProvider(LocalNomiNativeContentColor provides contentColor) {
                ProvideNomiContentStyle(
                    color = colors.contentColor,
                    textStyle = TextStyle.Default,
                    content = content
                )
            }
        }
    }

    /**
     * 独立派发到 Nomi 卡片实现，不在中立层定义所有设计系统的卡片。
     *
     * @param modifier 应用于卡片外层表面的修饰符。
     * @param shape 卡片轮廓；null 使用当前 Nomi 主题的 large 形状。
     * @param colors 表面颜色覆盖；Unspecified 回退到 Nomi surface/onSurface，Transparent 是有效覆盖。
     * @param border Stroke 绘制指定描边；Default 与 None 均不绘制 Nomi 默认描边。
     * @param contentPadding 内容内边距；null 使用当前 Nomi 主题的 large 间距。
     * @param content 在 ColumnScope 中组合的卡片内容，继承 Nomi 内容色及显式 slot 增量。
     */
    @Composable
    override fun Card(
        modifier: Modifier,
        shape: Shape?,
        colors: NomiSurfaceColors,
        border: NomiBorder,
        contentPadding: PaddingValues?,
        content: @Composable ColumnScope.() -> Unit,
    ) = DefaultNomiCard(
        modifier = modifier,
        shape = shape,
        colors = colors,
        border = border,
        contentPadding = contentPadding,
        content = content,
    )
}
