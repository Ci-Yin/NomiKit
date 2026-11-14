package ciyin.designsystem.nomi.navigation

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
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
 * 侧边导航项选中时的容器底色透明度；只在 primary 上叠加，保留原生导航的轻量选中提示。
 */
private const val RailSelectedContainerAlpha = 0.1f

/**
 * Nomi 原生导航套件实现，复刻 Nomi 自身底部导航栏与侧边导航栏的视觉形态。
 *
 * 图标、徽标与文案是调用方组合的槽位，实现只负责把它们放进原生导航项；图标尺寸与无障碍名称
 * 由槽内容负责。颜色与排版经 [NomiTheme] 读取，槽内容通过 [ProvideNomiContentStyle] 继承导航项
 * 内容色，因此 NomiText 槽能随选中状态变色。
 * 导航项级 [Modifier] 应用到可选中表面，交互源直接交给原生选择语义；
 * Drawer 布局由能力声明禁用，此处仅保留防御性失败。
 */
internal object DefaultNomiNavigationRenderer : NomiNavigationRenderer {
    /**
     * 按布局类型建立 Nomi 原生导航外壳。
     *
     * @param layoutType 调用方选择的导航布局；当前只支持 Bar 与 Rail。
     * @param modifier 应用于导航外壳根节点的修饰符；Bar 作用于纵向容器，Rail 作用于横向容器。
     * @param items 主导航项声明，按调用顺序渲染。
     * @param footerItems 底部导航项声明；Bar 与主导航项同排渲染，Rail 固定到栏底。
     * @param content 页面内容，与导航外壳共同完成排布。
     * @throws IllegalStateException [NomiNavigationSuiteType.Drawer] 传入时抛出；统一入口已按能力声明提前失败，此处只作为直接调用 renderer 的防御。
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
            NomiNavigationSuiteType.Bar -> BarNavigationSuite(
                modifier = modifier,
                items = items,
                footerItems = footerItems,
                content = content,
            )

            NomiNavigationSuiteType.Rail -> RailNavigationSuite(
                modifier = modifier,
                items = items,
                footerItems = footerItems,
                content = content,
            )

            NomiNavigationSuiteType.Drawer -> error("nomi 不支持 Drawer 导航布局")
        }
    }
}

/**
 * 底部导航布局：内容在上，底部栏占满宽度并固定在内容下方。
 *
 * 主导航项与底部导航项按声明顺序渲染在同一条导航栏中，不合并、不丢弃分组。
 *
 * @param modifier 应用于纵向容器的修饰符。
 * @param items 主导航项声明。
 * @param footerItems 底部导航项声明，与主导航项同排渲染。
 * @param content 占满剩余高度的页面内容。
 */
@Composable
private fun BarNavigationSuite(
    modifier: Modifier,
    items: @Composable NomiNavigationItemScope.() -> Unit,
    footerItems: @Composable NomiNavigationItemScope.() -> Unit,
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
            containerColor = NomiTheme.colorScheme.surfaceLow,
        ) {
            val scope = BarItemScope(rowScope = this)
            items(scope)
            footerItems(scope)
        }
    }
}

/**
 * 侧边导航布局：导航栏固定在起始侧，内容占满其余空间。
 *
 * 主导航项从栏顶开始排列，底部导航项被推至栏底；两组的相对顺序由调用方声明决定。
 *
 * @param modifier 应用于横向容器的修饰符。
 * @param items 主导航项声明。
 * @param footerItems 底部导航项声明，固定到栏底。
 * @param content 导航栏右侧的页面内容。
 */
@Composable
private fun RailNavigationSuite(
    modifier: Modifier,
    items: @Composable NomiNavigationItemScope.() -> Unit,
    footerItems: @Composable NomiNavigationItemScope.() -> Unit,
    content: @Composable () -> Unit,
) {
    Row(modifier = modifier) {
        NavigationRail(
            modifier = Modifier
                .width(RailWidth)
                .fillMaxHeight(),
            containerColor = NomiTheme.colorScheme.surfaceLow,
        ) {
            items(RailItemScope)
            Spacer(modifier = Modifier.weight(1f))
            footerItems(RailItemScope)
        }
        content()
    }
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
        val colorScheme = NomiTheme.colorScheme
        val contentColor = if (selected) {
            colors.selectedContentColor.takeOrElse { colorScheme.primary }
        } else {
            colors.contentColor.takeOrElse { colorScheme.onBackground }
        }
        Box(
            modifier = rowScope.run { Modifier.weight(1f) },
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                modifier = modifier.selectable(
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
                        if (label != null) {
                            label()
                        }
                    }
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
        val colorScheme = NomiTheme.colorScheme
        val contentColor = if (selected) {
            colors.selectedContentColor.takeOrElse { colorScheme.primary }
        } else {
            colors.contentColor.takeOrElse { colorScheme.onBackground }
        }
        val containerColor = if (selected) {
            colors.indicatorColor.takeOrElse { colorScheme.primary.copy(alpha = RailSelectedContainerAlpha) }
        } else {
            Color.Transparent
        }
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
