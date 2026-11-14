package ciyin.designsystem.fluent.button

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.TextStyle
import ciyin.designsystem.component.button.NomiActionRenderer
import ciyin.designsystem.component.button.NomiButtonColors
import ciyin.designsystem.component.button.NomiButtonVariant
import ciyin.designsystem.component.NomiControlSize
import ciyin.designsystem.runtime.ProvideNomiContentStyle
import io.github.composefluent.LocalContentColor
import io.github.composefluent.LocalTextStyle
import io.github.composefluent.component.AccentButton as NativeAccentButton
import io.github.composefluent.component.Button as NativeButton
import io.github.composefluent.component.HyperlinkButton as NativeHyperlinkButton
import io.github.composefluent.component.SubtleButton as NativeSubtleButton

/**
 * 直接复用 Fluent v0.1.0 原生按钮族，不以 Layer 重绘按钮或伪造交互语义。
 */
internal object FluentButton : NomiActionRenderer {
    /**
     * 原生 API 不支持的覆盖参数立即失败，其余参数按原生变体分派。
     *
     * @param onClick 原生按钮启用且被激活时调用；业务状态由调用方持有。
     * @param modifier 原生按钮修饰符，不追加点击层或统一尺寸。
     * @param enabled 是否启用，映射为原生 disabled 的反值。
     * @param variant null/Default 使用 Button，Primary 使用 AccentButton，Subtle 使用 SubtleButton，Text 使用 HyperlinkButton，Destructive 使用 critical 配色的 Button。
     * @param size 必须为 null；原生按钮未公开尺寸覆盖。
     * @param colors 逐字段覆盖启用四态和禁用态；Unspecified 保留相应原生通道，Transparent 是有效覆盖。
     * @param shape 必须为 null；原生按钮未公开形状覆盖。
     * @param contentPadding 必须为 null；原生按钮未公开内边距覆盖。
     * @param textStyle null 保留原生排版，非 null 按字段合并；样式色低于当前状态显式内容色、高于原生状态色。
     * @param content 原生 RowScope 内容；原生 locals 保留实际状态结果，中立 slot 仅传播显式颜色和样式增量。
     * @throws IllegalArgumentException size、shape 或 contentPadding 非 null 时抛出。
     */
    @Composable
    override fun Button(
        onClick: () -> Unit,
        modifier: Modifier,
        enabled: Boolean,
        variant: NomiButtonVariant?,
        size: NomiControlSize?,
        colors: NomiButtonColors,
        shape: Shape?,
        contentPadding: PaddingValues?,
        textStyle: TextStyle?,
        content: @Composable RowScope.() -> Unit,
    ) {
        require(size == null) { "Fluent 原生 Button 不支持 size 覆盖" }
        require(shape == null) { "Fluent 原生 Button 不支持 shape 覆盖" }
        require(contentPadding == null) { "Fluent 原生 Button 不支持 contentPadding 覆盖" }
        val buttonColors = colors.toFluentButtonColors(variant)
        val nativeContent: @Composable RowScope.() -> Unit = {
            val stateColor = if (enabled) colors.contentColor else colors.disabledContentColor
            val explicitStyleColor = textStyle?.color ?: Color.Unspecified
            val explicitColor = stateColor.takeOrElse { explicitStyleColor }
            val nativeColor = LocalContentColor.current
            val resolvedColor = explicitColor.takeOrElse { nativeColor }
            val mergedStyle = LocalTextStyle.current.merge(textStyle)
            val resolvedStyle =
                if (explicitColor == Color.Unspecified) mergedStyle else mergedStyle.copy(color = resolvedColor)
            val scope = this
            CompositionLocalProvider(
                LocalContentColor provides resolvedColor,
                LocalTextStyle provides resolvedStyle,
            ) {
                // 原生解析结果留在原生 locals；中立 slot 只携带明确覆盖的增量。
                ProvideNomiContentStyle(
                    color = explicitColor,
                    textStyle = textStyle ?: TextStyle.Default
                ) {
                    content(scope)
                }
            }
        }
        when (variant) {
            null, NomiButtonVariant.Default -> NativeButton(
                onClick = onClick,
                modifier = modifier,
                disabled = !enabled,
                buttonColors = buttonColors,
                content = nativeContent,
            )

            NomiButtonVariant.Primary -> NativeAccentButton(
                onClick = onClick,
                modifier = modifier,
                disabled = !enabled,
                buttonColors = buttonColors,
                content = nativeContent,
            )

            NomiButtonVariant.Subtle -> NativeSubtleButton(
                onClick = onClick,
                modifier = modifier,
                disabled = !enabled,
                buttonColors = buttonColors,
                content = nativeContent,
            )

            NomiButtonVariant.Text -> NativeHyperlinkButton(
                onClick = onClick,
                modifier = modifier,
                disabled = !enabled,
                buttonColors = buttonColors,
                content = nativeContent,
            )

            NomiButtonVariant.Destructive -> NativeButton(
                onClick = onClick,
                modifier = modifier,
                disabled = !enabled,
                buttonColors = buttonColors,
                content = nativeContent,
            )
        }
    }
}
