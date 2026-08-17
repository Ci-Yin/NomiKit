package com.ciyin.app.ui.screen.medialibrary

import ciyin.media.library.MediaCollection
import ciyin.permissions.Permission
import ciyin.permissions.PermissionStatus
import ciyin.permissions.Permissions
import ciyin.platform.Context

/** 图片和视频请求 Photos 授权，不支持的分类交由媒体库返回 Unsupported。 */
internal actual suspend fun ensureMediaLibraryDemoPermission(
    context: Context,
    collection: MediaCollection,
): Boolean {
    val permission = collection.iosMediaLibraryPermission() ?: return true
    return Permissions.request(context, permission).statuses[permission] == PermissionStatus.Granted
}

/** 返回媒体分类所需的 iOS Photos 权限，不支持的分类返回 null。 */
internal fun MediaCollection.iosMediaLibraryPermission(): Permission? = when (this) {
    MediaCollection.Images -> Permission.MediaImages
    MediaCollection.Videos -> Permission.MediaVideo
    MediaCollection.Audio,
    MediaCollection.Downloads,
    -> null
}
