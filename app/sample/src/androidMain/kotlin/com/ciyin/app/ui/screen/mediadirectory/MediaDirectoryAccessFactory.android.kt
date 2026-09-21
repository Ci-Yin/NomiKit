package com.ciyin.app.ui.screen.mediadirectory

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import ciyin.media.directory.MediaDirectoryAccess
import ciyin.media.directory.MediaDirectoryAccessToken
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.resume

/**
 * Android 端 [rememberMediaDirectoryAccessFactory] 的实际实现。
 *
 * 把 SAF `OpenDocumentTree` 的 [ManagedActivityResultLauncher] 包装为可挂起的工厂：
 * 调用方每次调用工厂都会启动系统目录选择器；用户取消时工厂返回 `null`，授权成功时
 * 工厂内部立刻调用 `takePersistableUriPermission` 并查询目录展示名，再返回带授权令牌的 [MediaDirectoryAccess]。
 *
 * 进入组合时把 launcher 句柄存到 [AtomicReference]；离开组合时清空，避免 Activity 已
 * 销毁后还持有 launcher 句柄。`LocalContext` 由 Compose 自己注入到 factory 闭包内，
 * 不使用任何全局变量。
 */
@Composable
internal actual fun rememberMediaDirectoryAccessFactory(): MediaDirectoryAccessFactory? {
    val context = LocalContext.current
    val launcherReference =
        remember { AtomicReference<ManagedActivityResultLauncher<Uri?, Uri?>?>() }
    val resolverAccess = remember(context) { context.contentResolver }
    val factory = remember(resolverAccess, launcherReference) {
        AndroidMediaDirectoryAccessFactory(launcherReference, resolverAccess)
    }
    val activityResultLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree(),
    ) { uri -> factory.completeWithUri(uri) }
    DisposableEffect(activityResultLauncher, factory) {
        launcherReference.set(activityResultLauncher)
        onDispose {
            launcherReference.set(null)
            factory.cancelPending()
        }
    }
    return factory::requestAccess
}

/**
 * Android 端可挂起的 Access 工厂实现。
 *
 * 真实 trigger 来自系统返回的 `Uri?`；通过 [suspendCancellableCoroutine] 把
 * [requestAccess] 桥接成可挂起调用。
 */
private class AndroidMediaDirectoryAccessFactory(
    private val launcherReference: AtomicReference<ManagedActivityResultLauncher<Uri?, Uri?>?>,
    private val resolver: ContentResolver,
) {
    /** 当前等待中的续延。同一时刻只允许一次未完成的 pick 请求。 */
    private val pending = AtomicReference<kotlin.coroutines.Continuation<MediaDirectoryAccess?>?>()

    /** 启动 SAF 目录选择器；用户取消时 suspend 端 resume `null`。 */
    suspend fun requestAccess(): MediaDirectoryAccess? =
        suspendCancellableCoroutine { continuation ->
            val launcher = launcherReference.get() ?: run {
                // composition 已销毁，立即结束。
                continuation.resume(null)
                return@suspendCancellableCoroutine
            }
            val active = pending.getAndSet(continuation)
            if (active != null) {
                // 上一轮请求尚未结束；当前实现不允许并发的 SAF 选择器，
                // 因此直接把上一轮当作"取消"处理：resume null 走 Cancelled 路径。
                active.resume(null)
                pending.set(continuation)
            }
            continuation.invokeOnCancellation {
                pending.compareAndSet(continuation, null)
            }
            launcher.launch(null)
        }

    /** 系统回调命中；解析 SAF Tree URI 并 resume 给挂起端。 */
    fun completeWithUri(uri: Uri?) {
        val continuation = pending.getAndSet(null) ?: return
        continuation.resume(uri?.toMediaDirectoryAccess(resolver))
    }

    /** 主动清场（页面销毁）。 */
    fun cancelPending() {
        pending.getAndSet(null)?.resume(null)
    }
}

/** 对 SAF Tree URI 执行 take permission 并查询展示名，构造业务层 [MediaDirectoryAccess]。 */
private fun Uri.toMediaDirectoryAccess(resolver: ContentResolver): MediaDirectoryAccess {
    runCatching {
        resolver.takePersistableUriPermission(
            this,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
        )
    }
    val displayName = queryDisplayName(resolver, this) ?: lastPathSegment.orEmpty()
    return MediaDirectoryAccess(
        accessToken = MediaDirectoryAccessToken(toString()),
        displayName = displayName,
        canonicalPath = null,
    )
}

/** 读取 SAF 目录的 displayName；任何失败返回 null。 */
private fun queryDisplayName(resolver: ContentResolver, treeUri: Uri): String? = try {
    resolver.query(
        treeUri,
        arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME),
        null,
        null,
        null,
    )?.use { cursor ->
        if (cursor.moveToFirst()) cursor.getString(0) else null
    }
} catch (exception: Throwable) {
    null
}
