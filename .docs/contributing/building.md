# 构建与打包

> 最近核对：2026-11-10；源码基线 `cd08542f`。本页基于构建脚本静态整理，未执行构建、安装或发布。

[返回参与开发](<README.md>) · [环境准备](<setup.md>) · [测试与运行](<testing.md>)

## 运行命令前

- Windows PowerShell 使用 `.\gradlew.bat`，macOS / Linux 使用 `./gradlew`；以下 Android/Desktop 示例采用
  PowerShell。
- 平台开关与工具链限制见 [环境准备](<setup.md>)。使用完整模块路径，避免无意触发同名聚合任务。
- 插件提供的任务是否注册取决于当前平台与配置；先用目标模块的 `tasks --all` 确认。命令列表不是本次执行成功记录。

## Android 发布签名

[Android 构建配置](<../../app/android/build.gradle.kts>) 从根目录本机 `local.properties` 读取：

| 属性                     | 含义                                                      |
|--------------------------|-----------------------------------------------------------|
| `android.store.file`     | Keystore 路径；相对路径按 Android 模块的 `file(...)` 解析 |
| `android.store.password` | Keystore 密码                                             |
| `android.key.alias`      | 签名别名                                                  |
| `android.key.password`   | 私钥密码                                                  |

只有配置了 `android.store.file`，当前脚本才进入 release 签名及压缩配置分支。没有这组配置不能据此宣称已生成可发布签名产物。
密钥、密码和个人路径不得提交仓库；本文不提供生产凭据。

## 打包 Android APP

先确认 Android SDK 与签名条件，再选择一个任务：

```powershell
.\gradlew.bat :app:android:assembleDebug --console=plain
.\gradlew.bat :app:android:assembleRelease --console=plain
```

如需安装，先连接并确认目标设备，再显式运行 `:app:android:installDebug` 或
`:app:android:installRelease`。 安装任务会影响连接设备，不是纯编译检查；release 安装需要有效签名。

## 打包 iOS APP

在 macOS 中打开 [Xcode 工程](<../../app/ios/ios.xcodeproj/project.pbxproj>) 所在工程目录，配置签名和目标设备。
该工程通过 `:app:shared:embedAndSignAppleFrameworkForXcode` 接入 Kotlin Framework。

**当前阻塞需先核对：** 同一 target 还挂载了旧的 `:composeApp:embedAndSignAppleFrameworkForXcode` 阶段，
但 [模块声明](<../../settings.gradle.kts>) 没有 `:composeApp`。本文只记录差异，未修改 Xcode 工程； 修复并在
macOS 验证前，不承诺构建或归档成功。

旧文档中的 `podInstall`、`patchInfoPlist`、`buildDebugIpa`、`buildReleaseIpa` 未在本次核对的应用与构建逻辑中
找到对应实现，也未发现 Podfile，不再作为当前可用命令列出。后续打包应以实际 Xcode 工程、已注册任务和签名配置为准。

## 打包桌面应用

应用配置见 [Desktop 构建脚本](<../../app/desktop/build.gradle.kts>)；插件打包概念可参考
[Compose Multiplatform 原生分发文档](<https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-native-distribution.html>)。
外部文档本次未做联网验证。

```powershell
.\gradlew.bat :app:desktop:createReleaseDistributable --console=plain
```

输出在 Desktop 模块的 `build/compose/binaries` 下；不要手工修改生成物。只能在对应操作系统构建其原生分发产物。
当前脚本另有依赖本机绝对路径的 `buildInstaller`，不作为通用构建入口。

## 运行与测试

运行应用、选择单元测试/设备测试/显式集成测试、重复执行及结果记录，统一见
[测试与运行指南](<testing.md>)。`check` 是聚合验证任务，不代表所有平台、所有设备测试或真实网络集成测试均已执行。

## 常见构建和运行问题

### 编译报错找不到 Res

先核对 [共享模块资源配置](<../../app/shared/build.gradle.kts>) 中的 `compose.resources`、资源目录和调用处导入包，
再检查 Gradle 同步及资源生成任务的错误。不能将所有 `Res` 缺失一概归为 Compose bug。

若当前插件注册了 `:app:shared:generateComposeResClass`，可运行该任务定位生成失败；先通过 `tasks --all`
确认任务名。 修复生成输入或依赖关系，不手改生成的 `Res` 类。

### JVM 目标或平台任务不存在

见 [JDK 与 Gradle 配置差异](<setup.md#准备-jdk-与-gradle>)。任务缺失时先核对模块是否启用目标及插件是否应用，
不要直接改成全仓 `check` 或反复 `clean` 掩盖配置问题。

## 验证记录

| 检查                                     | 结果                           |
|------------------------------------------|--------------------------------|
| 模块路径、Android 签名属性、Desktop 配置 | 已静态核对源码                 |
| iOS 接入                                 | 已发现旧阶段差异；未运行或修复 |
| Gradle 任务列表、编译、打包、安装        | 未执行                         |
| 外部文档链接内容                         | 未联网验证                     |

执行后补充实际日期、操作系统、工具链、命令及成功/失败/跳过结果；本文不作平台验收声明。
