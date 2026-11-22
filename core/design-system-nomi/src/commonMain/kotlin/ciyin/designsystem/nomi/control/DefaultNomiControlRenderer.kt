package ciyin.designsystem.nomi.control

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon as NativeIcon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import ciyin.designsystem.component.NomiControlSize
import ciyin.designsystem.component.button.NomiButtonDefaults
import ciyin.designsystem.component.control.NomiCheckboxSpec
import ciyin.designsystem.component.control.NomiComboBoxSpec
import ciyin.designsystem.component.control.NomiControlRenderer
import ciyin.designsystem.component.control.NomiRadioButtonSpec
import ciyin.designsystem.component.control.NomiSwitchSpec
import ciyin.designsystem.icon.ChevronDown
import ciyin.designsystem.icon.IconPack
import ciyin.designsystem.nomi.menu.NomiMenuPopup
import ciyin.designsystem.runtime.ProvideNomiContentStyle
import ciyin.designsystem.theme.NomiColorScheme
import ciyin.designsystem.theme.NomiTheme
import androidx.compose.material3.Checkbox as NativeCheckbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.RadioButton as NativeRadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch as NativeSwitch
import androidx.compose.material3.SwitchDefaults

/**
 * Nomi 自身的选择控件实现；以原生控件为交互载体，配色读取 Nomi 主题令牌。
 *
 * Nomi 不安装外层原生主题，因此所有颜色都显式取自 [NomiColorScheme]，不读取宿主主题；
 * 文本输入框由 [ciyin.designsystem.nomi.text.DefaultNomiTextRenderer] 实现。
 */
