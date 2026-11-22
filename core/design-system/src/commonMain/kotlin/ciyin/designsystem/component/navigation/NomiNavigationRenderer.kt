package ciyin.designsystem.component.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * 导航套件的原生渲染契约。
 *
 * 实现负责按 [layoutType] 建立当前设计系统的原生导航外壳，并把 [content] 放进原生布局。
 * [items] 与 [footerItems] 由调用方分组，实现只能按声明顺序渲染组内项目，不能交换或合并两组；
 * 底部组是否参与当前布局、是否固定到底部由原生实现按原生导航语义决定（Nomi 的 Bar 只渲染主导航项）。
 * 导航外壳容器色由 [containerColor] 稀疏覆盖；实现无法承载该覆盖时自行降级或记录，核心入口不拦截。
 */
@Stable
interface NomiNavigationRenderer {
    /**
     * 按布局类型建立原生导航套件。
     *
     * @param layoutType 调用方选择的导航布局。
     * @param modifier 应用于导航外壳根节点的修饰符。
     * @param containerColor 导航外壳容器底色；[Color.Unspecified] 保留当前设计系统的原生值，显式颜色（含 Transparent）覆盖。
     * @param items 主导航项；按调用顺序渲染。
     * @param footerItems 底部导航项；空 lambda 表示没有底部项，实现应自然省略底部区域，Bar 布局可不展示该组。
     * @param nativeSpec 导航套件专属参数；null 或未携带本系统类型时全部沿用原生默认。
     * @param content 页面内容；必须与导航外壳共同完成排布。
     */
    @Composable
    fun NavigationSuite(
        layoutType: NomiNavigationSuiteType,
        modifier: Modifier,
        containerColor: Color,
        items: @Composable NomiNavigationItemScope.() -> Unit,
        footerItems: @Composable NomiNavigationItemScope.() -> Unit,
        nativeSpec: NomiNavigationSpec?,
        content: @Composable () -> Unit,
    )
}
