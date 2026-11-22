package ciyin.designsystem.material3.menu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ciyin.designsystem.component.menu.NomiMenuItemSpec
import ciyin.designsystem.component.menu.NomiMenuRenderer
import ciyin.designsystem.component.menu.NomiMenuSpec
import ciyin.designsystem.icon.Check
import ciyin.designsystem.icon.IconPack

/**
 * 原生 Material 3 菜单适配；浮层使用原生 DropdownMenu，菜单项使用原生 DropdownMenuItem。
 */
internal object Material3MenuRenderer : NomiMenuRenderer {
    /**
     * 使用原生下拉菜单呈现浮层，位置与关闭行为由原生实现决定。
     *
     * @param expanded 是否展开。
     * @param onDismissRequest 用户要求关闭浮层时调用。
     * @param modifier 应用于原生下拉菜单的修饰符。
     * @param nativeSpec 菜单浮层专属参数；当前没有实现，原样接受并忽略。
     * @param content 菜单项内容。
     */
    @Composable
    override fun Menu(
        expanded: Boolean,
        onDismissRequest: () -> Unit,
        modifier: Modifier,
        nativeSpec: NomiMenuSpec?,
        content: @Composable ColumnScope.() -> Unit,
    ) {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismissRequest,
            modifier = modifier,
            content = content,
        )
    }

    /**
     * 使用原生下拉菜单项承载内容；选中指示放进原生前导图标位。
     *
     * @param onClick 菜单项启用且被激活时调用。
     * @param modifier 应用于原生菜单项的修饰符。
     * @param enabled 是否接受用户操作。
     * @param selected 是否为当前选中项；true 时在前导位追加勾选图标。
     * @param leading 前导槽；null 表示不占位。
     * @param trailing 尾部槽；null 表示不占位。
     * @param nativeSpec 菜单项专属参数；当前没有实现，原样接受并忽略。
     * @param content 菜单项主体内容。
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
        DropdownMenuItem(
            text = content,
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            leadingIcon = if (selected || leading != null) {
                {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Material3MenuItemSpacing),
                    ) {
                        if (selected) {
                            Icon(imageVector = IconPack.Check, contentDescription = null)
                        }
                        leading?.invoke()
                    }
                }
            } else {
                null
            },
            trailingIcon = trailing,
        )
    }
}

/**
 * 菜单项前导位中勾选图标与自定义槽之间的间距，与原生图标间距一致。
 */
private val Material3MenuItemSpacing = 8.dp
