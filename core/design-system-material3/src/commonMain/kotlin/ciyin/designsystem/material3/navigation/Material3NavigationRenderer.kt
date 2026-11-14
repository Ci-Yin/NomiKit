package ciyin.designsystem.material3.navigation

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemColors
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemColors
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.takeOrElse
import ciyin.designsystem.component.navigation.NomiNavigationItemColors
import ciyin.designsystem.component.navigation.NomiNavigationItemScope
import ciyin.designsystem.component.navigation.NomiNavigationRenderer
import ciyin.designsystem.component.navigation.NomiNavigationSuiteType
import ciyin.designsystem.theme.NomiTheme

/**
 * Material 3 原生导航套件适配。
 *
 * 三种布局都直接使用 Material 3 原生组件，只按能力声明可用的布局；
 * 图标、文案与徽标由调用方以槽位组合，适配器不规定槽内容尺寸或排版，
 * 外壳容器色读取当前 [NomiTheme]，导航项颜色只做稀疏覆盖。
 */
internal object Material3NavigationRenderer : NomiNavigationRenderer {
    /**
     * 按布局类型建立对应的原生导航外壳，并把页面内容交给原生布局排布。
     *
     * 本实现支持 Bar、Rail 与 Drawer 三种布局；布局是否可用由统一入口先行按能力校验，
     * 因此这里不保留不可达的兜底分支。
     *
     * @param layoutType 调用方选择的导航布局。
     * @param modifier 应用于导航外壳根节点的修饰符。
     * @param items 主导航项声明，按声明顺序进入主导航区域。
     * @param footerItems 底部导航项声明；空 lambda 时原生布局自然省略底部区域。
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
        when (layoutType) {
            NomiNavigationSuiteType.Bar -> Material3BarNavigationSuite(
                modifier = modifier,
                items = items,
                footerItems = footerItems,
                content = content,
            )

            NomiNavigationSuiteType.Rail -> Material3RailNavigationSuite(
                modifier = modifier,
                items = items,
                footerItems = footerItems,
                content = content,
            )

            NomiNavigationSuiteType.Drawer -> Material3DrawerNavigationSuite(
                modifier = modifier,
                items = items,
                footerItems = footerItems,
                content = content,
            )
        }
    }
}

/**
 * Bar 布局：页面内容使用剩余高度，原生底部导航栏固定在其下方。
 *
 * @param modifier 应用于外层 Column 的修饰符。
 * @param items 主导航项声明。
 * @param footerItems 底部导航项声明，与主导航项同处一条原生导航栏。
 * @param content 导航栏上方的页面内容。
 */
@Composable
private fun Material3BarNavigationSuite(
    modifier: Modifier,
    items: @Composable NomiNavigationItemScope.() -> Unit,
    footerItems: @Composable NomiNavigationItemScope.() -> Unit,
    content: @Composable () -> Unit,
) {
    val colorScheme = NomiTheme.colorScheme
    Column(modifier = modifier) {
        Box(modifier = Modifier.weight(1f, fill = false)) {
            content()
        }
        NavigationBar(containerColor = colorScheme.surfaceLow) {
            val scope = Material3BarNavigationItemScope(rowScope = this)
            scope.items()
            scope.footerItems()
        }
    }
}

/**
 * Rail 布局：导航栏占满高度并把底部项推到最低处，页面内容位于导航栏右侧。
 *
 * @param modifier 应用于外层 Row 的修饰符。
 * @param items 主导航项声明。
 * @param footerItems 底部导航项声明，通过弹性占位与主导航项分开。
 * @param content 导航栏右侧的页面内容。
 */
@Composable
private fun Material3RailNavigationSuite(
    modifier: Modifier,
    items: @Composable NomiNavigationItemScope.() -> Unit,
    footerItems: @Composable NomiNavigationItemScope.() -> Unit,
    content: @Composable () -> Unit,
) {
    val colorScheme = NomiTheme.colorScheme
    Row(modifier = modifier) {
        NavigationRail(
            modifier = Modifier.fillMaxHeight(),
            containerColor = colorScheme.surfaceLow,
        ) {
            val scope = Material3RailNavigationItemScope
            scope.items()
            Spacer(modifier = Modifier.weight(1f))
            scope.footerItems()
        }
        content()
    }
}

/**
 * Drawer 布局：使用原生永久抽屉，导航项与底部项共处同一个抽屉表。
 *
 * @param modifier 应用于原生永久抽屉根节点的修饰符。
 * @param items 主导航项声明。
 * @param footerItems 底部导航项声明，通过弹性占位与主导航项分开。
 * @param content 抽屉右侧的页面内容。
 */
@Composable
private fun Material3DrawerNavigationSuite(
    modifier: Modifier,
    items: @Composable NomiNavigationItemScope.() -> Unit,
    footerItems: @Composable NomiNavigationItemScope.() -> Unit,
    content: @Composable () -> Unit,
) {
    PermanentNavigationDrawer(
        drawerContent = {
            PermanentDrawerSheet {
                val scope = Material3DrawerNavigationItemScope
                scope.items()
                Spacer(modifier = Modifier.weight(1f))
                scope.footerItems()
            }
        },
        modifier = modifier,
    ) {
        content()
    }
}

/**
 * 组合导航项的图标与徽标槽。
 *
 * 徽标存在时用原生 BadgedBox 作为锚点容器包住图标槽，两者都为空时不组合任何内容；
 * 图标尺寸与可选的无障碍描述完全由调用方的槽内容决定。
 *
 * @param icon 图标槽；null 表示不显示图标。
 * @param badge 徽标槽；null 表示不显示徽标。
 */
@Composable
private fun NavigationItemIconSlot(
    icon: @Composable (() -> Unit)?,
    badge: @Composable (() -> Unit)?,
) {
    val iconSlot = icon
    val badgeSlot = badge
    if (badgeSlot != null) {
        BadgedBox(badge = { badgeSlot() }) {
            if (iconSlot != null) {
                iconSlot()
            }
        }
    } else if (iconSlot != null) {
        iconSlot()
    }
}

/**
 * 把中立导航项颜色叠加到 Material 3 原生底部导航项颜色上。
 *
 * 原生颜色逐字段作为兜底，中立颜色为 Unspecified 的字段保持原生值。
 *
 * @receiver 原生底部导航项颜色，通常来自 NavigationBarItemDefaults.colors()。
 * @param colors 中立导航项稀疏颜色覆盖。
 * @return 叠加显式覆盖后的原生底部导航项颜色。
 */
private fun NavigationBarItemColors.withNeutralColors(colors: NomiNavigationItemColors): NavigationBarItemColors {
    val base = this
    return copy(
        selectedIconColor = colors.selectedContentColor.takeOrElse { base.selectedIconColor },
        selectedTextColor = colors.selectedContentColor.takeOrElse { base.selectedTextColor },
        selectedIndicatorColor = colors.indicatorColor.takeOrElse { base.selectedIndicatorColor },
        unselectedIconColor = colors.contentColor.takeOrElse { base.unselectedIconColor },
        unselectedTextColor = colors.contentColor.takeOrElse { base.unselectedTextColor },
        disabledIconColor = colors.disabledContentColor.takeOrElse { base.disabledIconColor },
        disabledTextColor = colors.disabledContentColor.takeOrElse { base.disabledTextColor },
    )
}

/**
 * 把中立导航项颜色叠加到 Material 3 原生侧边导航项颜色上。
 *
 * 原生颜色逐字段作为兜底，中立颜色为 Unspecified 的字段保持原生值。
 *
 * @receiver 原生侧边导航项颜色，通常来自 NavigationRailItemDefaults.colors()。
 * @param colors 中立导航项稀疏颜色覆盖。
 * @return 叠加显式覆盖后的原生侧边导航项颜色。
 */
private fun NavigationRailItemColors.withNeutralColors(colors: NomiNavigationItemColors): NavigationRailItemColors {
    val base = this
    return copy(
        selectedIconColor = colors.selectedContentColor.takeOrElse { base.selectedIconColor },
        selectedTextColor = colors.selectedContentColor.takeOrElse { base.selectedTextColor },
        selectedIndicatorColor = colors.indicatorColor.takeOrElse { base.selectedIndicatorColor },
        unselectedIconColor = colors.contentColor.takeOrElse { base.unselectedIconColor },
        unselectedTextColor = colors.contentColor.takeOrElse { base.unselectedTextColor },
        disabledIconColor = colors.disabledContentColor.takeOrElse { base.disabledIconColor },
        disabledTextColor = colors.disabledContentColor.takeOrElse { base.disabledTextColor },
    )
}

/**
 * 把中立导航项颜色叠加到 Material 3 原生抽屉项颜色上。
 *
 * NavigationDrawerItemColors 是只读接口且没有 copy，因此先读取原生默认颜色再逐字段 takeOrElse；
 * 抽屉项没有禁用状态，disabledContentColor 在此布局下不生效；徽标色沿用原生“跟随文案色”的默认。
 *
 * @param colors 中立导航项稀疏颜色覆盖。
 * @return 叠加显式覆盖后的原生抽屉项颜色。
 */
@Composable
private fun neutralDrawerItemColors(colors: NomiNavigationItemColors): NavigationDrawerItemColors {
    val base = NavigationDrawerItemDefaults.colors()
    val nativeSelectedContainer = base.containerColor(selected = true).value
    val nativeSelectedIcon = base.iconColor(selected = true).value
    val nativeUnselectedIcon = base.iconColor(selected = false).value
    val nativeSelectedText = base.textColor(selected = true).value
    val nativeUnselectedText = base.textColor(selected = false).value
    return NavigationDrawerItemDefaults.colors(
        selectedContainerColor = colors.indicatorColor.takeOrElse { nativeSelectedContainer },
        selectedIconColor = colors.selectedContentColor.takeOrElse { nativeSelectedIcon },
        unselectedIconColor = colors.contentColor.takeOrElse { nativeUnselectedIcon },
        selectedTextColor = colors.selectedContentColor.takeOrElse { nativeSelectedText },
        unselectedTextColor = colors.contentColor.takeOrElse { nativeUnselectedText },
    )
}

/**
 * Bar 布局的导航项作用域。
 *
 * @property rowScope 原生导航栏内容的 Row 作用域；NavigationBarItem 是 RowScope 扩展，
 * 需要该作用域把导航项放进原生导航栏并获得原生等宽排布。
 */
private class Material3BarNavigationItemScope(
    private val rowScope: RowScope,
) : NomiNavigationItemScope {
    /**
     * 渲染一个原生底部导航项；图标、徽标与文案槽原样交给原生组件组合。
     *
     * @param selected 是否为当前选中项。
     * @param onClick 点击回调。
     * @param modifier 应用于原生导航项的修饰符。
     * @param icon 图标槽；null 表示不显示图标。
     * @param badge 徽标槽；null 表示不显示徽标。
     * @param label 文案槽；null 表示不显示可见文案。
     * @param colors 稀疏颜色覆盖；Unspecified 字段保留原生导航项颜色。
     * @param interactionSource 可选交互源；null 时由原生导航项自行创建。
     */
    @Composable
    override fun NavigationItem(
        selected: Boolean,
        onClick: () -> Unit,
        modifier: Modifier,
        icon: @Composable (() -> Unit)?,
        badge: @Composable (() -> Unit)?,
        label: @Composable (() -> Unit)?,
        colors: NomiNavigationItemColors,
        interactionSource: MutableInteractionSource?,
    ) {
        with(rowScope) {
            NavigationBarItem(
                selected = selected,
                onClick = onClick,
                icon = { NavigationItemIconSlot(icon = icon, badge = badge) },
                modifier = modifier,
                label = label,
                colors = NavigationBarItemDefaults.colors().withNeutralColors(colors = colors),
                interactionSource = interactionSource,
            )
        }
    }
}

/**
 * Rail 布局的导航项作用域；文案槽由原生侧边导航项按原生规则显示或收起。
 */
private object Material3RailNavigationItemScope : NomiNavigationItemScope {
    /**
     * 渲染一个原生侧边导航项；图标、徽标与文案槽原样交给原生组件组合。
     *
     * @param selected 是否为当前选中项。
     * @param onClick 点击回调。
     * @param modifier 应用于原生导航项的修饰符。
     * @param icon 图标槽；null 表示不显示图标。
     * @param badge 徽标槽；null 表示不显示徽标。
     * @param label 文案槽；null 表示不显示可见文案。
     * @param colors 稀疏颜色覆盖；Unspecified 字段保留原生导航项颜色。
     * @param interactionSource 可选交互源；null 时由原生导航项自行创建。
     */
    @Composable
    override fun NavigationItem(
        selected: Boolean,
        onClick: () -> Unit,
        modifier: Modifier,
        icon: @Composable (() -> Unit)?,
        badge: @Composable (() -> Unit)?,
        label: @Composable (() -> Unit)?,
        colors: NomiNavigationItemColors,
        interactionSource: MutableInteractionSource?,
    ) {
        NavigationRailItem(
            selected = selected,
            onClick = onClick,
            icon = { NavigationItemIconSlot(icon = icon, badge = badge) },
            modifier = modifier,
            label = label,
            colors = NavigationRailItemDefaults.colors().withNeutralColors(colors = colors),
            interactionSource = interactionSource,
        )
    }
}

/**
 * Drawer 布局的导航项作用域。
 */
private object Material3DrawerNavigationItemScope : NomiNavigationItemScope {
    /**
     * 渲染一个原生抽屉导航项；文案槽缺失时组合空内容，原生项仍显示图标与徽标。
     *
     * @param selected 是否为当前选中项。
     * @param onClick 点击回调。
     * @param modifier 应用于原生导航项的修饰符，原生抽屉项额外叠加默认项内边距。
     * @param icon 图标槽；null 表示不显示图标。
     * @param badge 徽标槽；null 表示不显示徽标。
     * @param label 文案槽；null 表示不显示可见文案。
     * @param colors 稀疏颜色覆盖；Unspecified 字段保留原生导航项颜色。
     * @param interactionSource 可选交互源；null 时由原生导航项自行创建。
     */
    @Composable
    override fun NavigationItem(
        selected: Boolean,
        onClick: () -> Unit,
        modifier: Modifier,
        icon: @Composable (() -> Unit)?,
        badge: @Composable (() -> Unit)?,
        label: @Composable (() -> Unit)?,
        colors: NomiNavigationItemColors,
        interactionSource: MutableInteractionSource?,
    ) {
        NavigationDrawerItem(
            label = label ?: {},
            selected = selected,
            onClick = onClick,
            modifier = modifier.then(Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)),
            icon = { NavigationItemIconSlot(icon = icon, badge = badge) },
            colors = neutralDrawerItemColors(colors = colors),
            interactionSource = interactionSource,
        )
    }
}
