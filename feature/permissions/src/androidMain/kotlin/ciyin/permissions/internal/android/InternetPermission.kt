package ciyin.permissions.internal.android

import android.content.Context
import com.hjq.permissions.permission.common.DangerousPermission
import com.hjq.permissions.tools.PermissionVersion
import kotlinx.parcelize.Parcelize

/** 将 Android 普通网络权限接入 XXPermissions 的内部适配器。 */
@Parcelize
internal class InternetPermission : DangerousPermission() {
    /** 返回 Android 网络权限名称。 */
    override fun getPermissionName(): String = android.Manifest.permission.INTERNET

    /** 网络权限从 Android 2.0 起可用。 */
    override fun getFromAndroidVersion(context: Context): Int = PermissionVersion.ANDROID_2_0
}
