package com.ciyin.app.ui.screen.platformshare

/**
 * 系统分享示例页一次性副作用。
 */
internal sealed interface PlatformShareDemoEffect {
    /**
     * 返回上一页。
     */
    data object NavigateBack : PlatformShareDemoEffect
}
