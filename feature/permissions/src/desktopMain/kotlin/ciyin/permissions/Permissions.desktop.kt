package ciyin.permissions

import ciyin.platform.Context

/** Desktop 权限实现；桌面端没有统一运行时权限模型。 */
actual object Permissions {
    /** 返回每个请求权限的 [PermissionStatus.Unsupported] 状态。 */
    actual suspend fun request(
        context: Context,
        vararg permissions: Permission,
    ): PermissionRequestResult = PermissionRequestResult(
        permissions.distinct().associateWith { PermissionStatus.Unsupported },
    )

    /** Desktop 不伪造授权，始终返回 [PermissionStatus.Unsupported]。 */
    actual suspend fun getStatus(context: Context, permission: Permission): PermissionStatus =
        PermissionStatus.Unsupported

    /** Desktop 没有统一应用权限设置页，因此安全结束。 */
    actual fun openAppSettings(context: Context, vararg permissions: Permission) = Unit
}
