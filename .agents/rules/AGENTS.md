---
apply: always
---

# AGENTS.md

你是一位经验丰富的 Android + Compose Multiplatform（KMP）开发者。请将本文件视为**硬性约束 + 项目速览索引
**。

## 一、硬性约束（必须遵守）

- **必须使用中文**回复我任何问题。
- **生成/修改 Kotlin 代码时**：除非我单独声明，否则新增的 `class/interface/object/enum`
  、公开/内部/私有函数/属性、扩展函数/属性等，必须补充**标准中文 KDoc 文档注释**。
- **按项目既定架构做事**：优先从“问题产生的源头”修正，避免临时补丁式改动（见第十节《问题解决原则》）。
- **不要修改生成物**：禁止手改 `build/`、`generated/`、`*.xcworkspace/` 等构建/生成目录内的文件。
- **遇到第三节《何时优先查 skill》中列出的场景**：必须先用 Read 工具读取对应 `SKILL.md` 后再动手，不要凭记忆直接编码。
- **保持模块库与 skill 同步**：修改已有模块库代码时（`app/*` 下的应用模块除外），必须同步检查并更新
  `.agents/skills/` 下对应模块的
  `SKILL.md`，确保 skill 中的用法、注意事项、API 名称与真实代码一致；若没有对应 skill，必须在最终回复中说明无需更新的原因。
- **保护用户改动**：开始改文件前先查看工作树状态；禁止回滚、覆盖、格式化或移动与当前任务无关的用户改动。
- **修改后要验证**：代码变更后必须尽量运行最小相关 Gradle task 或测试；若无法验证，必须在回复中说明原因。

## 规则执行协议（必须遵守）

本文件中的硬性约束均为任务执行的一部分，而不是最终回复前才检查的建议。Agent 必须按以下流程执行：

### 开始任务前

- 必须先读取本文件。
- 必须根据用户请求、本次将修改的文件类型和涉及模块，从本文件中提取“本次相关规则”。
- 如果涉及第三节列出的场景，必须先读取对应 `SKILL.md`。
- 如果工作区已有未提交改动，必须区分本次任务相关改动与用户已有改动，不得回滚或覆盖无关改动。

### 修改过程中

- 必须边实现边遵守本文件规则，不得以“最小改动”“保持原样”“只是迁移”为理由跳过规则。
- 当实现方式与本文件规则冲突时，必须调整实现方式。
- 只有系统、开发者或安全策略与本文件冲突时，才允许不执行本文件规则，并必须说明原因。

### 收尾前

最终回复前，必须对本次新增或修改的文件执行一次规则回扫。

规则回扫不是重新列出规则，而是逐项核对“开始任务前提取的本次相关规则”是否已经满足。

编译、测试或 Gradle task 通过，只能证明基础正确性，不能替代规则回扫。

### 发现违规时

如果回扫发现违反本文件规则，必须先修复违规，再重新执行必要验证。

不得用解释替代修复，除非用户明确要求只解释、不修改。

### 最终回复

最终回复必须说明：

- 完成的关键改动。
- 已运行的验证命令。
- 是否存在无法验证项或明确的规则例外。

## 二、权威文档入口（按需用 Read 工具读取，不要全部预读）

- 项目/环境搭建：`.docs/contributing/setup.md`
- 构建打包：`.docs/contributing/building.md`
- 测试说明：`.docs/contributing/testing.md`
- 代码风格与规范：`.docs/contributing/code-style.md`
- 分层架构与错误处理：`.docs/contributing/layered.md`
- MVI 与 ViewModel：`.docs/contributing/mvi.md`
- FlowRedux2 状态机：`.docs/guides/flow-redux.md`
- 架构设计总览：`.docs/contributing/architecture.md`
- KMP 相关说明：`.docs/contributing/kmp.md`

## 三、何时优先查 skill（在动手前自检）

系统已自动注入 `.agents/skills/` 下所有 `SKILL.md` 的描述（见会话上下文 `<available_skills>`）。
**在以下场景，必须先用 Read 工具读取对应 `SKILL.md` 后再动手**，不要凭记忆/直觉直接写代码：

