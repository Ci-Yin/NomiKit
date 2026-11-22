package ciyin.designsystem.component.menu

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier

/**
 * 菜单族的原生渲染契约。
 *
 * 浮层由 [Menu] 承载，菜单项 [MenuItem] 是可复用行原语：它既能放进 [Menu] 的浮层，
 * 也能由组合框等自带浮层的组件渲染。两者都不维护展开状态，也不替调用方决定关闭时机。
 */
@Stable
interface NomiMenuRenderer {
    /**
     * 渲染一个原生菜单浮层。
     *
     * 浮层必须以当前父布局为锚点：实现按原生规则决定弹出方向与位移，核心不传位置参数。
     * 因此调用方须把菜单与触发元素放进同一个父布局。
     *
     * @param expanded 是否展开；false 时不组合浮层，也不保留外层节点。
     * @param onDismissRequest 用户要求关闭浮层时调用，例如点击浮层外部或按返回键；业务状态由调用方更新。
     * @param modifier 应用于浮层容器根节点的修饰符。
     * @param nativeSpec 菜单浮层专属参数；null，或未携带本系统定义的类型时，全部沿用原生默认。
     * @param content 在原生浮层的纵向作用域中组合的菜单项。
     */
    @Composable
    fun Menu(
        expanded: Boolean,
        onDismissRequest: () -> Unit,
        modifier: Modifier,
        nativeSpec: NomiMenuSpec?,
        content: @Composable ColumnScope.() -> Unit,
    )

    /**
     * 渲染一个原生菜单项。
     *
     * 行高、内边距、悬停与按下外观、文本排版与内容色全部由原生菜单项决定；
     * 核心只传递状态、槽位与点击回调。
     *
     * @param onClick 菜单项启用且被激活时调用；关闭浮层的时机由调用方在回调中决定。
     * @param modifier 应用于菜单项根节点的修饰符。
     * @param enabled 是否接受用户操作，并据此选择启用或禁用外观。
     * @param selected 是否为当前选中项；为 true 时实现追加勾选指示，且不影响槽位内容。
     * @param leading 前导槽；null 表示不占位，槽内图标尺寸由调用方约束。
     * @param trailing 尾部槽；null 表示不占位，可放快捷键文本等。
     * @param nativeSpec 菜单项专属参数；null，或未携带本系统定义的类型时，全部沿用原生默认。
     * @param content 菜单项的主体内容，继承原生菜单项的内容色与排版。
     */
    @Composable
    fun MenuItem(
        onClick: () -> Unit,
        modifier: Modifier,
        enabled: Boolean,
        selected: Boolean,
        leading: (@Composable () -> Unit)?,
        trailing: (@Composable () -> Unit)?,
        nativeSpec: NomiMenuItemSpec?,
        content: @Composable () -> Unit,
    )
}
