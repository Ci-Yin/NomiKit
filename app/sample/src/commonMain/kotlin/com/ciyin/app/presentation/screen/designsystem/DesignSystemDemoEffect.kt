package com.ciyin.app.presentation.screen.designsystem

import androidx.compose.runtime.Immutable

/**
 * 展示页一次性副作用，仅交给导航宿主执行。
 */
@Immutable
internal sealed interface DesignSystemDemoEffect {
    /**
     * 返回样例列表，不把导航命令存入页面状态。
     */
    data object NavigateBack : DesignSystemDemoEffect
}
