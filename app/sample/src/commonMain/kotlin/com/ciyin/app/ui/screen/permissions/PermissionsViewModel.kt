package com.ciyin.app.ui.screen.permissions

import ciyin.permissions.PermissionGroup
import ciyin.permissions.Permissions
import ciyin.permissions.getStatuses
import ciyin.permissions.openAppSettings
import ciyin.permissions.request
import ciyin.platform.Context
import ciyin.ui.foundation.viewmodel.StateMachineMviViewModel
import com.freeletics.flowredux2.FlowReduxBuilder
import com.freeletics.flowredux2.FlowReduxStateMachineFactory
import com.freeletics.flowredux2.initializeWith
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

/** 使用 FlowRedux2 编排权限查询、申请与设置跳转的示例 ViewModel。 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class PermissionsViewModel :
    StateMachineMviViewModel<PermissionsUiState, PermissionsAction, PermissionsEffect>() {

    /** 初始化空权限状态。 */
    override fun FlowReduxStateMachineFactory<PermissionsUiState, PermissionsAction>.initialize() {
        initializeWith { PermissionsUiState() }
    }

    /** 声明权限页面的状态转移。 */
    override fun FlowReduxBuilder<PermissionsUiState, PermissionsAction>.spec() {
        inState<PermissionsUiState> {
            // 用户点击返回时通知导航层退出当前页面。
            onActionEffect<PermissionsAction.BackClick> {
                poseEffect(PermissionsEffect.NavigateBack)
            }

            // 空闲时标记刷新状态并启动全量权限查询。
            on<PermissionsAction.Refresh> { action ->
                if (snapshot.isBusy) {
                    noChange()
                } else {
                    val changedState = mutate { copy(isRefreshing = true, errorMessage = null) }
                    refreshStatuses(action.context)
                    changedState
                }
            }

            // 空闲时标记目标权限组并启动单组申请。
            on<PermissionsAction.Request> { action ->
                if (snapshot.isBusy) {
                    noChange()
                } else {
                    val changedState = mutate {
                        copy(activeGroupId = action.groupId, errorMessage = null)
                    }
                    requestGroup(action.context, action.groupId)
                    changedState
                }
            }

            // 用户主动打开目标权限组对应的系统设置页。
            onActionEffect<PermissionsAction.OpenSettings> { action ->
                Permissions.openAppSettings(action.context, action.groupId.toModel().group)
            }

            // 全量查询完成后替换页面中的权限状态。
            on<PermissionsAction.StatusesLoaded> { action ->
                mutate {
                    copy(
                        statuses = action.result.statuses,
                        isRefreshing = false,
                        errorMessage = null,
                    )
                }
            }

            // 单组申请完成后合并本组的最新权限状态。
            on<PermissionsAction.RequestCompleted> { action ->
                mutate {
                    copy(
                        statuses = statuses + action.result.statuses,
                        activeGroupId = null,
                        errorMessage = null,
                    )
                }
            }

            // 平台权限操作失败时解除忙碌状态并展示错误。
            on<PermissionsAction.OperationFailed> { action ->
                mutate {
                    copy(
                        isRefreshing = false,
                        activeGroupId = null,
                        errorMessage = action.message,
                    )
                }
            }
        }
    }

    /** 在受控后台作用域查询全部内置权限。 */
    private fun refreshStatuses(context: Context) {
        backgroundScope.launch {
            runPermissionOperation {
                val result = Permissions.getStatuses(context, *PermissionGroup.builtIn.toTypedArray())
                dispatchAction(PermissionsAction.StatusesLoaded(result))
            }
        }
    }

    /** 在受控后台作用域申请一个权限组。 */
    private fun requestGroup(context: Context, groupId: PermissionsGroupId) {
        backgroundScope.launch {
            runPermissionOperation {
                val result = Permissions.request(context, groupId.toModel().group)
                dispatchAction(PermissionsAction.RequestCompleted(groupId, result))
            }
        }
    }

    /** 将非取消异常回灌为页面 Action。 */
    private suspend fun runPermissionOperation(operation: suspend () -> Unit) {
        try {
            operation()
        } catch (exception: CancellationException) {
            throw exception
        } catch (throwable: Throwable) {
            dispatchAction(
                PermissionsAction.OperationFailed(
                    throwable.message ?: throwable::class.simpleName.orEmpty(),
                ),
            )
        }
    }
}
