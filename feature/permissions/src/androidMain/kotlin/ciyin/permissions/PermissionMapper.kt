package ciyin.permissions

import ciyin.permissions.internal.android.InstallShortcutPermission
import ciyin.permissions.internal.android.InternetPermission
import com.hjq.permissions.permission.PermissionLists
import com.hjq.permissions.permission.base.IPermission

/** 跨平台权限到 XXPermissions 权限对象的完整映射。 */
private val androidPermissionMap: Map<Permission, List<IPermission>> by lazy {
    Permission.entries.associateWith { permission ->
        when (permission) {
            Permission.Camera -> listOf(PermissionLists.getCameraPermission())
            Permission.Microphone -> listOf(PermissionLists.getRecordAudioPermission())
            Permission.Storage -> listOf(PermissionLists.getManageExternalStoragePermission())
            Permission.MediaAudio -> listOf(PermissionLists.getReadMediaAudioPermission())
            Permission.MediaVideo -> listOf(PermissionLists.getReadMediaVideoPermission())
            Permission.MediaImages -> listOf(PermissionLists.getReadMediaImagesPermission())
            Permission.LocationCoarse -> listOf(PermissionLists.getAccessCoarseLocationPermission())
            Permission.LocationFine -> listOf(PermissionLists.getAccessFineLocationPermission())
            Permission.LocationBackground -> listOf(PermissionLists.getAccessBackgroundLocationPermission())
            Permission.BluetoothConnect -> listOf(PermissionLists.getBluetoothConnectPermission())
            Permission.BluetoothScan -> listOf(PermissionLists.getBluetoothScanPermission())
            Permission.BluetoothAdvertise -> listOf(PermissionLists.getBluetoothAdvertisePermission())
            Permission.NearbyWifi -> listOf(PermissionLists.getNearbyWifiDevicesPermission())
            Permission.Sensors -> listOf(PermissionLists.getReadHeartRatePermission())
            Permission.Motion -> listOf(PermissionLists.getActivityRecognitionPermission())
            Permission.Notifications -> listOf(PermissionLists.getPostNotificationsPermission())
            Permission.Contacts -> listOf(PermissionLists.getReadContactsPermission())
            Permission.Calendar -> listOf(PermissionLists.getReadCalendarPermission())
            Permission.CallLog -> listOf(PermissionLists.getReadCallLogPermission())
            Permission.Phone -> listOf(PermissionLists.getCallPhonePermission())
            Permission.Sms -> listOf(PermissionLists.getSendSmsPermission())
            Permission.ReadPhoneState -> listOf(PermissionLists.getReadPhoneStatePermission())
            Permission.IgnoreBatteryOptimizations ->
                listOf(PermissionLists.getRequestIgnoreBatteryOptimizationsPermission())
            Permission.SystemAlertWindow -> listOf(PermissionLists.getSystemAlertWindowPermission())
            Permission.PictureInPicture -> listOf(PermissionLists.getPictureInPicturePermission())
            Permission.RequestInstallPackages -> listOf(PermissionLists.getRequestInstallPackagesPermission())
            Permission.GetInstalledApps -> listOf(PermissionLists.getGetInstalledAppsPermission())
            Permission.Internet -> listOf(InternetPermission())
            Permission.BackgroundTasks ->
                listOf(PermissionLists.getRequestIgnoreBatteryOptimizationsPermission())
            Permission.Shortcut -> listOf(InstallShortcutPermission())
        }
    }
}

/** 返回单个跨平台权限对应的 XXPermissions 权限对象。 */
internal fun Permission.toAndroidPermissions(): List<IPermission> =
    checkNotNull(androidPermissionMap[this]) { "未找到 Android 权限映射：$this" }

/** 展开并按 Android 权限名称去重。 */
internal fun Collection<Permission>.toAndroidPermissions(): List<IPermission> =
    flatMap(Permission::toAndroidPermissions).distinctBy { it.permissionName }
