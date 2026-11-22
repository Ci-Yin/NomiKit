package ciyin.designsystem.component.control

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ciyin.designsystem.runtime.NomiNativeSpec
import ciyin.designsystem.runtime.NomiSpecFamily
import ciyin.designsystem.runtime.requireNomiDesignSystem

/**
 * 统一滑块入口；轨道、拇指、刻度与交互反馈沿用当前设计系统的原生默认。
 *
 * 入口不维护状态：value 由调用方持有，onValueChange 收到的是实现夹紧到 [valueRange] 之后的值。
 * 只提供水平方向与单一数值；需要竖直滑块、自定义拇指或轨道时，由调用方用修饰符与上层组件组合。
 * 未提供设计系统时由 [requireNomiDesignSystem] 抛出带指引的异常。
 *
 * @param value 当前值，由调用方持有；超出 [valueRange] 时由实现夹紧。
 * @param onValueChange 值变化回调，收到夹紧后的新值。
 * @param modifier 应用于滑块根节点的修饰符。
 * @param enabled 是否接受用户操作，并据此选择启用或禁用外观。
 * @param valueRange 取值区间，默认 0f..1f；起止相等时比例按 0 处理。
 * @param steps 离散刻度数量，0 表示连续滑块；负值按 0 处理。
 * @param onValueChangeFinished 用户结束拖拽或点按后调用；null 表示不关心结束事件。
 * @param nativeSpec 滑块专属参数；null 表示不覆盖任何原生属性。
 */
@Composable
fun NomiSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
    nativeSpec: NomiSliderSpec? = null,
) = requireNomiDesignSystem().components.controls.Slider(
    value = value,
    onValueChange = onValueChange,
    modifier = modifier,
    enabled = enabled,
    valueRange = valueRange,
    steps = steps,
    onValueChangeFinished = onValueChangeFinished,
    nativeSpec = nativeSpec,
)

/**
 * 滑块专属参数的族标记。
 *
 * 只作为 [NomiNativeSpec] 的类型参数使用；与开关、复选框、单选按钮、组合框的标记互不兼容，
 * 因此其它选择控件的 spec 无法传给 [NomiSlider]。
 */
sealed interface NomiSliderSpecFamily : NomiSpecFamily

/**
 * 滑块专属参数。
 *
 * 只接受 [NomiSlider]；当前没有实现，属于预留插孔。
 */
typealias NomiSliderSpec = NomiNativeSpec<NomiSliderSpecFamily>

