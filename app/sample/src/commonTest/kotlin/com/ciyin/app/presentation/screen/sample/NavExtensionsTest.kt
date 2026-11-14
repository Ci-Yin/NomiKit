package com.ciyin.app.presentation.screen.sample

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * 验证样例子导航栈的扩展函数始终保留栈底根页，避免 Navigation3 的空 backstack 异常。
 */
class NavExtensionsTest {

    /**
     * 处于样例根页时 back() 不弹出根页，子导航栈保持非空。
     */
    @Test
    fun backKeepsSampleHubRoot() {
        val sampleBackStack = NavBackStack<NavKey>(SampleHubRouter)

        sampleBackStack.back()

        assertEquals(expected = listOf<NavKey>(SampleHubRouter), actual = sampleBackStack.toList())
    }

    /**
     * 处于样例子页面时 back() 回到样例根页。
     */
    @Test
    fun backPopsToSampleHubRoot() {
        val sampleBackStack = NavBackStack<NavKey>(SampleHubRouter, DesignSystemDemoRouter)

        sampleBackStack.back()

        assertEquals(expected = listOf<NavKey>(SampleHubRouter), actual = sampleBackStack.toList())
    }

    /**
     * 再次点击样例根页时不新增页面，并清空其上的子页面。
     */
    @Test
    fun navigateWithSingleTopReturnsToSampleHubRoot() {
        val sampleBackStack = NavBackStack<NavKey>(SampleHubRouter, DesignSystemDemoRouter)

        sampleBackStack.navigateWithSingleTop(SampleHubRouter)

        assertEquals(expected = listOf<NavKey>(SampleHubRouter), actual = sampleBackStack.toList())
    }
}
