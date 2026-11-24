package com.ciyin.app.presentation.screen.permissions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.composefluent.icons.Icons
import io.github.composefluent.icons.regular.Alert
import io.github.composefluent.icons.regular.ArrowSync
import io.github.composefluent.icons.regular.Bluetooth
import io.github.composefluent.icons.regular.CalendarLtr
import io.github.composefluent.icons.regular.Call
import io.github.composefluent.icons.regular.Camera
import io.github.composefluent.icons.regular.Chat
import io.github.composefluent.icons.regular.Folder
import io.github.composefluent.icons.regular.Globe
import io.github.composefluent.icons.regular.ImageMultiple
import io.github.composefluent.icons.regular.Location
import io.github.composefluent.icons.regular.Mic
import io.github.composefluent.icons.regular.People
import io.github.composefluent.icons.regular.Pulse
import io.github.composefluent.icons.regular.Wifi4
import ciyin.designsystem.component.button.NomiButton
import ciyin.designsystem.component.button.NomiButtonVariant
import ciyin.designsystem.component.button.NomiIconButton
import ciyin.designsystem.component.divider.NomiDivider
import ciyin.designsystem.component.feedback.NomiProgressIndicator
import ciyin.designsystem.component.feedback.NomiProgressIndicatorType
import ciyin.designsystem.component.icon.NomiIcon
import ciyin.designsystem.component.list.NomiListItem
import ciyin.designsystem.component.text.NomiText
import ciyin.material.theme.AppTheme
import ciyin.permissions.Permission
import ciyin.permissions.PermissionStatus
import ciyin.platform.LocalContext
import ciyin.ui.foundation.viewmodel.collectSideEffects
import ciyin.ui.foundation.viewmodel.viewModel
import com.ciyin.app.presentation.component.SamplePageScaffold
import com.ciyin.app.sample.Res
import com.ciyin.app.sample.permissions_back
import com.ciyin.app.sample.permissions_group_bluetooth
import com.ciyin.app.sample.permissions_group_calendar
import com.ciyin.app.sample.permissions_group_camera
import com.ciyin.app.sample.permissions_group_contacts
import com.ciyin.app.sample.permissions_group_internet
import com.ciyin.app.sample.permissions_group_location
import com.ciyin.app.sample.permissions_group_media
import com.ciyin.app.sample.permissions_group_microphone
import com.ciyin.app.sample.permissions_group_notifications
import com.ciyin.app.sample.permissions_group_phone
import com.ciyin.app.sample.permissions_group_sensors
import com.ciyin.app.sample.permissions_group_sms
import com.ciyin.app.sample.permissions_group_storage
import com.ciyin.app.sample.permissions_group_wifi
import com.ciyin.app.sample.permissions_operation_failed
import com.ciyin.app.sample.permissions_permission_bluetooth_advertise
import com.ciyin.app.sample.permissions_permission_bluetooth_connect
import com.ciyin.app.sample.permissions_permission_bluetooth_scan
import com.ciyin.app.sample.permissions_permission_calendar
import com.ciyin.app.sample.permissions_permission_camera
import com.ciyin.app.sample.permissions_permission_contacts
import com.ciyin.app.sample.permissions_permission_internet
import com.ciyin.app.sample.permissions_permission_location_coarse
import com.ciyin.app.sample.permissions_permission_location_fine
import com.ciyin.app.sample.permissions_permission_media_audio
import com.ciyin.app.sample.permissions_permission_media_images
import com.ciyin.app.sample.permissions_permission_media_video
import com.ciyin.app.sample.permissions_permission_microphone
import com.ciyin.app.sample.permissions_permission_motion
import com.ciyin.app.sample.permissions_permission_nearby_wifi
import com.ciyin.app.sample.permissions_permission_notifications
import com.ciyin.app.sample.permissions_permission_phone
import com.ciyin.app.sample.permissions_permission_read_phone_state
import com.ciyin.app.sample.permissions_permission_sensors
import com.ciyin.app.sample.permissions_permission_sms
import com.ciyin.app.sample.permissions_permission_status
import com.ciyin.app.sample.permissions_permission_storage
import com.ciyin.app.sample.permissions_refresh
import com.ciyin.app.sample.permissions_request
import com.ciyin.app.sample.permissions_settings
import com.ciyin.app.sample.permissions_status_denied
import com.ciyin.app.sample.permissions_status_granted
import com.ciyin.app.sample.permissions_status_not_determined
import com.ciyin.app.sample.permissions_status_permanently_denied
import com.ciyin.app.sample.permissions_status_restricted
import com.ciyin.app.sample.permissions_status_unknown
import com.ciyin.app.sample.permissions_status_unsupported
import com.ciyin.app.sample.permissions_title
import com.ciyin.app.sample.permissions_unknown_error
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.AppPreview

