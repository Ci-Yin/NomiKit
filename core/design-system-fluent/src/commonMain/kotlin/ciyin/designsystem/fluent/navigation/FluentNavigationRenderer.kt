package ciyin.designsystem.fluent.navigation

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import ciyin.designsystem.component.navigation.NomiNavigationItemColors
import ciyin.designsystem.component.navigation.NomiNavigationItemScope
import ciyin.designsystem.component.navigation.NomiNavigationRenderer
import ciyin.designsystem.component.navigation.NomiNavigationSuiteType
import io.github.composefluent.FluentTheme
import io.github.composefluent.component.MenuItem
import io.github.composefluent.component.NavigationDefaults
import io.github.composefluent.component.NavigationDisplayMode
import io.github.composefluent.component.NavigationItemColor
import io.github.composefluent.component.NavigationItemColorScheme
import io.github.composefluent.component.NavigationMenuScope
import io.github.composefluent.component.NavigationView

/**
 * Fluent 导航套件适配。
 *
 * 导航项以槽位形式在组合阶段记录，再按声明顺序交给原生菜单重建，颜色只在调用方显式覆盖时
 * 改写原生方案，不重建排版或尺寸配方。顶部与紧凑抽屉布局交由原生 [NavigationView]；
 * 左置布局改用公开 SideNav 组合（原生 footer 上方固定插入分割线且无关闭入口，无法直接复用）。
 */
internal object FluentNavigationRenderer : NomiNavigationRenderer {
    /**
     * 按布局类型建立原生 Fluent 导航外壳，并把页面内容交给原生导航布局排布。
     *
     * 主导航项与底部项分别进入原生菜单区与底部区，不交换、不合并、不丢弃分组；
     * 顶部与紧凑抽屉使用原生 NavigationView，左置布局使用公开 SideNav 重组且不绘制任何分割线。
     *
     * @param layoutType 调用方选择的导航布局；Bar 映射顶部、Drawer 映射紧凑侧边，Rail 使用左置外壳。
     * @param modifier 应用于原生导航外壳根节点的修饰符。
     * @param items 主导航项声明；按声明顺序进入原生菜单项区域。
     * @param footerItems 底部导航项声明；空 lambda 时原生导航自然省略底部区域。
     * @param content 被导航外壳包裹的页面内容。
     */
    @Composable
    override fun NavigationSuite(
        layoutType: NomiNavigationSuiteType,
        modifier: Modifier,
        items: @Composable NomiNavigationItemScope.() -> Unit,
        footerItems: @Composable NomiNavigationItemScope.() -> Unit,
        content: @Composable () -> Unit,
    ) {
        val mainItems = rememberFluentNavigationItemDeclarations(items)
        val footerDeclarations = rememberFluentNavigationItemDeclarations(footerItems)
        val paneColor = fluentNavigationPaneColor()
        when (layoutType) {
            NomiNavigationSuiteType.Rail -> FluentSideNavigationShell(
                modifier = modifier,
                paneColor = paneColor,
                mainItems = mainItems,
                footerItems = footerDeclarations,
                pane = content,
            )

            NomiNavigationSuiteType.Bar, NomiNavigationSuiteType.Drawer -> NavigationView(
                menuItems = mainItems.toFluentNavigationMenu(),
                modifier = modifier,
                displayMode = layoutType.toFluentDisplayMode(),
                footerItems = footerDeclarations.toFluentNavigationMenu(),
                // 上游默认给内容面板加 1dp 描边，这里显式关闭，避免导航栏与内容之间出现分割线。
                border = null,
                color = paneColor,
                pane = content,
            )
        }
    }
}

/**
 * 标记系统窗口背景材质（如 Windows 11 Acrylic）是否已经生效。
 *
 * 生效时导航内容面板改为透明，使窗口材质透出；未生效时保留原生默认面板颜色，避免无材质环境出现透明缺底。
 * 该值只由窗口宿主提供，导航渲染器不自行判断平台能力。
 */
val LocalFluentSystemBackdropActive = compositionLocalOf { false }

/**
 * 解析导航内容面板的背景色：系统材质生效时透明，否则使用原生默认图层色。
 *
 * @return 导航面板背景色。
 */
@Composable
private fun fluentNavigationPaneColor(): Color =
    if (LocalFluentSystemBackdropActive.current) {
        Color.Transparent
    } else {
        FluentTheme.colors.background.layer.default
    }