- 新建 UI 页面 / ViewModel / MVI 三件套（Action/Effect/UiState/Model/Mapper/ViewModel/Screen）→
  `create-kmp-screen`
- 写复杂 FlowRedux2 状态机（子状态机、`condition`、`untilIdentityChanges`、`collectWhileInState` 等）→
  `flowredux-statemachine`
- 新增 / 修改 `app/shared/src/commonMain/kotlin/{data,domain}/<feature>/` → `data-domain`
- 使用 `component:room`、Room 实体/DAO/版本、`TypeConverter` / `@TypeConverters`、Koin `singleDao` /
  `singleDatabase` → `room`（**不要**主动写 `Migration` / 数据兼容，除非用户明确要求；见该 skill）
- 使用 `component:data-store`、`DataStoreFactory`、`JsonDataStoreSerializer`、
  `DataStoreBootInitializer`、
  Koin `dataStore(...)` / `DataStorePath` → `data-store`
- 涉及 AI 抽象层（`AiEngine` / `ChatEngine` / `ImageEngine` / `Registry` / 通用错误模型）→ `ai-core`
- 业务侧调用 AI（`AiChatIntegrate` / `AiImageIntegrate` / `ChatEngineConfig` / `ImageEngineConfig` /
  `ChatEngineSpec` / `ImageEngineSpec`）→ `ai-integrate`
- 接入 OpenAI 兼容协议引擎（OpenAI / OpenRouter / DeepSeek / Ollama / vLLM 等）→
  `ai-chat-openai-engine`
- 接入 SD WebUI 图像引擎（作为 ai-core `ImageEngine`）→ `ai-image-sdwebui-engine`
- 直接调 AUTOMATIC1111 SD WebUI REST API（`feature/sdwebui` 模块本身）→ `sdwebui`
- 新增或修改通用 Compose 组件、Modifier、布局、窗口、刷新、网格、滚动或 UI 基建 →
  `core-ui-foundation`

**自检**：如果不确定某改动属于哪一层 / 哪个模块，先看 skill 描述再决定是否读全文，不要直接编码。

### 公共 UI 组件复用

- 编写通用 Compose 实现前，必须先读取 `core-ui-foundation` skill，并在
  `core/ui-foundation/src/commonMain/kotlin/ciyin/ui/foundation` 中按语义检索，打开候选实现和一个真实调用方。
- 找到近似能力时，优先直接复用或扩展现有 API；不得复制布局、手势、状态机或滚动实现。若必须新增公共能力，应在最终说明中写明现有候选为何不适用。

## 四、项目模块地图（从上到下单向依赖）

> 核心原则：`app` → `business/feature/component` → `core`，禁止反向依赖。

- `app/*`：最终应用入口与平台壳
    - `:app:android` / `:app:ios` / `:app:desktop`：平台工程
    - `:app:shared`：跨平台应用业务代码（重点改动区）
    - `:app:application`：应用层基础设施（DI/装配/平台 glue）
- `business/*`：可复用业务中间层（领域更偏“业务平台/业务能力复用”）
- `feature/*`：可复用通用特性（播放器、弹幕、动态布局等）
- `component/*`：第三方库封装与适配（Koin/Ktor/Room/DataStore/Kermit/Sentry…）
- `core/*`：与业务无关的基础能力与 UI 基建（platform/system/io/coroutines/ui-* 等）

模块定义以 `settings.gradle.kts` 为准。**不确定某代码该放哪个模块时**，用 Glob/Grep 搜
`settings.gradle.kts` 或对应模块的 `build.gradle.kts` 确认依赖方向，不要凭命名猜测。

## 五、常用代码入口（快速定位）

- 平台差异实现：各模块的 `src/androidMain`、`src/iosMain`、`src/desktopMain`
- ViewModel 基类与 MVI 基建：`core/ui-foundation/src/commonMain/kotlin/viewmodel/`
- Compose 基础组件/主题/预览：`core/ui-foundation/src/commonMain/kotlin/widgets/`
- 依赖版本与坐标：`gradle/libs.versions.toml`
- 自定义 Gradle 逻辑：`buildSrc/`

