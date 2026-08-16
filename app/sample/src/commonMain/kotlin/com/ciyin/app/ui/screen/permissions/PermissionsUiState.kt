package com.ciyin.app.ui.screen.permissions

import androidx.compose.runtime.Immutable
import ciyin.permissions.Permission
import ciyin.permissions.PermissionStatus

/**
 * 权限管理示例页面状态。
 *
 * @property statuses 已查询的逐权限状态。
 * @property isRefreshing 是否正在刷新全部状态。
 * @property activeGroupId 当前正在申请的权限组。
 * @property errorMessage 最近一次操作错误。
 */
@Immutable
internal data class PermissionsUiState(
    val statuses: Map<Permission, PermissionStatus> = emptyMap(),
    val isRefreshing: Boolean = false,
    val activeGroupId: PermissionsGroupId? = null,
    val errorMessage: String? = null,
) {
    /** 页面是否正在执行权限操作。 */
    val isBusy: Boolean
        get() = isRefreshing || activeGroupId != null
}