/**
 * 把中立导航布局映射为 Fluent 原生显示模式。
 *
 * Bar 使用顶部导航，Rail 使用完整侧边导航，Drawer 使用可展开的紧凑侧边导航；
 * Rail 的左置外壳由适配层按公开 SideNav 组合（原生 Left 布局无法关闭 footer 分割线与内容描边），
 * 其余布局直接交给原生 NavigationView；映射只决定外壳形态，选中状态与路由仍由调用方持有。
 *
 * @receiver 调用方选择的导航布局。
 * @return 与该布局语义最接近的原生显示模式。
 */
internal fun NomiNavigationSuiteType.toFluentDisplayMode(): NavigationDisplayMode = when (this) {
    NomiNavigationSuiteType.Bar -> NavigationDisplayMode.Top
    NomiNavigationSuiteType.Rail -> NavigationDisplayMode.Left
    NomiNavigationSuiteType.Drawer -> NavigationDisplayMode.LeftCompact
}

/**
 * 在组合中记录中立导航项声明，并返回按声明顺序重放的原生 Fluent 菜单 DSL。
 *
 * 记录只发生在组合阶段：每次重组都按当前状态重新记录，原生 [NavigationView] 读取 DSL 时才构建菜单项，
 * 因此选中状态、槽位内容与点击回调始终来自本次组合，不缓存过期声明。
 *
 * @param items 调用方提供的中立导航项声明，可以读取组合状态。
 * @return 把已记录声明写入 Fluent 原生菜单作用域的 DSL lambda。
 */
@Composable
private fun rememberFluentNavigationItemDeclarations(
    items: @Composable NomiNavigationItemScope.() -> Unit,
): List<FluentNavigationItemDeclaration> {
    val declarations = mutableListOf<FluentNavigationItemDeclaration>()
    FluentNavigationItemRecorder(declarations).items()
    return declarations
}

/**
 * 把已记录声明重放为原生菜单 DSL；顶部与紧凑抽屉布局仍由原生 NavigationView 排布。
 *
 * @receiver 本次组合记录到的导航项声明，按声明顺序输出。
 * @return 供原生菜单作用域调用的 DSL，每次调用都按当前声明重建菜单项。
 */
private fun List<FluentNavigationItemDeclaration>.toFluentNavigationMenu(): NavigationMenuScope.() -> Unit =
    {
        val menuScope = this
        forEach { declaration ->
            menuScope.item {
                MenuItem(
                    selected = declaration.selected,
                    onClick = { declaration.onClick() },
                    text = declaration.label ?: {},
                    icon = declaration.icon,
                    badge = declaration.badge,
                    enabled = true,
                    interactionSource = declaration.interactionSource,
                    colors = resolveFluentItemColors(
                        displayMode = displayMode,
                        selected = declaration.selected,
                        overrides = declaration.colors,
                    ),
                )
            }
        }
    }

/**
 * 解析一个导航项的原生颜色方案；只有显式覆盖的通道改写原生方案。
 *
 * 未选中项使用 contentColor，选中项使用 selectedContentColor，禁用状态使用 disabledContentColor；
 * 指示器颜色对所有状态生效。未指定的字段保留对应原生状态的颜色，不把其他状态的默认值扩散过来。
 *
 * @param displayMode 原生导航显示模式，决定读取顶部还是侧边导航项的原生默认方案。
 * @param selected 是否为当前选中项，决定内容色读取未选中还是选中覆盖。
 * @param overrides 中立稀疏颜色覆盖；字段为 Unspecified 时保留原生颜色。
 * @return 已叠加显式覆盖的原生五态颜色方案，focused 状态与 default 使用同一覆盖内容色。
 */
@Composable
internal fun resolveFluentItemColors(
    displayMode: NavigationDisplayMode,
    selected: Boolean,
    overrides: NomiNavigationItemColors,
): NavigationItemColorScheme {
    val nativeColors = when {
        displayMode == NavigationDisplayMode.Top && selected -> NavigationDefaults.selectedTopItemColors()
        displayMode == NavigationDisplayMode.Top -> NavigationDefaults.defaultTopItemColors()
        selected -> NavigationDefaults.selectedSideItemColors()
        else -> NavigationDefaults.defaultSideItemColors()
    }
    val contentColor = if (selected) overrides.selectedContentColor else overrides.contentColor
    return nativeColors.copy(
        default = nativeColors.default.withNomiColors(contentColor, overrides.indicatorColor),
        hovered = nativeColors.hovered.withNomiColors(contentColor, overrides.indicatorColor),
        pressed = nativeColors.pressed.withNomiColors(contentColor, overrides.indicatorColor),
        focused = nativeColors.focused.withNomiColors(contentColor, overrides.indicatorColor),
        disabled = nativeColors.disabled.withNomiColors(
            overrides.disabledContentColor,
            overrides.indicatorColor
        ),
    )
}

