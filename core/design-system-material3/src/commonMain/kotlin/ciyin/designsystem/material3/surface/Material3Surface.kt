package ciyin.designsystem.material3.surface

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface as NativeSurface
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import ciyin.designsystem.component.NomiBorder
import ciyin.designsystem.component.surface.NomiSurfaceColors
import ciyin.designsystem.component.surface.NomiSurfaceRenderer
import ciyin.designsystem.runtime.ProvideNomiContentStyle
import ciyin.designsystem.material3.card.Material3Card

/**
 * 原生 Surface/Card 同族 SPI；两个组件各自保留原生默认和独立实现。
 */
object Material3SurfaceRenderer : NomiSurfaceRenderer {
    /**
     * 无显式参数时保留原生矩形表面、surface 色及原生内容色推导。
     *
     * @param modifier 原生 Surface 的布局修饰符。
     * @param shape 显式表面形状；null 使用原生矩形默认。
     * @param colors 逐通道颜色覆盖；容器 Unspecified 使用 surface，内容 Unspecified 由 contentColorFor 推导，透明色为覆盖。
     * @param border Default 与 None 均无描边；Stroke 精确传入原生表面。
     * @param content 原生表面中的内容；只将显式内容颜色发布为中立 slot 增量。
     */
    @Composable
    override fun Surface(
        modifier: Modifier,
        shape: Shape?,
        colors: NomiSurfaceColors,
        border: NomiBorder,
        content: @Composable () -> Unit,
    ) {
        val container =
            if (colors.containerColor == Color.Unspecified) MaterialTheme.colorScheme.surface else colors.containerColor
        NativeSurface(
            modifier = modifier,
            shape = shape ?: RectangleShape,
            color = container,
            contentColor = if (colors.contentColor == Color.Unspecified) contentColorFor(container) else colors.contentColor,
            border = border.toMaterialBorder(),
        ) {
            ProvideNomiContentStyle(
                color = colors.contentColor,
                textStyle = TextStyle.Default,
                content = content,
            )
        }
    }

    /**
     * Card 独立派发到原生 Card，不再用 Surface 与默认中立 padding 模拟。
     *
     * @param modifier 原生卡片外层修饰符。
     * @param shape 显式卡片形状；null 保留 CardDefaults.shape。
     * @param colors 逐通道颜色覆盖；Unspecified 保留原生默认，Transparent 是有效覆盖。
     * @param border Default 与 None 均不加描边，Stroke 使用给定描边。
     * @param contentPadding null 直接使用原生 Column slot；非 null 新建带内边距的内层 Column。
     * @param content ColumnScope 内容，继承原生内容色，仅显式内容色写入中立 slot。
     */
    @Composable
    override fun Card(
        modifier: Modifier,
        shape: Shape?,
        colors: NomiSurfaceColors,
        border: NomiBorder,
        contentPadding: PaddingValues?,
        content: @Composable ColumnScope.() -> Unit,
    ) {
        Material3Card(
            modifier = modifier,
            shape = shape,
            colors = colors,
            border = border,
            contentPadding = contentPadding,
            content = content,
        )
    }
}

/**
 * Material 普通 Surface/Card 的原生默认均无描边；Stroke 精确传入调用方值。
 *
 * @receiver 中立三态描边意图。
 * @return Default 或 None 返回 null；Stroke 返回其原始 BorderStroke。
 */
internal fun NomiBorder.toMaterialBorder(): BorderStroke? = when (this) {
    NomiBorder.Default, NomiBorder.None -> null
    is NomiBorder.Stroke -> value
}
