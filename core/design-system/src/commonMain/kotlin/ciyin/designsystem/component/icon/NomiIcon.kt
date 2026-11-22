package ciyin.designsystem.component.icon

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import ciyin.designsystem.runtime.NomiNativeSpec
import ciyin.designsystem.runtime.NomiSpecFamily
import ciyin.designsystem.runtime.requireNomiDesignSystem

/**
 * 统一图标入口；着色与尺寸沿用当前设计系统的原生默认。
 *
 * 不提供尺寸参数：内置矢量固有尺寸统一为 24dp，需要其它尺寸时传入
 * [Modifier.size]，各设计系统都按调用方约束绘制。
 * 未提供设计系统时由 [requireNomiDesignSystem] 抛出带指引的异常。
 *
 * @param imageVector 要绘制的图标矢量。
 * @param contentDescription 无障碍名称；null 表示装饰性图标。
 * @param modifier 应用于图标根节点的修饰符。
 * @param tint 图标着色；Unspecified 保留原生内容色，Transparent 是有效覆盖。
 * @param nativeSpec 图标专属参数；null 表示不覆盖任何原生属性。
 */
@Composable
fun NomiIcon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
    nativeSpec: NomiIconSpec? = null,
) = requireNomiDesignSystem().components.icons.Icon(
    imageVector = imageVector,
    contentDescription = contentDescription,
    modifier = modifier,
    tint = tint,
    nativeSpec = nativeSpec,
)

/**
 * NomiIcon专属参数的族标记。
 *
 * 只作为 [NomiNativeSpec] 的类型参数使用；其它组件的标记与它互不兼容，
 * 因此别的组件的 spec 无法传给 [NomiIcon]。
 */
sealed interface NomiIconSpecFamily : NomiSpecFamily

/**
 * 图标专属参数。
 *
 * 只有实现本类型的 spec 才能传给 [NomiIcon] 的 nativeSpec 参数；具体实现由 adapter 模块定义，
 * 多个 spec 用 `+` 组合后一次传入。当前没有实现，属于预留插孔。
 */
typealias NomiIconSpec = NomiNativeSpec<NomiIconSpecFamily>
