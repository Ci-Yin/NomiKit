package com.ciyin.app.ui.screen.permissions

import ciyin.permissions.PermissionRequestResult
import ciyin.platform.Context

/** 权限管理示例页面动作。 */
internal sealed interface PermissionsAction {
    /** 点击返回按钮。 */
    data object BackClick : PermissionsAction

    /**
     * 刷新全部内置权限组状态。
     *
     * @property context 当前平台上下文。
     */
    data class Refresh(val context: Context) : PermissionsAction

    /**
     * 申请单个权限组。
     *
     * @property context 当前平台上下文。
     * @property groupId 目标权限组。
     */
    data class Request(
        val context: Context,
        val groupId: PermissionsGroupId,
    ) : PermissionsAction

    /**
     * 打开单个权限组的应用设置页。
     *
     * @property context 当前平台上下文。
     * @property groupId 目标权限组。
     */
    data class OpenSettings(
        val context: Context,
        val groupId: PermissionsGroupId,
    ) : PermissionsAction

    /**
     * 全部权限状态查询完成。
     *
     * @property result 聚合状态。
     */
    data class StatusesLoaded(val result: PermissionRequestResult) : PermissionsAction

    /**
     * 单组申请完成。
     *
     * @property groupId 已完成的权限组。
     * @property result 本次请求结果。
     */
    data class RequestCompleted(
        val groupId: PermissionsGroupId,
        val result: PermissionRequestResult,
    ) : PermissionsAction

    /**
     * 查询或申请失败。
     *
     * @property message 可读错误信息。
     */
    data class OperationFailed(val message: String) : PermissionsAction
}
