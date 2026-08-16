---
name: video-player
description: Use the feature/video-player Kotlin Multiplatform module (package ciyin.video.player) for Android ExoPlayer, Desktop VLC, and iOS AVKit playback. Covers MediampPlayer construction and lifecycle, URL playback, VideoPlayer rendering, controls, progress, gestures, audio tracks, side sheets, screenshots, Picture-in-Picture, VLC discovery, and app/sample integration. Use when 用户要在 NomiKit 中接入或维护视频播放器、播放 URL/HLS、播放器控制栏/手势/进度/音轨/侧边栏/PiP，或排查 ExoPlayer、VLCJ、AVKit、Mediamp 相关问题。
---

# feature/video-player 使用指南

`feature/video-player` 基于 Mediamp `0.0.30` 提供 Android、Desktop、iOS 的统一播放 UI 与平台适配，公共包名为 `ciyin.video.player`。

## 依赖与边界

- 调用模块使用 `implementation(projects.feature.videoPlayer)`。
- 模块可依赖 `core-platform`、`core-ui-foundation`、`core-material` 与 `core-lang`，不要反向依赖 `app/*`。
- 不在本模块接入加密媒体 SPI；需要加密能力时由调用侧先转换成 Mediamp 支持的 `MediaData`。
- 播放器、平台 `Context` 或原生对象由 Screen/平台代码持有，不放入可序列化的 `UiState`。
- 修改公开 API、平台构造方式、运行条件或验证命令时，同步更新本 skill。

## 创建播放器

Android 使用模块的 ExoPlayer 适配器，并随组合生命周期关闭：

```kotlin
val context = LocalContext.current
val scope = rememberCoroutineScope()
val player = remember(context, scope) {
    ExoPlayerMediampPlayer(
        context = context,
        parentCoroutineContext = scope.coroutineContext,
    )
}
DisposableEffect(player) {
    onDispose(player::close)
}
```

Desktop 使用模块提供的安全初始化入口，避免 VLC 原生库缺失时让链接错误逃出 Composition：

```kotlin
when (val result = rememberVlcMediampPlayer()) {
    is VlcPlayerInitializationResult.Ready -> VideoPlayer(result.player, Modifier.fillMaxSize())
    is VlcPlayerInitializationResult.Unavailable -> VlcUnavailableContent(result.cause)
}
```

- `rememberVlcMediampPlayer()` 会随组合生命周期关闭成功创建的播放器，并把 `UnsatisfiedLinkError`、`NoClassDefFoundError` 等 `LinkageError` 以及 VLCJ 的 `NativeLibraryMappingException` 转换为 `Unavailable`；其他实现异常继续抛出。
- Desktop 原生实例是 `VlcMediampPlayer`；需要下载速度 Feature 时包装为 `VlcDownloadSpeedMediampPlayer(delegate, scope)`。
- iOS 可直接使用 Mediamp 的 `rememberMediampPlayer()`，原生实例是 `AVKitMediampPlayer`；需要下载速度 Feature 时包装为 `AvKitDownloadSpeedMediampPlayer(delegate, scope)`。
- `VideoPlayer` 同时接受 Mediamp 原生实例和上述下载速度装饰器。

## 加载 URL 与渲染

提交 URL 时只拒绝空输入；用 `trim()` 去除首尾空白，保留查询参数和片段。播放器异常应映射成明确用户错误，`CancellationException` 必须继续抛出。

```kotlin
val normalizedUrl = input.trim()
require(normalizedUrl.isNotEmpty())

try {
    player.playUri(normalizedUrl)
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    onPlaybackFailed(e.message ?: "视频加载失败")
}
```

渲染入口：

```kotlin
VideoPlayer(
    player = player,
    modifier = Modifier.fillMaxSize(),
)
```

组合完整播放器时按需复用：

- `PlayerControllerBar`：播放暂停、时间、进度拖动、音量、倍速和全屏控制。
- `PlayerGestureHost`：快进、亮度/音量等手势；全屏页面显式传 `isFullscreen`。
- `PlayerTopBar`、`VideoSideSheets`、`AudioSwitcher`、`ScreenshotButton`：顶栏、侧栏、音轨与截图。
- `MediaCacheProgressProvider`：向进度条提供缓存分段；未知或不匹配的数据应返回空进度。

