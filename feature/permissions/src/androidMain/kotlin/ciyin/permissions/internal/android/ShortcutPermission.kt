package ciyin.permissions.internal.android

import android.content.Context
import android.os.Build
import ciyin.platform.logger
import java.util.Locale

/** 厂商快捷方式权限检查结果。 */
internal enum class ShortcutPermissionResult {
    /** 厂商权限已授予。 */
    Granted,

    /** 厂商权限已拒绝。 */
    Denied,

    /** 厂商权限仍需询问用户。 */
    Ask,

    /** 厂商状态无法可靠读取。 */
    Unknown,
}

/** 根据 Android 厂商选择快捷方式权限检查器。 */
internal object ShortcutPermission {
    /** 快捷方式权限日志。 */
    private val logger = logger("ShortcutPermission")

    /** 规范化后的设备厂商名称。 */
    private val manufacturer = Build.MANUFACTURER.lowercase(Locale.getDefault())

    /** 查询当前厂商可读取的快捷方式权限状态。 */
    fun check(context: Context): ShortcutPermissionResult {
        logger.i { "manufacturer=$manufacturer, apiLevel=${Build.VERSION.SDK_INT}" }
        return when {
            manufacturer.contains("huawei") -> ShortcutPermissionChecker.checkOnEmui(context)
            manufacturer.contains("xiaomi") -> ShortcutPermissionChecker.checkOnMiui(context)
            manufacturer.contains("oppo") -> ShortcutPermissionChecker.checkOnOppo(context)
            manufacturer.contains("vivo") -> ShortcutPermissionChecker.checkOnVivo(context)
            manufacturer.contains("samsung") || manufacturer.contains("meizu") ->
                ShortcutPermissionResult.Granted
            else -> ShortcutPermissionResult.Unknown
        }
    }
}
