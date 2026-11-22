package ciyin.designsystem.component.control

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ciyin.designsystem.runtime.NomiNativeSpec
import ciyin.designsystem.runtime.NomiSpecFamily
import ciyin.designsystem.runtime.requireNomiDesignSystem

/**
 * 统一开关入口；尺寸、配色与交互反馈沿用当前设计系统的原生默认。
 *
 * 未提供设计系统时由 [requireNomiDesignSystem] 抛出带指引的异常。
 *
 * @param checked 当前开关状态。
 * @param onCheckedChange 状态切换回调。
 * @param modifier 应用于原生开关根节点的修饰符。
 * @param enabled 是否接受用户操作。
 * @param nativeSpec 开关专属参数；null 表示不覆盖任何原生属性。
 */
@Composable
fun NomiSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    nativeSpec: NomiSwitchSpec? = null,
) = requireNomiDesignSystem().components.controls.Switch(
    checked = checked,
    onCheckedChange = onCheckedChange,
    modifier = modifier,
    enabled = enabled,
    nativeSpec = nativeSpec,
)

/**
 * NomiSwitch专属参数的族标记。
 *
 * 只作为 [NomiNativeSpec] 的类型参数使用；其它组件的标记与它互不兼容，
 * 因此别的组件的 spec 无法传给 [NomiSwitch]。
 */
sealed interface NomiSwitchSpecFamily : NomiSpecFamily

/**
 * 开关专属参数。
 *
 * 只接受 [NomiSwitch]；同族的复选与单选各有独立契约，不会被本类型的值接受。当前没有实现，
 * 属于预留插孔。
 */
typealias NomiSwitchSpec = NomiNativeSpec<NomiSwitchSpecFamily>
