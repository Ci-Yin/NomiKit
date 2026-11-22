package ciyin.designsystem.component.navigation

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import ciyin.designsystem.runtime.NomiNativeSpec
import ciyin.designsystem.runtime.NomiSpecFamily
import ciyin.designsystem.runtime.requireNomiDesignSystem

/**
 * 统一导航套件入口；选中状态、导航项分组与路由分发全部由调用方持有。
 *
 * 布局类型由调用方根据窗口形态选择，组件本身不读取窗口宽度或平台状态。
 * 颜色、排版与尺寸沿用当前设计系统的原生导航默认值，不在此入口补齐统一视觉；
 * 外壳容器色通过 [containerColor] 稀疏覆盖，导航项颜色仍由 [NomiNavigationItemColors] 表达。
 * 未提供设计系统时由 [requireNomiDesignSystem] 抛出带指引的异常；
 * 实现不支持所选布局或容器色覆盖时自行降级或记录，核心入口不按能力拦截。
 *
 * @param layoutType 调用方按窗口形态选择的导航布局。
 * @param modifier 应用于导航外壳根节点的修饰符。
 * @param containerColor 导航外壳容器底色；Unspecified 保留原生值，Transparent 是有效覆盖。
 * @param items 主导航项声明。
 * @param footerItems 底部导航项声明；空 lambda 表示没有底部项，Bar 布局可不展示该组。
 * @param nativeSpec 导航套件专属参数；null 表示不覆盖任何原生属性。
 * @param content 被导航外壳包裹的页面内容。
 */
@Composable
fun NomiNavigationSuite(
    layoutType: NomiNavigationSuiteType,
    modifier: Modifier = Modifier,
    containerColor: Color = Color.Unspecified,
    items: @Composable NomiNavigationItemScope.() -> Unit,
    footerItems: @Composable NomiNavigationItemScope.() -> Unit = {},
    nativeSpec: NomiNavigationSpec? = null,
    content: @Composable () -> Unit,
) = requireNomiDesignSystem().components.navigation.NavigationSuite(
    layoutType = layoutType,
    modifier = modifier,
    containerColor = containerColor,
    items = items,
    footerItems = footerItems,
    nativeSpec = nativeSpec,
    content = content,
)

/**
 * 导航套件的布局类型；由调用方按窗口形态选择，组件不读取窗口或平台状态。
 */
@Immutable
enum class NomiNavigationSuiteType {
    /**
     * 底部导航栏，适合窄屏单列窗口。
     */
    Bar,

    /**
     * 侧边导航栏，适合横向空间充足的窗口。
     */
    Rail,

    /**
     * 抽屉式导航，适合宽屏常驻的侧边导航。
     */
    Drawer,
}

/**
 * 导航项声明作用域；槽位由调用方组合，未提供的槽位由原生实现省略。
 *
 * 图标、文案与徽标不再由核心限定为 ImageVector 或 String，调用方可以在槽内组合
 * 任意内容（例如中立文本、原生图标或徽标）；无障碍名称与图标尺寸也由调用方负责。
 * 中立 IconPack 的固有尺寸为 24dp；需要其它尺寸时在槽内用 `Modifier.size(...)` 显式约束。
 */
@Stable
interface NomiNavigationItemScope {
    /**
     * 声明一个导航项。
     *
     * 选中状态、可用性与点击行为由调用方持有；实现只负责把槽位放进当前设计系统的原生导航项。
     *
     * @param selected 是否为当前选中项。
     * @param onClick 点击回调，不负责维护选中状态或路由。
     * @param modifier 应用于原生导航项的修饰符；不支持项级修饰符的实现必须明确拒绝非默认值。
     * @param icon 图标槽；null 表示不显示图标。
     * @param badge 徽标槽；null 表示不显示徽标。
     * @param label 文案槽；null 表示不显示可见文案。
     * @param colors 稀疏颜色覆盖；Unspecified 字段保留原生导航项颜色。
     * @param interactionSource 可选交互源；null 时由原生导航项自行创建。
     * @param nativeSpec 导航项专属参数；当前预留给未来的布局专属原生能力，实现应原样接受并忽略。
     */
    @Composable
    fun NavigationItem(
        selected: Boolean,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        icon: @Composable (() -> Unit)? = null,
        badge: @Composable (() -> Unit)? = null,
        label: @Composable (() -> Unit)? = null,
        colors: NomiNavigationItemColors = NomiNavigationItemColors(),
        interactionSource: MutableInteractionSource? = null,
        nativeSpec: NomiNavigationItemSpec? = null,
    )
}

/**
 * 导航项的稀疏颜色覆盖；Unspecified 表示保留当前设计系统的原生导航项颜色。
 *
 * 图标、文案与徽标槽内部的内容应读取原生内容环境（当前设计系统的原生内容色或 Nomi 槽环境），
 * 这样悬停、按下、禁用等原生状态色仍然生效；显式颜色只覆盖未指定字段。
 *
 * @property contentColor 未选中项的内容色；Unspecified 保留原生未选中色。
 * @property selectedContentColor 选中项的内容色；Unspecified 保留原生选中色。
 * @property disabledContentColor 禁用项的内容色；Unspecified 保留原生禁用色。
 * @property indicatorColor 选中指示器或选中底色的颜色；Unspecified 保留原生指示器色。
 */
@Immutable
data class NomiNavigationItemColors(
    val contentColor: Color = Color.Unspecified,
    val selectedContentColor: Color = Color.Unspecified,
    val disabledContentColor: Color = Color.Unspecified,
    val indicatorColor: Color = Color.Unspecified,
)

/**
 * 导航套件专属参数的族标记。
 *
 * 只作为 [NomiNativeSpec] 的类型参数使用；与导航项族的标记互不兼容。
 */
sealed interface NomiNavigationSpecFamily : NomiSpecFamily

/**
 * 导航套件专属参数。
 *
 * 只承载中立参数没有入口的原生外壳能力（色调阴影、窗口内边距、侧栏头等），
 * 具体实现由 adapter 模块定义；与具体布局无关的字段由实现自行决定是否生效。
 */
typealias NomiNavigationSpec = NomiNativeSpec<NomiNavigationSpecFamily>

/**
 * 导航项专属参数的族标记。
 *
 * 只作为 [NomiNativeSpec] 的类型参数使用；当前没有任何实现，属于预留插孔，
 * 用于将来透传各布局专属的原生导航项能力。
 */
sealed interface NomiNavigationItemSpecFamily : NomiSpecFamily

/**
 * 导航项专属参数。
 *
 * 三种布局的原生导航项颜色类型互不相同，单一 spec 只能写成联合体，因此本轮没有实现；
 * 预留该契约是为了让未来的布局专属能力无需修改公共签名即可接入。
 */
typealias NomiNavigationItemSpec = NomiNativeSpec<NomiNavigationItemSpecFamily>
