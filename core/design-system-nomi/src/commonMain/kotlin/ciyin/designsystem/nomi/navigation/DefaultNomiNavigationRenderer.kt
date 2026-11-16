package ciyin.designsystem.nomi.navigation

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import ciyin.designsystem.component.navigation.NomiNavigationItemColors
import ciyin.designsystem.component.navigation.NomiNavigationItemScope
import ciyin.designsystem.component.navigation.NomiNavigationRenderer
import ciyin.designsystem.component.navigation.NomiNavigationSuiteType
import ciyin.designsystem.runtime.ProvideNomiContentStyle
import ciyin.designsystem.theme.NomiTheme

/**
 * 侧边导航栏宽度；Nomi 原生导航视觉的稳定栏宽，非修错位用的临时度量。
 */
private val RailWidth = 60.dp

/**
 * 导航项选中时的容器底色透明度；只在 primary 上叠加，保留原生导航的轻量选中提示。
 */
private const val NavigationSelectedContainerAlpha = 0.1f

/**
 * Nomi 原生导航套件实现，复刻 Nomi 自身底部导航栏与侧边导航栏的视觉形态，并为宽屏提供永久抽屉。
 *
 * 图标、徽标与文案是调用方组合的槽位，实现只负责把它们放进原生导航项；图标尺寸与无障碍名称
 * 由槽内容负责。颜色与排版经 [NomiTheme] 读取，槽内容通过 [ProvideNomiContentStyle] 继承导航项
 * 内容色，因此 NomiText 槽能随选中状态变色。
 * 导航项级 [Modifier] 应用到可选中表面，交互源直接交给原生选择语义；
 * 三种布局都受支持：Bar 使用原生底部栏且只渲染主导航项，Rail 使用原生侧边栏，Drawer 使用原生永久抽屉。
 * 导航外壳容器色只在调用方显式覆盖时改写，未指定时保留 Nomi surfaceLow。
 */
internal object DefaultNomiNavigationRenderer : NomiNavigationRenderer {
    /**
     * 按布局类型建立 Nomi 原生导航外壳。
     *
     * @param layoutType 调用方选择的导航布局；支持 Bar、Rail 与 Drawer 三种布局。
     * @param modifier 应用于导航外壳根节点的修饰符；Bar 作用于纵向容器，Rail 作用于横向容器，Drawer 作用于永久抽屉根节点。
     * @param containerColor 导航外壳容器底色；Unspecified 保留 Nomi surfaceLow，Transparent 是有效覆盖。
     * @param items 主导航项声明，按调用顺序渲染。
     * @param footerItems 底部导航项声明；Rail 与 Drawer 固定到栏底，Bar 不渲染底部组（复刻旧底部栏只展示主导航项）。
     * @param content 页面内容，与导航外壳共同完成排布。
     */
    @Composable
    override fun NavigationSuite(
        layoutType: NomiNavigationSuiteType,
        modifier: Modifier,
        containerColor: Color,
        items: @Composable NomiNavigationItemScope.() -> Unit,
        footerItems: @Composable NomiNavigationItemScope.() -> Unit,
        content: @Composable () -> Unit,
    ) {
        when (layoutType) {
            NomiNavigationSuiteType.Bar -> BarNavigationSuite(
                modifier = modifier,
                containerColor = containerColor,
                items = items,
                content = content,
            )

            NomiNavigationSuiteType.Rail -> RailNavigationSuite(
                modifier = modifier,
                containerColor = containerColor,
                items = items,
                footerItems = footerItems,
                content = content,
            )

            NomiNavigationSuiteType.Drawer -> DrawerNavigationSuite(
                modifier = modifier,
                containerColor = containerColor,
                items = items,
                footerItems = footerItems,
                content = content,
            )
        }
    }
}

/**
 * 底部导航布局：内容在上，底部栏占满宽度并固定在内容下方。
 *
 * 底部栏只渲染主导航项，复刻既有底部导航在分组占位项之前终止的行为；调用方声明的底部组
 * 不在 Bar 中展示，由 Rail 与 Drawer 的垂直导航栏承载。
 *
 * @param modifier 应用于纵向容器的修饰符。
 * @param containerColor 底部导航栏容器色；Unspecified 回退 Nomi surfaceLow。
 * @param items 主导航项声明，按调用顺序等宽渲染。
 * @param content 占满剩余高度的页面内容。
 */
