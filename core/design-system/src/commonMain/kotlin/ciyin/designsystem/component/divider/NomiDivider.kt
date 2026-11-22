package ciyin.designsystem.component.divider

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import ciyin.designsystem.runtime.NomiNativeSpec
import ciyin.designsystem.runtime.NomiSpecFamily
import ciyin.designsystem.runtime.requireNomiDesignSystem

/**
 * 统一分割线入口；粗细与颜色沿用当前设计系统的原生默认。
 *
 * 默认横向；需要纵向时显式传 [NomiDividerOrientation.Vertical]。上游没有纵向分割线原语的
 * 实现会忽略该方向并记录警告，不会改用自绘线条冒充原生。
 * 未提供设计系统时由 [requireNomiDesignSystem] 抛出带指引的异常。
 *
 * @param orientation 分割线方向，默认横向。
 * @param modifier 应用于分割线根节点的修饰符。
 * @param thickness 显式粗细；null 保留原生粗细。
 * @param color 显式颜色；Unspecified 保留原生描边色，Transparent 是有效覆盖。
 * @param nativeSpec 分割线专属参数；null 表示不覆盖任何原生属性。
 */
@Composable
fun NomiDivider(
    orientation: NomiDividerOrientation = NomiDividerOrientation.Horizontal,
    modifier: Modifier = Modifier,
    thickness: Dp? = null,
    color: Color = Color.Unspecified,
    nativeSpec: NomiDividerSpec? = null,
) = requireNomiDesignSystem().components.decorations.Divider(
    orientation = orientation,
    modifier = modifier,
    thickness = thickness,
    color = color,
    nativeSpec = nativeSpec,
)

/**
 * 分割线方向。
 */
enum class NomiDividerOrientation {
    /**
     * 水平分割线；通常放在纵向排列的内容之间。
     */
    Horizontal,

    /**
     * 垂直分割线；通常放在横向排列的内容之间。
     */
    Vertical,
}

/**
 * NomiDivider专属参数的族标记。
 *
 * 只作为 [NomiNativeSpec] 的类型参数使用；其它组件的标记与它互不兼容，
 * 因此别的组件的 spec 无法传给 [NomiDivider]。
 */
sealed interface NomiDividerSpecFamily : NomiSpecFamily

/**
 * 分割线专属参数。
 *
 * 水平与垂直分割线是同一个入口的两种方向，共用本契约；只有实现本类型的 spec 才能传给
 * [NomiDivider] 的 nativeSpec 参数。当前没有实现，属于预留插孔。
 */
typealias NomiDividerSpec = NomiNativeSpec<NomiDividerSpecFamily>
