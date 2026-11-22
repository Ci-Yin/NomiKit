package ciyin.designsystem.component.scaffold

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import ciyin.designsystem.runtime.NomiNativeSpec
import ciyin.designsystem.runtime.NomiSpecFamily
import ciyin.designsystem.runtime.requireNomiDesignSystem

/**
 * 统一页面脚手架入口；页面标题、最大宽度、滚动状态与系统栏避让仍由调用方持有。
 *
 * 颜色、排版与尺寸沿用当前设计系统的原生默认值，不在此入口补齐统一视觉；
 * 未提供设计系统时由 [requireNomiDesignSystem] 抛出带指引的异常。
 *
 * @param modifier 应用于页面外壳根节点的修饰符。
 * @param colors 容器色与内容色的逐字段覆盖；Unspecified 保留原生值，Transparent 是有效覆盖。
 * @param topBar 顶部栏槽；空 lambda 表示没有顶部栏。
 * @param bottomBar 底部栏槽；空 lambda 表示没有底部栏。
 * @param nativeSpec 页面脚手架专属参数；null 表示不覆盖任何原生属性。
 * @param content 页面主体内容，位于顶栏与底栏之间。
 */
@Composable
fun NomiScaffold(
    modifier: Modifier = Modifier,
    colors: NomiScaffoldColors = NomiScaffoldColors(),
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    nativeSpec: NomiScaffoldSpec? = null,
    content: @Composable () -> Unit,
) = requireNomiDesignSystem().components.scaffold.Scaffold(
    modifier = modifier,
    colors = colors,
    topBar = topBar,
    bottomBar = bottomBar,
    nativeSpec = nativeSpec,
    content = content,
)

/**
 * 页面脚手架的颜色稀疏覆盖；Transparent 是有效覆盖，Unspecified 才表示未指定。
 *
 * @property containerColor 页面容器背景色，未指定时由当前设计系统的原生默认决定。
 * @property contentColor 页面内容色，未指定时由当前设计系统的原生默认决定。
 */
@Immutable
data class NomiScaffoldColors(
    val containerColor: Color = Color.Unspecified,
    val contentColor: Color = Color.Unspecified,
)

/**
 * 页面脚手架专属参数的族标记。
 *
 * 只作为 [NomiNativeSpec] 的类型参数使用；其它组件族的标记与它互不兼容。
 */
sealed interface NomiScaffoldSpecFamily : NomiSpecFamily

/**
 * 页面脚手架专属参数。
 *
 * 只承载中立参数没有入口的原生外壳能力（提示条宿主、悬浮按钮、内容窗口内边距），
 * 具体实现由 adapter 模块定义。
 */
typealias NomiScaffoldSpec = NomiNativeSpec<NomiScaffoldSpecFamily>
