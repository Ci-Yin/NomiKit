package ciyin.designsystem.component.feedback

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ciyin.designsystem.runtime.NomiNativeSpec
import ciyin.designsystem.runtime.NomiSpecFamily
import ciyin.designsystem.runtime.requireNomiDesignSystem

/**
 * 统一提示浮层入口；触发方式、位置与外观沿用当前设计系统的原生默认。
 *
 * 悬停或长按由原生实现决定，核心不设置延时、位置或可见状态。未提供设计系统时由
 * [requireNomiDesignSystem] 抛出带指引的异常。
 *
 * @param tooltip 提示内容槽。
 * @param modifier 应用于浮层锚点容器的修饰符。
 * @param nativeSpec 提示专属参数；null 表示不覆盖任何原生属性。
 * @param content 触发提示的内容。
 */
@Composable
fun NomiTooltip(
    tooltip: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    nativeSpec: NomiTooltipSpec? = null,
    content: @Composable () -> Unit,
) = requireNomiDesignSystem().components.feedback.Tooltip(
    tooltip = tooltip,
    modifier = modifier,
    nativeSpec = nativeSpec,
    content = content,
)

/**
 * NomiTooltip专属参数的族标记。
 *
 * 只作为 [NomiNativeSpec] 的类型参数使用；其它组件的标记与它互不兼容，
 * 因此别的组件的 spec 无法传给 [NomiTooltip]。
 */
sealed interface NomiTooltipSpecFamily : NomiSpecFamily

/**
 * 提示浮层专属参数。
 *
 * 只接受 [NomiTooltip]；未来徽标等反馈组件会各自拥有独立的 spec 族，不共用本契约。当前没有实现，
 * 属于预留插孔。
 */
typealias NomiTooltipSpec = NomiNativeSpec<NomiTooltipSpecFamily>
