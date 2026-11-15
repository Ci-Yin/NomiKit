package ciyin.designsystem.material3.list

import androidx.compose.foundation.clickable
import androidx.compose.material3.ListItem as NativeListItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ciyin.designsystem.component.list.NomiListItemRenderer

/**
 * Material 3 原生列表项适配。
 *
 * 行高、容器、文本样式、内容色与两行/三行推断全部交给原生组件；本适配器只选择原生槽位，
 * 并在调用方提供点击回调时附加原生点击修饰符。
 */
object Material3ListItemRenderer : NomiListItemRenderer {
    /**
     * 按原生槽位渲染列表项；不传 colors、tonalElevation 或 shadowElevation。
     *
     * @param modifier 应用于原生列表项的修饰符。
     * @param onClick 点击回调；null 时不附加点击语义，行保持静态。
     * @param leading 前导槽；null 表示不占位。
     * @param title 标题槽，进入原生 headlineContent。
     * @param subtitle 副标题槽；null 表示不占位，非 null 时进入原生 supportingContent。
     * @param trailing 尾部槽；null 表示不占位，避免原生尾部槽的起始内边距。
     */
    @Composable
    override fun ListItem(
        modifier: Modifier,
        onClick: (() -> Unit)?,
        leading: (@Composable () -> Unit)?,
        title: @Composable () -> Unit,
        subtitle: (@Composable () -> Unit)?,
        trailing: (@Composable () -> Unit)?,
    ) {
        NativeListItem(
            headlineContent = title,
            modifier = if (onClick == null) modifier else modifier.clickable(onClick = onClick),
            supportingContent = subtitle,
            leadingContent = leading,
            trailingContent = trailing,
        )
    }
}
