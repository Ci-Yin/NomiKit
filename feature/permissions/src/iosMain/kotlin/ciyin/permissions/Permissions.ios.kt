package ciyin.permissions

import ciyin.platform.Context
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionRecordPermissionDenied
import platform.AVFAudio.AVAudioSessionRecordPermissionGranted
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusDenied
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
import platform.AVFoundation.AVAuthorizationStatusRestricted
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import platform.Contacts.CNAuthorizationStatusAuthorized
import platform.Contacts.CNAuthorizationStatusDenied
import platform.Contacts.CNAuthorizationStatusNotDetermined
import platform.Contacts.CNAuthorizationStatusRestricted
import platform.Contacts.CNContactStore
import platform.Contacts.CNEntityType
import platform.CoreBluetooth.CBCentralManager
import platform.CoreBluetooth.CBCentralManagerDelegateProtocol
import platform.CoreBluetooth.CBManager
import platform.CoreBluetooth.CBManagerAuthorizationAllowedAlways
import platform.CoreBluetooth.CBManagerAuthorizationDenied
import platform.CoreBluetooth.CBManagerAuthorizationNotDetermined
import platform.CoreBluetooth.CBManagerAuthorizationRestricted
import platform.CoreBluetooth.CBManagerStateUnsupported
import platform.CoreLocation.CLAuthorizationStatus
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLAuthorizationStatusRestricted
import platform.CoreLocation.kCLLocationAccuracyBest
import platform.CoreMotion.CMAuthorizationStatusAuthorized
import platform.CoreMotion.CMAuthorizationStatusDenied
import platform.CoreMotion.CMAuthorizationStatusNotDetermined
import platform.CoreMotion.CMAuthorizationStatusRestricted
import platform.CoreMotion.CMMotionActivityManager
import platform.EventKit.EKAuthorizationStatusAuthorized
import platform.EventKit.EKAuthorizationStatusDenied
import platform.EventKit.EKAuthorizationStatusNotDetermined
import platform.EventKit.EKAuthorizationStatusRestricted
import platform.EventKit.EKEntityType
import platform.EventKit.EKEventStore
import platform.Foundation.NSBundle
import platform.Foundation.NSDate
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSURL
import platform.Foundation.dateWithTimeIntervalSinceNow
import platform.Photos.PHAuthorizationStatusAuthorized
import platform.Photos.PHAuthorizationStatusDenied
import platform.Photos.PHAuthorizationStatusLimited
import platform.Photos.PHAuthorizationStatusNotDetermined
import platform.Photos.PHAuthorizationStatusRestricted
import platform.Photos.PHPhotoLibrary
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusDenied
import platform.UserNotifications.UNAuthorizationStatusEphemeral
import platform.UserNotifications.UNAuthorizationStatusNotDetermined
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNUserNotificationCenter
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.darwin.NSObject

/** 在系统回调完成前持有 manager 与 delegate 的活动请求。 */
private val activeRequests = mutableSetOf<NSObject>()

/**
 * 需要显式释放平台 delegate 的权限请求句柄。
 *
 * @property owner 需要强引用的平台请求对象。
 * @property start 在主线程发起系统请求。
 * @property cancel 取消请求并释放 delegate。
 */
private data class RetainedRequestHandle(
    val owner: NSObject,
    val start: () -> Unit,
    val cancel: () -> Unit,
)

