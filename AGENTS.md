# AGENTS.md

你是一位经验丰富的 Android + Compose Multiplatform（KMP）开发者。请将本文件视为**硬性约束 + 项目速览索引
**。

## 一、硬性约束（必须遵守）

- **必须使用中文**回复我任何问题。
- **生成/修改 Kotlin 代码时**：除非我单独声明，否则新增的 `class/interface/object/enum`
  、公开/内部函数、扩展函数等，必须补充**标准中文 KDoc 文档注释**。
- **按项目既定架构做事**：优先从“问题产生的源头”修正，避免临时补丁式改动（见第十节）。
- **不要修改生成物**：禁止手改 `build/`、`generated/`、`*.xcworkspace/` 等构建/生成目录内的文件。

## 二、权威文档入口（不确定时先读）

- 项目/环境搭建：@.docs/contributing/setup.md
- 构建打包：@.docs/contributing/building.md
- 测试说明：@.docs/contributing/testing.md
- 代码风格与规范：@.docs/contributing/code-style.md
- 分层架构与错误处理：@.docs/contributing/layered.md
- MVI 与 ViewModel：@.docs/contributing/mvi.md
- FlowRedux2 状态机：@.docs/guides/flow-redux.md
- 架构设计总览：@.docs/contributing/architecture.md
- KMP 相关说明：@.docs/contributing/kmp.md

## 三、项目模块地图（从上到下单向依赖）

> 核心原则：`app` → `business/feature/component` → `core`，禁止反向依赖。

- `app/*`：最终应用入口与平台壳
    - `:app:android` / `:app:ios` / `:app:desktop`：平台工程
    - `:app:shared`：跨平台应用业务代码（重点改动区）
    - `:app:application`：应用层基础设施（DI/装配/平台 glue）
- `business/*`：可复用业务中间层（领域更偏“业务平台/业务能力复用”）
- `feature/*`：可复用通用特性（播放器、弹幕、动态布局等）
- `component/*`：第三方库封装与适配（Koin/Ktor/Room/DataStore/Kermit/Sentry…）
- `core/*`：与业务无关的基础能力与 UI 基建（platform/system/io/coroutines/ui-* 等）

模块定义以 `settings.gradle.kts` 为准。

## 四、常用代码入口（快速定位）

- 应用业务（跨平台）：`app/shared/src/commonMain/kotlin/`
    - 推荐按三层组织：`presentation/`、`domain/`、`data/`
- 平台差异实现：各模块的 `src/androidMain`、`src/iosMain`、`src/desktopMain`
- ViewModel 基类与 MVI 基建：`core/ui-foundation/src/commonMain/kotlin/viewmodel/`
- Compose 基础组件/主题/预览：`core/ui-foundation/src/commonMain/kotlin/widgets/`
- 依赖版本与坐标：`gradle/libs.versions.toml`
- 自定义 Gradle 逻辑：`buildSrc/`

## 五、工程与依赖约定（改 build 相关时遵守）

- 依赖与版本优先通过 `gradle/libs.versions.toml` 维护，代码中使用 `libs.*`（Version Catalog）。
- 工程开启 `TYPESAFE_PROJECT_ACCESSORS`，模块引用优先使用类型安全访问器（若工程内已采用该写法）。
- KMP 源集结构遵循 `commonMain/commonTest` + 平台 `androidMain/desktopMain/iosMain`。

## 六、分层与错误处理（关键规则）

- **Data 层**：只产出通用错误 `DomainError`（不产出 UI/场景错误）。
- **Domain 层**：将 `DomainError` 映射为场景错误 `XxxError`，用 `UseCase/Validator` 编排业务流程。
- **Presentation 层**：只消费场景错误，驱动 UI（配合 MVI/状态机），不要把技术错误直接透传到 UI。

细节以 @.docs/contributing/layered.md 为准。

## 七、MVI/状态机约定（复杂状态优先状态机）

- 简单页面可用 MVVM；复杂状态管理使用 MVI + FlowRedux2 状态机。
- ViewModel 基类选择与接口边界以 @.docs/contributing/mvi.md 为准。
- FlowRedux2 的 DSL 规范以 @.docs/guides/flow-redux.md 为准。

## 八、Compose Preview 规则（必须）

生成 Preview 函数时，必须使用项目内 `AppPreview` 注解与容器：

```kotlin
import com.yy.myuko.core.ui.foundation.widgets.AppPreview

@AppPreview
@Composable
fun Preview() = AppPreview {
}
```

## 九、常用命令（给出建议时优先引用）

- Android：`./gradlew assembleDebug` / `./gradlew installDebug`（详见 @.docs/contributing/building.md）
- iOS：`./gradlew podInstall`、`./gradlew patchInfoPlist`、`./gradlew buildDebugIpa`
- Desktop：`./gradlew createReleaseDistributable`
- 生成 Compose 资源 Res：`./gradlew generateComposeResClass`
- 测试：`./gradlew check`（更全量可用 `./gradlew clean check`）

## 十、问题解决原则（禁止亡羊补牢）

解决问题必须从根本上消除问题，而不是用 hack 掩盖症状。

### 思维方式

- 问题出现时先退后一步：是什么导致了这个问题？
- 不要在问题发生的地方修补，要在问题产生的源头修正。
- 好的解决方案应该让代码更简洁，而不是增加魔法数字和时序依赖。

### 禁止（典型坏味道）

- 硬编码 `offset/padding` 修正 UI 错位。
- 用 `delay` 解决时序问题。
- 用 `try-catch` 吞异常（允许把异常转换为明确的错误模型并上抛/返回，但禁止静默忽略）。
- 用 if/null 检查“过滤脏数据”来掩盖上游数据错误。

### 正确做法

- UI 问题 → 检查布局约束与 `Modifier` 链。
- 时序问题 → 用 Flow/状态驱动，建立正确的依赖关系。
- 数据问题 → 在源头保证数据正确性（校验、映射、类型建模）。

**自检**：如果删掉你的“修复”问题会重现，或在另一个场景下重现，说明这是 hack，需要重新思考。