## 六、工程与依赖约定（改 build 相关时遵守）

- 依赖与版本优先通过 `gradle/libs.versions.toml` 维护，代码中使用 `libs.*`（Version Catalog）。
- 工程开启 `TYPESAFE_PROJECT_ACCESSORS`，模块引用优先使用类型安全访问器（若工程内已采用该写法）。
- `build.gradle.kts` 中引用项目模块时，仅允许使用 `projects.xxx.xxx`，禁止使用 `project(":...")`。
- 新增第三方依赖时先查 `gradle/libs.versions.toml`，不要在 build 脚本中直接写裸版本号。
- KMP 源集结构遵循 `commonMain/commonTest` + 平台 `androidMain/desktopMain/iosMain`。
- KMP 平台差异优先使用 `expect/actual` 或项目已有 platform abstraction，不要在 `commonMain` 中硬塞平台判断。

## 七、分层与错误处理（关键规则）

- **Data 层**：只产出通用错误 `DataError`（不产出 UI/场景错误）。
- **Domain 层**：将 `DataError` 映射为场景错误 `XxxError`，用 `UseCase/Validator` 编排业务流程。
- **UI 层**：只消费场景错误，驱动 UI（配合 MVI/状态机），不要把技术错误直接透传到 UI。
- 新增业务逻辑、映射器、状态机、Repository、UseCase 时，优先补对应单测；纯 UI 微调可不强制。

细节以 `.docs/contributing/layered.md` 为准。

## 八、MVI/状态机约定（复杂状态优先状态机）

- 简单页面可用 MVVM；复杂状态管理使用 MVI + FlowRedux2 状态机。
- ViewModel 基类选择与接口边界以 `.docs/contributing/mvi.md` 为准。
- FlowRedux2 的 DSL 规范以 `.docs/guides/flow-redux.md` 为准。
- Compose 状态与模型数据类必须添加 `@Immutable` 注解。
- `UiState`、`Model`、`Action`、`Effect` 等 Compose/MVI 数据模型优先使用 `val` 与不可变集合。
- 禁止在业务逻辑中裸用 `GlobalScope` 或随意 `launch`；协程生命周期应由 ViewModel、状态机、UseCase
  或既有作用域管理。

## 九、常用命令（给出建议时优先引用）

> Windows PowerShell 用 `.\gradlew.bat <task>`，macOS/Linux 用 `./gradlew <task>`。下文以 Unix
> 风格示例，Windows 自行替换。

- Android：`./gradlew assembleDebug` / `./gradlew installDebug`（详见
  `.docs/contributing/building.md`）
- iOS：`./gradlew podInstall`、`./gradlew patchInfoPlist`、`./gradlew buildDebugIpa`
- Desktop：`./gradlew createReleaseDistributable`
- 生成 Compose 资源 Res：`./gradlew generateComposeResClass`
- 测试：`./gradlew check`（更全量可用 `./gradlew clean check`）

## 十、编码风格与依赖注入

- 默认使用最小可见性；能用 `private` 就不要暴露为 `internal` 或 `public`。
- Kotlin 枚举类的类型名称和枚举项名称都必须以大写字母开头，例如
  `enum class AppMode { Normal, Compact }`；禁止使用 `appMode`、`normal` 等小写开头的名称。
- Kotlin 源码的类型声明、函数签名和表达式中禁止直接使用全限定类名；必须通过 `import` 后使用短类名。若存在同名类型冲突，
  必须使用语义明确的 `import ... as ...` 别名，不得通过反复书写全限定名规避冲突。生成代码、反射字符串等无法使用
  `import` 的场景除外。
- 函数参数数量大于等于 3 时，调用处使用命名参数并换行对齐书写。
- 函数参数中的复杂构造默认先提取为局部变量；但短小、单行且不超过项目行宽的简单包装/事件转发可以内联，例如
  `onCategorySelect = { onAction(PromptTagPickerAction.CategoryChange(it)) }`。
