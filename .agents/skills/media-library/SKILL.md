---
name: media-library
description: Use the feature/media-library Kotlin Multiplatform module (package ciyin.media.library) and component/media-library KoinBoot adapter to publish local files to Android MediaStore, iOS Photos, or Desktop standard media directories, then persist references for exists/delete operations. Covers ciyin.io.File requests, collections, structured errors, direct factories, Koin injection, platform permissions and host declarations, transaction guarantees, and the app/sample demo. Use when integrating, consuming, testing, or maintaining cross-platform system media publishing in NomiKit.
---

# feature/media-library 使用指南

`feature/media-library` 是 NomiKit 的跨平台系统媒体库抽象，模块名为
`:feature:media-library`，公共包名为 `ciyin.media.library`。它只表达平台媒体库语义，不负责网络下载、
缓存目录、应用私有目录或产品的可见性策略。

`component/media-library` 是可选的 KoinBoot 适配层。feature 本身不依赖 Koin、Room、
`feature/file-downloader` 或 `app/*`。

## 公共 API

公共请求必须使用 `core/io` 的 `ciyin.io.File`，不要把 Okio `Path` 暴露到调用方：

```kotlin
import ciyin.io.File
import ciyin.media.library.MediaCollection
import ciyin.media.library.MediaPublishRequest

val request = MediaPublishRequest(
    source = File(sourcePath),
    displayName = "example.png",
    mimeType = "image/png",
    collection = MediaCollection.Images,
    relativeDirectory = null,
)
```

若调用方确认目标只包含 Android/Desktop，可以设置 `relativeDirectory = "NomiKit/Samples"`；iOS Photos
无法表达相对目录，会返回 `Unsupported`。

只有平台 API 或 `File` 没有提供的底层原语才允许在模块内部调用 `File.toPath()`。常规路径解析、
存在性检查、复制和删除优先使用 `ciyin.io.File` 与 `core/io` 扩展。

发布、检查和删除的完整闭环：

```kotlin
val published = mediaLibrary.publish(request)
val stillExists = mediaLibrary.exists(published)
mediaLibrary.delete(published)
```

调用方需要持久化 `PublishedMedia` 的全部字段。该类型未标记 `@Serializable`，feature 也不引入序列化依赖；
应用应定义自己的可序列化 DTO，保存后在读取时重建 `PublishedMedia`。其中 `platformId` 是后续 `exists` 和
`delete` 的主键；`uri` 只在平台能提供稳定 URI 时返回，不能用 `uri == null` 判断媒体是否存在。

## 创建与注入

不使用 KoinBoot 时，通过当前平台 `Context` 直接创建：

```kotlin
val mediaLibrary = createMediaLibrary(context)
```

使用 `component/media-library` 时，将模块加入宿主的 KoinBoot component dependencies，然后注入默认单例：

```kotlin
val componentDependencies = listOf<Dependency>(
    projects.component.mediaLibrary,
)

koinBootInitializer {
    includes(componentDependencies)
}

val mediaLibrary: MediaLibrary by inject()
```

生成器会收集根包 `ciyin.MediaLibraryBootInitializer`。未使用生成器的宿主需要把
`MediaLibraryBootInitializer` 纳入自己的 KoinBoot 初始化流程；自定义 module 必须先于自动配置加载，
才能覆盖默认 binding。

自动配置通过 `onMissInstances<MediaLibrary>` 注册，用户提前声明的无 qualifier 实现优先：

```kotlin
module {
    singleOf(::CustomMediaLibrary) {
        bind<MediaLibrary>()
    }
}
```

默认实例需要 Koin 中已经存在 `ciyin.platform.Context`。缺少 Context 时解析应明确失败，不创建虚假
Context，也不回退到全局静态状态。

## 平台语义

- Android 使用 MediaStore。Android 29+ 通过 `IS_PENDING` 提交，应用发布自己创建的媒体通常不需要旧存储
  权限；Android 26-28 由宿主声明并在调用前授予 `WRITE_EXTERNAL_STORAGE`。发布结果的
  `platformId` 和 `uri` 都是 MediaStore URI。`Downloads` 仅在系统 API 支持时可用。
- iOS 的 `Images` 和 `Videos` 写入 Photos，`platformId` 是 `PHAsset.localIdentifier`，`uri` 为
  `null`。`Audio`、`Downloads` 和非空 `relativeDirectory` 返回 `Unsupported`。
