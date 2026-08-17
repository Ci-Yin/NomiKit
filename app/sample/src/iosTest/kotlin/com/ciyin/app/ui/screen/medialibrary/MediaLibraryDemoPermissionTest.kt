package com.ciyin.app.ui.screen.medialibrary

import ciyin.media.library.MediaCollection
import ciyin.permissions.Permission
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** iOS 媒体测试权限分类映射测试。 */
class MediaLibraryDemoPermissionTest {
    /** 图片和视频应请求对应 Photos 权限。 */
    @Test
    fun photosCollectionsMapToPhotosPermissions() {
        assertEquals(Permission.MediaImages, MediaCollection.Images.iosMediaLibraryPermission())
        assertEquals(Permission.MediaVideo, MediaCollection.Videos.iosMediaLibraryPermission())
    }

    /** 音频和下载应跳过 Photos 权限并交由 feature 返回 Unsupported。 */
    @Test
    fun unsupportedCollectionsDoNotRequestPhotosPermission() {
        assertNull(MediaCollection.Audio.iosMediaLibraryPermission())
        assertNull(MediaCollection.Downloads.iosMediaLibraryPermission())
    }
}
