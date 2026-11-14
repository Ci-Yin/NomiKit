package com.ciyin.app.presentation.screen.sample

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * 验证展示页路由已登记到真实导航恢复配置。
 */
class DesignSystemDemoNavigationTest {
    /**
     * 以导航接口序列化并还原新页面，覆盖多态注册而非仅具体类型。
     */
    @Test
    fun designSystemRouteSurvivesSavedStateRoundTrip() {
        val savedState = encodeToSavedState<NavKey>(
            value = DesignSystemDemoRouter,
            configuration = NavSavedStateConfig,
        )
        val restored = decodeFromSavedState<NavKey>(
            savedState = savedState,
            configuration = NavSavedStateConfig,
        )
        assertEquals(DesignSystemDemoRouter, restored)
    }
}
