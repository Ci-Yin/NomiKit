package ciyin.designsystem.component.control

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ciyin.designsystem.runtime.NomiNativeSpec
import ciyin.designsystem.runtime.NomiSpecFamily
import ciyin.designsystem.runtime.requireNomiDesignSystem

/**
 * 统一单选按钮入口；尺寸、配色与交互反馈沿用当前设计系统的原生默认。
 *
 * 同一组内的选中状态由调用方持有，核心不提供分组容器。
 * 未提供设计系统时由 [requireNomiDesignSystem] 抛出带指引的异常。
 *
 * @param selected 是否为当前选中项。
 * @param onClick 点击回调。
 * @param modifier 应用于原生单选按钮根节点的修饰符。
 * @param enabled 是否接受用户操作。
 * @param nativeSpec 单选按钮专属参数；null 表示不覆盖任何原生属性。
 */
@Composable
fun NomiRadioButton(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    nativeSpec: NomiRadioButtonSpec? = null,
) = requireNomiDesignSystem().components.controls.RadioButton(
    selected = selected,
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    nativeSpec = nativeSpec,
)

/**
 * NomiRadioButton专属参数的族标记。
 *
 * 只作为 [NomiNativeSpec] 的类型参数使用；其它组件的标记与它互不兼容，
 * 因此别的组件的 spec 无法传给 [NomiRadioButton]。
 */
sealed interface NomiRadioButtonSpecFamily : NomiSpecFamily

/**
 * 单选按钮专属参数。
 *
 * 只接受 [NomiRadioButton]；与开关、复选框互不兼容。当前没有实现，属于预留插孔。
 */
typealias NomiRadioButtonSpec = NomiNativeSpec<NomiRadioButtonSpecFamily>
