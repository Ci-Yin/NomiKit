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
 *
 * SingleTop 模式的行为：
 * - 如果目标路由已经在栈顶，则不执行导航
 * - 如果目标路由在栈中（但不在栈顶），则移除从栈顶到该路由的所有元素（包括该路由），然后添加到栈顶
 * - 如果目标路由是起始目的地，则清空回退栈（保留起始目的地）
 *
 * @param route 要导航到的路由
 */
fun NavBackStack<NavKey>.navigateWithSingleTop(route: NavKey) {
    // 如果当前目的地已经是目标路由，直接返回
    if (lastOrNull() == route) return

    val startRoute = firstOrNull()

    // 如果是起始目的地，清空回退栈（保留起始目的地）
    if (route == startRoute) {
        // 移除所有元素直到只剩下起始目的地
        while (size > 1) {
            removeLastOrNull()
        }
        return
    }

    // 使用 singleTop 模式：如果栈中已存在该路由，先移除它
    // 从栈顶向下查找匹配的路由（索引从后向前）
    val stackList = toList()
    val index = stackList.indexOfLast { it == route }

    if (index >= 0) {
        // 计算需要移除的元素数量（从栈顶到该位置，包括该路由）
        val removeCount = stackList.size - index
        repeat(removeCount) {
            removeLastOrNull()
        }
    }

    // 添加到栈顶
    add(route)
}

