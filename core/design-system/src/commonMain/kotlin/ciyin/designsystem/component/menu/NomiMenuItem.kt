package ciyin.designsystem.component.menu

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ciyin.designsystem.runtime.NomiNativeSpec
import ciyin.designsystem.runtime.NomiSpecFamily
import ciyin.designsystem.runtime.requireNomiDesignSystem

/**
 * 统一菜单项入口；行高、内边距、状态外观与文本样式沿用当前设计系统的原生默认。
 *
 * 本入口不自带浮层：普通下拉菜单放进 [NomiMenu]，组合框的菜单项则由组合框实现承载。
 * 未提供设计系统时由 [requireNomiDesignSystem] 抛出带指引的异常。
 *
 * @param onClick 菜单项启用且被激活时调用；关闭浮层由调用方在回调里处理。
 * @param modifier 应用于菜单项根节点的修饰符。
 * @param enabled 是否接受用户操作。
 * @param selected 是否为当前选中项；true 时实现追加勾选指示。
 * @param leading 前导槽；null 表示不占位。
 * @param trailing 尾部槽；null 表示不占位。
 * @param nativeSpec 菜单项专属参数；null 表示不覆盖任何原生属性。
 * @param content 菜单项的主体内容。
 */
@Composable
fun NomiMenuItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selected: Boolean = false,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    nativeSpec: NomiMenuItemSpec? = null,
    content: @Composable () -> Unit,
) = requireNomiDesignSystem().components.menus.MenuItem(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    selected = selected,
    leading = leading,
    trailing = trailing,
    nativeSpec = nativeSpec,
    content = content,
)

/**
 * 菜单项专属参数的族标记。
 *
 * 只作为 [NomiNativeSpec] 的类型参数使用；与 [NomiMenuSpecFamily] 互不兼容，
 * 因此浮层参数无法传给菜单项。
 */
sealed interface NomiMenuItemSpecFamily : NomiSpecFamily

/**
 * 菜单项专属参数。
 *
 * 只接受 [NomiMenuItem]；当前没有实现，属于预留插孔。
 */
typealias NomiMenuItemSpec = NomiNativeSpec<NomiMenuItemSpecFamily>
