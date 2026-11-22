package ciyin.designsystem.nomi.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon as NativeIcon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import ciyin.designsystem.component.menu.NomiMenuRenderer
import ciyin.designsystem.component.menu.NomiMenuItemSpec
import ciyin.designsystem.component.menu.NomiMenuSpec
import ciyin.designsystem.icon.Check
import ciyin.designsystem.icon.IconPack
import ciyin.designsystem.runtime.ProvideNomiContentStyle
import ciyin.designsystem.theme.NomiTheme

/**
 * Nomi 菜单实现：浮层使用原生弹窗锚定父布局，容器、描边与项外观读取 Nomi 令牌。
 */
internal object DefaultNomiMenuRenderer : NomiMenuRenderer {
    /**
     * 渲染 Nomi 菜单浮层；未展开时不组合任何节点。
     *
     * @param expanded 是否展开。
     * @param onDismissRequest 用户要求关闭浮层时调用。
     * @param modifier 应用于浮层容器根节点的修饰符。
     * @param nativeSpec 菜单浮层专属参数；Nomi 外观由令牌决定，本实现忽略它。
     * @param content 在浮层纵向作用域中组合的菜单项。
     */
    @Composable
    override fun Menu(
        expanded: Boolean,
        onDismissRequest: () -> Unit,
        modifier: Modifier,
        nativeSpec: NomiMenuSpec?,
        content: @Composable ColumnScope.() -> Unit,
    ) {
        NomiMenuPopup(
            expanded = expanded,
            onDismissRequest = onDismissRequest,
            modifier = modifier,
            content = content,
        )
    }

    /**
     * 渲染 Nomi 菜单项：透明行叠加悬停/按下指示，选中项使用主体色勾选。
     *
     * @param onClick 菜单项启用且被激活时调用。
     * @param modifier 应用于菜单项根节点的修饰符。
     * @param enabled 是否接受用户操作。
     * @param selected 是否为当前选中项；true 时在内容之前追加勾选图标。
     * @param leading 前导槽；null 表示不占位。
     * @param trailing 尾部槽；null 表示不占位，提供时靠右对齐。
     * @param nativeSpec 菜单项专属参数；Nomi 外观由令牌决定，本实现忽略它。
     * @param content 菜单项主体内容，读取 Nomi 正文内容色与排版。
     */
    @Composable
    override fun MenuItem(
        onClick: () -> Unit,
        modifier: Modifier,
        enabled: Boolean,
        selected: Boolean,
        leading: (@Composable () -> Unit)?,
        trailing: (@Composable () -> Unit)?,
        nativeSpec: NomiMenuItemSpec?,
        content: @Composable () -> Unit,
    ) {
        val scheme = NomiTheme.colorScheme
        val spacings = NomiTheme.spacings
        val interactionSource = remember { MutableInteractionSource() }
        val hovered by interactionSource.collectIsHoveredAsState()
        val pressed by interactionSource.collectIsPressedAsState()
        val highlight = when {
            !enabled -> Color.Transparent
            pressed -> scheme.onSurface.copy(alpha = NomiMenuItemPressedOpacity)
                .compositeOver(scheme.surfaceHigher)
            hovered -> scheme.onSurface.copy(alpha = NomiMenuItemHoveredOpacity)
                .compositeOver(scheme.surfaceHigher)
            else -> Color.Transparent
        }
        val contentColor = if (enabled) scheme.textPrimary else scheme.textDisabled
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = spacings.extraSmall)
                .clip(NomiTheme.shapes.small)
                .background(highlight)
                .clickable(
                    enabled = enabled,
                    role = Role.Button,
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                )
                .padding(horizontal = spacings.small, vertical = spacings.small),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacings.small),
        ) {
            if (selected) {
                NativeIcon(
                    imageVector = IconPack.Check,
                    contentDescription = null,
                    modifier = Modifier.size(NomiTheme.sizes.icon.extraSmall),
                    tint = if (enabled) scheme.primary else scheme.textDisabled,
                )
            }
            leading?.invoke()
            ProvideNomiContentStyle(
                color = contentColor,
                textStyle = NomiTheme.typography.bodyMedium,
            ) {
                content()
            }
            if (trailing != null) {
                Spacer(modifier = Modifier.weight(1f))
                ProvideNomiContentStyle(
                    color = scheme.textSecondary,
                    textStyle = NomiTheme.typography.bodySmall,
                ) {
                    trailing()
                }
            }
        }
    }
}

