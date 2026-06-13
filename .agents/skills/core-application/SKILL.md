---
name: core-application
description: Use the core/application Kotlin Multiplatform module (package ciyin.application) as NomiKit 的跨平台应用生命周期与平台 Context 装配层。Covers MultiplatformApplication、BaseAndroidApplication、desktop/iOS runApplication、LocalContext 注入、CommonContextFiles、AppFolderResolver 路径初始化。Use when 用户提到 core/application、ciyin.application、MultiplatformApplication、BaseAndroidApplication、runApplication、应用启动入口、跨平台 Application 生命周期、平台 Context 或排查本模块构建问题。
---

# core/application 使用指南

`core/application` 是 NomiKit 的跨平台应用启动与生命周期薄封装层。它负责把平台入口转换为统一的
`MultiplatformApplication`，并在 Compose 根节点注入 `ciyin.platform.LocalContext`。

本模块只做启动 glue 与平台 `Context` 装配，不承载业务初始化细节；业务初始化放在应用层的
`CommonApplication` / 平台 `Application` 子类或对应 DI 入口中。

## 模块边界

- 通用契约：`MultiplatformApplication`，包含 `context`、`onCreate()`、`onDestroy()`。
- Android 入口：`BaseAndroidApplication` 继承 `android.app.Application`，把 `onCreate` /
  `onTerminate` 转发给 `MultiplatformApplication`。
- Desktop 入口：`runApplication(createApplication, exitProcessOnExit, content)` 包装 Compose
  Desktop `application`，创建 `DesktopContext` 并注入 `LocalContext`。
- iOS 入口：`runApplication(createApplication, configure, content)` 返回 `ComposeUIViewController`，
  创建 `IosContext` 并注入 `LocalContext`。
- 平台文件目录：通过 `CommonContextFiles` 描述 data/cache/media cache 目录，不要让调用侧自行拼平台路径。

## 使用方式

Android 平台继承 `BaseAndroidApplication`，只提供统一应用实例：

```kotlin
class AndroidApplication : BaseAndroidApplication() {
    override val application: MultiplatformApplication = InternalApplication(this)

    private class InternalApplication(
        override val context: Context,
    ) : CommonApplication()
}
```

Desktop / iOS 平台优先调用顶层 `runApplication`：

```kotlin
fun main(args: Array<String>) = runApplication(::DesktopApplication) {
    App()
}

fun MainViewController() = runApplication(::IosApplication) {
    App()
}
```

注意事项：

- `createApplication` 必须接收本模块创建的平台 `Context`，避免在调用侧手动 new `DesktopContext` /
  `IosContext`。
- Compose 根内容必须包在 `CompositionLocalProvider(LocalContext provides context)` 之后，确保下游组件能读取当前平台上下文。
- `onCreate()` 用于启动 DI、日志、全局配置等应用初始化；`onDestroy()` 用于释放应用级资源。
- 不要在 `core/application` 引入 `app:*`、`business:*`、`feature:*` 依赖，保持 core 层在依赖方向底部。

## 修改平台入口时

- Android：保持 `super.onCreate()` 先执行，再调用 `application.onCreate()`；`onTerminate()` 只适合模拟器/调试生命周期，不要依赖它做必须执行的持久化。
- Desktop：文件目录仍通过 `AppFolderResolver.resolve(AppInfo(...))` 生成；新增目录字段时优先扩展
  `CommonContextFiles` 或 `ciyin.platform` 的平台抽象。
- iOS：系统目录通过 `NSSearchPathForDirectoriesInDomains` 获取，访问 Foundation API 时保留必要的
  `@OptIn(ExperimentalForeignApi::class)`。
- 新增平台时：先在 `ciyin.platform` 建好对应 `Context`，再在本模块提供平台入口；不要把平台判断塞进
  `commonMain`。

## 与其他模块的边界

- `core/platform` 负责 `Context`、`LocalContext`、`CommonContextFiles` 等平台抽象。
- `component/koin` 只作为初始化依赖的基础设施；实际 Koin module 装配不放在本模块。
- `app/shared` 的 `CommonApplication` 负责业务级初始化，例如 `initKoin(context)`。
- UI 根组件只消费已注入的 `LocalContext`，不要绕过本模块重新构造上下文。

## 代码约定

- 新增 Kotlin API 遵守项目规则补中文 KDoc。
- public API 尽量保持小而稳定；新增入口前先确认是否只是应用层需求。
- 平台 actual/入口函数命名要反映真实平台语义，避免把 desktop 命名复制到 iOS 等其它平台。
- 处理初始化错误时让异常暴露或转换为明确错误模型，不要静默吞掉。

## 验证命令

修改本模块后优先运行：

```powershell
.\gradlew.bat :core:application:compileCommonMainKotlinMetadata --console=plain
```

涉及平台入口时按改动范围补充：

```powershell
.\gradlew.bat :core:application:compileKotlinDesktop --console=plain
.\gradlew.bat :core:application:compileDebugKotlinAndroid --console=plain
```
