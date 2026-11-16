package com.ciyin.app.presentation.app.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ciyin.designsystem.component.navigation.NomiNavigationSuite
import ciyin.designsystem.component.navigation.NomiNavigationSuiteType
import ciyin.designsystem.component.text.NomiText
import ciyin.designsystem.theme.NomiTheme
import com.ciyin.app.presentation.component.WindowType
import com.ciyin.app.presentation.component.windowAdaptive
import com.ciyin.app.presentation.component.windowBackgroundColor

/**
 * 应用主导航容器：按窗口形态选择中立导航布局，并把导航数据拆分为主导航与底部导航两组。
 *
 * 路由、选中状态与导航项数据仍由调用方持有；本函数只做窗口适配、数据分组映射与槽位内容组合。
 *
 * @param selection 当前选中的导航项在完整导航列表中的索引；-1 表示没有选中项。
 * @param navList 完整导航项列表；第一个空占位项之后的内容进入底部导航组。
 * @param onNavigateItemClick 导航项点击回调，不负责路由跳转。
 * @param modifier 应用于导航外壳根节点的修饰符。
 * @param content 被导航外壳包裹的页面内容。
 */
@Composable
fun NavigationBar(
    selection: Int,
    navList: List<NavUiItem>,
    onNavigateItemClick: (NavUiItem) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val layoutType = when (windowAdaptive) {
        WindowType.PHONE -> NomiNavigationSuiteType.Bar
        WindowType.TABLET, WindowType.PHONE_HORIZONTAL -> NomiNavigationSuiteType.Rail
        else -> NomiNavigationSuiteType.Drawer
    }
    // Bar 与 Drawer 直接渲染文案，Rail 只显示图标并把文案作为 tooltip / 无障碍名称。
    val showsLabel = layoutType == NomiNavigationSuiteType.Bar ||
            layoutType == NomiNavigationSuiteType.Drawer
    val splitIndex = navList.indexOfFirst { it.id == NavId.Null }
    val items = if (splitIndex < 0) navList else navList.take(splitIndex)
    val footerItems = if (splitIndex < 0) emptyList() else navList.drop(splitIndex + 1)
    println("items: ${items.size}, footerItems: ${footerItems.size}")
    NomiNavigationSuite(
        modifier = modifier,
        layoutType = layoutType,
        containerColor = windowBackgroundColor(NomiTheme.colorScheme.surfaceLow),
        items = {
            items.forEach { nav ->
                NavigationItem(
                    selected = navList.indexOf(nav) == selection,
                    onClick = { onNavigateItemClick(nav) },
                    icon = {
                        Icon(
                            imageVector = nav.icon,
                            contentDescription = if (showsLabel) null else nav.title,
                            modifier = Modifier.size(NomiTheme.sizes.icon.large),
                        )
                    },
                    label = { NomiText(text = nav.title) }
                )
            }
        },
        footerItems = {
            footerItems.forEach { nav ->
                NavigationItem(
                    selected = navList.indexOf(nav) == selection,
                    onClick = { onNavigateItemClick(nav) },
                    icon = {
                        Icon(
                            modifier = Modifier.size(NomiTheme.sizes.icon.large),
                            imageVector = nav.icon,
                            contentDescription = if (showsLabel) null else nav.title,
                        )
                    },
                    label = { NomiText(text = nav.title) }
                )
            }
        },
        content = {
            when (layoutType) {
                NomiNavigationSuiteType.Bar -> Column {
                    content()
                    HorizontalDivider(
                        thickness = NomiTheme.sizes.strokes.thin,
                        color = NomiTheme.colorScheme.divider
                    )
                }

                NomiNavigationSuiteType.Rail,
                NomiNavigationSuiteType.Drawer -> {
                    VerticalDivider(
                        thickness = NomiTheme.sizes.strokes.thin,
                        color = NomiTheme.colorScheme.divider
                    )
                    content()
                }
            }
        },
    )
}
