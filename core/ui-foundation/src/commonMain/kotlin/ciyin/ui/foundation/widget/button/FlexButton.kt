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
 * 全局按钮样式集合
 *
 * 提供一组基于 [ButtonStyle] 的标准化样式，用于保持项目内按钮的统一性。
 *
 * - 每个样式基于 [Default] 基础样式派生。
 * - 未显式覆盖的状态由 [ButtonStateStyle.disabled] 与 [ButtonStyle] 的默认值派生。
 */
object FlexButtonStyles

/**
 * 预设样式（基础样板）
 *
 * - 背景：主题主色
 * - 文本：白色
 * - 边框：无
 * - 禁用态：背景与文字半透明
 * - 形状：圆形
 * - 内边距：小
 * - 字体：titleSmall
 *
 * ⚠️ 注意：该样式仅作为样板使用，不允许直接改动。
 */
val FlexButtonStyles.Default: ButtonStyle
    @Composable
    get() = ButtonStyle(
        content = ButtonStateStyle(
            containerBrush = MaterialTheme.colorScheme.primary.toBrush(),
            contentColor = MaterialTheme.colorScheme.onPrimary,
            textStyle = MaterialTheme.typography.titleSmall,
            shape = RoundedCornerShape(25),
            contentPadding = PaddingValues(vertical = 8.dp, horizontal = 16.dp),
        ),
    )

/**
 * 单个状态的完整视觉规格。
 *
 * 这是按钮在某一业务状态下的全部视觉属性，本身不包含任何状态分支。
 * 状态到视觉的映射由 [ButtonStyle] 负责。
 *
 * @property containerBrush 容器画刷，支持纯色与渐变。
 * @property contentColor 内容（文字与图标）颜色。
 * @property borderStroke 边框样式，为 null 时表示无边框。
 * @property textStyle 文字样式，其颜色会被 [contentColor] 覆盖。
 * @property shape 形状，同时作用于背景、边框与裁剪。
 * @property contentPadding 内容内边距。
 * @property elevation 阴影高度。
 */
@Immutable
data class ButtonStateStyle(
    val containerBrush: Brush,
    val contentColor: Color,
    val borderStroke: BorderStroke? = null,
    val textStyle: TextStyle,
    val shape: Shape,
    val contentPadding: PaddingValues,
    val elevation: Dp = 0.dp,
)

/**
 * 按钮样式：由启用、选中与禁用的组合构成。
 *
 * 四个状态互斥，[resolve] 负责按当前状态选出唯一视觉。未显式给出的状态按以下规则派生：
 * - [selected] 默认与 [content] 相同，表示该样式不区分选中态。
 * - [disabled] 默认由 [content] 派生。
 * - [selectedDisabled] 默认由 [selected] 派生。
 *
 * 注意：修改形状、内边距或字号时应使用 [mapStates] 同步四个状态，否则状态切换会出现几何跳变。
 *
 * @property content 启用且未选中。
 * @property disabled 未选中且禁用。
 * @property selected 选中且启用。
 * @property selectedDisabled 选中且禁用。
 */
@Immutable
data class ButtonStyle(
    val content: ButtonStateStyle,
    val disabled: ButtonStateStyle = content.disabled(),
    val selected: ButtonStateStyle = content,
    val selectedDisabled: ButtonStateStyle = selected.disabled(),
) {

    /**
     * 解析当前唯一生效的状态样式。
     *
     * 禁用优先于选中，与 M3 的禁用态覆盖规则一致。
     *
     * @param enabled 是否可用。
     * @param selected 是否处于选中态。
     * @return 当前应渲染的状态样式。
     */
    fun resolve(enabled: Boolean, selected: Boolean): ButtonStateStyle = when {
        !enabled && selected -> selectedDisabled
        !enabled -> disabled
        selected -> this.selected
        else -> content
    }

    companion object {
        /** 禁用态默认透明度，用于派生禁用样式。 */
        const val DisabledAlpha: Float = 0.5f
    }
}

/** 扩展函数方便从 [Color] 创建纯色画刷。 */
fun Color.toBrush(): Brush = SolidColor(this)

/**
 * 由当前状态派生禁用态。
 *
 * 容器、内容与描边按 [alpha] 衰减，阴影归零。仅对纯色画刷生效，渐变等复合画刷保持原样。
 *
 * @param alpha 衰减后的透明度，默认取 [ButtonStyle.DisabledAlpha]。
 * @return 禁用态视觉规格。
 */