- 定义 Koin 依赖时，默认使用 `xxxOf(::XXX)`，避免手写 lambda（如 `single { XXX(get()) }`）。
- 新增依赖必须挂到对应模块的 Koin module，避免在调用处手动 new 依赖。
- 面向用户的 UI 文案优先使用项目既有资源/多语言机制，不要在 Compose 中随手硬编码大量文案，除非当前模块已明确采用该方式。

## 十一、问题解决原则（禁止亡羊补牢）

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
- 用 Flow 中的 `delay` 拼时序或等待状态变化。

### 正确做法

- UI 问题 → 检查布局约束与 `Modifier` 链。
- 时序问题 → 用 Flow/状态驱动，建立正确的依赖关系。
- 数据问题 → 在源头保证数据正确性（校验、映射、类型建模）。

**自检**：如果删掉你的“修复”问题会重现，或在另一个场景下重现，说明这是 hack，需要重新思考。

## 十二、UI 设计令牌使用细则（必须遵守）

项目统一设计令牌集中在 `core/material/src/commonMain/kotlin/ciyin/material/theme/`，全部通过
`AppTheme` 访问（见 `Theme.kt`），不要各自为政：

- `AppTheme.spacings`：间距令牌（`tiny`→`colossal` 9 级，组件级/区块级/页面级语义）
- `AppTheme.sizes`：图标/头像尺寸等级、`strokes` 线条宽度、`componentHeights` 组件高度、
  `layoutConstraints` 布局约束
- `AppTheme.shapes`：圆角令牌（`tiny`→`colossal` 9 级，区分组件级/容器级/页面覆盖级）
- `AppTheme.colorScheme`：颜色令牌（主色/表面色/语义色/文本五级色/outline/divider）
- `AppTheme.typography`：排版（字号、行高、字重，已内置 `TextStyle.lineHeight`）
- `AppTheme.darkMode`：深浅色模式判断

### 必须遵守

- 业务页面、Screen、ViewModel、UI model 中禁止直接新增裸 `N.dp` 魔法值；新增尺寸、间距、圆角、
  图标尺寸应优先复用 `AppTheme.spacings`、`AppTheme.sizes`、`AppTheme.shapes`；颜色一律取自
  `AppTheme.colorScheme`，文本样式一律取自 `AppTheme.typography`，禁止在业务代码中硬编码
  `Color(0x...)`、`FontSize`/`lineHeight` 数值。
- 若确实存在当前页面独有的设计度量，必须提取为具名 `private val`，补充中文
  KDoc，并说明它不是用于修正布局错位或固定高度；后续若可复用，应沉淀到主题 token（`core/material` 模块）。
- 禁止在承载文字、按钮、列表项、卡片、弹窗内容的容器上使用 `.height(N.dp)`、`.requiredHeight(N.dp)`
  或 `.size(N.dp)` 间接固定高度。
- 按钮、输入框、菜单项、卡片等组件的高度必须由 `AppTheme.typography` 中 `TextStyle.lineHeight`
  、图标固有尺寸（可取自 `AppTheme.sizes.icon`）、`PaddingValues`、`Arrangement.spacedBy` 和内容本身共同决定。
- 禁止用固定 `top/bottom padding`、`Spacer(Modifier.height(N.dp))`、`Spacer(Modifier.size(N.dp))` 或
  `offset(y = N.dp)` 修 UI 对齐、避让系统栏、撑空态位置；应使用布局约束、`WindowInsets`、`weight`、
  `Arrangement` 或内容分组解决。
- 允许在以下场景使用明确 `dp`：主题 token 定义文件（`core/material`）、矢量图标
  `defaultWidth/defaultHeight`、
  发丝线/描边（优先取 `AppTheme.sizes.strokes`）、设计系统圆角/阴影/图标固有尺寸、弹窗最大宽度等稳定
  设计规格（优先取 `AppTheme.sizes.layoutConstraints`）。
- `0.dp` 可作为边界值使用；非零裸 `N.dp` 出现在业务 UI 文件时，必须能解释其语义，否则视为需要整改。
