package com.ciyin.app.ui.screen.medialibrary

import ciyin.media.library.MediaLibraryError
import ciyin.media.library.PublishedMedia

/** 将 feature 媒体引用映射为稳定页面模型。 */
internal fun PublishedMedia.toDemoModel(): MediaLibraryDemoPublishedModel =
    MediaLibraryDemoPublishedModel(
        platformId = platformId,
        uri = uri,
        displayName = displayName,
        mimeType = mimeType,
        size = size,
    )

/** 将媒体库技术错误映射为页面错误分类。 */
internal fun MediaLibraryError.toDemoModel(): MediaLibraryDemoErrorModel =
    MediaLibraryDemoErrorModel(
        type = when (this) {
            is MediaLibraryError.NotFound -> MediaLibraryDemoErrorType.NotFound
            is MediaLibraryError.AlreadyExists -> MediaLibraryDemoErrorType.AlreadyExists
            is MediaLibraryError.PermissionDenied -> MediaLibraryDemoErrorType.PermissionDenied
            is MediaLibraryError.NoSpace -> MediaLibraryDemoErrorType.NoSpace
            is MediaLibraryError.Unsupported -> MediaLibraryDemoErrorType.Unsupported
            is MediaLibraryError.Io -> MediaLibraryDemoErrorType.Io
        },
    )