/**
 * 权限管理示例页面入口。
 *
 * @param onBack 外层导航返回回调。
 * @param viewModel 页面 ViewModel。
 */
@Composable
internal fun PermissionsScreen(
    onBack: () -> Unit,
    viewModel: PermissionsViewModel = viewModel(::PermissionsViewModel),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(context) {
        viewModel.dispatchAction(PermissionsAction.Refresh(context))
    }
    viewModel.collectSideEffects { effect ->
        when (effect) {
            PermissionsEffect.NavigateBack -> onBack()
        }
    }

    PermissionsContent(
        state = state,
        onAction = viewModel.dispatchAction,
    )
}

/** 权限管理示例的纯 UI。 */
@Composable
private fun PermissionsContent(
    state: PermissionsUiState,
    onAction: (PermissionsAction) -> Unit,
) {
    val context = LocalContext.current
    SamplePageScaffold(
        title = stringResource(Res.string.permissions_title),
        onBack = { onAction(PermissionsAction.BackClick) },
        backContentDescription = stringResource(Res.string.permissions_back),
        actions = {
            NomiIconButton(
                onClick = { onAction(PermissionsAction.Refresh(context)) },
                enabled = !state.isBusy,
            ) {
                if (state.isRefreshing) {
                    NomiProgressIndicator(
                        progress = null,
                        type = NomiProgressIndicatorType.Circular,
                        modifier = Modifier.size(AppTheme.sizes.icon.large),
                    )
                } else {
                    NomiIcon(
                        imageVector = Icons.Regular.ArrowSync,
                        contentDescription = stringResource(Res.string.permissions_refresh),
                    )
                }
            }
        },
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            state.errorMessage?.let { message ->
                item(key = "error") {
                    val errorMessage = message.ifBlank {
                        stringResource(Res.string.permissions_unknown_error)
                    }
                    NomiText(
                        text = stringResource(
                            Res.string.permissions_operation_failed,
                            errorMessage
                        ),
                        color = AppTheme.colorScheme.error,
                        style = AppTheme.typography.bodyMedium,
                        modifier = Modifier.padding(AppTheme.spacings.large),
                    )
                    NomiDivider()
                }
            }
            items(
                items = permissionsGroupModels,
                key = { it.id.name },
            ) { model ->
                PermissionGroupRow(
                    model = model,
                    state = state,
                    onRequest = {
                        onAction(PermissionsAction.Request(context, model.id))
                    },
                    onOpenSettings = {
                        onAction(PermissionsAction.OpenSettings(context, model.id))
                    },
                )
                NomiDivider()
            }
        }
    }
}

/**
 * 渲染一个权限组及其逐权限状态和操作。
 *
 * 标题左侧展示权限盾牌图标，状态摘要与行尾操作由 [NomiListItem] 统一承载，与样例其他列表页保持一致。
 */
@Composable
private fun PermissionGroupRow(
    model: PermissionsGroupModel,
    state: PermissionsUiState,
    onRequest: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val statusLabels = mutableListOf<String>()
    for (permission in model.group.permissions) {
        statusLabels += stringResource(
            Res.string.permissions_permission_status,
            stringResource(permission.titleResource()),
            stringResource(state.statuses[permission].statusResource()),
        )
    }
    val statusText = statusLabels.joinToString(separator = " · ")

    NomiListItem(
        leading = {
            NomiIcon(
                imageVector = model.id.icon(),
                contentDescription = null,
                modifier = Modifier.size(AppTheme.sizes.icon.large),
                tint = AppTheme.colorScheme.primary,
            )
        },
        title = {
            NomiText(
                text = stringResource(model.id.titleResource()),
                style = AppTheme.typography.titleMedium,
            )
        },
        subtitle = {
            NomiText(
                text = statusText,
                color = AppTheme.colorScheme.textSecondary,
                style = AppTheme.typography.bodyMedium,
            )
        },
        trailing = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacings.small),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                NomiButton(
                    onClick = onRequest,
                    enabled = !state.isBusy,
                    variant = NomiButtonVariant.Default,
                ) {
                    NomiText(stringResource(Res.string.permissions_request))
                }
                NomiButton(
                    onClick = onOpenSettings,
                    variant = NomiButtonVariant.Text,
                    enabled = !state.isBusy,
                ) {
                    NomiText(stringResource(Res.string.permissions_settings))
                }
            }
        },
        modifier = Modifier.fillMaxWidth(),
    )
}

