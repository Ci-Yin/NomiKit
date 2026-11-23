package ciyin.designsystem.component.button

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ciyin.designsystem.runtime.NomiNativeSpec
import ciyin.designsystem.runtime.NomiSpecFamily
import ciyin.designsystem.runtime.requireNomiDesignSystem

/**
 * 统一图标按钮入口；只承载图标或等价内容，容器、触摸目标与状态色沿用当前设计系统的原生默认。
 *
 * 入口不追加文本排版，也不强制图标尺寸：图标大小由调用方在内容中用 `Modifier.size` 约束，
 * 内容的加载态同样可以直接放入。未提供设计系统时由 [requireNomiDesignSystem] 抛出带指引的异常。
 *
 * @param onClick 启用状态下用户激活按钮时调用；业务状态由调用方更新。
 * @param modifier 应用于按钮根节点的修饰符。
 * @param enabled 是否接受用户操作，并据此选择启用或禁用外观。
 * @param colors 按启用/禁用状态逐字段覆盖颜色；Unspecified 保留对应原生值，Transparent 是有效覆盖。
 * @param nativeSpec 图标按钮专属参数；null 表示不覆盖任何原生属性。
 * @param content 在按钮内居中的内容，通常是一个图标，不负责维护点击状态。
 */
@Composable
fun NomiIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: NomiButtonColors = NomiButtonColors(),
    nativeSpec: NomiIconButtonSpec? = null,
    content: @Composable () -> Unit,
) = requireNomiDesignSystem().components.actions.IconButton(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    colors = colors,
    nativeSpec = nativeSpec,
    content = content,
)

/**
 * 图标按钮专属参数的族标记。
 *
 * 只作为 [NomiNativeSpec] 的类型参数使用；与普通按钮、开关按钮和下拉按钮的标记互不兼容。
 */
sealed interface NomiIconButtonSpecFamily : NomiSpecFamily

/**
 * 图标按钮专属参数。
 *
 * 只接受 [NomiIconButton]；当前没有实现，属于预留插孔。
 */
typealias NomiIconButtonSpec = NomiNativeSpec<NomiIconButtonSpecFamily>
