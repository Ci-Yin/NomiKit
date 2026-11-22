package ciyin.designsystem.material3.button

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button as NativeButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon as NativeIcon
import androidx.compose.material3.IconButton as NativeIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import ciyin.designsystem.component.NomiControlSize
import ciyin.designsystem.component.button.NomiActionRenderer
import ciyin.designsystem.component.button.NomiButtonColors
import ciyin.designsystem.component.button.NomiButtonDefaults
import ciyin.designsystem.component.button.NomiButtonVariant
import ciyin.designsystem.component.button.NomiButtonSpec
import ciyin.designsystem.icon.ChevronDown
import ciyin.designsystem.icon.IconPack
import ciyin.designsystem.runtime.NomiNativeSpec
import ciyin.designsystem.runtime.ProvideNomiContentStyle
import ciyin.designsystem.runtime.findSpec

/**
 * 原生按钮适配；未指定的尺寸、形状、排版和颜色全部保留 Material 默认。
 *
 * 普通按钮、开关按钮与下拉按钮共用同一条原生绘制路径，只在语义状态、状态色与尾部指示上分叉；
 * 图标开关改用原生图标按钮，避免把方形图标按钮塞进文字按钮的度量里。
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
     * @param nativeSpec 按钮专属参数；只识别 Material 3 定义的类型，其它系统的参数静默忽略。
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
        nativeSpec: NomiButtonSpec?,
        content: @Composable RowScope.() -> Unit,
    ) {
        Material3ActionButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            variant = variant,
            size = size,
            colors = colors,
            shape = shape,
            contentPadding = contentPadding,
            textStyle = textStyle,
            nativeSpec = nativeSpec,
            trailing = null,
            toggleableState = null,
            content = content,
        )
    }

}

/**
 * 三个文字按钮入口共用的 Material 3 绘制路径。
 *
 * @param onClick 原生按钮启用且被激活时调用。
 * @param modifier 按钮外层修饰符；显式 size 会追加最小触摸尺寸。
 * @param enabled 是否允许原生按钮交互。
 * @param variant 选择原生按钮族；null 与 Primary 使用 Button。
 * @param size 显式中立尺寸预设。
 * @param colors 逐字段颜色覆盖。
 * @param shape 显式形状；null 保留所选原生按钮默认。
 * @param contentPadding 显式内边距。
 * @param textStyle 文本样式增量。
 * @param nativeSpec 任意入口的专属参数；只在携带 Material 3 类型时生效。
 * @param trailing 追加在内容之后的尾部槽；null 表示不追加。
 * @param toggleableState 开关状态；null 表示普通按钮，不发布开关语义。
 * @param content 原生 RowScope 内容。
 */
@Composable
private fun Material3ActionButton(
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    variant: NomiButtonVariant?,
    size: NomiControlSize?,
    colors: NomiButtonColors,
    shape: Shape?,
    contentPadding: PaddingValues?,
    textStyle: TextStyle?,
    nativeSpec: NomiNativeSpec<*>?,
    trailing: (@Composable () -> Unit)?,
    toggleableState: Boolean?,
    content: @Composable RowScope.() -> Unit,
) {
    val spec = nativeSpec.findSpec<Material3ButtonSpec>()
    val metrics = if (size == null) null else NomiButtonDefaults.metrics(size)
    val sizedModifier = if (metrics == null) {
        modifier
    } else {
        modifier.defaultMinSize(
            minWidth = metrics.minTouchTarget,
            minHeight = metrics.minTouchTarget,
        )
    }
    val actualModifier = if (toggleableState == null) {
        sizedModifier
    } else {
        sizedModifier.semantics { this.toggleableState = ToggleableState(toggleableState) }
    }
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
                    if (trailing != null) {
                        Spacer(modifier = Modifier.width(Material3ButtonIconSpacing))
                        trailing()
                    }
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
            elevation = spec?.elevation ?: ButtonDefaults.filledTonalButtonElevation(),
            border = spec?.border,
            interactionSource = spec?.interactionSource,
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
            elevation = spec?.elevation,
            border = spec?.border,
            interactionSource = spec?.interactionSource,
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
            elevation = spec?.elevation ?: ButtonDefaults.buttonElevation(),
            border = spec?.border,
            interactionSource = spec?.interactionSource,
            contentPadding = contentPadding ?: metrics?.contentPadding
            ?: ButtonDefaults.ContentPadding,
            content = buttonContent,
        )
    }
}

/**
 * 把中立四字段覆盖映射到原生图标按钮色板，未指定的通道沿用对应状态的默认值。
 *
 * @receiver 中立启用/禁用四色覆盖；Unspecified 表示不覆盖。
 * @param checked 是否选中；选中态以原生填充图标按钮为基线。
 * @return 可直接传给原生图标按钮的颜色板。
 */
@Composable
private fun NomiButtonColors.toIconButtonColors(checked: Boolean) = if (checked) {
    val native = IconButtonDefaults.filledIconButtonColors()
    IconButtonDefaults.filledIconButtonColors(
        containerColor = containerColor.takeOrElse { native.containerColor },
        contentColor = contentColor.takeOrElse { native.contentColor },
        disabledContainerColor = disabledContainerColor.takeOrElse { native.disabledContainerColor },
        disabledContentColor = disabledContentColor.takeOrElse { native.disabledContentColor },
    )
} else {
    val native = IconButtonDefaults.iconButtonColors()
    IconButtonDefaults.iconButtonColors(
        containerColor = containerColor.takeOrElse { native.containerColor },
        contentColor = contentColor.takeOrElse { native.contentColor },
        disabledContainerColor = disabledContainerColor.takeOrElse { native.disabledContainerColor },
        disabledContentColor = disabledContentColor.takeOrElse { native.disabledContentColor },
    )
}

/**
 * 按钮内图标尺寸，与原生文字按钮的图标约定一致。
 */
private val Material3ButtonIconSize = 18.dp

/**
 * 按钮内图标与文本之间的间距，与原生文字按钮的图标间距一致。
 */
private val Material3ButtonIconSpacing = 8.dp