/** iOS 权限实现。 */
actual object Permissions {
    /** 按调用顺序逐项请求，避免多个系统授权回调相互竞争。 */
    actual suspend fun request(
        context: Context,
        vararg permissions: Permission,
    ): PermissionRequestResult {
        val statuses = linkedMapOf<Permission, PermissionStatus>()
        permissions.distinct().forEach { permission ->
            statuses[permission] = requestPermission(permission)
        }
        return PermissionRequestResult(statuses)
    }

    /** 查询 iOS 当前权限状态，不阻塞调用线程。 */
    actual suspend fun getStatus(context: Context, permission: Permission): PermissionStatus =
        when (permission) {
            Permission.Camera -> cameraStatus()
            Permission.Microphone -> microphoneStatus()
            Permission.MediaImages,
            Permission.MediaVideo,
            -> photoLibraryStatus()
            Permission.LocationCoarse,
            Permission.LocationFine,
            Permission.LocationBackground,
            -> locationStatus(permission)
            Permission.BluetoothConnect,
            Permission.BluetoothScan,
            Permission.BluetoothAdvertise,
            -> bluetoothStatus()
            Permission.Sensors,
            Permission.Motion,
            -> motionStatus()
            Permission.Notifications -> notificationStatus()
            Permission.Contacts -> contactsStatus()
            Permission.Calendar -> calendarStatus()
            Permission.Storage,
            Permission.PictureInPicture,
            Permission.Internet,
            -> PermissionStatus.Granted
            Permission.BackgroundTasks -> backgroundTasksStatus()
            Permission.MediaAudio,
            Permission.NearbyWifi,
            Permission.CallLog,
            Permission.Phone,
            Permission.Sms,
            Permission.ReadPhoneState,
            Permission.IgnoreBatteryOptimizations,
            Permission.SystemAlertWindow,
            Permission.RequestInstallPackages,
            Permission.GetInstalledApps,
            Permission.Shortcut,
            -> PermissionStatus.Unsupported
    }

    /** 打开 iOS 应用设置页。 */
    actual fun openAppSettings(context: Context, vararg permissions: Permission) {
        dispatch_async(dispatch_get_main_queue()) {
            val settingsUrl = NSURL.URLWithString(UIApplicationOpenSettingsURLString)
                ?: return@dispatch_async
            UIApplication.sharedApplication.openURL(settingsUrl, emptyMap<Any?, Any?>()) { }
        }
    }
}

/** 请求单个权限；已确定或不支持的状态不会重复触发系统弹窗。 */
private suspend fun requestPermission(permission: Permission): PermissionStatus {
    val current = Permissions.getStatus(IosPermissionContext, permission)
    if (current != PermissionStatus.NotDetermined) return current

    return when (permission) {
        Permission.Camera -> requestCamera()
        Permission.Microphone -> requestMicrophone()
        Permission.MediaImages,
        Permission.MediaVideo,
        -> requestPhotoLibrary()
        Permission.LocationCoarse,
        Permission.LocationFine,
        Permission.LocationBackground,
        -> requestLocation(permission)
        Permission.BluetoothConnect,
        Permission.BluetoothScan,
        Permission.BluetoothAdvertise,
        -> requestBluetooth()
        Permission.Sensors,
        Permission.Motion,
        -> requestMotion()
        Permission.Notifications -> requestNotifications()
        Permission.Contacts -> requestContacts()
        Permission.Calendar -> requestCalendar()
        else -> current
    }
}

/** iOS actual Context 没有运行时数据，内部查询可复用空实例。 */
private object IosPermissionContext : Context()

/** 查询相机权限。 */
private fun cameraStatus(): PermissionStatus = when (
    AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)
) {
    AVAuthorizationStatusAuthorized -> PermissionStatus.Granted
    AVAuthorizationStatusDenied -> PermissionStatus.PermanentlyDenied
    AVAuthorizationStatusRestricted -> PermissionStatus.Restricted
    AVAuthorizationStatusNotDetermined -> PermissionStatus.NotDetermined
    else -> PermissionStatus.Unsupported
}

/** 请求相机权限。 */
private suspend fun requestCamera(): PermissionStatus = awaitStatus { complete ->
    AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) {
        complete(cameraStatus())
    }
}

/** 查询麦克风权限。 */
private fun microphoneStatus(): PermissionStatus = when (AVAudioSession.sharedInstance().recordPermission) {
    AVAudioSessionRecordPermissionGranted -> PermissionStatus.Granted
    AVAudioSessionRecordPermissionDenied -> PermissionStatus.PermanentlyDenied
    else -> PermissionStatus.NotDetermined
}

/** 请求麦克风权限。 */
private suspend fun requestMicrophone(): PermissionStatus = awaitStatus { complete ->
    AVAudioSession.sharedInstance().requestRecordPermission {
        complete(microphoneStatus())
    }
}

