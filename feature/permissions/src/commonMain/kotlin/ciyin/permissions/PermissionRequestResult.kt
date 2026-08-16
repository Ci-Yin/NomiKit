package ciyin.permissions

/**
 * 一次权限查询或请求中，每个去重权限对应的最终状态。
 *
 * @property statuses 按请求顺序保存的权限状态。
 */
data class PermissionRequestResult(
    val statuses: Map<Permission, PermissionStatus>,
) {
    /** 已授权的权限。 */
    val granted: Set<Permission>
        get() = permissionsWith(PermissionStatus.Granted)

    /** 被拒绝、永久拒绝或受系统策略限制的权限。 */
    val denied: Set<Permission>
        get() = statuses.filterValues {
            it == PermissionStatus.Denied ||
                it == PermissionStatus.PermanentlyDenied ||
                it == PermissionStatus.Restricted
        }.keys

    /** 被永久拒绝的权限。 */
    val permanentlyDenied: Set<Permission>
        get() = permissionsWith(PermissionStatus.PermanentlyDenied)

    /** 受系统策略或家长控制限制的权限。 */
    val restricted: Set<Permission>
        get() = permissionsWith(PermissionStatus.Restricted)

    /** 当前平台不支持的权限。 */
    val unsupported: Set<Permission>
        get() = permissionsWith(PermissionStatus.Unsupported)

    /** 尚未向用户请求的权限。 */
    val notDetermined: Set<Permission>
        get() = permissionsWith(PermissionStatus.NotDetermined)

    /** 是否所有权限均已授权；空结果视为已满足。 */
    val allGranted: Boolean
        get() = statuses.values.all { it == PermissionStatus.Granted }

    /** 返回具有指定状态的权限集合。 */
    private fun permissionsWith(status: PermissionStatus): Set<Permission> =
        statuses.filterValues { it == status }.keys
}
