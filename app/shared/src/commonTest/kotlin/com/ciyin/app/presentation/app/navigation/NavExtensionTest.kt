package com.ciyin.app.presentation.app.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * 验证主页面导航扩展函数始终保留栈底起点，避免 Navigation3 的空 backstack 异常。
 */
class NavExtensionTest {

    /**
     * 处于根路由时 back() 不弹出栈底页面，导航栈保持非空。
     */
    @Test
    fun backKeepsStartDestination() {
        val navBackStack = NavBackStack<NavKey>(MainRouter)

        navBackStack.back()

        assertEquals(expected = listOf<NavKey>(MainRouter), actual = navBackStack.toList())
    }

    /**
     * 有多层页面时 back() 只弹出栈顶页面。
     */
    @Test
    fun backPopsTopDestination() {
        val navBackStack = NavBackStack<NavKey>(MainRouter, SettingRouter)

        navBackStack.back()

        assertEquals(expected = listOf<NavKey>(MainRouter), actual = navBackStack.toList())
    }

    /**
     * 重复点击当前导航项不新增页面。
     */
    @Test
    fun navigateWithSingleTopKeepsSingleEntryForCurrentRoute() {
        val navBackStack = NavBackStack<NavKey>(MainRouter)

        navBackStack.navigateWithSingleTop(MainRouter)

        assertEquals(expected = listOf<NavKey>(MainRouter), actual = navBackStack.toList())
    }

    /**
     * 回到起点路由时清空其上的所有页面。
     */
    @Test
    fun navigateWithSingleTopReturnsToStartRoute() {
        val navBackStack = NavBackStack<NavKey>(MainRouter, SettingRouter)

        navBackStack.navigateWithSingleTop(MainRouter)

        assertEquals(expected = listOf<NavKey>(MainRouter), actual = navBackStack.toList())
    }

    /**
     * 进入未在栈中的路由时追加为新的栈顶页面。
     */
    @Test
    fun navigateWithSingleTopAppendsNewRoute() {
        val navBackStack = NavBackStack<NavKey>(MainRouter)

        navBackStack.navigateWithSingleTop(SettingRouter)

        assertEquals(
            expected = listOf<NavKey>(MainRouter, SettingRouter),
            actual = navBackStack.toList(),
        )
    }
}