/** 查询照片库权限，有限照片访问视为已授权。 */
private fun photoLibraryStatus(): PermissionStatus = when (PHPhotoLibrary.authorizationStatus()) {
    PHAuthorizationStatusAuthorized,
    PHAuthorizationStatusLimited,
    -> PermissionStatus.Granted
    PHAuthorizationStatusDenied -> PermissionStatus.PermanentlyDenied
    PHAuthorizationStatusRestricted -> PermissionStatus.Restricted
    PHAuthorizationStatusNotDetermined -> PermissionStatus.NotDetermined
    else -> PermissionStatus.Unsupported
}

/** 请求照片库权限。 */
private suspend fun requestPhotoLibrary(): PermissionStatus = awaitStatus { complete ->
    PHPhotoLibrary.requestAuthorization { complete(photoLibraryStatus()) }
}

/** 查询定位权限，并区分后台定位是否获得始终允许。 */
private fun locationStatus(permission: Permission): PermissionStatus = when (
    CLLocationManager.authorizationStatus()
) {
    kCLAuthorizationStatusAuthorizedAlways -> PermissionStatus.Granted
    kCLAuthorizationStatusAuthorizedWhenInUse -> if (permission == Permission.LocationBackground) {
        PermissionStatus.NotDetermined
    } else {
        PermissionStatus.Granted
    }
    kCLAuthorizationStatusDenied -> PermissionStatus.PermanentlyDenied
    kCLAuthorizationStatusRestricted -> PermissionStatus.Restricted
    kCLAuthorizationStatusNotDetermined -> PermissionStatus.NotDetermined
    else -> PermissionStatus.Unsupported
}

/** 请求定位权限并在结束前强持有 CLLocationManager 与 delegate。 */
private suspend fun requestLocation(permission: Permission): PermissionStatus =
    awaitRetainedRequest { complete ->
        val request = LocationPermissionRequest(permission, complete)
        RetainedRequestHandle(request, request::start, request::cancel)
    }

/** 查询蓝牙权限。 */
private fun bluetoothStatus(): PermissionStatus = when (CBManager.authorization) {
    CBManagerAuthorizationAllowedAlways -> PermissionStatus.Granted
    CBManagerAuthorizationDenied -> PermissionStatus.PermanentlyDenied
    CBManagerAuthorizationRestricted -> PermissionStatus.Restricted
    CBManagerAuthorizationNotDetermined -> PermissionStatus.NotDetermined
    else -> PermissionStatus.Unsupported
}

/** 请求蓝牙权限并在结束前强持有 CBCentralManager 与 delegate。 */
private suspend fun requestBluetooth(): PermissionStatus =
    awaitRetainedRequest { complete ->
        val request = BluetoothPermissionRequest(complete)
        RetainedRequestHandle(request, request::start, request::cancel)
    }

/** 查询运动与传感器权限。 */
private fun motionStatus(): PermissionStatus = when (CMMotionActivityManager.authorizationStatus()) {
    CMAuthorizationStatusAuthorized -> PermissionStatus.Granted
    CMAuthorizationStatusDenied -> PermissionStatus.PermanentlyDenied
    CMAuthorizationStatusRestricted -> PermissionStatus.Restricted
    CMAuthorizationStatusNotDetermined -> PermissionStatus.NotDetermined
    else -> PermissionStatus.Unsupported
}

/** 通过最小运动数据查询触发系统授权。 */
private suspend fun requestMotion(): PermissionStatus = awaitStatus { complete ->
    val manager = CMMotionActivityManager()
    manager.queryActivityStartingFromDate(
        NSDate.dateWithTimeIntervalSinceNow(-1.0),
        NSDate(),
        NSOperationQueue.mainQueue,
    ) { _, _ ->
        complete(motionStatus())
    }
}