/**
 * 绘制 Nomi 菜单浮层，供菜单入口与组合框共用。
 *
 * 浮层以调用处的父布局为锚点，优先出现在锚点下方，空间不足时翻转到上方；
 * 宽度为空时按最宽菜单项自适应。
 *
 * @param expanded 是否展开；false 时不组合任何节点。
 * @param onDismissRequest 用户要求关闭浮层时调用。
 * @param modifier 应用于浮层容器根节点的修饰符。
 * @param width 浮层固定宽度；null 表示按内容自适应。
 * @param content 在浮层纵向作用域中组合的内容。
 */
@Composable
internal fun NomiMenuPopup(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    width: Dp? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (!expanded) return
    val scheme = NomiTheme.colorScheme
    val shape = NomiTheme.shapes.medium
    val gapPx = with(LocalDensity.current) { NomiMenuAnchorGap.roundToPx() }
    val positionProvider = remember(gapPx) { NomiMenuPositionProvider(gapPx) }
    Popup(
        popupPositionProvider = positionProvider,
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(focusable = true),
    ) {
        Column(
            modifier = modifier
                .then(
                    if (width == null) {
                        Modifier.width(IntrinsicSize.Max)
                    } else {
                        Modifier.width(width)
                    },
                )
                .shadow(NomiMenuElevation, shape)
                .clip(shape)
                .background(scheme.surfaceHigher)
                .border(NomiTheme.sizes.strokes.thin, scheme.outline, shape)
                .padding(vertical = NomiTheme.spacings.extraSmall),
            content = content,
        )
    }
}

/**
 * 把菜单浮层放在锚点下方，空间不足时翻转到上方。
 *
 * @property gap 浮层与锚点之间的像素间距。
 */
private class NomiMenuPositionProvider(private val gap: Int) : PopupPositionProvider {
    /**
     * 计算浮层左上角位置，横向按书写方向对齐锚点起始边，并夹在窗口范围内。
     *
     * @param anchorBounds 锚点父布局在窗口中的边界。
     * @param windowSize 窗口尺寸。
     * @param layoutDirection 书写方向，决定横向起始边。
     * @param popupContentSize 浮层自身尺寸。
     * @return 浮层左上角相对窗口的偏移。
     */
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val preferredX = if (layoutDirection == LayoutDirection.Ltr) {
            anchorBounds.left
        } else {
            anchorBounds.right - popupContentSize.width
        }
        val maxX = (windowSize.width - popupContentSize.width).coerceAtLeast(0)
        val below = anchorBounds.bottom + gap
        val above = anchorBounds.top - popupContentSize.height - gap
        val fitsBelow = below + popupContentSize.height <= windowSize.height
        val y = if (fitsBelow || above < 0) below else above
        return IntOffset(preferredX.coerceIn(0, maxX), y)
    }
}

/**
 * 菜单浮层阴影高度，与原生覆盖层保持同一量级。
 */
private val NomiMenuElevation = 8.dp

/**
 * 菜单浮层与锚点之间的间距。
 */
private val NomiMenuAnchorGap = 4.dp

/**
 * 菜单项按下状态的叠加透明度。
 */
private const val NomiMenuItemPressedOpacity = 0.12f

/**
 * 菜单项悬停状态的叠加透明度。
 */
private const val NomiMenuItemHoveredOpacity = 0.06f
