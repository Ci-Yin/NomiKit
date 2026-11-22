package ciyin.designsystem.component.control

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ciyin.designsystem.runtime.NomiNativeSpec
import ciyin.designsystem.runtime.NomiSpecFamily
import ciyin.designsystem.runtime.requireNomiDesignSystem

/**
 * 统一复选框入口；尺寸、配色与交互反馈沿用当前设计系统的原生默认。
 *
 * 未提供设计系统时由 [requireNomiDesignSystem] 抛出带指引的异常。
 *
 * @param checked 当前勾选状态。
 * @param onCheckedChange 状态切换回调。
 * @param modifier 应用于原生复选框根节点的修饰符。
 * @param enabled 是否接受用户操作。
 * @param nativeSpec 复选框专属参数；null 表示不覆盖任何原生属性。
 */
@Composable
fun NomiCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    nativeSpec: NomiCheckboxSpec? = null,
) = requireNomiDesignSystem().components.controls.Checkbox(
    checked = checked,
    onCheckedChange = onCheckedChange,
    modifier = modifier,
    enabled = enabled,
    nativeSpec = nativeSpec,
)

/**
 * NomiCheckbox专属参数的族标记。
 *
 * 只作为 [NomiNativeSpec] 的类型参数使用；其它组件的标记与它互不兼容，
 * 因此别的组件的 spec 无法传给 [NomiCheckbox]。
 */
sealed interface NomiCheckboxSpecFamily : NomiSpecFamily

/**
 * 复选框专属参数。
 *
 * 只接受 [NomiCheckbox]；与开关、单选控件互不兼容。当前没有实现，属于预留插孔。
 */
typealias NomiCheckboxSpec = NomiNativeSpec<NomiCheckboxSpecFamily>
