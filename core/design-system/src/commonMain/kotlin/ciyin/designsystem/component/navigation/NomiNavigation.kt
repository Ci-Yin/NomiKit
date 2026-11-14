package ciyin.designsystem.component.navigation

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import ciyin.designsystem.runtime.requireNomiDesignSystem

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
     * 抽屉式导航，适合宽屏或需要隐藏导航项的窗口。
     */
    Drawer,
}

/**
 * 导航项的稀疏颜色覆盖；Unspecified 表示保留当前设计系统的原生导航项颜色。
 *
 * 图标、文案与徽标槽内部的内容应读取原生内容环境（M3/Fluent 的原生内容色或 Nomi 槽环境），
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
 * 导航项声明作用域；槽位由调用方组合，未提供的槽位由原生实现省略。
 *
 * 图标、文案与徽标不再由核心限定为 ImageVector 或 String，调用方可以在槽内组合
 * 任意内容（例如中立文本、原生图标或徽标）；无障碍名称与图标尺寸也由调用方负责。
 * 中立 IconPack 的固有尺寸为 256dp，槽内图标必须显式约束尺寸，不能直接按固有尺寸绘制。
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
    )
}

/**
 * 导航套件的原生渲染契约。
 *
 * 实现负责按 [layoutType] 建立当前设计系统的原生导航外壳，并把 [content] 放进原生布局。
 * [items] 与 [footerItems] 由调用方分组，实现只能按顺序渲染，不能交换、合并或丢弃分组；
 * 是否把 footer 固定在底部、是否显示可见文案由原生实现决定。
 */
@Stable
interface NomiNavigationRenderer {
    /**
     * 按布局类型建立原生导航套件。
     *
     * @param layoutType 调用方选择的导航布局；实现必须在能力声明中逐项声明是否支持。
     * @param modifier 应用于导航外壳根节点的修饰符。
     * @param items 主导航项；按调用顺序渲染。
     * @param footerItems 底部导航项；空 lambda 表示没有底部项，实现应自然省略底部区域。
     * @param content 页面内容；必须与导航外壳共同完成排布。
     * @throws IllegalArgumentException 实现不支持给定布局类型时抛出；正常入口会先行校验能力并提前失败。
     */
    @Composable
    fun NavigationSuite(
        layoutType: NomiNavigationSuiteType,
        modifier: Modifier,
        items: @Composable NomiNavigationItemScope.() -> Unit,
        footerItems: @Composable NomiNavigationItemScope.() -> Unit,
        content: @Composable () -> Unit,
    )
}

/**
 * 统一导航套件入口；选中状态、导航项分组与路由分发全部由调用方持有。
 *
 * 布局类型由调用方根据窗口形态选择，组件本身不读取窗口宽度或平台状态。
 * 颜色、排版与尺寸沿用当前设计系统的原生导航默认值，不在此入口补齐统一视觉。
 * 未提供设计系统时由 [requireNomiDesignSystem] 抛出带指引的异常；
 * 当前设计系统不支持所选布局时在进入原生渲染前明确失败。
 *
 * @param layoutType 调用方按窗口形态选择的导航布局。
 * @param modifier 应用于导航外壳根节点的修饰符。
 * @param items 主导航项声明。
 * @param footerItems 底部导航项声明；空 lambda 表示没有底部项。
 * @param content 被导航外壳包裹的页面内容。
 * @throws IllegalArgumentException 当前设计系统不支持 [layoutType] 时抛出。
 */
@Composable
fun NomiNavigationSuite(
    layoutType: NomiNavigationSuiteType,
    modifier: Modifier = Modifier,
    items: @Composable NomiNavigationItemScope.() -> Unit,
    footerItems: @Composable NomiNavigationItemScope.() -> Unit = {},
    content: @Composable () -> Unit,
) {
    val designSystem = requireNomiDesignSystem()
    val capabilities = designSystem.capabilities
    val supported = when (layoutType) {
        NomiNavigationSuiteType.Bar -> capabilities.navigationBar
        NomiNavigationSuiteType.Rail -> capabilities.navigationRail
        NomiNavigationSuiteType.Drawer -> capabilities.navigationDrawer
    }
    require(supported) {
        designSystem.id.value + " 不支持 " + layoutType + " 导航布局"
    }
    designSystem.components.navigation.NavigationSuite(
        layoutType = layoutType,
        modifier = modifier,
        items = items,
        footerItems = footerItems,
        content = content,
    )
}
