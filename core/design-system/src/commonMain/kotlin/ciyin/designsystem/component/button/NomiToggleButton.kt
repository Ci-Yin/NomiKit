package ciyin.designsystem.component.button

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import ciyin.designsystem.component.NomiControlSize
import ciyin.designsystem.runtime.NomiNativeSpec
import ciyin.designsystem.runtime.NomiSpecFamily
import ciyin.designsystem.runtime.requireNomiDesignSystem

/**
 * 统一开关按钮入口；按下后由调用方翻转状态，选中与未选中的外观差异沿用当前设计系统的原生默认。
 *
 * 入口不维护状态：checked 由调用方持有，onCheckedChange 收到的是请求后的目标状态。
 * iconOnly 为 true 时容器收敛为原生图标按钮的方形尺寸，此时 size 不生效，由实现忽略（有告警通道的实现
 * 会记录一次警告）；为 false 时 size 与普通按钮一致。无障碍名称由内容槽自行提供。
 * 未提供设计系统时由 [requireNomiDesignSystem] 抛出带指引的异常。
 *
 * @param checked 当前是否为选中状态，由调用方持有。
 * @param onCheckedChange 状态切换回调，收到请求后的目标状态。
 * @param modifier 应用于按钮根节点的修饰符。
 * @param enabled 是否接受用户操作，并据此选择启用或禁用外观。
 * @param iconOnly 是否只包含图标；true 时容器收敛为方形并忽略 size。
 * @param size 显式中立尺寸预设；null 保留原生尺寸，仅在 iconOnly 为 false 时生效。
 * @param colors 两种状态的逐字段颜色覆盖；全部未指定时沿用原生默认。
 * @param nativeSpec 开关按钮专属参数；null 表示不覆盖任何原生属性。
 * @param content 在按钮 RowScope 中组合的内容，继承按钮内容样式，不负责维护选中状态。
 */
@Composable
fun NomiToggleButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    iconOnly: Boolean = false,
    size: NomiControlSize? = null,
    colors: NomiToggleButtonColors = NomiToggleButtonColors(),
    nativeSpec: NomiToggleButtonSpec? = null,
    content: @Composable RowScope.() -> Unit,
) = requireNomiDesignSystem().components.actions.ToggleButton(
    checked = checked,
    onCheckedChange = onCheckedChange,
    modifier = modifier,
    enabled = enabled,
    iconOnly = iconOnly,
    size = size,
    colors = colors,
    nativeSpec = nativeSpec,
    content = content,
)

/**
 * 开关按钮两种状态的颜色；每个状态复用 [NomiButtonColors] 的四字段稀疏覆盖。
 *
 * 字段全部保留 Unspecified 时，两种状态都沿用当前设计系统的原生默认；Transparent 是有效覆盖。
 *
 * @property checked 选中状态的颜色覆盖，基线通常是强调外观。
 * @property unchecked 未选中状态的颜色覆盖，基线通常是普通或弱强调外观。
 */
@Immutable
data class NomiToggleButtonColors(
    val checked: NomiButtonColors = NomiButtonColors(),
    val unchecked: NomiButtonColors = NomiButtonColors(),
)

/**
 * 开关按钮专属参数的族标记。
 *
 * 只作为 [NomiNativeSpec] 的类型参数使用；与普通按钮、下拉按钮的标记互不兼容。
 */
sealed interface NomiToggleButtonSpecFamily : NomiSpecFamily

/**
 * 开关按钮专属参数。
 *
 * 只接受 [NomiToggleButton]；当前没有实现，属于预留插孔。
 */
typealias NomiToggleButtonSpec = NomiNativeSpec<NomiToggleButtonSpecFamily>