/** 异步查询通知权限，避免信号量阻塞主线程。 */
private suspend fun notificationStatus(): PermissionStatus = awaitStatus { complete ->
    UNUserNotificationCenter.currentNotificationCenter()
        .getNotificationSettingsWithCompletionHandler { settings ->
            complete(
                when (settings?.authorizationStatus ?: UNAuthorizationStatusNotDetermined) {
                    UNAuthorizationStatusAuthorized,
                    UNAuthorizationStatusProvisional,
                    UNAuthorizationStatusEphemeral,
                    -> PermissionStatus.Granted
                    UNAuthorizationStatusDenied -> PermissionStatus.PermanentlyDenied
                    UNAuthorizationStatusNotDetermined -> PermissionStatus.NotDetermined
                    else -> PermissionStatus.Unsupported
                },
            )
        }
}

/** 请求通知权限，临时授权同样按已授权处理。 */
private suspend fun requestNotifications(): PermissionStatus = awaitStatus { complete ->
    UNUserNotificationCenter.currentNotificationCenter().requestAuthorizationWithOptions(
        UNAuthorizationOptionAlert or UNAuthorizationOptionSound,
    ) { _, _ ->
        UNUserNotificationCenter.currentNotificationCenter()
            .getNotificationSettingsWithCompletionHandler { settings ->
                complete(
                    when (settings?.authorizationStatus ?: UNAuthorizationStatusNotDetermined) {
                        UNAuthorizationStatusAuthorized,
                        UNAuthorizationStatusProvisional,
                        UNAuthorizationStatusEphemeral,
                        -> PermissionStatus.Granted
                        UNAuthorizationStatusDenied -> PermissionStatus.PermanentlyDenied
                        else -> PermissionStatus.NotDetermined
                    },
                )
            }
    }
}

/** 查询通讯录权限。 */
private fun contactsStatus(): PermissionStatus = when (
    CNContactStore.authorizationStatusForEntityType(CNEntityType.CNEntityTypeContacts)
) {
    CNAuthorizationStatusAuthorized -> PermissionStatus.Granted
    CNAuthorizationStatusDenied -> PermissionStatus.PermanentlyDenied
    CNAuthorizationStatusRestricted -> PermissionStatus.Restricted
    CNAuthorizationStatusNotDetermined -> PermissionStatus.NotDetermined
    else -> PermissionStatus.Unsupported
}

/** 请求通讯录权限。 */
private suspend fun requestContacts(): PermissionStatus = awaitStatus { complete ->
    val contactStore = CNContactStore()
    contactStore.requestAccessForEntityType(CNEntityType.CNEntityTypeContacts) { _, _ ->
        complete(contactsStatus())
    }
}

/** 查询日历权限。 */
private fun calendarStatus(): PermissionStatus = when (
    EKEventStore.authorizationStatusForEntityType(EKEntityType.EKEntityTypeEvent)
) {
    EKAuthorizationStatusAuthorized -> PermissionStatus.Granted
    EKAuthorizationStatusDenied -> PermissionStatus.PermanentlyDenied
    EKAuthorizationStatusRestricted -> PermissionStatus.Restricted
    EKAuthorizationStatusNotDetermined -> PermissionStatus.NotDetermined
    else -> PermissionStatus.Unsupported
}

/** 请求日历权限。 */
private suspend fun requestCalendar(): PermissionStatus = awaitStatus { complete ->
    val eventStore = EKEventStore()
    eventStore.requestAccessToEntityType(EKEntityType.EKEntityTypeEvent) { _, _ ->
        complete(calendarStatus())
    }
}

/** 查询 Info.plist 是否声明后台模式。 */
private fun backgroundTasksStatus(): PermissionStatus =
    if (NSBundle.mainBundle.infoDictionary?.get("UIBackgroundModes") != null) {
        PermissionStatus.Granted
    } else {
        PermissionStatus.Unsupported
    }

/** 将单次平台回调转换为可取消挂起函数。 */
private suspend fun awaitStatus(
    start: (complete: (PermissionStatus) -> Unit) -> Unit,
): PermissionStatus = suspendCancellableCoroutine { continuation ->
    dispatch_async(dispatch_get_main_queue()) {
        if (!continuation.isActive) return@dispatch_async
        start { status ->
            if (continuation.isActive) continuation.resume(status)
        }
    }
}

