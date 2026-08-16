package ciyin.permissions

/**
 * 一组按业务场景组合的权限。
 *
 * @property permissions 该组按声明顺序包含的权限。
 */
sealed class PermissionGroup(vararg permissions: Permission) {

    /** 该组按声明顺序包含的权限。 */
    val permissions: List<Permission> = permissions.distinct()

    /** 自定义权限组。 */
    class Custom(vararg permissions: Permission) : PermissionGroup(*permissions)

    /** 相机权限组。 */
    data object Camera : PermissionGroup(Permission.Camera)

    /** 电话权限组。 */
    data object Phone : PermissionGroup(Permission.Phone, Permission.ReadPhoneState)

    /** 麦克风权限组。 */
    data object Microphone : PermissionGroup(Permission.Microphone)

    /** 短信权限组。 */
    data object Sms : PermissionGroup(Permission.Sms)

    /** 定位权限组。 */
    data object Location : PermissionGroup(Permission.LocationCoarse, Permission.LocationFine)

    /** 媒体读取权限组。 */
    data object Media : PermissionGroup(
        Permission.MediaAudio,
        Permission.MediaVideo,
        Permission.MediaImages,
    )

    /** 传感器与运动权限组。 */
    data object Sensors : PermissionGroup(Permission.Sensors, Permission.Motion)

    /** 文件存储权限组。 */
    data object Storage : PermissionGroup(Permission.Storage)

    /** 通讯录权限组。 */
    data object Contacts : PermissionGroup(Permission.Contacts)

    /** 日历权限组。 */
    data object Calendar : PermissionGroup(Permission.Calendar)

    /** 通知权限组。 */
    data object Notifications : PermissionGroup(Permission.Notifications)

    /** 蓝牙权限组。 */
    data object Bluetooth : PermissionGroup(
        Permission.BluetoothConnect,
        Permission.BluetoothScan,
        Permission.BluetoothAdvertise,
    )

    /** 附近 Wi-Fi 设备权限组。 */
    data object Wifi : PermissionGroup(Permission.NearbyWifi)

    /** 网络访问权限组。 */
    data object Internet : PermissionGroup(Permission.Internet)

    companion object {
        /** 供示例和权限面板使用的全部内置权限组。 */
        val builtIn: List<PermissionGroup>
            get() = listOf(
                Camera,
                Phone,
                Microphone,
                Sms,
                Location,
                Media,
                Sensors,
                Storage,
                Contacts,
                Calendar,
                Notifications,
                Bluetooth,
                Wifi,
                Internet,
            )
    }
}