可见文案和无障碍标签放在模块 Compose string resources 中。优先使用 `AppTheme` 颜色、间距、形状与尺寸令牌；播放器固有尺寸使用具名常量并补中文 KDoc。

## Picture-in-Picture

使用平台工厂创建控制器，并在拥有者销毁时释放：

```kotlin
val context = LocalContext.current
val pipController = remember(context, player) {
    createPipController(context, player)
}
DisposableEffect(pipController) {
    onDispose(pipController::release)
}
```

- Android 支持进入/退出、自动进入、源矩形和小窗播放控制；宿主 Activity 必须启用系统 PiP 能力。
- iOS 使用 AVKit `AVPictureInPictureController`，Android 使用系统 PiP；Desktop 当前返回不支持控制器。调用方始终先检查 `isPipSupported`。
- Android 广播 Action 和资源必须保持 `ciyin.video.player` 命名空间。

`app:sample` 的 `VideoPlayerDemoPlayerView` 展示完整组合方式：`VideoScaffold` 负责层级，`PlayerGestureHost` 负责鼠标/触摸与快捷键，底栏接入进度、倍速、音量、音轨和全屏，顶栏接入 PiP 与设置侧栏，截图能力只在播放器提供 `Screenshots` Feature 时显示。Android 宿主同时需要 `android:supportsPictureInPicture="true"`。

## Desktop VLC 运行条件

Desktop 渲染依赖本机可发现的 VLC/libvlc：

1. 优先检查 JVM 属性 `ciyin.video.player.vlc.library.path` 指向的目录。
2. 其次检查环境变量 `CIYIN_VIDEO_PLAYER_VLC_LIBRARY_PATH` 指向的目录。
3. 打包应用继续检查 `${compose.application.resources.dir}/lib`。
4. 上述目录中没有内置 VLC 时，由 VLCJ 继续执行系统原生库发现。
5. 开发机若未安装 VLC 且未提供外部目录，`rememberVlcMediampPlayer()` 返回 `Unavailable`；编译和单测不受影响，真实播放不可用。

显式目录必须直接包含 `libvlc` 与 `libvlccore`，并保留同版本的 `plugins` 目录。通过环境变量启动 Desktop 开发任务的示例：

```powershell
$env:CIYIN_VIDEO_PLAYER_VLC_LIBRARY_PATH = "D:\tools\vlc\lib"
.\gradlew.bat :app:desktop:run --console=plain
```

不要新增指向某个产品仓库或个人开发目录的探测逻辑。

## Sample 参考

真实 URL 示例位于 `app/sample/src/commonMain/kotlin/com/ciyin/app/ui/screen/videoplayer/`。该示例使用 MVI + FlowRedux2，Screen 持有平台播放器并处理 `LoadUrl` Effect，`UiState` 只保存 URL、播放状态、媒体存在性和错误信息。

`app:sample` 不得依赖 `app:shared`；页面容器使用 Material3 `Scaffold` 和 `AppTheme`。Android 宿主需要 `android.permission.INTERNET`。Desktop VLC 初始化失败时示例应禁用播放并显示资源化错误，不能让 `LinkageError` 逃出 Composition。

## 修改与验证

新增或修改 Kotlin 类、接口、对象、枚举、函数、属性和扩展时补中文 KDoc。先运行最窄验证，再按影响平台扩展：

```powershell
.\gradlew.bat :feature:video-player:desktopTest :app:sample:desktopTest --console=plain
.\gradlew.bat :feature:video-player:compileAndroidMain :feature:video-player:compileKotlinDesktop :feature:video-player:compileKotlinIosSimulatorArm64 --console=plain
.\gradlew.bat :app:sample:compileAndroidMain :app:sample:compileKotlinDesktop :app:sample:compileKotlinIosSimulatorArm64 --console=plain
git diff --check
rg -n -i 'legacy-product-name|legacy-package-name' feature/video-player app/sample/src/commonMain/kotlin/com/ciyin/app/ui/screen/videoplayer .agents/skills/video-player
```

最后一个扫描命令中的占位词应替换为迁移来源的旧产品名和旧包名，并要求零命中。若本机存在 VLC，再运行 Desktop sample 检查 URL 加载、播放暂停、进度拖动和错误重试。