@Composable
private fun BarNavigationSuite(
    modifier: Modifier,
    containerColor: Color,
    items: @Composable NomiNavigationItemScope.() -> Unit,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier) {
        Box(modifier = Modifier.weight(1f, fill = false)) {
            content()
        }
        NavigationBar(
            modifier = Modifier
                .fillMaxWidth()
                .height(NomiTheme.sizes.componentHeights.bottomBar),
            containerColor = containerColor.takeOrElse { NomiTheme.colorScheme.surfaceLow },
        ) {
            val scope = BarItemScope(rowScope = this)
            items(scope)
        }
    }
}

/**
 * 侧边导航布局：导航栏固定在起始侧，内容占满其余空间。
 *
 * 主导航项从栏顶开始排列，底部导航项被推至栏底；两组的相对顺序由调用方声明决定。
 *
 * @param modifier 应用于横向容器的修饰符。
 * @param containerColor 侧边导航栏容器色；Unspecified 回退 Nomi surfaceLow。
 * @param items 主导航项声明。
 * @param footerItems 底部导航项声明，固定到栏底。
 * @param content 导航栏右侧的页面内容。
 */
@Composable
private fun RailNavigationSuite(
    modifier: Modifier,
    containerColor: Color,
    items: @Composable NomiNavigationItemScope.() -> Unit,
    footerItems: @Composable NomiNavigationItemScope.() -> Unit,
    content: @Composable () -> Unit,
) {
    Row(modifier = modifier) {
        NavigationRail(
            modifier = Modifier
                .width(RailWidth)
                .fillMaxHeight(),
            containerColor = containerColor.takeOrElse { NomiTheme.colorScheme.surfaceLow },
        ) {
            items(RailItemScope)
            Spacer(modifier = Modifier.weight(1f))
            footerItems(RailItemScope)
        }
        content()
    }
}

/**
 * 抽屉导航布局：永久抽屉固定在起始侧，主导航项从抽屉顶部排列，底部导航项推至抽屉底部。
 *
 * 抽屉宽度与排布沿用 Material 3 [PermanentNavigationDrawer] 的原生实现，容器色读取 Nomi surfaceLow；
 * 抽屉不消费窗口 inset，安全区由调用方页面外壳处理（与 Bar/Rail 一致）。
 *
 * @param modifier 应用于原生永久抽屉根节点的修饰符。
 * @param containerColor 抽屉容器色；Unspecified 回退 Nomi surfaceLow。
 * @param items 主导航项声明。
 * @param footerItems 底部导航项声明，通过弹性占位推到抽屉底部。
 * @param content 抽屉右侧的页面内容。
 */
@Composable
private fun DrawerNavigationSuite(
    modifier: Modifier,
    containerColor: Color,
    items: @Composable NomiNavigationItemScope.() -> Unit,
    footerItems: @Composable NomiNavigationItemScope.() -> Unit,
    content: @Composable () -> Unit,
) {
    val spacings = NomiTheme.spacings
    PermanentNavigationDrawer(
        modifier = modifier,
        drawerContent = {
            PermanentDrawerSheet(
                modifier = Modifier.fillMaxHeight(),
                drawerContainerColor = containerColor.takeOrElse { NomiTheme.colorScheme.surfaceLow },
                // 抽屉不消费窗口 inset：安全区由调用方页面外壳处理，避免与 Bar/Rail 不一致的双重内缩。
                windowInsets = WindowInsets(0, 0, 0, 0),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(horizontal = spacings.medium, vertical = spacings.large),
                    verticalArrangement = Arrangement.spacedBy(spacings.extraSmall),
                ) {
                    items(DrawerItemScope)
                    Spacer(modifier = Modifier.weight(1f))
                    footerItems(DrawerItemScope)
                }
            }
        },
        content = content,
    )
}

/**
 * 抽屉布局的项作用域；导航项占满抽屉宽度，图标与文案槽同排，选中时使用圆角底色提示。
 */