/**
 * 用中立覆盖改写单个原生导航项颜色，保留未覆盖的内容色、指示器色与填充色。
 *
 * @receiver 原生导航项颜色。
 * @param contentColor 显式内容色；Unspecified 时保留原生内容色。
 * @param indicatorColor 显式指示器色；Unspecified 时保留原生指示器色。
 * @return 仅改写显式通道后的新颜色对象，fillColor 与 trailingColor 保持原生值。
 */
private fun NavigationItemColor.withNomiColors(
    contentColor: Color,
    indicatorColor: Color,
): NavigationItemColor = copy(
    contentColor = contentColor.takeOrElse { this.contentColor },
    indicatorColor = indicatorColor.takeOrElse { this.indicatorColor },
)

/**
 * 一次中立导航项声明；只保存渲染所需数据，不持有 Compose 状态。
 *
 * @property selected 是否为当前选中项。
 * @property onClick 点击回调；由原生菜单项触发，不维护选中状态或路由。
 * @property icon 图标槽；null 表示不显示图标。
 * @property badge 徽标槽；null 表示不显示徽标。
 * @property label 文案槽；null 表示不显示可见文案。
 * @property colors 中立稀疏颜色覆盖，在组合阶段映射为原生五态方案。
 * @property interactionSource 调用方提供的交互源；null 时由原生菜单项自行创建。
 */
internal class FluentNavigationItemDeclaration(
    val selected: Boolean,
    val onClick: () -> Unit,
    val icon: (@Composable () -> Unit)?,
    val badge: (@Composable () -> Unit)?,
    val label: (@Composable () -> Unit)?,
    val colors: NomiNavigationItemColors,
    val interactionSource: MutableInteractionSource?,
)

/**
 * 导航项记录器；把中立导航项声明收集为原生菜单项的构建数据。
 *
 * 它只做声明到数据的转换，不产生布局；原生菜单项由 [rememberFluentNavigationMenu] 返回的 DSL 创建。
 *
 * @property declarations 本次组合的声明收集容器，按声明顺序追加。
 */
private class FluentNavigationItemRecorder(
    private val declarations: MutableList<FluentNavigationItemDeclaration>,
) : NomiNavigationItemScope {
    /**
     * 记录一个导航项；调用顺序决定原生菜单项的排列顺序。
     *
     * Fluent 原生 NavigationView 没有项级修饰符入口，因此只接受默认 modifier，
     * 需要在导航项外调整布局时应使用套件级 modifier。
     *
     * @param selected 是否为当前选中项。
     * @param onClick 点击回调。
     * @param modifier 应用于原生导航项的修饰符；必须保持默认值。
     * @param icon 图标槽；null 表示不显示图标。
     * @param badge 徽标槽；null 表示不显示徽标。
     * @param label 文案槽；null 表示不显示可见文案。
     * @param colors 中立稀疏颜色覆盖。
     * @param interactionSource 调用方提供的交互源；null 时由原生菜单项自行创建。
     * @throws IllegalArgumentException modifier 不是默认 [Modifier] 时抛出。
     */
    @Composable
    override fun NavigationItem(
        selected: Boolean,
        onClick: () -> Unit,
        modifier: Modifier,
        icon: (@Composable () -> Unit)?,
        badge: (@Composable () -> Unit)?,
        label: (@Composable () -> Unit)?,
        colors: NomiNavigationItemColors,
        interactionSource: MutableInteractionSource?,
    ) {
        require(modifier == Modifier) { "fluent NavigationItem 不支持 modifier；请使用容器级修饰" }
        declarations += FluentNavigationItemDeclaration(
            selected = selected,
            onClick = onClick,
            icon = icon,
            badge = badge,
            label = label,
            colors = colors,
            interactionSource = interactionSource,
        )
    }
}
