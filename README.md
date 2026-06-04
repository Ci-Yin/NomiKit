# NomiKit

NomiKit 是一个基于 Kotlin Multiplatform 与 Compose Multiplatform
的跨平台应用开发脚手架。项目以模块化、单向依赖和可复用能力沉淀为核心，目标是在 Android、Desktop、iOS
等平台上共享业务逻辑、基础能力与大部分 UI 实现。

## 技术栈

- Kotlin Multiplatform 2.3.0
- Compose Multiplatform 1.10.0
- Android Gradle Plugin 9.1.0
- Compose Material3 / Navigation3 / Lifecycle
- Koin 4.1.1
- Ktor Client 3.4.0
- kotlinx.serialization 1.10.0
- Room / DataStore / Okio
- FlowRedux2 / Arrow

依赖版本统一维护在 `gradle/libs.versions.toml`，Gradle 插件与模块引用优先使用 Version Catalog
与类型安全项目访问器。

## 支持平台

当前工程包含以下应用入口：

- `:app:android`：Android 应用入口。
- `:app:desktop`：Desktop 应用入口。
- `:app:ios`：iOS 应用工程与 KMP Framework 集成。
- `:app:web`：Web 应用模块，默认未启用。
- `:app:shared`：跨平台业务、UI 与应用层共享代码。

默认配置见 `gradle.properties`：

- Android `compileSdk`：36
- Android `minSdk`：26
- Android `applicationId`：`com.ciyin.nomikit`
- JDK 工具链：JetBrains Runtime with JCEF 17
- 默认启用 Desktop 与 iOS，Web 默认关闭

## 项目结构

```text
NomiKit
├── app/          # 平台应用入口与跨平台业务代码
├── business/     # 可复用业务能力层
├── component/    # 第三方能力封装与适配
├── core/         # 与业务无关的基础能力
├── feature/      # 可复用通用特性
├── buildSrc/     # 自定义 Gradle 构建逻辑
├── gradle/       # Version Catalog 与 Gradle Wrapper 配置
├── .docs/        # 项目开发、构建、测试与架构文档
└── .agents/      # Agent 规则与项目技能说明
```

依赖方向遵循：

```text
app -> business / feature / component -> core
```

上层可以依赖下层，下层不能反向依赖上层。模块清单以 `settings.gradle.kts` 为准。

## 核心模块

### app

- `app:android`、`app:desktop`、`app:ios`、`app:web` 提供平台壳。
- `app:shared` 承载跨平台应用业务、页面、导航与资源。
- `app:sample` 用于示例或局部能力验证。

### business

- `business:base` 提供可复用业务中间层能力。

### component

- `component:koin`：依赖注入基础封装。
- `component:room`：KMP Room 与数据库能力封装。
- `component:data-store`：KMP DataStore 与持久化能力封装。

### core

- `core:io`、`core:platform`、`core:system`：跨平台基础能力与平台抽象。
- `core:serialization`：JSON 序列化帮助库。
- `core:coroutines`、`core:lang`：协程与语言级工具。
- `core:application`：应用级基础设施。
- `core:ui-foundation`、`core:ui-preview`、`core:testing`：Compose UI 基础、预览与测试支撑。

### feature

- `feature:serialization`：非官方序列化格式封装，目前包含 YAML 适配。
- `feature:parser`、`feature:parser-site`：解析相关能力。
- `feature:kotlin-script`：Kotlin 脚本能力。
- `feature:sdwebui`：AUTOMATIC1111 Stable Diffusion WebUI REST API 客户端。
- `feature:ai-core`：AI 引擎无关抽象层。
- `feature:ai-facade`：业务侧 AI 调用统一入口。
- `feature:ai-image-sdwebui-engine`：SD WebUI ImageEngine 实现。
- `feature:ai-chat-openai-engine`：OpenAI 兼容 ChatEngine 实现。
- `feature:ai-integrate`：AI 引擎聚合与装配层。

## 环境准备

1. 使用最新正式版 Android Studio 或 IntelliJ IDEA。
2. 安装 JetBrains Runtime with JCEF 17，并将 Gradle JDK 配置为该 JBR。
3. 安装 Android SDK，版本以 `gradle.properties` 中的 `android.compile.sdk` 为准。
4. 如需构建 iOS，需在 macOS 上安装 Xcode 与 CocoaPods。
5. 首次导入建议使用 IDE 打开根目录，等待 Gradle 同步完成。

如果本机不需要 iOS，可在本地 `gradle.properties` 中关闭：

```properties
multiplatform.enable.ios=false
```

如果只调试特定 Android ABI，也可以按需设置：

```properties
multiplatform.android.abis=arm64-v8a
```

## 常用命令

Windows PowerShell 使用 `.\gradlew.bat`，macOS / Linux 使用 `./gradlew`。以下以 Windows PowerShell 为例。

### Android

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat installDebug
.\gradlew.bat assembleRelease
```

### Desktop

```powershell
.\gradlew.bat runReleaseDistributable
.\gradlew.bat createReleaseDistributable
```

Desktop 打包结果位于 `app/desktop/build/compose/binaries`。

### iOS

```powershell
.\gradlew.bat podInstall
.\gradlew.bat patchInfoPlist
.\gradlew.bat buildDebugIpa
```

iOS 构建需要 macOS、Xcode 与 CocoaPods。

### 测试

```powershell
.\gradlew.bat check
```

如需清理缓存后重新执行：

```powershell
.\gradlew.bat clean check
```

Android Instrumented Test 需要连接设备或启动模拟器：

```powershell
.\gradlew.bat connectedCheck
```

## 开发约定

- 修改代码前先阅读 `.agents/rules/AGENTS.md`。
- 架构、构建、测试、代码风格等约定以 `.docs/contributing/` 下文档为准。
- 新增 Kotlin API 默认补充标准中文 KDoc。
- Compose 状态模型优先使用不可变数据结构，并按项目约定标注 `@Immutable`。
- Data 层只产出 `DataError`，Domain 层映射为场景错误，UI 层只消费场景错误。
- 新增依赖优先写入 `gradle/libs.versions.toml`，模块依赖使用 `projects.xxx.xxx`。
- 不手改 `build/`、`generated/`、`*.xcworkspace/` 等生成目录。

## 文档索引

- 环境搭建：`.docs/contributing/setup.md`
- 构建打包：`.docs/contributing/building.md`
- 测试说明：`.docs/contributing/testing.md`
- 代码风格：`.docs/contributing/code-style.md`
- 分层架构与错误处理：`.docs/contributing/layered.md`
- MVI 与 ViewModel：`.docs/contributing/mvi.md`
- FlowRedux2：`.docs/guides/flow-redux.md`
- 架构总览：`.docs/contributing/architecture.md`
- KMP 说明：`.docs/contributing/kmp.md`

## License

当前仓库尚未声明许可证。如需对外发布，请先补充明确的开源或私有使用许可。
