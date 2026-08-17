package com.ciyin.app.ui.screen.medialibrary

import android.app.Activity
import android.content.ContextWrapper
import android.os.Build
import ciyin.media.library.MediaCollection
import ciyin.platform.Context
import com.hjq.permissions.XXPermissions
import com.hjq.permissions.permission.PermissionLists
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

/** Android 10 及以上无需旧存储权限，旧系统只请求外部存储写入能力。 */
@Suppress("UNUSED_PARAMETER")
internal actual suspend fun ensureMediaLibraryDemoPermission(
    context: Context,
    collection: MediaCollection,
): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) return true
    val activity = requireNotNull(context.findActivity()) {
        "系统媒体库示例的 Android context 必须能够解析到 Activity"
    }
    val permission = PermissionLists.getWriteExternalStoragePermission()
    if (XXPermissions.isGrantedPermissions(activity, listOf(permission))) return true

    return suspendCancellableCoroutine { continuation ->
        activity.runOnUiThread {
            if (!continuation.isActive) return@runOnUiThread
            XXPermissions.with(activity)
                .permissions(listOf(permission))
                .request { _, _ ->
                    if (!continuation.isActive) return@request
                    continuation.resume(
                        XXPermissions.isGrantedPermissions(activity, listOf(permission)),
                    )
                }
        }
    }
}

/** 沿 Android ContextWrapper 链查找承载权限弹窗的 Activity。 */
private tailrec fun android.content.Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