/** 返回权限组标题左侧使用的 Fluent 图标。 */
private fun PermissionsGroupId.icon(): ImageVector = when (this) {
    PermissionsGroupId.Camera -> Icons.Regular.Camera
    PermissionsGroupId.Phone -> Icons.Regular.Call
    PermissionsGroupId.Microphone -> Icons.Regular.Mic
    PermissionsGroupId.Sms -> Icons.Regular.Chat
    PermissionsGroupId.Location -> Icons.Regular.Location
    PermissionsGroupId.Media -> Icons.Regular.ImageMultiple
    PermissionsGroupId.Sensors -> Icons.Regular.Pulse
    PermissionsGroupId.Storage -> Icons.Regular.Folder
    PermissionsGroupId.Contacts -> Icons.Regular.People
    PermissionsGroupId.Calendar -> Icons.Regular.CalendarLtr
    PermissionsGroupId.Notifications -> Icons.Regular.Alert
    PermissionsGroupId.Bluetooth -> Icons.Regular.Bluetooth
    PermissionsGroupId.Wifi -> Icons.Regular.Wifi4
    PermissionsGroupId.Internet -> Icons.Regular.Globe
}

/** 返回权限组标题资源。 */
private fun PermissionsGroupId.titleResource(): StringResource = when (this) {
    PermissionsGroupId.Camera -> Res.string.permissions_group_camera
    PermissionsGroupId.Phone -> Res.string.permissions_group_phone
    PermissionsGroupId.Microphone -> Res.string.permissions_group_microphone
    PermissionsGroupId.Sms -> Res.string.permissions_group_sms
    PermissionsGroupId.Location -> Res.string.permissions_group_location
    PermissionsGroupId.Media -> Res.string.permissions_group_media
    PermissionsGroupId.Sensors -> Res.string.permissions_group_sensors
    PermissionsGroupId.Storage -> Res.string.permissions_group_storage
    PermissionsGroupId.Contacts -> Res.string.permissions_group_contacts
    PermissionsGroupId.Calendar -> Res.string.permissions_group_calendar
    PermissionsGroupId.Notifications -> Res.string.permissions_group_notifications
    PermissionsGroupId.Bluetooth -> Res.string.permissions_group_bluetooth
    PermissionsGroupId.Wifi -> Res.string.permissions_group_wifi
    PermissionsGroupId.Internet -> Res.string.permissions_group_internet
}

/** 返回内置组使用的权限标题资源。 */
private fun Permission.titleResource(): StringResource = when (this) {
    Permission.Camera -> Res.string.permissions_permission_camera
    Permission.Microphone -> Res.string.permissions_permission_microphone
    Permission.Storage -> Res.string.permissions_permission_storage
    Permission.MediaAudio -> Res.string.permissions_permission_media_audio
    Permission.MediaVideo -> Res.string.permissions_permission_media_video
    Permission.MediaImages -> Res.string.permissions_permission_media_images
    Permission.LocationCoarse -> Res.string.permissions_permission_location_coarse
    Permission.LocationFine -> Res.string.permissions_permission_location_fine
    Permission.BluetoothConnect -> Res.string.permissions_permission_bluetooth_connect
    Permission.BluetoothScan -> Res.string.permissions_permission_bluetooth_scan
    Permission.BluetoothAdvertise -> Res.string.permissions_permission_bluetooth_advertise
    Permission.NearbyWifi -> Res.string.permissions_permission_nearby_wifi
    Permission.Sensors -> Res.string.permissions_permission_sensors
    Permission.Motion -> Res.string.permissions_permission_motion
    Permission.Notifications -> Res.string.permissions_permission_notifications
    Permission.Contacts -> Res.string.permissions_permission_contacts
    Permission.Calendar -> Res.string.permissions_permission_calendar
    Permission.Phone -> Res.string.permissions_permission_phone
    Permission.Sms -> Res.string.permissions_permission_sms
    Permission.ReadPhoneState -> Res.string.permissions_permission_read_phone_state
    Permission.Internet -> Res.string.permissions_permission_internet
    else -> error("内置权限组未配置标题：$this")
}

/** 返回权限状态标题资源，null 表示尚未查询。 */
private fun PermissionStatus?.statusResource(): StringResource = when (this) {
    null -> Res.string.permissions_status_unknown
    PermissionStatus.NotDetermined -> Res.string.permissions_status_not_determined
    PermissionStatus.Granted -> Res.string.permissions_status_granted
    PermissionStatus.Denied -> Res.string.permissions_status_denied
    PermissionStatus.PermanentlyDenied -> Res.string.permissions_status_permanently_denied
    PermissionStatus.Restricted -> Res.string.permissions_status_restricted
    PermissionStatus.Unsupported -> Res.string.permissions_status_unsupported
}

/** 权限管理面板预览。 */
@AppPreview
@Composable
private fun PermissionsScreenPreview() {
    AppTheme {
        PermissionsContent(
            state = PermissionsUiState(
                statuses = permissionsGroupModels
                    .flatMap { it.group.permissions }
                    .distinct()
                    .associateWith { PermissionStatus.Unsupported },
            ),
            onAction = {},
        )
    }
}
