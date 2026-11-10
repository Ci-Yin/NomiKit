# 测试与运行

> 最近核对：2026-11-10；源码基线 `cd08542f`。本次仅静态核对构建配置与已追踪文件，未运行应用或测试。

[返回参与开发](<README.md>) · [环境准备](<setup.md>) · [构建与打包](<building.md>)

## 运行 APP 测试

调试运行与自动化测试是两件事。可以先[运行 PC APP](<#如何运行-pc-app>)验证共享 UI，但桌面效果不能代替
Android 真机、权限、系统服务或 iOS 验证。性能测试应记录构建类型、优化配置及设备，不作“所有 Debug
都很卡”的绝对判断。

### Run Configuration

IDE 可以创建本地运行配置，但本次没有找到仓库已追踪的共享运行配置，不能保证存在旧指南中的
`Run Desktop (Hot Reload)`、`Run Desktop (Normal)`、`app.android` 或 `Run iOS Debug` 名称。
按实际模块与入口创建配置；如需修改共享配置，先确认影响范围，不提交个人机器参数。

### 如何运行 PC APP

Desktop 入口与分发设置见 [Desktop 构建配置](<../../app/desktop/build.gradle.kts>)。
以下命令是待在本机确认的插件任务入口，先检查 `:app:desktop:tasks --all`：

```powershell
.\gradlew.bat :app:desktop:run --console=plain
.\gradlew.bat :app:desktop:runReleaseDistributable --console=plain
```

Windows PowerShell 使用 `.\gradlew.bat`，macOS / Linux 改用 `./gradlew`。 当前 Desktop 脚本未声明
Compose Hot Reload 插件，本文不承诺自动编译或热重载；启用此能力时应另行记录实际配置与验证结果。

### 如何运行调试版本 Android APP

在 IDE 中选择实际的 `:app:android` 模块与已连接设备创建 Android 运行配置。
命令行构建与安装入口见 [Android 构建说明](<building.md#打包-android-app>)。

### 如何运行 iOS APP

在 macOS 使用 Xcode 打开 [iOS 工程](<../../app/ios/ios.xcodeproj/project.pbxproj>)，而不是仅打开内部源码目录。
先处理 [iOS 构建说明](<building.md#打包-ios-app>) 中记录的旧构建阶段差异，再进行模拟器/真机运行；本次未执行该平台验证。

## 选择最窄验证任务

测试任务与源集以 [KMP 目标约定](<../../buildSrc/src/main/kotlin/multiplatform-lib-targets.gradle.kts>)
及目标模块构建脚本为准。 下面列举查询/执行入口，不表示本次已运行；替换模块时也要确认该模块确实应用对应约定。

| 验证目标                      | 前置条件与范围                              | 示例或查询方式                                                                 |
|-------------------------------|---------------------------------------------|--------------------------------------------------------------------------------|
| 某模块共享逻辑的 Desktop 单测 | 模块启用 Desktop；不覆盖 Android/iOS 运行时 | `./gradlew :core:lang:desktopTest --console=plain`                             |
| 某模块真实网络/环境集成测试   | 明确允许访问外部环境，准备相应数据和配置    | `./gradlew :feature:parser-site:desktopIntegrationTest --console=plain`        |
| KMP Android 主机测试          | 新 Android KMP 插件的 host test 已启用      | 先运行目标模块 `tasks --all`，选择 host test 任务                              |
| Android 设备测试              | 已启用 device test、连接正确设备            | 先查询目标模块的设备测试任务，再显式运行                                       |
| iOS 测试                      | macOS、Apple 工具链、iOS 目标启用           | 先确认模块注册的 `iosSimulatorArm64Test` 及资源任务                            |
| 聚合检查                      | 明确需要更大范围验证时                      | `./gradlew :core:lang:check --console=plain`；查看实际执行任务而非只看成功退出 |

`desktopIntegrationTest` 在约定脚本中由 `onlyIf` 限制为显式指定该任务时执行；默认聚合验证可能显示
SKIPPED。
`check` 不等于“所有测试都跑过”，也不能替代设备端验证或未启用平台的编译。

需要强制重新执行时，给最窄任务加 `--rerun-tasks --no-build-cache`，而不是默认执行全仓 `clean check`。
`clean` 删除构建输出，不等于清空 Gradle 构建缓存；必须区分 UP-TO-DATE、FROM-CACHE、SKIPPED 和实际测试执行。

## 单元测试与源集

NomiKit 使用多平台测试，但没有本次运行报告可支持固定测试数量保证。数量与覆盖范围以实际报告为准。
通用逻辑优先写在 `commonTest`，平台逻辑写入对应平台测试源集；执行某个平台任务，不等于所有平台都执行了共享测试。

当前 KMP 约定使用 `com.android.kotlin.multiplatform.library` 并启用 `withHostTest`、`withDeviceTest`：

| 源集/位置                                         | 用途                                                              |
|---------------------------------------------------|-------------------------------------------------------------------|
| `commonTest`                                      | 可复用的通用逻辑测试                                              |
| `desktopTest`                                     | Desktop JVM 单元测试                                              |
| `desktopIntegrationTest`                          | 独立的显式集成测试编译                                            |
| `androidHostTest` / `androidDeviceTest`           | KMP Android 主机与设备端测试；约定显式为 device test 配置测试依赖 |
| `iosTest` / `iosSimulatorArm64Test`               | 已启用 iOS 目标下的共享/模拟器测试，具体层级以实际注册源集为准    |
| Android 应用模块的 `src/test` / `src/androidTest` | 应用模块自己的单测与设备测试，不能与 KMP 库命名混用               |

旧指南中的 `androidUnitTest` / `androidInstrumentedTest` 不应直接套用到新的 KMP Android 插件。
源集共享概念可参考 [KMP 说明](<kmp.md>)，当前配置事实以构建脚本为准。

## Android 设备测试

设备测试用于 Android API、权限、系统组件及平台差异，Desktop/JVM 通过不能替代它。

1. 确认目标模块的设备测试配置、测试 runner 和实际注册任务。
2. 连接目标真机或启动模拟器，检查设备状态与所选序列号。
3. 显式执行对应模块的设备测试任务，记录设备/API 版本及测试报告。

Android 应用模块可从 `:app:android:connectedCheck` 查询/运行；KMP 库模块按新插件的实际任务列表选择，
不在文档里假定所有模块都使用同一任务名。不要求为了测试函数命名统一提高应用 `minSdk`。

是否在提交前运行设备测试取决于改动：涉及 Android 专有行为、权限、设备 I/O 或 UI 交互时应补充相应验证。
本次未找到仓库已追踪的 GitHub/Gitea workflow，不再承诺“PR CI 总会运行设备测试”；其他外部 CI 是否存在尚未确认。
无法执行时记录原因与剩余验证项，不把“稍后 CI 会跑”作为已验证依据。

## UI 测试

应用 Compose 插件的模块会由约定为 `commonTest` 增加 Compose UI 测试依赖，启用 Desktop 时还配置其测试依赖。
依赖存在不代表所有平台均有可用运行环境，也不代表某条 UI 测试跨平台通过。 新增 UI 测试时明确目标平台、测试
runner、环境及实际任务，避免用缩小 Desktop 窗口代替移动端验收。

## 验证记录

本次静态核对了测试依赖、host/device test 配置、显式集成测试门槛、应用运行入口与已追踪 CI/运行配置。 未执行
Gradle 任务发现、任何测试或应用运行；未给出测试数量或通过率。

后续运行记录至少包含：日期、源码基线、模块、命令、平台/设备、测试报告位置、结果及未验证范围。
任务成功但全部跳过时，必须写“未执行测试”，不能写“测试通过”。