/** 将需要强引用的平台请求转换为可取消挂起函数。 */
private suspend fun awaitRetainedRequest(
    create: (complete: (PermissionStatus) -> Unit) -> RetainedRequestHandle,
): PermissionStatus = suspendCancellableCoroutine { continuation ->
    dispatch_async(dispatch_get_main_queue()) {
        if (!continuation.isActive) return@dispatch_async
        val request = create { status ->
            if (continuation.isActive) continuation.resume(status)
        }
        activeRequests += request.owner
        continuation.invokeOnCancellation {
            dispatch_async(dispatch_get_main_queue()) { request.cancel() }
        }
        request.start()
    }
}

/** CLLocationManager 的单次定位授权请求。 */
private class LocationPermissionRequest(
    private val permission: Permission,
    private val complete: (PermissionStatus) -> Unit,
) : NSObject(), CLLocationManagerDelegateProtocol {
    /** 请求期间持有的定位管理器。 */
    private lateinit var manager: CLLocationManager

    /** 防止新旧 delegate 回调重复完成。 */
    private var completed = false

    /** 创建 manager、设置 delegate 并请求对应级别的定位授权。 */
    fun start() {
        if (completed) return
        manager = CLLocationManager()
        manager.delegate = this
        if (permission == Permission.LocationFine) manager.desiredAccuracy = kCLLocationAccuracyBest
        if (permission == Permission.LocationBackground) {
            manager.requestAlwaysAuthorization()
        } else {
            manager.requestWhenInUseAuthorization()
        }
    }

    /** 兼容旧版 iOS 的授权变化回调。 */
    override fun locationManager(
        manager: CLLocationManager,
        didChangeAuthorizationStatus: CLAuthorizationStatus,
    ) {
        finishIfDetermined(locationStatus(permission))
    }

    /** 处理 iOS 14 及以上授权变化回调。 */
    override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
        finishIfDetermined(locationStatus(permission))
    }

    /** 取消并释放定位 delegate。 */
    fun cancel() = finish(null)

    /** 仅在状态已确定时完成请求。 */
    private fun finishIfDetermined(status: PermissionStatus) {
        val requestStatus = if (
            permission == Permission.LocationBackground &&
            status == PermissionStatus.NotDetermined &&
            CLLocationManager.authorizationStatus() == kCLAuthorizationStatusAuthorizedWhenInUse
        ) {
            PermissionStatus.PermanentlyDenied
        } else {
            status
        }
        if (requestStatus != PermissionStatus.NotDetermined) finish(requestStatus)
    }

    /** 单次完成并释放强引用。 */
    private fun finish(status: PermissionStatus?) {
        if (completed) return
        completed = true
        if (::manager.isInitialized) manager.delegate = null
        activeRequests -= this
        status?.let(complete)
    }
}

/** CBCentralManager 的单次蓝牙授权请求。 */
private class BluetoothPermissionRequest(
    private val complete: (PermissionStatus) -> Unit,
) : NSObject(), CBCentralManagerDelegateProtocol {
    /** 请求期间持有的蓝牙管理器。 */
    private lateinit var manager: CBCentralManager

    /** 防止 delegate 重复完成。 */
    private var completed = false

    /** 创建 manager 以触发蓝牙授权。 */
    fun start() {
        if (completed) return
        manager = CBCentralManager(this, null)
    }

    /** 蓝牙管理器状态初始化后读取授权结果。 */
    override fun centralManagerDidUpdateState(central: CBCentralManager) {
        val status = if (central.state == CBManagerStateUnsupported) {
            PermissionStatus.Unsupported
        } else {
            bluetoothStatus()
        }
        if (status != PermissionStatus.NotDetermined) finish(status)
    }

    /** 取消并释放蓝牙 delegate。 */
    fun cancel() = finish(null)

    /** 单次完成并释放强引用。 */
    private fun finish(status: PermissionStatus?) {
        if (completed) return
        completed = true
        if (::manager.isInitialized) manager.delegate = null
        activeRequests -= this
        status?.let(complete)
    }
}