internal object DefaultNomiControlRenderer : NomiControlRenderer {
    /**
     * 渲染开关。
     *
     * @param checked 当前开关状态。
     * @param onCheckedChange 状态切换回调。
     * @param modifier 应用于原生开关的修饰符。
     * @param enabled 是否接受用户操作。
     * @param nativeSpec 开关专属参数；Nomi 外观完全由 [NomiTheme] 令牌决定，本实现忽略它。
     */
    @Composable
    override fun Switch(
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit,
        modifier: Modifier,
        enabled: Boolean,
        nativeSpec: NomiSwitchSpec?,
    ) {
        val scheme = NomiTheme.colorScheme
        NativeSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = modifier,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = scheme.onPrimary,
                checkedTrackColor = scheme.primary,
                checkedBorderColor = Color.Transparent,
                checkedIconColor = scheme.onPrimary,
                uncheckedThumbColor = scheme.surface,
                uncheckedTrackColor = scheme.surfaceLow,
                uncheckedBorderColor = scheme.outline,
                uncheckedIconColor = scheme.textSecondary,
                disabledCheckedThumbColor = scheme.surface,
                disabledCheckedTrackColor = scheme.textDisabled,
                disabledCheckedBorderColor = Color.Transparent,
                disabledCheckedIconColor = scheme.surface,
                disabledUncheckedThumbColor = scheme.surface,
                disabledUncheckedTrackColor = scheme.surfaceLow,
                disabledUncheckedBorderColor = scheme.textDisabled,
                disabledUncheckedIconColor = scheme.textDisabled,
            ),
        )
    }

    /**
     * 渲染复选框。
     *
     * @param checked 当前勾选状态。
     * @param onCheckedChange 状态切换回调。
     * @param modifier 应用于原生复选框的修饰符。
     * @param enabled 是否接受用户操作。
     * @param nativeSpec 复选框专属参数；Nomi 外观完全由 [NomiTheme] 令牌决定，本实现忽略它。
     */
    @Composable
    override fun Checkbox(
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit,
        modifier: Modifier,
        enabled: Boolean,
        nativeSpec: NomiCheckboxSpec?,
    ) {
        val scheme = NomiTheme.colorScheme
        NativeCheckbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = modifier,
            enabled = enabled,
            colors = CheckboxDefaults.colors(
                checkedColor = scheme.primary,
                uncheckedColor = scheme.outline,
                checkmarkColor = scheme.onPrimary,
                disabledCheckedColor = scheme.textDisabled,
                disabledUncheckedColor = scheme.textDisabled,
            ),
        )
    }

    /**
     * 渲染单选按钮。
     *
     * @param selected 是否为当前选中项。
     * @param onClick 点击回调。
     * @param modifier 应用于原生单选按钮的修饰符。
     * @param enabled 是否接受用户操作。
     * @param nativeSpec 单选按钮专属参数；Nomi 外观完全由 [NomiTheme] 令牌决定，本实现忽略它。
     */
    @Composable
    override fun RadioButton(
        selected: Boolean,
        onClick: () -> Unit,
        modifier: Modifier,
        enabled: Boolean,
        nativeSpec: NomiRadioButtonSpec?,
    ) {
        val scheme = NomiTheme.colorScheme
        NativeRadioButton(
            selected = selected,
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            colors = RadioButtonDefaults.colors(
                selectedColor = scheme.primary,
                unselectedColor = scheme.outline,
                disabledSelectedColor = scheme.textDisabled,
                disabledUnselectedColor = scheme.textDisabled,
            ),
        )
    }

    /**
     * 渲染 Nomi 组合框：收起态是描边容器，展开态复用菜单浮层并按容器宽度对齐。
     *
     * @param expanded 当前是否展开。
     * @param onExpandedChange 展开状态变化回调。
     * @param menuItems 展开时显示的菜单项槽，放进 Nomi 菜单浮层。
     * @param modifier 应用于组合框根节点的修饰符；尺寸变化用于同步浮层宽度。
     * @param enabled 是否接受用户操作。
     * @param nativeSpec 组合框专属参数；Nomi 外观由 [NomiTheme] 令牌决定，本实现忽略它。
     * @param content 收起态内容槽，读取 Nomi 正文内容色与排版。
     */
    @Composable
    override fun ComboBox(
        expanded: Boolean,
        onExpandedChange: (Boolean) -> Unit,
        menuItems: @Composable () -> Unit,
        modifier: Modifier,
        enabled: Boolean,
        nativeSpec: NomiComboBoxSpec?,
        content: @Composable RowScope.() -> Unit,
    ) {
        val scheme = NomiTheme.colorScheme
        val shape = NomiTheme.shapes.small
        val minTouchTarget = NomiButtonDefaults.metrics(NomiControlSize.Medium).minTouchTarget
        var anchorWidth by remember { mutableStateOf(0) }
        val anchorWidthDp: Dp = with(LocalDensity.current) { anchorWidth.toDp() }
        Box(modifier = modifier.onSizeChanged { anchorWidth = it.width }) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = minTouchTarget)
                    .clip(shape)
                    .background(scheme.surface)
                    .border(NomiTheme.sizes.strokes.thin, scheme.outline, shape)
                    .clickable(
                        interactionSource = null,
                        indication = null,
                        enabled = enabled,
                        role = Role.Button,
                        onClick = { onExpandedChange(true) },
                    )
                    .padding(
                        horizontal = NomiTheme.spacings.medium,
                        vertical = NomiTheme.spacings.small,
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(NomiTheme.spacings.small),
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ProvideNomiContentStyle(
                        color = if (enabled) scheme.textPrimary else scheme.textDisabled,
                        textStyle = NomiTheme.typography.bodyMedium,
                    ) {
                        content()
                    }
                }
                NativeIcon(
                    imageVector = IconPack.ChevronDown,
                    contentDescription = null,
                    modifier = Modifier
                        .size(NomiTheme.sizes.icon.extraSmall)
                        .rotate(if (expanded) 180f else 0f),
                    tint = if (enabled) scheme.textSecondary else scheme.textDisabled,
                )
            }
            NomiMenuPopup(
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) },
                width = anchorWidthDp,
            ) {
                menuItems()
            }
        }
    }

}
