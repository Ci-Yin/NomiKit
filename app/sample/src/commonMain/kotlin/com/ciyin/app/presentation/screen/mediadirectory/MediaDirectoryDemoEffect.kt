package com.ciyin.app.presentation.screen.mediadirectory

/** 用户媒体目录示例的 Effect 集合。 */
internal sealed interface MediaDirectoryDemoEffect {
    /** 通知宿主返回 sample 首页。 */
    data object NavigateBack : MediaDirectoryDemoEffect
}
