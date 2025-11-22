package com.ciyin.app.ui.app.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
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
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
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

fun NavHostController.navigateTo(route: String) {
    if (currentDestination?.route == route) return
    navigate(route) {
        launchSingleTop = true
        if (route == graph.findStartDestination().route) {
            popUpTo(route)
        }
    }
}

/**
 * 类型安全的导航扩展函数
 */
inline fun <reified T : NavRouter> NavHostController.navigateTo(
    route: T,
    crossinline builder: NavOptionsBuilder.() -> Unit = {}
) {
    // 检查当前目的地是否已经是目标路由
    if (currentDestination?.route == route::class.qualifiedName) return

    navigate(route) {
        launchSingleTop = true

        // 如果是起始目的地，清空回退栈
        val startDestRoute = graph.findStartDestination().route
        if (route::class.qualifiedName == startDestRoute && startDestRoute != null) {
            popUpTo(startDestRoute) {
                inclusive = false
            }
        }

        // 应用额外的导航配置
        builder()
    }
}

@Composable
fun NavigationBar(
    selection: Int,
    navList: List<NavUiItem>,
    onNavigateItemClick: (NavUiItem) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) = Row(modifier) {

    val navLayoutType = when (windowAdaptive) {
        WindowType.PHONE -> NavigationBar
        WindowType.TABLET, WindowType.PHONE_HORIZONTAL -> NavigationRail
        else -> NavigationRail
        //else -> NavigationDrawer
    }

    NavigationSuiteScaffoldLayout(
        layoutType = navLayoutType,
        navigationSuite = {
            when (navLayoutType) {
                NavigationBar -> {
                    BottomNavigationBar(
                        selection = selection,
                        navList = navList,
                        onNavigateItemClick = onNavigateItemClick
                    )
                }

                NavigationRail -> NavigationRail(
                    selection = selection,
                    navList = navList,
                    onNavigateItemClick = onNavigateItemClick,
                )

                /*NavigationSuiteType.NavigationDrawer -> WaliNavigationDrawer(
                    selection = selection,
                    navList = navList,
                    onNavigateItemClick = onNavigateItemClick
                )*/
                NavigationDrawer -> {}
            }
        },
        content = content
    )

}

@Composable
private fun NavigationSuiteScaffoldLayout(
    layoutType: NavigationSuiteType,
    navigationSuite: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    when (layoutType) {
        NavigationBar -> {
            Column {
                content()
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


        // 用户头像
        /*NavigationUserAvatar(70.dp, Modifier.thenIf(isTabletopWindow) {
        padding(vertical = 12.dp)
    })*/

        // 导航菜单
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            for (nav in navList) {
                if (nav.id == NavId.Null) {
                    Spacer(Modifier.weight(1f))
                } else {
                    WaliNavigationRailItem(
                        selected = navList.indexOf(nav) == selection,
                        onClick = { onNavigateItemClick(nav) },
                        icon = rememberVectorPainter(nav.icon),
                    )
                }
            }
        }
    }
}

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
    NavigationBar(
        modifier = modifier
            .navigationBarsPadding()
            .height(BottomNavigationHeight)
            .fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        for (nav in navList) {
            if (nav.id == NavId.Null) break
            WaliNavigationBarItem(
                label = nav.title,
                icon = rememberVectorPainter(nav.icon),
                selected = navList.indexOf(nav) == selection,
                onClick = { onNavigateItemClick(nav) },
            )
        }
    }
}

@Composable
private fun RowScope.WaliNavigationBarItem(
    icon: Painter,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: NavigationBarItemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = MaterialTheme.colorScheme.primary,
        selectedTextColor = MaterialTheme.colorScheme.primary,
        unselectedIconColor = MaterialTheme.colorScheme.onBackground,
        unselectedTextColor = MaterialTheme.colorScheme.onBackground,
    ),
) = Box(
    modifier = modifier.weight(1f),
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
                    .padding(top = 2.dp)
                    .size(24.dp),
                painter = icon,
                contentDescription = null,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = MaterialTheme.typography.labelSmall.fontSize * 0.9f
                ),
            )
        }
    }
}

@Composable
private fun WaliNavigationRailItem(
    selected: Boolean,
    icon: Painter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: NavigationRailItemColors = NavigationRailItemDefaults.colors(
        selectedIconColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
        unselectedIconColor = Color.Transparent,
    ),
) = Surface(
    modifier = modifier,
    selected = selected,
    color = if (selected) colors.selectedIconColor else colors.unselectedIconColor,
    shape = RoundedCornerShape(20),
    contentColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
    enabled = enabled,
    onClick = onClick
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            modifier = Modifier
                .padding(10.dp)
                .size(20.dp),
            painter = icon,
            contentDescription = null,
        )
    }
}