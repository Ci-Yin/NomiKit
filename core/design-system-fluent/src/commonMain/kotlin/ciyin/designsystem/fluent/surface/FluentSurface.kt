package ciyin.designsystem.fluent.surface

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import ciyin.designsystem.component.NomiBorder
import ciyin.designsystem.component.card.NomiCardSpec
import ciyin.designsystem.component.surface.NomiSurfaceColors
import ciyin.designsystem.component.surface.NomiSurfaceRenderer
import ciyin.designsystem.component.surface.NomiSurfaceSpec
import ciyin.designsystem.runtime.ProvideNomiContentStyle
import ciyin.designsystem.runtime.findSpec
import io.github.composefluent.FluentTheme
import io.github.composefluent.background.Layer
import ciyin.designsystem.fluent.card.FluentCard

/**
 * 原生 Layer 表面适配；Card 转发到独立实现。
 */
internal object FluentSurface : NomiSurfaceRenderer {
    /**
     * 默认值取原生 Layer，只有明确覆盖的内容色才写入中立 slot。
     *
     * @param modifier 原生 Layer 的布局修饰符。
     * @param shape null 使用 FluentTheme.shapes.control，非 null 精确传入。
     * @param colors 逐通道颜色覆盖；Unspecified 分别回退原生 layer.default 和 text.primary，透明色仍是覆盖。
     * @param border Default 使用原生卡片描边，None 去除描边，Stroke 精确传入。
     * @param content 表面内容，原生局部值提供实际颜色，中立 slot 只增加明确的内容色。
     * @param nativeSpec 表面专属参数；只识别 Fluent 定义的类型，其它系统的参数静默忽略。
     */
    @Composable
    override fun Surface(
        modifier: Modifier,
        shape: Shape?,
        colors: NomiSurfaceColors,
        border: NomiBorder,
        nativeSpec: NomiSurfaceSpec?,
        content: @Composable () -> Unit,
    ) {
        val spec = nativeSpec.findSpec<FluentSurfaceSpec>()
        Layer(
            modifier = modifier,
            shape = shape ?: FluentTheme.shapes.control,
            color = colors.containerColor.takeOrElse { FluentTheme.colors.background.layer.default },
            contentColor = colors.contentColor.takeOrElse { FluentTheme.colors.text.text.primary },
            border = border.toFluentBorder(),
            elevation = spec?.elevation ?: 0.dp,
        ) {
            ProvideNomiContentStyle(
                color = colors.contentColor,
                textStyle = TextStyle.Default,
                content = content
            )
        }
    }

    /**
     * 分发真实非点击 Card，不通过表面重绘绕过原生能力限制。
     *
     * @param modifier 原生非点击 Card 的外层修饰符。
     * @param shape 显式形状；null 使用 FluentTheme.shapes.overlay。
     * @param colors 必须全部为 Unspecified；包括 Transparent 在内的任何显式颜色均不支持。
     * @param border 仅允许 Default 保留原生描边；None 与 Stroke 均不支持。
     * @param contentPadding null 不给内层 Column 添加内边距，非 null 仅作用于该内容布局。
     * @param nativeSpec 卡片专属参数；Fluent 静态 Card 没有原生专属入口，一律忽略。
     * @param content ColumnScope 内容，继承原生 Card 内容环境，不新增中立显式 slot。
     * @throws IllegalArgumentException 存在显式 colors 或 border 不是 Default 时抛出，应改用 NomiSurface。
     */
    @Composable
    override fun Card(
        modifier: Modifier,
        shape: Shape?,
        colors: NomiSurfaceColors,
        border: NomiBorder,
        contentPadding: PaddingValues?,
        @Suppress("UNUSED_PARAMETER") nativeSpec: NomiCardSpec?,
        content: @Composable ColumnScope.() -> Unit,
    ) = FluentCard(
        modifier = modifier,
        shape = shape,
        colors = colors,
        border = border,
        contentPadding = contentPadding,
        content = content,
    )
}

/**
 * 三态描边与上游 Layer 的公开默认值一致。
 *
 * @receiver 显式区分原生默认、不描边和自定义描边的意图。
 * @return Default 返回当前原生 card.default 色的 1.dp 描边，None 返回 null，Stroke 返回原始值。
 */
@Composable
private fun NomiBorder.toFluentBorder(): BorderStroke? = when (this) {
    NomiBorder.Default -> BorderStroke(width = 1.dp, color = FluentTheme.colors.stroke.card.default)
    NomiBorder.None -> null
    is NomiBorder.Stroke -> value
}