private object DrawerItemScope : NomiNavigationItemScope {
    /**
     * 渲染一个抽屉导航项：图标/徽标在起始侧，文案槽在右侧直接渲染。
     *
     * 选中时容器底色读取 [NomiNavigationItemColors.indicatorColor]（Unspecified 回退 primary 的轻量底色），
     * 内容色与 labelLarge 通过 [ProvideNomiContentStyle] 传给槽内容；项级修饰符作用于可选中表面。
     *
     * @param selected 是否为当前选中项。
     * @param onClick 点击回调。
     * @param modifier 应用于可选中表面（导航项根节点）的修饰符。
     * @param icon 图标槽；null 表示不显示图标。
     * @param badge 徽标槽；null 表示不显示徽标。
     * @param label 文案槽；null 表示不显示可见文案。
     * @param colors 稀疏颜色覆盖；读取 contentColor、selectedContentColor 与 indicatorColor。
     * @param interactionSource 可选交互源；null 时由原生选择语义自行创建。
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
        val contentColor = resolveNavigationItemContentColor(selected = selected, colors = colors)
        val containerColor = resolveNavigationItemContainerColor(
            selected = selected,
            indicatorColor = colors.indicatorColor,
        )
        val shape = NomiTheme.shapes.medium
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .clip(shape)
                .selectable(
                    selected = selected,
                    role = Role.Tab,
                    interactionSource = interactionSource,
                    onClick = onClick,
                ),
            color = containerColor,
            shape = shape,
            contentColor = contentColor,
        ) {
            ProvideNomiContentStyle(
                color = contentColor,
                textStyle = NomiTheme.typography.labelLarge,
            ) {
                Row(
                    modifier = Modifier.padding(
                        horizontal = NomiTheme.spacings.large,
                        vertical = NomiTheme.spacings.medium,
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(NomiTheme.spacings.medium),
                ) {
                    NavigationItemIconSlot(icon = icon, badge = badge)
                    label?.invoke()
                }
            }
        }
    }
}

/**
 * 解析导航项内容色：选中项读取 selectedContentColor，未选中项读取 contentColor。
 *
 * @param selected 是否为当前选中项。
 * @param colors 中立稀疏颜色覆盖；Unspecified 回退 Nomi primary / onBackground。
 * @return 应用于导航项表面与槽内容的内容色。
 */
@Composable
private fun resolveNavigationItemContentColor(
    selected: Boolean,
    colors: NomiNavigationItemColors,
): Color {
    val colorScheme = NomiTheme.colorScheme
    return if (selected) {
        colors.selectedContentColor.takeOrElse { colorScheme.primary }
    } else {
        colors.contentColor.takeOrElse { colorScheme.onBackground }
    }
}

/**
 * 解析导航项容器底色：选中项读取 indicatorColor，未选中项保持透明。
 *
 * @param selected 是否为当前选中项。
 * @param indicatorColor 显式指示器色；Unspecified 回退 primary 的轻量底色。
 * @return 导航项容器底色。
 */
@Composable
private fun resolveNavigationItemContainerColor(
    selected: Boolean,
    indicatorColor: Color,
): Color = if (selected) {
    indicatorColor.takeOrElse { NomiTheme.colorScheme.primary.copy(alpha = NavigationSelectedContainerAlpha) }
} else {
    Color.Transparent
}

/**
 * 底部导航栏的项作用域；把每个导航项放进父 Row 并分配等宽份额。
 *
 * @property rowScope 底部导航栏内容所在的 Row 作用域，用于给单个导航项应用 weight。
 */
private class BarItemScope(private val rowScope: RowScope) : NomiNavigationItemScope {
    /**
     * 渲染一个底部导航项，宽度由父 Row 的等宽份额决定。
     *
     * @param selected 是否为当前选中项。
     * @param onClick 点击回调，不维护选中状态或路由。
     * @param modifier 应用于导航项可选中表面的修饰符。
     * @param icon 图标槽；null 表示不显示图标。
     * @param badge 徽标槽；null 表示不显示徽标。
     * @param label 文案槽；null 表示不显示可见文案。
     * @param colors 稀疏颜色覆盖；Unspecified 字段回退到 Nomi primary/onBackground。
     * @param interactionSource 可选交互源；null 时由原生选择语义自行创建。
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
        val contentColor = resolveNavigationItemContentColor(selected = selected, colors = colors)
        Surface(
            modifier = modifier
                .run {
                    rowScope.run { weight(1f) }
                }
                .clip(CircleShape)
                .selectable(
                    selected = selected,
                    role = Role.Tab,
                    interactionSource = interactionSource,
                    onClick = onClick,
                ),
            color = Color.Transparent,
            shape = CircleShape,
            contentColor = contentColor,
        ) {
            ProvideNomiContentStyle(color = contentColor, textStyle = TextStyle.Default) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    NavigationItemIconSlot(icon = icon, badge = badge)
                    label?.invoke()
                }
            }
        }
    }
}

/**
 * 侧边导航栏的项作用域；导航项直接进入父列，不需要额外宽度份额。
 */
private object RailItemScope : NomiNavigationItemScope {
    /**
     * 侧边导航栏中的单个导航项：圆形表面内只排列图标槽，文案槽作为右侧 tooltip 提示。
     *
     * 选中时容器底色读取 [NomiNavigationItemColors.indicatorColor]（回退到 primary 的轻量底色），
     * 未选中保持透明。文案槽不进入可见内容，悬停时由 [TooltipBox] 以原生 tooltip 展示，
     * 不套用 [NomiTheme] 排版；槽位 API 没有启用状态，因此 [NomiNavigationItemColors.disabledContentColor] 在此不生效。
     *
     * @param selected 是否为当前选中项。
     * @param onClick 点击回调。
     * @param modifier 应用于可选中表面（导航项根节点）的修饰符。
     * @param icon 图标槽；null 表示不显示图标。
     * @param badge 徽标槽；null 表示不显示徽标。
     * @param label 文案槽；null 表示不提供 tooltip 提示，不进入可见内容。
     * @param colors 稀疏颜色覆盖；读取 contentColor、selectedContentColor 与 indicatorColor。
     * @param interactionSource 可选交互源；null 时由原生选择语义自行创建。
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
        val contentColor = resolveNavigationItemContentColor(selected = selected, colors = colors)
        val containerColor = resolveNavigationItemContainerColor(
            selected = selected,
            indicatorColor = colors.indicatorColor,
        )
        NavigationItemTooltipBoxSlot(label = label) {
            Surface(
                modifier = modifier
                    .clip(CircleShape)
                    .selectable(
                        selected = selected,
                        role = Role.Tab,
                        interactionSource = interactionSource,
                        onClick = onClick,
                    ),
                color = containerColor,
                shape = CircleShape,
                contentColor = contentColor,
            ) {
                Box(modifier = Modifier.padding(NomiTheme.spacings.medium)) {
                    ProvideNomiContentStyle(color = contentColor, textStyle = TextStyle.Default) {
                        NavigationItemIconSlot(icon = icon, badge = badge)
                    }
                }
            }
        }
    }
}

/**
 * 文案槽的 tooltip 包装：有文案时用 Material 3 [TooltipBox] 在导航项右侧提示，无文案时直接渲染内容。
 *
 * 提示内容只在悬停展开时组合，不进入常驻可见语义树；排版沿用原生 tooltip。
 *
 * @param label 文案槽；null 表示不提供提示，直接渲染内容。
 * @param content 导航项可见内容，保持原有选择语义与项级修饰符。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NavigationItemTooltipBoxSlot(
    label: @Composable (() -> Unit)?,
    content: @Composable () -> Unit,
) {
    if (label != null) {
        TooltipBox(
            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Right),
            tooltip = { PlainTooltip { label() } },
            state = rememberTooltipState(),
            content = content
        )
    } else {
        content()
    }
}

/**
 * 导航项图标槽的统一入口：有徽标槽时用 Material 3 [BadgedBox] 叠加，否则直接渲染图标槽。
 *
 * 图标尺寸由调用方在槽内约束；实现不插入尺寸或内边距，避免覆盖调用方的槽布局。
 *
 * @param icon 图标槽；null 表示不显示图标。
 * @param badge 徽标槽；null 表示不显示徽标。
 */
@Composable
private fun NavigationItemIconSlot(
    icon: @Composable (() -> Unit)?,
    badge: @Composable (() -> Unit)?,
) {
    if (badge != null) {
        BadgedBox(badge = { badge() }) {
            icon?.invoke()
        }
    } else if (icon != null) {
        icon()
    }
}