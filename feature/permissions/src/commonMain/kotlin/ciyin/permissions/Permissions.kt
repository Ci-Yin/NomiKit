package ciyin.permissions

import ciyin.platform.Context

/** 跨平台权限入口。 */
expect object Permissions {
    /**
     * 请求指定权限并挂起到所有权限得到最终状态。
     *
     * 用户拒绝会作为状态返回，协程取消会继续向上传播。
     */
    suspend fun request(
        context: Context,
        vararg permissions: Permission,
    ): PermissionRequestResult

    /** 查询指定权限的当前状态。 */
    suspend fun getStatus(context: Context, permission: Permission): PermissionStatus

    /** 打开应用设置页；当前平台不支持时安全结束。 */
    fun openAppSettings(context: Context, vararg permissions: Permission)
}

/** 请求权限组，并按首次出现顺序去重后交给平台实现。 */
suspend fun Permissions.request(
    context: Context,
    vararg groups: PermissionGroup,
): PermissionRequestResult = request(context, *groups.toPermissions())

/** 查询多个权限并按首次出现顺序聚合结果。 */
suspend fun Permissions.getStatuses(
    context: Context,
    vararg permissions: Permission,
): PermissionRequestResult = PermissionRequestResult(
    permissions.distinct().associateWith { getStatus(context, it) },
)

/** 查询多个权限组并按首次出现顺序聚合结果。 */
suspend fun Permissions.getStatuses(
    context: Context,
    vararg groups: PermissionGroup,
): PermissionRequestResult = getStatuses(context, *groups.toPermissions())

/** 打开权限组对应的应用设置页。 */
fun Permissions.openAppSettings(
    context: Context,
    vararg groups: PermissionGroup,
) {
    openAppSettings(context, *groups.toPermissions())
}

/** 判断列表是否完整包含指定权限组。 */
fun Collection<Permission>.contains(group: PermissionGroup): Boolean = containsAll(group.permissions)

/** 按权限组声明顺序展开，并保留每个权限的首次出现位置。 */
internal fun Array<out PermissionGroup>.toPermissions(): Array<Permission> =
    flatMap(PermissionGroup::permissions).distinct().toTypedArray()
