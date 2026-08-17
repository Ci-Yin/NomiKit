package com.ciyin.app.ui.screen.medialibrary

import ciyin.media.library.MediaLibraryError
import ciyin.media.library.PublishedMedia
import kotlin.test.Test
import kotlin.test.assertEquals

/** 系统媒体库示例模型映射测试。 */
class MediaLibraryDemoMapperTest {
    /** 平台媒体引用应逐字段映射到稳定页面模型。 */
    @Test
    fun publishedMediaMapsEveryField() {
        val model = PublishedMedia(
            platformId = "platform-id",
            uri = "content://media/1",
            displayName = "sample.png",
            mimeType = "image/png",
            size = 42L,
        ).toDemoModel()

        assertEquals("platform-id", model.platformId)
        assertEquals("content://media/1", model.uri)
        assertEquals("sample.png", model.displayName)
        assertEquals("image/png", model.mimeType)
        assertEquals(42L, model.size)
    }

    /** 六类技术错误应映射到对应的页面错误分类。 */
    @Test
    fun technicalErrorsMapToStableUiTypes() {
        val mappings = listOf(
            MediaLibraryError.NotFound() to MediaLibraryDemoErrorType.NotFound,
            MediaLibraryError.AlreadyExists() to MediaLibraryDemoErrorType.AlreadyExists,
            MediaLibraryError.PermissionDenied() to MediaLibraryDemoErrorType.PermissionDenied,
            MediaLibraryError.NoSpace() to MediaLibraryDemoErrorType.NoSpace,
            MediaLibraryError.Unsupported("unsupported") to MediaLibraryDemoErrorType.Unsupported,
            MediaLibraryError.Io() to MediaLibraryDemoErrorType.Io,
        )

        mappings.forEach { (error, expectedType) ->
            assertEquals(expectedType, error.toDemoModel().type)
        }
    }
}
