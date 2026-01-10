package ciyin.ui.foundation.widget.button

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ciyin.ui.foundation.extension.thenIf
import ciyin.ui.foundation.provider.ProvideTextStyleContentColor
import ciyin.ui.foundation.unit.toPx
import ciyin.ui.foundation.widget.MaterialPreview
import ciyin.ui.foundation.widget.SingleText
import org.jetbrains.compose.ui.tooling.preview.Preview


/**
 * 按钮样式数据类，用于定义按钮的各种视觉属性
 *
 * 该类包含了按钮在不同状态下的所有视觉配置，支持正常状态和禁用状态的独立设置。
 * 通过组合这些属性，可以创建出各种样式的按钮。
 *
 * @property containerBrush 按钮正常状态下的背景画刷（支持纯色、渐变等）
 * @property contentColor 按钮正常状态下的内容（文字）颜色
 * @property borderStroke 按钮正常状态下的边框样式，为 null 时表示无边框
 * @property textStyle 按钮正常状态下的文字样式，颜色值会被 [contentColor] [disabledContentColor] 覆盖
 * @property disabledContainerBrush 按钮禁用状态下的背景画刷
 * @property disabledContentColor 按钮禁用状态下的内容颜色
 * @property disabledBorderStroke 按钮禁用状态下的边框样式，为 null 时表示无边框
 * @property shape 按钮的形状（圆角、圆形等）
 * @property contentPadding 按钮内容的内边距
 * @property elevation 按钮的阴影高度
 *
 * @see FlexButtonStyles 预定义的按钮样式集合
 */
@Immutable
data class ButtonStyle(

    val containerBrush: Brush,
    val contentColor: Color,
    val borderStroke: BorderStroke?,

    val disabledContainerBrush: Brush,
    val disabledContentColor: Color,
    val disabledBorderStroke: BorderStroke?,

    val textStyle: TextStyle,
    val shape: Shape,
    val contentPadding: PaddingValues,
    val elevation: Dp,
)

/**
 * 全局按钮样式集合
 *
 * 提供一组基于 [ButtonStyle] 的标准化样式，用于保持项目内按钮的统一性。
 *
 * - 每个样式基于 [Default] 基础样式，通过 copy 修改不同属性实现。
 * - 禁用态样式通过透明度 (alpha) 进行区分。
 */
object FlexButtonStyles

/**
 * 预设样式（基础样板）
 *
 * - 背景：主题主色
 * - 文本：白色
 * - 边框：透明
 * - 禁用态：背景半透明，文字保持白色
 * - 形状：圆形
 * - 内边距：小
 * - 字体：titleSmall
 *
 * ⚠️ 注意：该样式仅作为样板使用，不允许直接改动。
 */
val FlexButtonStyles.Default: ButtonStyle
    @Composable
    get() = ButtonStyle(
        containerBrush = MaterialTheme.colorScheme.primary.toBrush(),
        contentColor = MaterialTheme.colorScheme.onPrimary,
        textStyle = MaterialTheme.typography.titleSmall,
        borderStroke = null,

        disabledContainerBrush = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f).toBrush(),
        disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
        disabledBorderStroke = null,

        shape = RoundedCornerShape(25),
        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 16.dp),
        elevation = 0.dp,
    )

/** 扩展函数方便从 [Color] 创建 */
fun Color.toBrush(): Brush = SolidColor(this)

@Stable
internal fun ButtonStyle.borderStroke(enabled: Boolean): BorderStroke? {
    return if (enabled) borderStroke else disabledBorderStroke
}

@Stable
internal fun ButtonStyle.contentColor(enabled: Boolean): Color {
    return if (enabled) contentColor else disabledContentColor
}

@Stable
internal fun ButtonStyle.containerBrush(enabled: Boolean): Brush {
    return if (enabled) containerBrush else disabledContainerBrush
}

@Stable
@Composable
fun FlexButton(
    modifier: Modifier = Modifier,
    style: ButtonStyle,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    onDoubleClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {

    val contentPadding = style.contentPadding
    val shape = style.shape
    val border = style.borderStroke(enabled)
    val brush = style.containerBrush(enabled)
    val contentColor = style.contentColor(enabled)
    val textStyle = style.textStyle.copy(color = contentColor)
    val shadowElevation = style.elevation.toPx()

    Row(
        modifier = modifier
            .background(brush, shape)
            .thenIf(border != null) { border(border!!, shape) }
            .graphicsLayer(
                shadowElevation = shadowElevation,
                shape = shape,
                clip = false,
            )
            .clip(shape)
            .combinedClickable(
                enabled = enabled,
                interactionSource = interactionSource,
                onClick = onClick,
                onLongClick = onLongClick,
                onDoubleClick = onDoubleClick
            )
            .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        content = {
            ProvideTextStyleContentColor(
                value = textStyle,
                color = contentColor,
                content = content
            )
        }
    )

}

@Preview(showBackground = true)
@Composable
private fun ButtonsPreview() = MaterialPreview {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        FlexButton(
            style = FlexButtonStyles.Default,
            content = { SingleText("正常按钮") },
            onClick = {}
        )
        FlexButton(
            style = FlexButtonStyles.Default,
            content = { SingleText("禁用按钮") },
            enabled = false,
            onClick = {}
        )
    }
}