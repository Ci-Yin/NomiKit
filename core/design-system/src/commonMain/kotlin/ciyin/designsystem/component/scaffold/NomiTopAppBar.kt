package ciyin.designsystem.component.scaffold

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import ciyin.designsystem.runtime.NomiNativeSpec
import ciyin.designsystem.runtime.NomiSpecFamily
import ciyin.designsystem.runtime.requireNomiDesignSystem

/**
 * 统一顶栏入口；标题、导航图标与操作槽由调用方组合，栏高、排版与系统栏内边距沿用当前设计系统的原生默认。
 *
 * 入口不维护状态；颜色只做逐字段稀疏覆盖，未指定的通道保留当前设计系统的原生值。
 * 通常放进 [NomiScaffold] 的 topBar 槽，也可以独立使用。
 * 未提供设计系统时由 [requireNomiDesignSystem] 抛出带指引的异常。
 *
 * @param title 标题槽，位于导航图标与操作槽之间并占据剩余宽度。
 * @param modifier 应用于顶栏根节点的修饰符。
 * @param navigationIcon 导航图标槽；null 表示不占位，也不保留占位宽度。
 * @param actions 操作槽，在标题之后按行排列。
 * @param colors 容器、标题、导航图标与操作图标的稀疏颜色覆盖；Unspecified 保留原生值，Transparent 是有效覆盖。
 * @param nativeSpec 顶栏专属参数；null 表示不覆盖任何原生属性。
 */
@Composable
fun NomiTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    colors: NomiTopAppBarColors = NomiTopAppBarColors(),
    nativeSpec: NomiTopAppBarSpec? = null,
) = requireNomiDesignSystem().components.scaffold.TopAppBar(
    title = title,
    modifier = modifier,
    navigationIcon = navigationIcon,
    actions = actions,
    colors = colors,
    nativeSpec = nativeSpec,
)

/**
 * 顶栏的颜色稀疏覆盖；Transparent 是有效覆盖，Unspecified 才表示未指定。
 *
 * @property containerColor 顶栏容器底色，未指定时由当前设计系统的原生默认决定。
 * @property titleContentColor 标题内容色，未指定时由当前设计系统的原生默认决定。
 * @property navigationIconContentColor 导航图标内容色，未指定时由当前设计系统的原生默认决定。
 * @property actionIconContentColor 操作图标内容色，未指定时由当前设计系统的原生默认决定。
 */
@Immutable
data class NomiTopAppBarColors(
    val containerColor: Color = Color.Unspecified,
    val titleContentColor: Color = Color.Unspecified,
    val navigationIconContentColor: Color = Color.Unspecified,
    val actionIconContentColor: Color = Color.Unspecified,
)

/**
 * 顶栏专属参数的族标记。
 *
 * 只作为 [NomiNativeSpec] 的类型参数使用；其它组件族的标记与它互不兼容，
 * 因此别的组件的 spec 无法传给 [NomiTopAppBar]。
 */
sealed interface NomiTopAppBarSpecFamily : NomiSpecFamily

/**
 * 顶栏专属参数。
 *
 * 只承载中立参数没有入口的原生顶栏能力（滚动行为、折叠高度、窗口内边距等），具体实现由 adapter
 * 模块定义；本版属于预留插孔，adapter 暂不定义具体类型。
 */
typealias NomiTopAppBarSpec = NomiNativeSpec<NomiTopAppBarSpecFamily>
