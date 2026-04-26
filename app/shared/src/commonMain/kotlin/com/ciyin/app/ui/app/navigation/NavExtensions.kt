package com.ciyin.app.ui.app.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

/**
 * 有条件地导航到新页面。
 *
 * @param navKey 要导航到的路由键
 * @param predicate 导航条件，默认允许所有导航。如果返回 false，则不执行导航
 */
fun <T : NavKey> NavBackStack<NavKey>.navigate(
    navKey: T,
    predicate: NavBackStack<NavKey>.(NavKey) -> Boolean = { true },
) {
    if (predicate(navKey)) add(navKey)
}

/**
 * 从导航栈返回上一页。
 * 移除栈顶元素，如果栈为空则不执行任何操作。
 */
fun NavBackStack<NavKey>.back() {
    removeLastOrNull()
}

/**
 * 导航到指定路由，使用 SingleTop 模式。
 */
fun NavBackStack<NavKey>.navigateWithSingleTop(route: NavKey) {
    if (lastOrNull() == route) return

    val startRoute = firstOrNull()

    if (route == startRoute) {
        while (size > 1) {
            removeLastOrNull()
        }
        return
    }

    val stackList = toList()
    val index = stackList.indexOfLast { it == route }

    if (index >= 0) {
        val removeCount = stackList.size - index
        repeat(removeCount) {
            removeLastOrNull()
        }
    }

    add(route)
}
