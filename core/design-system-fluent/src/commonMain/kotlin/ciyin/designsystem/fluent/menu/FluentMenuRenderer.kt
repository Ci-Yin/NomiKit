package ciyin.designsystem.fluent.menu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ciyin.designsystem.component.menu.NomiMenuItemSpec
import ciyin.designsystem.component.menu.NomiMenuRenderer
import ciyin.designsystem.component.menu.NomiMenuSpec
import ciyin.designsystem.icon.Check
import ciyin.designsystem.icon.IconPack
import io.github.composefluent.component.DropdownMenu
import io.github.composefluent.component.Icon
import io.github.composefluent.component.SubtleButton

/**
 * 原生 Fluent 菜单适配；浮层使用原生 DropdownMenu，菜单项使用原生 SubtleButton 行。
 */
internal object FluentMenuRenderer : NomiMenuRenderer {
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
     * 使用原生弱强调按钮作菜单项行，天然支持禁用态；选中指示放在最前。
     *
     * @param onClick 菜单项启用且被激活时调用。
     * @param modifier 应用于原生按钮行的修饰符。
     * @param enabled 是否接受用户操作，映射为原生 disabled 的反值。
     * @param selected 是否为当前选中项；true 时在最前追加勾选图标。
     * @param leading 前导槽；null 表示不占位。
     * @param trailing 尾部槽；null 表示不占位，提供时靠右对齐。
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
        SubtleButton(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            disabled = !enabled,
            content = {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = FluentMenuItemPadding),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(FluentMenuItemSpacing),
                ) {
                    if (selected) {
                        Icon(
                            imageVector = IconPack.Check,
                            contentDescription = null,
                            modifier = Modifier.size(FluentMenuItemIconSize),
                        )
                    }
                    leading?.invoke()
                    content()
                    if (trailing != null) {
                        Spacer(modifier = Modifier.weight(1f))
                        trailing()
                    }
                }
            },
        )
    }
}

/**
 * 菜单项左右内边距，与原生下拉菜单项的文本缩进一致。
 */
private val FluentMenuItemPadding = 8.dp

/**
 * 菜单项内容之间的间距，与原生按钮内容间距一致。
 */
private val FluentMenuItemSpacing = 8.dp

/**
 * 菜单项勾选图标尺寸，与原生菜单项图标约定一致。
 */
private val FluentMenuItemIconSize = 16.dp
