package ciyin.designsystem.component.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier

/**
 * 列表项的原生渲染契约。
 *
 * 实现负责把槽位放进当前设计系统的原生列表项，并保留原生行高、容器、圆角、交互状态、文本样式与
 * 内容色；不替槽内容指定尺寸、字号、行数或无障碍名称。
 */
@Stable
interface NomiListItemRenderer {
    /**
     * 渲染一个原生列表项。
     *
     * @param modifier 应用于原生列表项根节点的修饰符。
     * @param onClick 点击回调；null 表示静态行，实现不得附加点击语义或悬停/按下外观。
     * @param leading 前导槽；null 表示不占位。
     * @param title 标题槽，必须提供；槽内文本建议使用 `NomiText` 以继承原生标题样式。
     * @param subtitle 副标题槽；null 表示不占位。
     * @param trailing 尾部槽；null 表示不占位。
     * @param nativeSpec 列表项专属参数；null，或未携带本系统定义的类型时，全部沿用原生默认。
     *   实现不得为未携带的字段补值，也不得因携带其它设计系统的参数而报错或记录告警。
     */
    @Composable
    fun ListItem(
        modifier: Modifier,
        onClick: (() -> Unit)?,
        leading: (@Composable () -> Unit)?,
        title: @Composable () -> Unit,
        subtitle: (@Composable () -> Unit)?,
        trailing: (@Composable () -> Unit)?,
        nativeSpec: NomiListItemSpec?,
    )
}
