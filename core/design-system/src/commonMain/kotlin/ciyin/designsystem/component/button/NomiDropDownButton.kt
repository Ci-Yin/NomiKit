package ciyin.designsystem.component.button

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ciyin.designsystem.component.NomiControlSize
import ciyin.designsystem.runtime.NomiNativeSpec
import ciyin.designsystem.runtime.NomiSpecFamily
import ciyin.designsystem.runtime.requireNomiDesignSystem

/**
 * 统一下拉按钮入口；外观与普通按钮一致，尾部由实现追加原生下拉指示箭头。
 *
 * 入口只负责触发，不持有菜单展开状态，也不渲染菜单：菜单由调用方用 [NomiMenu] 组合，
 * 并放进与按钮相同的父布局。
 * 未提供设计系统时由 [requireNomiDesignSystem] 抛出带指引的异常。
 *
 * @param onClick 启用状态下用户激活按钮时调用；业务状态由调用方更新。
 * @param modifier 应用于按钮根节点的修饰符。
 * @param enabled 是否接受用户操作，并据此选择启用或禁用外观。
 * @param variant 按钮语义类型；null 使用当前设计系统的原生默认类型。
 * @param size 显式中立尺寸预设；null 保留原生尺寸。
 * @param colors 按启用/禁用状态逐字段覆盖颜色；Unspecified 保留对应原生值，Transparent 是有效覆盖。
 * @param nativeSpec 下拉按钮专属参数；null 表示不覆盖任何原生属性。
 * @param content 在按钮 RowScope 中组合的内容，不含下拉箭头。
 */
@Composable
fun NomiDropDownButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: NomiButtonVariant? = null,
    size: NomiControlSize? = null,
    colors: NomiButtonColors = NomiButtonColors(),
    nativeSpec: NomiDropDownButtonSpec? = null,
    content: @Composable RowScope.() -> Unit,
) = requireNomiDesignSystem().components.actions.DropDownButton(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    variant = variant,
    size = size,
    colors = colors,
    nativeSpec = nativeSpec,
    content = content,
)

/**
 * 下拉按钮专属参数的族标记。
 *
 * 只作为 [NomiNativeSpec] 的类型参数使用；与普通按钮、开关按钮的标记互不兼容。
 */
sealed interface NomiDropDownButtonSpecFamily : NomiSpecFamily

/**
 * 下拉按钮专属参数。
 *
 * 只接受 [NomiDropDownButton]；当前没有实现，属于预留插孔。
 */
typealias NomiDropDownButtonSpec = NomiNativeSpec<NomiDropDownButtonSpecFamily>
