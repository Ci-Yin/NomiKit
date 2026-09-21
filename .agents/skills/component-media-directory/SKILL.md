---
name: component-media-directory
description: Use NomiKit's component/media-directory Kotlin Multiplatform module (package ciyin.media.directory) as the KoinBoot adapter for feature/media-directory. Covers MediaDirectoryAutoConfiguration's onMissInstances semantics, MediaDirectoryBootInitializer entry point, the contract between component and feature modules, host override patterns, and the test surface that locks the fallback wiring. Use when adding a Koin module to consume this adapter, debugging NoBeanFoundException for MediaDirectorySession, or auditing whether a consumer page is providing DirectoryPickerLauncher at the right layer (Koin vs. Composable).
---

# component/media-directory 使用指南

`component/media-directory` 是 NomiKit 中 `feature/media-directory` 的 KoinBoot 适配器；包名同样是
`ciyin.media.directory`，但代码仅限公共层（不依赖 Android / Desktop / iOS API），专注装配与 fallback。

## 一、模块结构

- `commonMain`
    - `MediaDirectoryAutoConfiguration`：
      `koinAutoConfiguration { module { onMissInstances<MediaDirectorySession> { ... } } }`，在 Koin
      中尚未声明 `MediaDirectorySession` 时提供默认实现。
    - `MediaDirectoryBootInitializer`（顶层 `val`）：把 `MediaDirectoryAutoConfiguration` 注册到
      `runKoinBoot`，由消费模块在 `koinBootInitializer { includes(...) }` 中包含。
- `desktopTest`：`MediaDirectoryAutoConfigurationTest`，验证默认装配、用户自定义 binding 优先于
  fallback。

## 二、装配语义

```text
runKoinBoot {
    MediaDirectoryBootInitializer()
    modules(module {
        // 可选：宿主已绑定 SAF DirectoryPickerLauncher
        single<DirectoryPickerLauncher> { rememberActivityDirectoryPickerLauncher() }
        // 可选：宿主已经声明了 MediaDirectorySession（占位实现、AOSP mock 等）
        single<MediaDirectorySession> { FakeMediaDirectorySession() }
    })
}
```

- `onMissInstances<MediaDirectorySession>`：仅在 Koin 中没有 `MediaDirectorySession` 时调用
  `createMediaDirectorySession(get<Context>(), getOrNull())`。
- `getOrNull<DirectoryPickerLauncher>()`：即使宿主没有声明 launcher，自动配置仍可继续构造
  session；Android 上 `createMediaDirectorySession` 不再因 launcher 为 null 抛错，只在后续
  `requestAccess` 调用时延迟报错。这样 `validate` / `commit` / `delete` / `exists` 路径在页面级
  AccessFactory 已喂入 Token 时不受 Koin 影响。
- 业务 Koin 注册的 `MediaDirectorySession`（如有）会通过 `onMissInstances` 短路 fallback。

## 三、与 feature/media-directory 的差异

| 维度   | feature/media-directory              | component/media-directory                                    |
|------|--------------------------------------|--------------------------------------------------------------|
| 包名   | ciyin.media.directory                | ciyin.media.directory                                        |
| 平台源集 | common / android / desktop / ios     | common + desktopTest                                         |
| 主要职责 | 公共契约 + 平台实现                          | KoinBoot 适配                                                  |
| 顶层入口 | 无                                    | `val MediaDirectoryBootInitializer: KoinBootInitializer`     |
| 自动配置 | 无                                    | `val MediaDirectoryAutoConfiguration: KoinAutoConfiguration` |
| 依赖   | core/io / core/platform / coroutines | component/koin（api feature/mediaDirectory）                   |

## 四、消费方接入

- `:app:shared` 的 `koinBootInitializer { includes(...) }` 列表里加入
  `projects.component.mediaDirectory`，由
  `build/generated/koin_boot_initializer/AppBootInitializer.kt` 聚合到 `AppModules`。
- 业务页面（一般是 `:app:sample`）通过 `rememberMediaDirectoryAccessFactory()`（app-local
  expect/actual）准备页面级 SAF launcher；这是**推荐路径**，不应污染 Koin 单例。
- 真机/桌面冒烟时若 `koin.getOrNull<MediaDirectorySession>()` 仍为 `null`，第一反应是看
  `:app:shared/build.gradle.kts` 的 `includes` 是否包含 `projects.component.mediaDirectory`。

## 五、宿主覆盖方式

- 想跑自定义实现：声明同类型 binding，例如：
  ```kotlin
  module {
      single<MediaDirectorySession> { FakeMediaDirectorySession() }
  }
  ```
- 想跑真实平台但保留 SAF 测试：根据页面模式，把 `DirectoryPickerLauncher` 注入到 ViewModel 的
  `accessFactoryOverride`，让自动配置的最末 `getOrNull()` 仍然为 `null`——Android
  `createMediaDirectorySession` 会在 `requestAccess` 时抛 `Unsupported` 兜底。
- 不要用 `koin.loadModules` 重复注册覆盖默认实现；keep 它 `onMissInstances`，依赖 Koin 实例唯一性。

## 六、修改注意

- 修改 `MediaDirectoryAutoConfiguration` 时保持 `onMissInstances<MediaDirectorySession>`
  的语义；任何依赖方向变化都要重新确认 `feature/media-directory` 中 `createMediaDirectorySession` 的
  contract。
- 不要新增 `LifecycleExtender`；本模块只承担装配，没有全局副作用。
- 不要新增 `KoinPropInstance`；当前没有面向用户的可配置项。
- 新增 `MediaDirectoryAccessFactory` / `DirectoryPickerLauncher` 的二次封装时，必须避免引入业务态；保持只装配、不持有状态。
- 公共类、属性、扩展函数必须有中文 KDoc。

## 七、测试位置与运行命令

```bash
.\gradlew.bat :component:media-directory:desktopTest --console=plain
```

最小相关编译：

```bash
.\gradlew.bat :component:media-directory:compileCommonMainKotlinMetadata --console=plain
.\gradlew.bat :component:media-directory:assemble --console=plain
```

修改本模块后请同步更新 `feature-media-directory/SKILL.md` 与本 skill。