- Desktop 将四类 Collection 映射到 Pictures、Videos、Music、Downloads 用户标准目录。
  `platformId` 是规范化绝对路径，`uri` 通常是 `file:` URI。系统媒体索引何时刷新由操作系统决定。

`relativeDirectory` 只能是目标 Collection 下的相对目录，不得包含绝对路径、`.`、`..` 或空路径段。
`displayName` 只能是单个文件名。实现不覆盖已有同名目标。

## 宿主配置

Android 26-28 宿主按实际需要声明：

```xml
<uses-permission
    android:name="android.permission.WRITE_EXTERNAL_STORAGE"
    android:maxSdkVersion="28" />
```

iOS 若使用发布、查询和删除，需要提供 Photos 读写用途说明：

```xml
<key>NSPhotoLibraryUsageDescription</key>
<string>用于发布、检查和删除应用创建的媒体。</string>
<key>NSPhotoLibraryAddUsageDescription</key>
<string>用于将应用创建的媒体发布到系统照片库。</string>
```

模块只检查和映射平台权限结果，不弹出权限请求。权限申请由宿主 UI 或 `feature/permissions` 负责。

## 错误与事务

挂起 API 使用 `MediaLibraryException.error` 暴露结构化技术错误：

- `NotFound`：源文件或持久化引用不存在。
- `AlreadyExists`：目标命名空间已有同名媒体。
- `PermissionDenied`：平台授权或目录权限不足。
- `NoSpace`：媒体卷或目标文件系统空间不足。
- `Unsupported`：平台不能表达 Collection、相对目录或操作。
- `Io`：其他读取、写入、提交或查询失败。

`exists` 对已失效引用返回 `false`；`delete` 对已经不存在的引用幂等成功。无法判断的权限或 I/O 错误仍
抛出结构化异常。`CancellationException` 必须原样传播。

发布采用“准备、写入、校验、提交”边界：Android 回滚待处理 MediaStore 行，Desktop 删除同卷临时文件，
iOS 等待 Photos 事务完成后才返回。任何失败都不得返回半成品引用。

## Sample

示例位于
`app/sample/src/commonMain/kotlin/com/ciyin/app/ui/screen/medialibrary/MediaLibraryDemoScreen.kt`，可从 sample
首页的“系统媒体库”入口进入。页面提供 `Images / Videos / Audio / Downloads` 四类测试矩阵，分别使用内置
PNG、2 秒 H.264 MP4、2 秒单声道 WAV 和 UTF-8 文本。样本先通过 `ciyin.io.File` 写入
`cache/media-library-demo/`，再以 `relativeDirectory = null` 发布，确保图片和视频请求可直接用于 iOS Photos。

每个测试独立保留阶段、`PublishedMedia` 展示模型、存在性和结构化错误，支持单项发布、检查、删除。批量命令按
图片、视频、音频、下载顺序串行执行：`Unsupported` 和普通项目错误继续，`PermissionDenied` 与 `NoSpace`
提前终止，检查和清理会跳过没有活动引用的项目。删除或检查到不存在后保留最近结果，但释放活动引用并允许再次
发布；活动引用仍有效或尚未确认时禁止重复发布。

sample 的权限 helper 位于各平台 source set；不要把这些 UI 权限逻辑移动到 feature 模块。iOS 只为
`Images` 和 `Videos` 请求对应 Photos 权限，`Audio` 和 `Downloads` 不应先触发权限弹窗，应直接交给 feature
返回 `Unsupported`。

## 修改与验证

修改模块时同步维护公共 API、三端 actual、component 自动配置、测试、sample 和本 skill。优先运行：

```powershell
$env:ANDROID_HOME = "<Android SDK path>"
.\gradlew.bat :feature:media-library:desktopTest :feature:media-library:compileAndroidMain :feature:media-library:compileKotlinIosSimulatorArm64 --console=plain
.\gradlew.bat :component:media-library:desktopTest :component:media-library:compileAndroidMain :component:media-library:compileKotlinIosSimulatorArm64 --console=plain
.\gradlew.bat :app:shared:generateKoinBootInitializer :app:shared:compileCommonMainKotlinMetadata --console=plain
.\gradlew.bat :app:sample:desktopTest :app:sample:compileKotlinDesktop :app:sample:compileKotlinIosSimulatorArm64 --console=plain
.\gradlew.bat :app:android:assembleDebug --console=plain
```

Android MediaStore 与 iOS Photos 的最终权限、系统选择器和回滚行为还需要真机或平台测试替身验证；Desktop
测试必须注入临时目录，不能写入开发机的真实 Pictures、Videos、Music 或 Downloads。
