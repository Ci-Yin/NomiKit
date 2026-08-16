package com.ciyin.app.ui.screen.permissions

import androidx.compose.runtime.Immutable
import ciyin.permissions.PermissionGroup

/** 权限示例内置组标识。 */
internal enum class PermissionsGroupId {
    /** 相机权限组。 */
    Camera,

    /** 电话权限组。 */
    Phone,

    /** 麦克风权限组。 */
    Microphone,

    /** 短信权限组。 */
    Sms,

    /** 定位权限组。 */
    Location,

    /** 媒体权限组。 */
    Media,

    /** 传感器权限组。 */
    Sensors,

    /** 存储权限组。 */
    Storage,

    /** 通讯录权限组。 */
    Contacts,

    /** 日历权限组。 */
    Calendar,

    /** 通知权限组。 */
    Notifications,

    /** 蓝牙权限组。 */
    Bluetooth,

    /** 附近 Wi-Fi 权限组。 */
    Wifi,

    /** 网络访问权限组。 */
    Internet,
}

/**
 * 权限示例中的单个权限组模型。
 *
 * @property id 页面稳定标识。
 * @property group feature 模块提供的权限组。
 */
@Immutable
internal data class PermissionsGroupModel(
    val id: PermissionsGroupId,
    val group: PermissionGroup,
)

/** 页面展示的全部 14 个内置权限组。 */
internal val permissionsGroupModels: List<PermissionsGroupModel> = listOf(
    PermissionsGroupModel(PermissionsGroupId.Camera, PermissionGroup.Camera),
    PermissionsGroupModel(PermissionsGroupId.Phone, PermissionGroup.Phone),
    PermissionsGroupModel(PermissionsGroupId.Microphone, PermissionGroup.Microphone),
    PermissionsGroupModel(PermissionsGroupId.Sms, PermissionGroup.Sms),
    PermissionsGroupModel(PermissionsGroupId.Location, PermissionGroup.Location),
    PermissionsGroupModel(PermissionsGroupId.Media, PermissionGroup.Media),
    PermissionsGroupModel(PermissionsGroupId.Sensors, PermissionGroup.Sensors),
    PermissionsGroupModel(PermissionsGroupId.Storage, PermissionGroup.Storage),
    PermissionsGroupModel(PermissionsGroupId.Contacts, PermissionGroup.Contacts),
    PermissionsGroupModel(PermissionsGroupId.Calendar, PermissionGroup.Calendar),
    PermissionsGroupModel(PermissionsGroupId.Notifications, PermissionGroup.Notifications),
    PermissionsGroupModel(PermissionsGroupId.Bluetooth, PermissionGroup.Bluetooth),
    PermissionsGroupModel(PermissionsGroupId.Wifi, PermissionGroup.Wifi),
    PermissionsGroupModel(PermissionsGroupId.Internet, PermissionGroup.Internet),
)

/** 按稳定标识查找权限组模型。 */
internal fun PermissionsGroupId.toModel(): PermissionsGroupModel =
    checkNotNull(permissionsGroupModels.firstOrNull { it.id == this }) {
        "未找到权限组模型：$this"
    }