fun ButtonStateStyle.disabled(alpha: Float = ButtonStyle.DisabledAlpha): ButtonStateStyle = copy(
    containerBrush = containerBrush.fade(alpha),
    contentColor = contentColor.copy(alpha = contentColor.alpha * alpha),
    borderStroke = borderStroke?.let {
        BorderStroke(
            width = it.width,
            brush = it.brush.fade(alpha),
        )
    },
    elevation = 0.dp,
)

/**
 * 替换启用态并重建其余状态。
 *
 * 用于按数据动态着色的场景，例如按分类色渲染的 chip。
 * 与 [ButtonStyle.copy] 的区别：copy 会保留旧状态值，这里会按新的启用态重算选中态与禁用态。
 *
 * 声明为 `inline`，使 `transform` 内联到调用方，可在 lambda 内直接读取 `AppTheme` 令牌。
 *
 * @param transform 以当前启用态为接收者的变换。
 * @return 重建后的按钮样式。
 */
inline fun ButtonStyle.withContent(transform: ButtonStateStyle.() -> ButtonStateStyle): ButtonStyle {
    val newContent = content.transform()
    return ButtonStyle(
        content = newContent,
        selected = newContent,
        disabled = newContent.disabled(),
    )
}

/**
 * 对四个状态同时应用同一变换。
 *
 * 修改形状、内边距或字号时必须使用它，否则禁用态或选中态会保留旧几何。
 *
 * 声明为 `inline`，使 `transform` 内联到调用方，可在 lambda 内直接读取 `AppTheme` 令牌。
 *
 * @param transform 逐状态应用的变换。
 * @return 四个状态同步更新后的按钮样式。
 */
inline fun ButtonStyle.mapStates(transform: ButtonStateStyle.() -> ButtonStateStyle): ButtonStyle =
    ButtonStyle(
        content = content.transform(),
        selected = selected.transform(),
        disabled = disabled.transform(),
        selectedDisabled = selectedDisabled.transform(),
    )

/** 纯色画刷整体降低不透明度；渐变等复合画刷原样返回。 */
private fun Brush.fade(alpha: Float): Brush = if (this is SolidColor) {
    SolidColor(value.copy(alpha = value.alpha * alpha))
} else {
    this
}

/**
 * 弹性按钮：外观完全由 [ButtonStyle] 的状态集合决定。
 *
 * @param modifier 外部修饰符。
 * @param style 按钮样式。
 * @param enabled 是否可用，禁用时使用 [ButtonStyle.disabled] 或显式禁用态。
 * @param selected 是否选中，选中时使用 [ButtonStyle.selected]。
 * @param interactionSource 交互源，为空时由内部创建。
 * @param onClick 点击回调。
 * @param onLongClick 长按回调。
 * @param onDoubleClick 双击回调。
 * @param content 按钮内容。
 */
@Composable
fun FlexButton(
    modifier: Modifier = Modifier,
    style: ButtonStyle,
    enabled: Boolean = true,
    selected: Boolean = false,
    interactionSource: MutableInteractionSource? = null,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    onDoubleClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {

    val state = style.resolve(enabled = enabled, selected = selected)
    val border = state.borderStroke
    val textStyle = state.textStyle.copy(color = state.contentColor)
    val shadowElevation = state.elevation.toPx()

    Row(
        modifier = modifier
            .background(state.containerBrush, state.shape)
            .thenIf(border != null) { border(border!!, state.shape) }
            .graphicsLayer(
                shadowElevation = shadowElevation,
                shape = state.shape,
                clip = false,
            )
            .clip(state.shape)
            .combinedClickable(
                enabled = enabled,
                interactionSource = interactionSource,
                onClick = onClick,
                onLongClick = onLongClick,
                onDoubleClick = onDoubleClick
            )
            .padding(state.contentPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        content = {
            ProvideTextStyleContentColor(
                value = textStyle,
                color = state.contentColor,
                content = content
            )
        }
    )

}

@Preview(showBackground = true)
@Composable
private fun ButtonsPreview() = MaterialPreview {
    val selectable = FlexButtonStyles.Default.copy(
        selected = FlexButtonStyles.Default.content.copy(
            containerBrush = MaterialTheme.colorScheme.secondary.toBrush(),
            contentColor = MaterialTheme.colorScheme.onSecondary,
        ),
    )
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
            enabled = false,
            content = { SingleText("禁用按钮") },
            onClick = {}
        )
        FlexButton(
            style = selectable,
            selected = true,
            content = { SingleText("选中按钮") },
            onClick = {}
        )
        FlexButton(
            style = selectable,
            enabled = false,
            selected = false,
            content = { SingleText("禁用选中按钮") },
            onClick = {}
        )
    }
}
