package ciyin.permissions

/**
 * 跨平台权限标识。
 *
 * 各平台无法表达的权限会返回 [PermissionStatus.Unsupported]，不会伪造为已授权。
 */
enum class Permission {
    Camera,
    Microphone,
    Storage,
    MediaAudio,
    MediaVideo,
    MediaImages,
    LocationCoarse,
    LocationFine,
    LocationBackground,
    BluetoothConnect,
    BluetoothScan,
    BluetoothAdvertise,
    NearbyWifi,
    Sensors,
    Motion,
    Notifications,
    Contacts,
    Calendar,
    CallLog,
    Phone,
    Sms,
    ReadPhoneState,
    IgnoreBatteryOptimizations,
    SystemAlertWindow,
    PictureInPicture,
    RequestInstallPackages,
    GetInstalledApps,
    Internet,
    BackgroundTasks,
    Shortcut,
}
