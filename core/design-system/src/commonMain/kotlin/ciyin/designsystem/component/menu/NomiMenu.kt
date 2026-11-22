package ciyin.designsystem.component.menu

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ciyin.designsystem.runtime.NomiNativeSpec
import ciyin.designsystem.runtime.NomiSpecFamily
import ciyin.designsystem.runtime.requireNomiDesignSystem

/**
 * 统一菜单浮层入口；弹出方向、位移、容器与交互反馈沿用当前设计系统的原生默认。
 *
 * 浮层锚定当前父布局；调用方必须把本组件与触发它的元素放进同一个父布局，
 * 否则各设计系统按父节点计算的位置会与视觉预期不一致。
 * 未提供设计系统时由 [requireNomiDesignSystem] 抛出带指引的异常。
 *
 * @param expanded 是否展开；由调用方持有，入口不维护状态。
 * @param onDismissRequest 用户要求关闭浮层时调用。
 * @param modifier 应用于浮层容器根节点的修饰符。
 * @param nativeSpec 菜单浮层专属参数；null 表示不覆盖任何原生属性。
 * @param content 在原生浮层作用域中组合的菜单项。
 */
@Composable
fun NomiMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    nativeSpec: NomiMenuSpec? = null,
    content: @Composable ColumnScope.() -> Unit,
) = requireNomiDesignSystem().components.menus.Menu(
    expanded = expanded,
    onDismissRequest = onDismissRequest,
    modifier = modifier,
    nativeSpec = nativeSpec,
    content = content,
)

/**
 * 菜单浮层专属参数的族标记。
 *
 * 只作为 [NomiNativeSpec] 的类型参数使用；其它组件的标记与它互不兼容，
 * 因此别的组件的 spec 无法传给 [NomiMenu]。
 */
sealed interface NomiMenuSpecFamily : NomiSpecFamily

/**
 * 菜单浮层专属参数。
 *
 * 只接受 [NomiMenu]；菜单项有独立契约 [NomiMenuItemSpec]，不会被本类型的值接受。
 * 当前没有实现，属于预留插孔。
 */
typealias NomiMenuSpec = NomiNativeSpec<NomiMenuSpecFamily>
