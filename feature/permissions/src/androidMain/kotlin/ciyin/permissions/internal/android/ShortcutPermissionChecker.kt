package ciyin.permissions.internal.android

import android.app.AppOpsManager
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import ciyin.platform.logger

/** MIUI 安装快捷方式权限的 AppOps 操作码。 */
private const val MiuiInstallShortcutOperation = 10017

/** 标准安装快捷方式权限的 AppOps 回退操作码。 */
internal const val DefaultInstallShortcutOperation = 101

/** Android 厂商快捷方式权限状态读取器。 */
internal object ShortcutPermissionChecker {
    /** 厂商权限检查日志。 */
    private val logger = logger("ShortcutPermissionChecker")

    /** EMUI 不提供普通应用可稳定调用的读取接口。 */
    fun checkOnEmui(context: Context): ShortcutPermissionResult {
        logger.i { "EMUI shortcut state is not reliably readable, package=${context.packageName}" }
        return ShortcutPermissionResult.Unknown
    }

    /** 通过 MIUI AppOps 扩展操作码读取快捷方式权限。 */
    fun checkOnMiui(context: Context): ShortcutPermissionResult {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return ShortcutPermissionResult.Unknown
        }
        val mode = checkAppOperation(context, MiuiInstallShortcutOperation)
            ?: return ShortcutPermissionResult.Unknown
        return when (mode) {
            AppOpsManager.MODE_ALLOWED -> ShortcutPermissionResult.Granted
            AppOpsManager.MODE_IGNORED -> ShortcutPermissionResult.Denied
            5 -> ShortcutPermissionResult.Ask
            else -> ShortcutPermissionResult.Unknown
        }
    }

    /** ColorOS 权限 Provider 受系统签名保护，普通应用只能返回未知。 */
    fun checkOnOppo(context: Context): ShortcutPermissionResult {
        logger.i { "ColorOS shortcut state is not readable, package=${context.packageName}" }
        return ShortcutPermissionResult.Unknown
    }

    /** 尝试通过 VIVO Launcher Provider 读取快捷方式权限。 */
    fun checkOnVivo(context: Context): ShortcutPermissionResult {
        val resolver = context.contentResolver
        val packageName = context.applicationContext.packageName
        val uris = listOf(
            "content://com.bbk.launcher2.settings/permissions",
            "content://com.vivo.launcher.permission/shortcut_permission",
            "content://com.bbk.launcher2.settings/shortcut_permission",
        )
        for (uri in uris) {
            val result = queryVivoPermission(resolver, Uri.parse(uri), packageName)
            if (result != ShortcutPermissionResult.Unknown) return result
        }

        logger.i { "VIVO shortcut state is not readable, package=$packageName" }
        return ShortcutPermissionResult.Unknown
    }

    /** 读取单个 VIVO 权限 Provider，访问失败时返回未知。 */
    private fun queryVivoPermission(
        resolver: ContentResolver,
        uri: Uri,
        packageName: String,
    ): ShortcutPermissionResult {
        var cursor: Cursor? = null
        return try {
            cursor = resolver.query(uri, null, "package_name=?", arrayOf(packageName), null)
            if (cursor?.moveToFirst() != true) return ShortcutPermissionResult.Unknown
            listOf("permission", "shortcutPermission", "permission_value", "value")
                .firstNotNullOfOrNull { column ->
                    val index = cursor.getColumnIndex(column)
                    if (index >= 0) cursor.getInt(index).toShortcutPermissionResult() else null
                } ?: ShortcutPermissionResult.Unknown
        } catch (exception: RuntimeException) {
            logger.d { "Cannot query VIVO provider $uri: ${exception.message}" }
            ShortcutPermissionResult.Unknown
        } finally {
            cursor?.close()
        }
    }

    /** 通过反射调用隐藏的 AppOps 整数操作码 API。 */
    internal fun checkAppOperation(context: Context, operation: Int): Int? = try {
        val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager
            ?: return null
        val method = AppOpsManager::class.java.getMethod(
            "checkOpNoThrow",
            Int::class.javaPrimitiveType,
            Int::class.javaPrimitiveType,
            String::class.java,
        )
        method.invoke(
            appOpsManager,
            operation,
            context.applicationInfo.uid,
            context.packageName,
        ) as? Int
    } catch (exception: Exception) {
        logger.w(exception) { "Shortcut AppOps check failed" }
        null
    }
}

/** 将 VIVO Provider 的数值转换为统一状态。 */
private fun Int.toShortcutPermissionResult(): ShortcutPermissionResult = when (this) {
    0, 1, 17 -> ShortcutPermissionResult.Denied
    16 -> ShortcutPermissionResult.Granted
    18 -> ShortcutPermissionResult.Ask
    else -> ShortcutPermissionResult.Unknown
}
