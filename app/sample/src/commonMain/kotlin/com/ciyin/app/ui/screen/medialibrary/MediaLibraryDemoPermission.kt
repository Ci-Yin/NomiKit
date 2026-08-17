package com.ciyin.app.ui.screen.medialibrary

import ciyin.platform.Context
import ciyin.media.library.MediaCollection

/** 为指定媒体分类准备当前平台所需权限。 */
internal expect suspend fun ensureMediaLibraryDemoPermission(
    context: Context,
    collection: MediaCollection,
): Boolean
