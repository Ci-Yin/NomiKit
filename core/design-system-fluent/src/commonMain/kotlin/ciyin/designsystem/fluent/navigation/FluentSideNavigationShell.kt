package ciyin.designsystem.fluent.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import io.github.composefluent.ExperimentalFluentApi
import io.github.composefluent.FluentTheme
import io.github.composefluent.background.Layer
import io.github.composefluent.component.MenuItem
import io.github.composefluent.component.NavigationDisplayMode
import io.github.composefluent.component.NavigationMenuItemScope
import io.github.composefluent.component.SideNav
import io.github.composefluent.component.SideNavItem

/**
 * Fluent 左置导航外壳：主导航项交给原生 SideNav，底部项由适配层紧贴在 SideNav 下方自绘。
 *
 * 上游 NavigationView 的 Left 布局会在原生 footer 上方固定插入一条 1dp 分割线、并给内容面板
 * 加默认描边，两者都没有关闭入口；这里按同一套公开组件重排：SideNav 负责主导航与折叠状态，
 * 底部项沿用原生 SideNavItem 的配色与交互，内容面板使用无描边的原生 Layer。
 */
@Composable
internal fun FluentSideNavigationShell(
    modifier: Modifier,
    paneColor: Color,
    mainItems: List<FluentNavigationItemDeclaration>,
    footerItems: List<FluentNavigationItemDeclaration>,
    pane: @Composable () -> Unit,
) {
    var expanded by remember { mutableStateOf(true) }
    var sideNavWidthPx by remember { mutableIntStateOf(0) }
    Row(modifier = modifier) {
        Column(modifier = Modifier.fillMaxHeight()) {
            SideNav(
                expanded = expanded,
                onExpandStateChange = { expanded = it },
                modifier = Modifier
                    .weight(1f)
                    .onSizeChanged { sideNavWidthPx = it.width },
                footer = null,
            ) {
                val itemScope = SideNavigationItemScope(
                    displayMode = NavigationDisplayMode.Left,
                    isFooter = false
                )
                mainItems.forEach { item ->
                    with(itemScope) { FluentMenuItem(item) }
                }
            }
            val footerWidth = with(LocalDensity.current) { sideNavWidthPx.toDp() }
            Column(modifier = Modifier.width(footerWidth).fillMaxWidth()) {
                footerItems.forEach { item -> item.FluentFooterItem(expanded = expanded) }
            }
            Spacer(Modifier.height(4.dp))
        }
        Box(modifier = Modifier.weight(1f)) {
            Layer(
                color = paneColor,
                border = null,
                shape = RoundedCornerShape(topStart = FluentTheme.cornerRadius.overlay),
                content = pane,
            )
        }
    }
}

/**
 * 原生 SideNav 菜单项所需的组合作用域；左置导航固定为非底部项。
 *
 * @property displayMode 原生导航显示模式，决定项配色与指示器方向。
 * @property isFooter 是否为底部区域项；本外壳只为主导航项提供作用域。
 */
private class SideNavigationItemScope(
    override val displayMode: NavigationDisplayMode,
    override val isFooter: Boolean,
) : NavigationMenuItemScope

/**
 * 在原生 SideNav 的菜单环境中渲染一个主导航项。
 *
 * @receiver 原生导航菜单项作用域。
 */
@Composable
private fun NavigationMenuItemScope.FluentMenuItem(declaration: FluentNavigationItemDeclaration) {
    MenuItem(
        selected = declaration.selected,
        onClick = { declaration.onClick() },
        text = { declaration.label?.invoke() },
        icon = declaration.icon,
        badge = declaration.badge,
        colors = resolveFluentItemColors(
            displayMode = NavigationDisplayMode.Left,
            selected = declaration.selected,
            overrides = declaration.colors,
        ),
        interactionSource = declaration.interactionSource,
    )
}

/**
 * 渲染一个底部导航项；侧边折叠时与主导航项一样只保留图标并通过原生 tooltip 提示文案。
 */
@OptIn(ExperimentalFluentApi::class)
@Composable
private fun FluentNavigationItemDeclaration.FluentFooterItem(expanded: Boolean) {
    SideNavItem(
        selected = selected,
        onSelectedChanged = { onClick() },
        indicatorState = null,
        expand = expanded,
        colors = resolveFluentItemColors(
            displayMode = NavigationDisplayMode.Left,
            selected = selected,
            overrides = colors,
        ),
        icon = icon,
        badge = badge,
        interactionSource = interactionSource,
        text = { label?.invoke() },
    )
}
