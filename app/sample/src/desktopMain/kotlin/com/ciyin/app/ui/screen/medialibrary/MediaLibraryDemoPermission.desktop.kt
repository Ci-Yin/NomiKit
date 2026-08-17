package com.ciyin.app.ui.screen.medialibrary

import ciyin.media.library.MediaCollection
import ciyin.platform.Context

/** Desktop 没有媒体库运行时权限，目录权限由发布操作统一报告。 */
@Suppress("UNUSED_PARAMETER")
internal actual suspend fun ensureMediaLibraryDemoPermission(
    context: Context,
    collection: MediaCollection,
): Boolean = true
