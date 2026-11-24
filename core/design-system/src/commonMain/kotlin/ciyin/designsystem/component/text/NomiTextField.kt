package ciyin.designsystem.component.text

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation
import ciyin.designsystem.runtime.NomiNativeSpec
import ciyin.designsystem.runtime.NomiSpecFamily
import ciyin.designsystem.runtime.requireNomiDesignSystem

/**
 * 统一文本输入框入口；容器、边框、标签排版与交互反馈沿用当前设计系统的原生默认。
 *
 * 只接收字符串值，不提供辅助说明或前后缀；这些能力在两侧原生实现中形态差异较大，
 * 留待后续轮次。
 * 未提供设计系统时由 [requireNomiDesignSystem] 抛出带指引的异常。
 *
 * @param value 当前文本值，由调用方持有。
 * @param onValueChange 文本变化回调。
 * @param modifier 应用于输入框根节点的修饰符。
 * @param enabled 是否接受用户操作。
 * @param readOnly 是否只读。
 * @param singleLine 是否限制为单行。
 * @param maxLines 最大显示行数；[singleLine] 为 true 时实现按单行处理。
 * @param visualTransformation 文本可视化转换，例如密码遮罩；默认原样显示。
 * @param variant 显式视觉变体；null 使用原生默认变体。
 * @param label 标签槽；null 表示没有标签。
 * @param placeholder 占位符槽；null 表示没有占位符。
 * @param leadingIcon 前导图标槽；null 表示不占位。
 * @param trailingIcon 尾部图标槽；null 表示不占位。
 * @param isError 是否处于错误状态。
 * @param keyboardOptions 键盘类型与输入法动作配置。
 * @param keyboardActions 输入法动作回调。
 * @param nativeSpec 输入框专属参数；null 表示不覆盖任何原生属性。
 */
@Composable
fun NomiTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    variant: NomiTextFieldVariant? = null,
    label: (@Composable () -> Unit)? = null,
    placeholder: (@Composable () -> Unit)? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    nativeSpec: NomiTextFieldSpec? = null,
) = requireNomiDesignSystem().components.text.TextField(
    value = value,
    onValueChange = onValueChange,
    modifier = modifier,
    enabled = enabled,
    readOnly = readOnly,
    singleLine = singleLine,
    maxLines = maxLines,
    visualTransformation = visualTransformation,
    variant = variant,
    label = label,
    placeholder = placeholder,
    leadingIcon = leadingIcon,
    trailingIcon = trailingIcon,
    isError = isError,
    keyboardOptions = keyboardOptions,
    keyboardActions = keyboardActions,
    nativeSpec = nativeSpec,
)

/**
 * 输入框的视觉变体；未指定时使用当前设计系统的原生默认变体。
 */
enum class NomiTextFieldVariant {
    /**
     * 填充式输入框。
     */
    Filled,

    /**
     * 描边式输入框。
     */
    Outlined,
}

/**
 * NomiTextField专属参数的族标记。
 *
 * 只作为 [NomiNativeSpec] 的类型参数使用；其它组件的标记与它互不兼容，
 * 因此别的组件的 spec 无法传给 [NomiTextField]。
 */
sealed interface NomiTextFieldSpecFamily : NomiSpecFamily

/**
 * 文本输入框专属参数。
 *
 * 只接受 [NomiTextField]；三个选择控件与它互不兼容。当前没有实现，属于预留插孔。
 */
typealias NomiTextFieldSpec = NomiNativeSpec<NomiTextFieldSpecFamily>
