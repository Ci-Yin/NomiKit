package ciyin.permissions

import android.app.Activity
import android.content.ContextWrapper
import android.os.Handler
import android.os.Looper
import ciyin.platform.Context
import com.hjq.permissions.XXPermissions
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

/** 基于 XXPermissions 的 Android 权限实现。 */
actual object Permissions {
    /**
     * 请求权限并返回逐项状态。
     *
     * Android 权限弹窗必须由 [Activity] 承载，因此无法解析 Activity 时抛出明确参数错误。
     */
    actual suspend fun request(
        context: Context,
        vararg permissions: Permission,
    ): PermissionRequestResult {
        val requestedPermissions = permissions.distinct()
        if (requestedPermissions.isEmpty()) return PermissionRequestResult(emptyMap())

        val activity = requireNotNull(context.findActivity()) {
            "Permissions.request 的 Android context 必须是 Activity，或能够通过 ContextWrapper 解析到 Activity"
        }
        val androidPermissions = requestedPermissions.toAndroidPermissions()

        return suspendCancellableCoroutine { continuation ->
            activity.runOnUiThread {
                if (!continuation.isActive) return@runOnUiThread
                XXPermissions.with(activity)
                    .permissions(androidPermissions)
                    .request { _, _ ->
                        if (!continuation.isActive) return@request

                        val statuses = requestedPermissions.associateWith { permission ->
                            permission.getStatusAfterRequest(activity)
                        }
                        continuation.resume(PermissionRequestResult(statuses))
                    }
            }
        }
    }

    /** Android 查询只区分当前已授权或未授权。 */
    actual suspend fun getStatus(context: Context, permission: Permission): PermissionStatus =
        if (XXPermissions.isGrantedPermissions(context, permission.toAndroidPermissions())) {
            PermissionStatus.Granted
        } else {
            PermissionStatus.Denied
        }

    /** 打开 XXPermissions 提供的应用权限设置页。 */
    actual fun openAppSettings(context: Context, vararg permissions: Permission) {
        val androidPermissions = permissions.distinct().toAndroidPermissions()
        runOnMainThread {
            XXPermissions.startPermissionActivity(context, androidPermissions)
        }
    }
}

/** 在 Android 主线程执行平台 UI 操作。 */
private fun runOnMainThread(action: () -> Unit) {
    if (Looper.myLooper() == Looper.getMainLooper()) {
        action()
    } else {
        Handler(Looper.getMainLooper()).post(action)
    }
}

/** 沿 ContextWrapper 链查找权限弹窗所需的 Activity。 */
private tailrec fun android.content.Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

/** 请求完成后进一步区分普通拒绝与永久拒绝。 */
private fun Permission.getStatusAfterRequest(activity: Activity): PermissionStatus {
    val androidPermissions = toAndroidPermissions()
    if (XXPermissions.isGrantedPermissions(activity, androidPermissions)) {
        return PermissionStatus.Granted
    }
    return if (XXPermissions.isDoNotAskAgainPermissions(activity, androidPermissions)) {
        PermissionStatus.PermanentlyDenied
    } else {
        PermissionStatus.Denied
    }
}
