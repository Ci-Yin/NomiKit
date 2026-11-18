package ciyin.designsystem.component.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import ciyin.designsystem.runtime.requireNomiDesignSystem

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

/**
 * 统一列表项入口；行高、容器、文本样式与交互外观全部沿用当前设计系统的原生默认。
 *
 * 未提供视觉参数时不填充任何样式、颜色或尺寸；槽位为 null 时不占位。标题与副标题建议使用
 * `NomiText` 继承原生列表文本样式，图标尺寸、无障碍名称与尾部控件状态由调用方负责。
 * 未进入公共 API 的原生属性通过 [nativeSpec] 透传，其它设计系统实现会静默忽略。
 * 未提供设计系统时由 [requireNomiDesignSystem] 抛出带指引的异常。
 *
 * @param title 标题槽，必须提供。
 * @param modifier 应用于列表项根节点的修饰符。
 * @param leading 前导槽；null 表示不占位。
 * @param subtitle 副标题槽；null 表示不占位。
 * @param trailing 尾部槽；null 表示不占位。
 * @param onClick 点击回调；null 表示静态行。
 * @param nativeSpec 列表项专属参数，类型由 [NomiListItemSpec] 约束，其它组件族的 spec 无法传入；
 *   null 表示不覆盖任何原生属性。多个实现可用 `+` 组合后一次传入，未携带的系统实现会忽略它。
 */
@Composable
fun NomiListItem(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null,
    subtitle: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    nativeSpec: NomiListItemSpec? = null,
) = requireNomiDesignSystem().components.listItems.ListItem(
    modifier = modifier,
    onClick = onClick,
    leading = leading,
    title = title,
    subtitle = subtitle,
    trailing = trailing,
    nativeSpec = nativeSpec,
)
