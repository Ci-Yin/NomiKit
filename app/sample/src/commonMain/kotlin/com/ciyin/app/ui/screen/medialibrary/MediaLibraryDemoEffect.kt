package com.ciyin.app.ui.screen.medialibrary

import androidx.compose.runtime.Immutable

/** 系统媒体库示例页面一次性副作用。 */
@Immutable
internal sealed interface MediaLibraryDemoEffect {
    /** 返回 sample 首页。 */
    data object NavigateBack : MediaLibraryDemoEffect
}
