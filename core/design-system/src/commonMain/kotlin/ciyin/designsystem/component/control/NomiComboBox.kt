package ciyin.designsystem.component.control

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ciyin.designsystem.runtime.NomiNativeSpec
import ciyin.designsystem.runtime.NomiSpecFamily
import ciyin.designsystem.runtime.requireNomiDesignSystem

/**
 * 统一组合框入口；收起态的容器与下拉指示、展开浮层的位置与宽度对齐沿用当前设计系统的原生默认。
 *
 * 展开状态由调用方持有：点击收起态内容请求展开，点击浮层外部或选中菜单项后在回调里关闭。
 * 菜单项使用 [ciyin.designsystem.component.menu.NomiMenuItem] 组合，不要再套一层菜单浮层——
 * 浮层已由本入口的实现承载。
 * 未提供设计系统时由 [requireNomiDesignSystem] 抛出带指引的异常。
 *
 * @param expanded 当前是否展开，由调用方持有。
 * @param onExpandedChange 展开状态变化回调，收到请求后的目标状态。
 * @param menuItems 展开时显示的菜单项槽；由实现放进自己的浮层。
 * @param modifier 应用于组合框根节点的修饰符。
 * @param enabled 是否接受用户操作。
 * @param nativeSpec 组合框专属参数；null 表示不覆盖任何原生属性。
 * @param content 收起态内容槽，显示当前值或占位内容；占位判断由调用方在槽内完成。
 */
@Composable
fun NomiComboBox(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    menuItems: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    nativeSpec: NomiComboBoxSpec? = null,
    content: @Composable RowScope.() -> Unit,
) = requireNomiDesignSystem().components.controls.ComboBox(
    expanded = expanded,
    onExpandedChange = onExpandedChange,
    menuItems = menuItems,
    modifier = modifier,
    enabled = enabled,
    nativeSpec = nativeSpec,
    content = content,
)

/**
 * 组合框专属参数的族标记。
 *
 * 只作为 [NomiNativeSpec] 的类型参数使用；与开关、复选框、单选按钮、滑块的标记互不兼容，
 * 因此其它选择控件的 spec 无法传给 [NomiComboBox]。
 */
sealed interface NomiComboBoxSpecFamily : NomiSpecFamily

/**
 * 组合框专属参数。
 *
 * 只接受 [NomiComboBox]；当前没有实现，属于预留插孔。
 */
typealias NomiComboBoxSpec = NomiNativeSpec<NomiComboBoxSpecFamily>

