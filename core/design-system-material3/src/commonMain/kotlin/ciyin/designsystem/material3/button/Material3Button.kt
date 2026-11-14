package ciyin.designsystem.material3.button

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.Button as NativeButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import ciyin.designsystem.component.button.NomiActionRenderer
import ciyin.designsystem.component.button.NomiButtonColors
import ciyin.designsystem.component.button.NomiButtonDefaults
import ciyin.designsystem.component.button.NomiButtonVariant
import ciyin.designsystem.component.NomiControlSize
import ciyin.designsystem.runtime.ProvideNomiContentStyle

/**
 * 原生按钮适配；未指定的尺寸、形状、排版和颜色全部保留 Material 默认。
 */
object Material3ActionRenderer : NomiActionRenderer {
    /**
     * 仅显式 size 应用中立度量，显式 padding/style 优先于尺寸预设。
     *
     * @param onClick 原生按钮启用且被激活时调用；业务状态由调用方维护。
     * @param modifier 按钮外层修饰符；显式 size 会追加最小触摸尺寸。
     * @param enabled 是否允许原生按钮交互并选择启用态颜色。
     * @param variant null 与 Primary 使用 Button，Default 使用 FilledTonalButton，Subtle/Text 使用 TextButton，Destructive 使用错误色 Button。
     * @param size null 不添加中立尺寸与排版；非 null 采用对应 metrics。
     * @param colors 逐字段覆盖原生启用/禁用颜色；Unspecified 保留原生值，Transparent 为覆盖。
     * @param shape null 保留所选原生按钮的默认形状；非 null 精确传入。
     * @param contentPadding 显式值优先，其次为 size 度量，最后为所选原生按钮默认。
     * @param textStyle null 不追加排版；非 null 按字段覆盖 size 排版和原生 slot，颜色低于当前状态显式内容色。
     * @param content 原生 RowScope 内容，继承原生实际状态色和排版；仅显式颜色/排版增量发布给中立 slot。
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
        val metrics = if (size == null) null else NomiButtonDefaults.metrics(size)
        val actualModifier = if (metrics == null) modifier else modifier.defaultMinSize(
            minWidth = metrics.minTouchTarget,
            minHeight = metrics.minTouchTarget,
        )
        val explicitStyle = metrics?.textStyle?.merge(textStyle ?: TextStyle.Default) ?: textStyle
        val buttonContent: @Composable RowScope.() -> Unit = {
            val scope = this
            val stateColor = if (enabled) colors.contentColor else colors.disabledContentColor
            val styleColor = textStyle?.color ?: Color.Unspecified
            val explicitContentColor = when {
                stateColor != Color.Unspecified -> stateColor
                styleColor != Color.Unspecified -> styleColor
                else -> Color.Unspecified
            }
            val resolvedContentColor = if (explicitContentColor == Color.Unspecified) {
                LocalContentColor.current
            } else {
                explicitContentColor
            }
            val mergedStyle = LocalTextStyle.current.merge(explicitStyle ?: TextStyle.Default)
            val resolvedStyle =
                if (stateColor != Color.Unspecified || styleColor != Color.Unspecified) {
                    mergedStyle.copy(color = resolvedContentColor)
                } else {
                    mergedStyle
                }
            CompositionLocalProvider(LocalContentColor provides resolvedContentColor) {
                ProvideTextStyle(resolvedStyle) {
                    ProvideNomiContentStyle(
                        color = explicitContentColor,
                        textStyle = explicitStyle ?: TextStyle.Default,
                    ) {
                        content(scope)
                    }
                }
            }
        }
        val nativeColors = when (variant) {
            NomiButtonVariant.Default -> ButtonDefaults.filledTonalButtonColors()
            NomiButtonVariant.Subtle, NomiButtonVariant.Text -> ButtonDefaults.textButtonColors()
            NomiButtonVariant.Destructive -> ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
            )

            null, NomiButtonVariant.Primary -> ButtonDefaults.buttonColors()
        }.copy(
            containerColor = colors.containerColor,
            contentColor = colors.contentColor,
            disabledContainerColor = colors.disabledContainerColor,
            disabledContentColor = colors.disabledContentColor,
        )
        when (variant) {
            NomiButtonVariant.Default -> FilledTonalButton(
                onClick = onClick,
                modifier = actualModifier,
                enabled = enabled,
                shape = shape ?: ButtonDefaults.filledTonalShape,
                colors = nativeColors,
                contentPadding = contentPadding ?: metrics?.contentPadding
                ?: ButtonDefaults.ContentPadding,
                content = buttonContent,
            )

            NomiButtonVariant.Subtle, NomiButtonVariant.Text -> TextButton(
                onClick = onClick,
                modifier = actualModifier,
                enabled = enabled,
                shape = shape ?: ButtonDefaults.textShape,
                colors = nativeColors,
                contentPadding = contentPadding ?: metrics?.contentPadding
                ?: ButtonDefaults.TextButtonContentPadding,
                content = buttonContent,
            )

            null, NomiButtonVariant.Primary, NomiButtonVariant.Destructive -> NativeButton(
                onClick = onClick,
                modifier = actualModifier,
                enabled = enabled,
                shape = shape ?: ButtonDefaults.shape,
                colors = nativeColors,
                contentPadding = contentPadding ?: metrics?.contentPadding
                ?: ButtonDefaults.ContentPadding,
                content = buttonContent,
            )
        }
    }
}
