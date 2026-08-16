package ciyin.permissions.internal.android

import android.app.AppOpsManager
import android.content.Context
import android.os.Build
import ciyin.platform.logger
import com.hjq.permissions.permission.common.DangerousPermission
import com.hjq.permissions.tools.PermissionVersion
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/** 将厂商快捷方式授权状态接入 XXPermissions 的内部适配器。 */
@Parcelize
internal class InstallShortcutPermission : DangerousPermission() {
    /** 快捷方式权限日志。 */
    @IgnoredOnParcel
    private val logger = logger("InstallShortcutPermission")

    /** 返回旧版安装快捷方式权限名称。 */
    override fun getPermissionName(): String = android.Manifest.permission.INSTALL_SHORTCUT

    /** 快捷方式权限适配从 Android 8 开始使用。 */
    override fun getFromAndroidVersion(context: Context): Int = PermissionVersion.ANDROID_8

    /** 优先采用厂商状态；无法可靠读取时继续标准权限检查。 */
    override fun isGrantedPermission(context: Context, skipRequest: Boolean): Boolean =
        when (ShortcutPermission.check(context)) {
            ShortcutPermissionResult.Granted -> true
            ShortcutPermissionResult.Denied,
            ShortcutPermissionResult.Ask,
            -> false
            ShortcutPermissionResult.Unknown ->
                checkWithAppOpsManager(context)
                    ?: super.isGrantedPermission(context, skipRequest)
        }

    /** AppOps 不能给出确定结论时返回 null，让调用方执行标准检查。 */
    private fun checkWithAppOpsManager(context: Context): Boolean? =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            null
        } else {
            when (ShortcutPermissionChecker.checkAppOperation(context, installShortcutOperation())) {
                AppOpsManager.MODE_ALLOWED -> true
                else -> null
            }
        }

    /** 获取系统公开字段，缺失时使用具名标准操作码。 */
    private fun installShortcutOperation(): Int = try {
        AppOpsManager::class.java.getDeclaredField("OP_INSTALL_SHORTCUT").getInt(null)
    } catch (exception: Exception) {
        logger.d { "OP_INSTALL_SHORTCUT unavailable: ${exception.message}" }
        DefaultInstallShortcutOperation
    }
}
