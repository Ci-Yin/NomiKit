package ciyin.designsystem.nomi.button

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.Shape
import ciyin.designsystem.runtime.ProvideNomiContentStyle
import ciyin.designsystem.theme.NomiTheme
import ciyin.designsystem.component.button.NomiActionRenderer
import ciyin.designsystem.component.button.NomiButtonColors
import ciyin.designsystem.component.button.NomiButtonDefaults
import ciyin.designsystem.component.button.NomiButtonVariant
import ciyin.designsystem.component.NomiControlSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.graphics.takeOrElse
import ciyin.designsystem.nomi.LocalNomiNativeContentColor
import ciyin.designsystem.nomi.LocalNomiNativeTextStyle

/**
 * Nomi 默认按钮渲染器。
 */
internal object DefaultNomiActionRenderer : NomiActionRenderer {
    /**
     * 使用 Foundation 绘制按钮并提供触摸、键盘和语义支持。
     *
     * 交互状态在组合内维护；禁用、按下和悬停状态决定背景，焦点状态绘制描边。
     *
     * @param onClick 按钮启用且被激活时触发，业务状态由调用方更新。
     * @param modifier 按钮根节点修饰符，在其后追加最小交互尺寸、绘制、交互和内边距。
     * @param enabled 是否接受操作，并选择启用或禁用颜色。
     * @param variant Nomi 颜色预设类型；null 使用 Primary。
     * @param size Nomi 布局度量预设；null 使用 Medium，但不把默认度量发布为跨系统显式 slot。
     * @param colors 四种状态颜色的逐字段覆盖；Unspecified 读取 Nomi 预设，Transparent 是显式基色覆盖，启用时仍参与悬停/按下叠层。
     * @param shape 按钮轮廓；null 使用 Nomi small 形状。
     * @param contentPadding 内容内边距；null 使用所选尺寸的 Nomi 度量。
     * @param textStyle 合并到尺寸预设文本样式的增量；null 保留预设，显式颜色在未指定状态内容色时生效。
     * @param content 在居中的 RowScope 中组合的内容，读取当前状态内容色及排版；仅显式增量向跨系统 slot 传播。
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
        val metrics = NomiButtonDefaults.metrics(size ?: NomiControlSize.Medium)
        val defaults = NomiButtonDefaults.colors(variant ?: NomiButtonVariant.Primary)
        val resolvedColors = NomiButtonColors(
            containerColor = colors.containerColor.takeOrElse { defaults.containerColor },
            contentColor = colors.contentColor.takeOrElse { defaults.contentColor },
            disabledContainerColor = colors.disabledContainerColor.takeOrElse { defaults.disabledContainerColor },
            disabledContentColor = colors.disabledContentColor.takeOrElse { defaults.disabledContentColor },
        )
        val resolvedShape = shape ?: NomiTheme.shapes.small
        val resolvedStyle = metrics.textStyle.merge(textStyle)
        val interactionSource = remember { MutableInteractionSource() }
        val pressed by interactionSource.collectIsPressedAsState()
        val hovered by interactionSource.collectIsHoveredAsState()
        var focused by remember { mutableStateOf(false) }
        val baseColor =
            if (enabled) resolvedColors.containerColor else resolvedColors.disabledContainerColor
        val explicitContentColor = if (enabled) colors.contentColor else colors.disabledContentColor
        val defaultContentColor =
            if (enabled) defaults.contentColor else defaults.disabledContentColor
        val contentColor = explicitContentColor.takeOrElse {
            (textStyle?.color ?: Color.Unspecified).takeOrElse { defaultContentColor }
        }
        val stateColor = when {
            !enabled -> baseColor
            pressed -> contentColor.copy(alpha = NomiPressedOpacity).compositeOver(baseColor)
            hovered -> contentColor.copy(alpha = NomiHoveredOpacity).compositeOver(baseColor)
            else -> baseColor
        }
        val stateModifier = modifier
            .defaultMinSize(minWidth = metrics.minTouchTarget, minHeight = metrics.minTouchTarget)
            .clip(resolvedShape)
            .background(stateColor, resolvedShape)
            .then(
                if (focused) Modifier.border(
                    width = NomiTheme.sizes.strokes.medium,
                    color = contentColor,
                    shape = resolvedShape,
                ) else Modifier,
            )
            .onFocusChanged { focused = it.isFocused }
            .clickable(
                enabled = enabled,
                role = Role.Button,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(contentPadding ?: metrics.contentPadding)
        Box(modifier = stateModifier, contentAlignment = Alignment.Center) {
            CompositionLocalProvider(
                LocalNomiNativeContentColor provides contentColor,
                LocalNomiNativeTextStyle provides resolvedStyle,
            ) {
                val explicitColor =
                    explicitContentColor.takeOrElse { textStyle?.color ?: Color.Unspecified }
                val explicitStyle =
                    if (size == null) textStyle else metrics.textStyle.merge(textStyle)
                ProvideNomiContentStyle(
                    color = explicitColor,
                    textStyle = explicitStyle ?: TextStyle.Default
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        content = content,
                    )
                }
            }
        }
    }
}

/**
 * Nomi 按下状态叠加透明度。
 */
private const val NomiPressedOpacity = 0.12f

/**
 * Nomi 悬停状态叠加透明度。
 */
private const val NomiHoveredOpacity = 0.08f
