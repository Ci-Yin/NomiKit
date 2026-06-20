package com.ciyin.app.ui.app.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItemColors
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import ciyin.material.theme.AppTheme
import com.ciyin.app.ui.app.navigation.NavigationSuiteType.NavigationBar
import com.ciyin.app.ui.app.navigation.NavigationSuiteType.NavigationDrawer
import com.ciyin.app.ui.app.navigation.NavigationSuiteType.NavigationRail
import com.ciyin.app.ui.component.WindowType
import com.ciyin.app.ui.component.windowAdaptive


/**
 *
 * kotlin文件作用描述
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2024/10/7 下午10:38
 */

/**
 * 定义导航抽屉的宽度为240.dp
 */
val NavigationDrawerWidth = 240.dp

/**
 * 定义导航栏的宽度为60.dp
 */
val NavigationRailWidth = 60.dp

/**
 * 定义底部导航栏的高度为50.dp
 */
val BottomNavigationHeight = 50.dp

/**
 * 根据窗口类型显示底部导航或侧边导航的应用导航容器。
 *
 * @param selection 当前选中的导航项索引
 * @param navList 导航项列表
 * @param onNavigateItemClick 导航项点击回调
 * @param modifier 容器修饰符
 * @param content 被导航容器包裹的页面内容
 */
@Composable
fun NavigationBar(
    selection: Int,
    navList: List<NavUiItem>,
    onNavigateItemClick: (NavUiItem) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) = Box(modifier) {

    val navLayoutType = when (windowAdaptive) {
        WindowType.PHONE -> NavigationBar
        WindowType.TABLET, WindowType.PHONE_HORIZONTAL -> NavigationRail
        else -> NavigationRail
    }

    NavigationSuiteScaffoldLayout(
        layoutType = navLayoutType, navigationSuite = {
            when (navLayoutType) {
                NavigationBar -> {
                    BottomNavigationBar(
                        selection = selection,
                        navList = navList,
                        onNavigateItemClick = onNavigateItemClick
                    )
                }

                NavigationRail -> {
                    NavigationRail(
                        selection = selection,
                        navList = navList,
                        onNavigateItemClick = onNavigateItemClick,
                    )
                }

                NavigationDrawer -> {
                    // TODO: 实现 NavigationDrawer
                }
            }
        },
        content = content
    )

}

/**
 * 按导航布局类型排列导航组件与页面内容。
 *
 * @param layoutType 当前导航布局类型
 * @param navigationSuite 导航组件内容
 * @param content 页面内容
 */
@Composable
private fun NavigationSuiteScaffoldLayout(
    layoutType: NavigationSuiteType,
    navigationSuite: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    when (layoutType) {
        NavigationBar -> {
            Column {
                Box(Modifier.weight(1f, false)) {
                    content()
                }
                navigationSuite()
            }
        }

        NavigationRail, NavigationDrawer -> {
            Row {
                navigationSuite()
                content()
            }
        }

    }
}


/**
 * 侧边导航栏。
 *
 * @param selection 当前选中的导航项索引
 * @param navList 导航项列表
 * @param onNavigateItemClick 导航项点击回调
 * @param modifier 列表修饰符
 * @param visible 是否显示导航栏
 */
@Composable
private fun NavigationRail(
    selection: Int,
    navList: List<NavUiItem>,
    onNavigateItemClick: (NavUiItem) -> Unit,
    modifier: Modifier = Modifier,
    visible: Boolean = true,
) {
    if (!visible) {
        return
    }
    NavigationRail(
        modifier = Modifier
            .width(NavigationRailWidth)
            .fillMaxHeight(),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        // 导航菜单
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacings.tiny)
        ) {
            navList.forEachIndexed { index, nav ->
                if (nav.id == NavId.Null) {
                    Spacer(Modifier.weight(1f))
                } else {
                    NavigationRailItem(
                        selected = index == selection,
                        onClick = { onNavigateItemClick(nav) },
                        icon = rememberVectorPainter(nav.icon),
                    )
                }
            }
        }
    }
}

/**
 * 底部导航栏。
 *
 * @param selection 当前选中的导航项索引
 * @param navList 导航项列表
 * @param onNavigateItemClick 导航项点击回调
 * @param modifier 导航栏修饰符
 * @param visible 是否显示导航栏
 */
@Composable
private fun BottomNavigationBar(
    selection: Int,
    navList: List<NavUiItem>,
    onNavigateItemClick: (NavUiItem) -> Unit,
    modifier: Modifier = Modifier,
    visible: Boolean = true,
) {
    if (!visible) {
        return
    }
    androidx.compose.material3.NavigationBar(
        modifier = modifier
            .height(BottomNavigationHeight)
            .fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        for ((index, nav) in navList.withIndex()) {
            if (nav.id == NavId.Null) break
            NavigationBarItem(
                icon = rememberVectorPainter(nav.icon),
                label = nav.title,
                selected = index == selection,
                onClick = { onNavigateItemClick(nav) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/**
 * 底部导航栏中的单个导航项。
 *
 * @param icon 导航图标
 * @param label 导航文案
 * @param selected 是否处于选中状态
 * @param onClick 点击回调
 * @param modifier 导航项修饰符
 * @param enabled 是否可点击
 * @param colors 导航项颜色配置
 */
@Composable
private fun NavigationBarItem(
    icon: Painter,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: NavigationBarItemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = AppTheme.colorScheme.primary,
        selectedTextColor = AppTheme.colorScheme.primary,
        unselectedIconColor = AppTheme.colorScheme.onBackground,
        unselectedTextColor = AppTheme.colorScheme.onBackground,
    ),
) = Box(
    modifier = modifier,
    contentAlignment = Alignment.Center,
) {
    Surface(
        modifier = Modifier.width(80.dp),
        color = Color.Transparent,
        shape = CircleShape,
        contentColor = if (selected) colors.selectedIconColor else colors.unselectedIconColor,
        enabled = enabled,
        onClick = onClick
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                modifier = Modifier
                    .padding(top = AppTheme.spacings.tiny)
                    .size(AppTheme.sizes.icon.large),
                painter = icon,
                contentDescription = null,
            )
            Text(
                text = label,
                style = AppTheme.typography.labelSmall.copy(
                    fontSize = AppTheme.typography.labelSmall.fontSize * 0.9f
                ),
            )
        }
    }
}

/**
 * 侧边导航栏中的单个导航项。
 *
 * @param selected 是否处于选中状态
 * @param icon 导航图标
 * @param onClick 点击回调
 * @param modifier 导航项修饰符
 * @param enabled 是否可点击
 * @param colors 导航项颜色配置
 */
@Composable
private fun NavigationRailItem(
    selected: Boolean,
    icon: Painter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: NavigationRailItemColors = NavigationRailItemDefaults.colors(
        selectedIconColor = AppTheme.colorScheme.primary.copy(alpha = 0.1f),
        unselectedIconColor = Color.Transparent,
    ),
) = Surface(
    modifier = modifier,
    selected = selected,
    color = if (selected) colors.selectedIconColor else colors.unselectedIconColor,
    shape = RoundedCornerShape(20),
    contentColor = AppTheme.colorScheme.onBackground.copy(alpha = 0.8f),
    enabled = enabled,
    onClick = onClick
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            modifier = Modifier
                .padding(AppTheme.spacings.medium)
                .size(AppTheme.sizes.icon.medium),
            painter = icon,
            contentDescription = null,
        )
    }
}
