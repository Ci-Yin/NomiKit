---
name: feature-permissions
description: Use the feature/permissions Kotlin Multiplatform module (package ciyin.permissions) to query and request runtime permissions on Android, iOS, and Desktop. Covers Permission, PermissionGroup, PermissionStatus, PermissionRequestResult, Permissions APIs, Android Manifest and Activity requirements, iOS Info.plist usage descriptions, platform status semantics, settings navigation, and the sample permissions screen. Use when integrating, consuming, testing, or maintaining cross-platform permissions in NomiKit.
---

# feature/permissions 使用指南

`feature/permissions` 是 NomiKit 的跨平台运行时权限入口，模块名为 `:feature:permissions`，包名为
`ciyin.permissions`。业务侧只依赖公共 API，不直接调用 XXPermissions 或平台权限框架。

## 快速使用

```kotlin
val result = Permissions.request(
    context = context,
    Permission.Camera,
    Permission.RecordAudio,
)

when {
    result.allGranted -> showCamera()
    result.permanentlyDenied.isNotEmpty() -> Permissions.openAppSettings(
        context = context,
        Permission.Camera,
        Permission.RecordAudio,
    )
}
```

按内置权限组请求时使用便捷重载。权限会去重，并保持权限组声明顺序：

```kotlin
val result = Permissions.request(context, PermissionGroup.Camera)
val status = Permissions.getStatus(context, Permission.Camera)
val statuses = Permissions.getStatuses(context, PermissionGroup.Media)
```

`PermissionRequestResult` 提供 `granted`、`denied`、`permanentlyDenied`、`restricted`、
`unsupported`、`notDetermined` 和 `allGranted` 等只读派生结果。用户拒绝属于正常状态，不作为异常；
调用协程被取消时，取消必须继续向上传播。

## 平台语义

- Android 查询返回 `Granted` 或 `Denied`；请求后可进一步返回 `PermanentlyDenied`。调用
  `Permissions.request` 时，`Context` 必须是 `Activity`，或能沿 `ContextWrapper` 解析到 Activity。
  模块使用的 XXPermissions 26.5 仍基于旧 Support Library；AndroidX 应用必须在根
  `gradle.properties` 中同时启用 `android.useAndroidX=true` 和 `android.enableJetifier=true`，否则运行时会
  因旧 `android.support.*` 类无法解析而崩溃。
  将 `Permission.Sensors` 作为心率传感器读取权限使用；XXPermissions 会把
  `READ_HEART_RATE` 回退到旧系统的 `BODY_SENSORS`。当 `targetSdk >= 36` 时，在 Manifest 中将
  `BODY_SENSORS` 限制为 `maxSdkVersion=35`，同时声明 `android.permission.health.READ_HEART_RATE`，并为
  `VIEW_PERMISSION_USAGE` + `HEALTH_PERMISSIONS` 提供真实的健康权限说明 Activity。
- iOS 返回 `NotDetermined`、`Granted`、`PermanentlyDenied`、`Restricted` 或 `Unsupported`。
  照片有限授权、通知临时授权均视为 `Granted`；电话、短信、Wi-Fi 等无直接对应项返回
  `Unsupported`。
- Desktop 全部返回 `Unsupported`；请求立即结束，设置跳转不执行系统操作。

## 平台声明

Android 应用只声明实际使用的 `<uses-permission>`。需要运行时弹窗的请求应从当前 Activity 对应的
`ciyin.platform.Context` 发起；没有 Activity 时不要请求，可先展示不可用状态或延后到页面恢复。

iOS 应用必须为实际使用的能力补齐对应 `Info.plist` 用途说明，例如相机、麦克风、照片、定位、通讯录、
日历、蓝牙和运动。缺少用途说明可能导致系统终止应用。

`app/sample/src/androidMain/AndroidManifest.xml` 和 `app/ios/ios/Info.plist` 为了演示 14 个内置权限组而包含
全量敏感声明。Android 平台壳 `app/android` 不承载 sample 专属权限。生产应用禁止整段照搬，只能声明产品
真实使用的权限及用途说明。

## Sample 入口

示例位于
`app/sample/src/commonMain/kotlin/com/ciyin/app/ui/screen/permissions/PermissionsScreen.kt`，可从 sample
首页的“权限管理示例”进入。页面支持刷新全部状态、逐组申请和逐组打开设置，不提供一次申请全部权限。

## 修改与验证

新增权限时同步维护：

- `Permission` 枚举和相应 `PermissionGroup`。
- Android 权限映射及 sample Manifest。
- iOS 查询、请求映射及 sample Info.plist。
- Desktop `Unsupported` 契约、公共测试和本 skill。

优先运行以下最窄验证：

```powershell
.\gradlew.bat :feature:permissions:desktopTest :feature:permissions:compileAndroidMain :feature:permissions:compileKotlinDesktop :feature:permissions:compileKotlinIosSimulatorArm64 --console=plain
.\gradlew.bat :app:sample:compileCommonMainKotlinMetadata :app:sample:compileKotlinDesktop :app:sample:compileKotlinIosSimulatorArm64 --console=plain
.\gradlew.bat :app:android:assembleDebug --console=plain
```

Android 编译前必须按本机环境配置 `ANDROID_HOME` 或 `local.properties` 中的 `sdk.dir`。
