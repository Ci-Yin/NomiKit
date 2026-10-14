---
name: file-picker
description: Use the feature/file-picker Kotlin Multiplatform module (package ciyin.file.picker) for Compose 跨平台文件选择、相册选择、目录选择、文件保存与移动端拍照。Covers rememberFilePicker（单选/多选两个重载，相册选择即 FileType.Image/Video/ImageAndVideo）、rememberDirectoryPicker、rememberFileSaver + FileSaverLauncher、rememberCameraPicker + isCameraPickerSupported（仅 Android/iOS 拍照、不支持录像）、FileType、FileMode、PickerResultLauncher，回调统一返回 core:io 的 ciyin.io.File，底层封装 FileKit（io.github.vinceglb.filekit）dialogs-compose。Use when 用户要在 NomiKit 中新增或维护文件/相册选择、目录选择、保存到本地、拍照，或排查该模块 API、ciyin.io.File 返回约定、web/Android SAF/相机平台差异与构建问题。
---

# File Picker 使用指南

`feature/file-picker` 基于 [FileKit](https://github.com/vinceglb/FileKit)（
`io.github.vinceglb.filekit`，版本见 `gradle/libs.versions.toml` 的 `filekit-*`）封装跨平台的 Compose
文件选择、相册选择、目录选择、文件保存与移动端拍照。公共包名统一为 `ciyin.file.picker`；文件 / 相册 /
目录 / 保存封装在 `commonMain`（无平台代码），**拍照因 FileKit 相机 API 仅存在于移动端变体，采用
expect/actual**（`commonMain` expect + `androidMain` / `iosMain` 实现 + `desktopMain` 不支持桩）。

所有回调返回 `core:io` 的 `ciyin.io.File`（okio 封装），**不再暴露裸路径字符串或 FileKit
的 `PlatformFile`**。因此模块对 `core:io` 是 `api` 依赖，调用方无需单独引入即可看到 `File`。

## 依赖与构建

`feature/file-picker/build.gradle.kts` 关键项：

```kotlin
plugins {
    `multiplatform-lib-targets`            // 约定插件：注册 android/desktop/ios 目标，自动注入 core:system / core:testing
    id(libs.plugins.jetbrains.compose)
    id(libs.plugins.compose.compiler)
}

kotlin {
    sourceSets.commonMain.dependencies {
        api(projects.core.io)              // File 出现在公开签名，必须 api
        implementation(compose.runtime)
        implementation(libs.filekit.dialogs)
        implementation(libs.filekit.dialogs.compose)
    }
}
```

- 模块引用一律用类型安全访问器 `projects.feature.filePicker`（kebab→camel），禁止 `project(":...")`。
- 新增第三方依赖先查 `gradle/libs.versions.toml`，不要在 build 脚本里写裸版本号。

## 文件选择

`rememberFilePicker` 有两个重载：简化版（单选）与泛型版（`FileMode` 决定单选/多选）。二者都返回
`PickerResultLauncher`，调用其 `launch()` 弹出系统对话框。

```kotlin
// 单选：任意文件
val singlePicker = rememberFilePicker(type = FileType.File()) { file: File? ->
    // file 为 null 表示未选中；用户取消时不回调
}

// 多选：仅图片，可限制数量
val multiplePicker = rememberFilePicker(
    type = FileType.Image,
    mode = FileMode.Multiple(maxItems = 9),
) { files: List<File?> ->
    // 取消时为空列表
}

Button(onClick = singlePicker::launch) { Text("选择文件") }
```

- `FileType`：`Image` / `Video` / `ImageAndVideo` / `File(extensions)`；`File` 支持 `vararg`、
  `List<String>`、单个 `String` 及可空 `Set<String>?`（null 表示不限扩展名）。
- `FileMode.Single` → 结果 `File?`；`FileMode.Multiple(maxItems)` → 结果 `List<File?>`（`maxItems` 为
  null 表示不限）。
- `directory: File?` 为初始目录，`title: String?` 为对话框标题，均可为 null 用系统默认。

## 相册选择（从系统图库选图片/视频）

「从系统相册选择图片/视频」**没有独立 API**，就是给 `rememberFilePicker` 传 `FileType.Image` /
`FileType.Video` / `FileType.ImageAndVideo`：Android 走系统 Photo Picker、iOS 走 PHPicker、desktop
走文件对话框（按类型过滤）。

```kotlin
val videoPicker = rememberFilePicker(
    type = FileType.Video,
    mode = FileMode.Multiple(),
) { files: List<File?> -> /* 选中的相册视频 */ }
```

> 边界：本模块是**从相册读取 / 选择**（选入）；`feature/media-library`（`ciyin.media.library`）是*
*把本地文件发布 / 写入**系统相册（MediaStore / Photos，写出）。二者方向相反、互补，不要混用。相册**选视频支持
**，但**拍摄视频不支持**（见「拍照」）。

## 目录选择

```kotlin
val directoryPicker = rememberDirectoryPicker { dir: File? ->
    // dir 为选中目录；用户取消时不回调
}
Button(onClick = directoryPicker::launch) { Text("选择目录") }
```

`rememberDirectoryPicker(title, directory, onResult)` 只支持单选目录，返回 `PickerResultLauncher`。

## 文件保存

`rememberFileSaver` 返回 `FileSaverLauncher`；调用 `launch(bytes, suggestedName, extension)`
弹出「保存到」对话框并**写入字节**，落盘后回调 `File?`。

```kotlin
val saver = rememberFileSaver { saved: File? ->
    // saved 为落盘后的文件；用户取消时不回调
}
Button(onClick = {
    saver.launch(
        bytes = "内容".encodeToByteArray(),
        suggestedName = "demo",       // 不含扩展名
        extension = "txt",            // 不含点，可空
    )
}) { Text("保存文件") }
```

`rememberFileSaver` / `FileSaverLauncher.launch` 内部走 FileKit 的字节写入重载（唯一真正写文件的入口），该重载在
FileKit 中标记为 `@Deprecated`（web 不支持），已在实现处 `@Suppress("DEPRECATION")`；调用方无需处理。

## 拍照（仅移动端 Android / iOS）

`rememberCameraPicker` 打开系统相机拍照，落盘后回调 `ciyin.io.File?`（用户取消时回调 `null`），返回
`PickerResultLauncher`。**FileKit 0.12.0 相机仅支持拍照（`FileKitCameraType.Photo`），不支持录像**
；「选视频」走相册选择，「拍视频」当前不可得。

```kotlin
val cameraPicker = rememberCameraPicker { file: File? ->
    // file 为拍摄并落盘后的文件；用户取消时为 null
}
if (isCameraPickerSupported) {                 // desktop 为 false，务必门控，避免出现点击无响应的按钮
    Button(onClick = cameraPicker::launch) { Text("拍照") }
}
```

- **平台支持由 `isCameraPickerSupported` 暴露**：Android / iOS 为 `true`，Desktop 为 `false`（FileKit
  0.12.0 相机仅移动端）。UI 必须据此门控入口，desktop 的 `rememberCameraPicker` 为不支持桩（`launch()`
  无动作）。
- **实现为 expect/actual**：`commonMain` 声明 `expect`（`CameraPicker.kt`），`androidMain` / `iosMain` 调
  FileKit `rememberCameraPickerLauncher`（该符号在 FileKit `mobileMain`，即 `-android` / `-ios*` 变体内，
  `-jvm` 变体没有），`desktopMain` 为桩（`CameraPicker.desktop.kt`）。因本工程无共享 `mobileMain`
  ，android / ios 两端 actual 各写一份（内容基本相同）。
- **结果转换复用** `internal fun PlatformFile?.toIoFileOrNull()`（`FilePickerMapper.kt`
  ，同包同模块，android/ios 源集直接可见，无需 import）。
- **iOS 宿主**：需要 `NSCameraUsageDescription`（`app/ios/ios/Info.plist` 已具备）；0.12.0 无录像，不需要
  `NSMicrophoneUsageDescription`。
- **Android 宿主**：使用 FileKit 默认 `destinationFile`（`FileKit.cacheDir/<uuid>.jpg`）时，库自带
  `FileKitFileProvider` 走 `ACTION_IMAGE_CAPTURE`，**无需修改 manifest，也不要声明 `CAMERA` 权限**
  （一旦声明就必须运行时授权，反而更麻烦）。

## 平台约束（重要）

- **web 目标未启用**：本工程 `multiplatform.enable.web/js/wasmJs` 均为 false，因此 `commonMain` 可直接引用
  FileKit 的 nonWeb-only 符号（`PlatformFile.path`、`rememberDirectoryPickerLauncher` 等），每个 target
  编译时把 commonMain 折叠进 FileKit 平台变体。**若日后启用 web，必须把目录/保存封装下沉为
  expect/actual**（web 侧 FileKit 不支持目录选择与文件保存）。
- **Android SAF 路径语义**：Android 上 FileKit 返回的 `PlatformFile.path` 可能是缓存文件路径或
  content URI；`ciyin.io.File` 直接包裹该字符串，对 content URI 的 okio 文件操作可能无效。desktop / ios
  为真实路径，无此问题。
- **相机是 FileKit `mobileMain` 符号**：`rememberCameraPickerLauncher` 只存在于 FileKit 的
  `-android` / `-ios*` 变体，`-jvm`（desktop）变体没有。因此拍照*
  *不能像文件/目录/保存那样直接放 `commonMain`**，必须 expect/actual，desktop 侧给不支持桩并用
  `isCameraPickerSupported` 门控。
- 内部映射集中在 `FilePickerMapper.kt`（`internal`）：`toFileKitType` / `toFileKitMode` / `toResult` /
  `toIoFileOrNull` / `toPickerResultLauncher`，负责在本模块类型与 FileKit 类型、`PlatformFile` 与
  `ciyin.io.File` 之间转换。新增能力优先复用这些映射器，不要在调用处手写转换。

## 演示入口

`app:sample` 提供无 ViewModel 的简单有状态 Composable 演示页 `FilePickerDemoScreen`（
`app/sample/src/commonMain/kotlin/com/ciyin/app/presentation/screen/filepicker/`
），覆盖单选文件、多选图片、相册选视频、相册选图片+视频、目录选择、文件保存，以及移动端拍照（用
`isCameraPickerSupported` 门控，desktop 隐藏），结果展示 `File.path`。文案统一走
`app/sample/src/commonMain/composeResources/values/strings.xml` 的 `file_picker_demo_*`，不要硬编码。

## 最窄验证命令

```powershell
.\gradlew.bat :feature:file-picker:compileKotlinDesktop          # desktop actual 桩
.\gradlew.bat :feature:file-picker:compileKotlinIosSimulatorArm64 # ios actual（引用 FileKit mobileMain 相机 API）
.\gradlew.bat :feature:file-picker:compileAndroidMain             # android actual
.\gradlew.bat :app:sample:compileKotlinDesktop                    # demo（desktop 下拍照按钮被门控隐藏，代码仍需编译）
.\gradlew.bat :app:sample:compileKotlinIosSimulatorArm64          # demo 移动端
.\gradlew.bat :feature:file-picker:assemble    # android + ios-metadata 全目标
```
