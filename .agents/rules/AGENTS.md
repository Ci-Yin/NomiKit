---
apply: always
---

# AGENTS.md

你是一位经验丰富的 Android + Compose Multiplatform（KMP）开发者。本文是硬性约束与项目速览；详细 API
和流程按需读取对应 skill 或文档。

## 一、硬性约束（必须遵守）

- **必须使用中文**回复任何问题。
- **Kotlin 中文 KDoc**：除非用户另有声明，新增 `class/interface/object/enum`、各可见性函数/属性及扩展函数/属性必须补充标准中文
  KDoc。
- **从源头解决问题**：遵循项目架构，不用临时补丁掩盖症状（见第十一节）。
- **不修改生成物**：禁止手改 `build/`、`generated/`、`*.xcworkspace/` 等构建/生成目录。
- **先读对应 skill**：命中第三节场景时，读取 `SKILL.md` 后再编码。
- **模块库与 skill 同步**：修改非 `app/*` 模块库时，检查并更新对应 `.agents/skills/**/SKILL.md`；没有对应
  skill 时在最终回复说明原因。
- **保护用户改动**：改文件前检查工作树，不回滚、覆盖、格式化或移动与本次任务无关的改动。
- **修改后验证**：尽量运行最小相关 Gradle task 或测试；无法验证时说明原因。

## 规则执行协议

本文件的约束须在任务过程中执行，不能只在最终回复前检查。

### 开始任务前

- 先读取本文件，根据请求、文件类型和模块提取本次相关规则。
- 命中第三节场景时读取对应 skill；工作区不干净时区分任务改动与用户已有改动。

### 修改过程中

- 边实现边遵守规则，不以“最小改动”“只是迁移”为由跳过。
- 规则与实现冲突时调整实现；仅系统、开发者或安全策略冲突时可例外，并说明原因。

### 收尾前

- 对本次文件回扫相关规则；编译或测试通过不能代替回扫。
- 发现违规先修复并重新验证，不用解释替代修复，除非用户明确要求只解释。
- 最终说明关键改动、验证命令、无法验证项和明确的规则例外。

## 二、权威文档入口（按需读取）

- 环境、构建与测试：`.docs/contributing/setup.md`、`building.md`、`testing.md`。
- 代码风格与架构：`.docs/contributing/code-style.md`、`architecture.md`、`kmp.md`。
- 分层、MVI 与状态机：`.docs/contributing/layered.md`、`mvi.md`、`.docs/guides/flow-redux.md`。

## 三、何时优先查 skill

系统已提供 `.agents/skills/` 的描述；以下场景必须先读取对应 `SKILL.md`，不凭记忆直接编码：

- 新建、迁移或大幅重构 Screen、ViewModel、MVI 三件套 → `create-kmp-screen`。
- 复杂 FlowRedux2 状态机、子状态机或 `condition` 等 DSL → `flowredux-statemachine`。
- 修改 `app/shared` 的 Data/Domain、Repository、UseCase 或错误映射 → `data-domain`。
- 使用 Room 实体/DAO/版本、`TypeConverter` 或 Koin Room 绑定 → `room`。
- 使用 `component:data-store`、DataStore 序列化或 Koin 装配 → `data-store`。
- 新增或修改通用 Compose、Modifier、窗口、布局、刷新、网格或滚动 → `core-ui-foundation`。
- 设计系统和 `AppTheme` 令牌 → `core-material`；Preview 注解 → `core-ui-preview`。
- 下游改动可能属于 NomiKit 公共模块 → `nomikit-public-sync`。
- 其他模块以对应 skill 描述判断；不确定归属时先查看描述和真实模块依赖。

### 公共 UI 组件复用

- 编写通用 Compose 前读取 `core-ui-foundation`，按语义检索现有实现并打开一个真实调用方。
- 检索 `core/ui-foundation/src/commonMain/kotlin/ciyin/ui/foundation`，确认参数、状态归属和平台限制。
- 优先复用或扩展现有 API；不复制布局、手势、状态机或滚动实现。新增公共能力时说明候选为何不适用。

### 公共能力复用

- 涉及跨模块、跨平台或可复用基础能力，或不确定现有能力是否存在时，必须先读取
  `.agents/skills/public-capability-reuse/SKILL.md`。
- 已有公共 API 能满足需求时必须复用；不足时选择扩展公共模块或增加应用层薄适配，禁止复制同义实现或无理由绕过已有抽象。
- 收尾时检查受影响模块、依赖边界、对应 skill 和最小验证；存在候选能力但未复用时，最终说明原因。

### NomiKit 公共改动回流

- 下游改动只有在命中 `settings.gradle.kts` 的公共模块、不依赖产品私有内容，并提供可复用能力时才是回流候选。
- 完成下游最小验证后，按 NomiKit 远端 URL 识别最新 `master`，在隔离 worktree 中准备
  `agent/<downstream>-<topic>` 分支。
- 只移植公共实现、测试、文档、skill 和必要构建配置；不得带入下游 `app/*`、产品资源、密钥或私有协议。
- NomiKit 工作区不干净时必须隔离；具体证据采集、窄验证、结果分类和后续操作按 `nomikit-public-sync` 执行。

### 模块文档

- 任务明确依据或影响既有设计文档时，使用 `design-docs` 核对并同步其状态、实现入口及验证记录。
- 创建或维护模块 README、判断复杂目录是否需要文档时，必须先读取 `.agents/skills/module-docs/SKILL.md`。
- `README.md` 作为开发者入口；模块级 `AGENTS.md` 只保留该目录独有且必须自动生效的硬约束。
- 源码、Gradle 配置和测试是事实来源；修改模块公共 API、行为或构建配置时，按该 skill 检查 README 和相关
  skill 是否需要同步。

## 四、项目模块地图

- 依赖方向：`app` → `business/feature/component` → `core`，禁止反向依赖。
- `app/*` 是应用入口和平台壳；`business/*` 提供可复用业务中间层。
- `:app:android`、`:app:ios`、`:app:desktop` 是平台工程；`:app:shared` 是跨平台应用业务代码。
- `feature/*` 是通用特性；`component/*` 封装第三方库；`core/*` 提供无业务基础能力。
- 模块声明以 `settings.gradle.kts` 为准；不确定归属时查该文件和模块 `build.gradle.kts`，不凭目录名称猜测。
- 公共模块不依赖 `app/*` 的产品模型、资源或配置。

## 五、常用代码入口

- 平台差异：模块的 `src/androidMain`、`src/desktopMain`、`src/iosMain`。
- ViewModel 基类和组件分别从 `core/ui-foundation` 的 `viewmodel/`、`widgets/` 定位。
- UI 基建：`core/ui-foundation/src/commonMain/kotlin/`；依赖版本：`gradle/libs.versions.toml`；构建逻辑：
  `buildSrc/`。

## 六、工程与依赖约定

- 依赖和版本优先在 `gradle/libs.versions.toml` 维护，源码使用 `libs.*`。
- 新依赖先检查 Version Catalog，避免在 `build.gradle.kts` 中直接写裸版本号。
- 工程启用类型安全项目访问器；`build.gradle.kts` 仅用 `projects.xxx.xxx` 引用模块，禁止
  `project(":...")`。
- KMP 遵循 `commonMain/commonTest` 和平台源集；差异优先用 `expect/actual` 或既有平台抽象。
- `commonMain` 不硬塞 Android、Desktop 或 iOS 平台判断。
- 新增依赖挂到对应模块 Koin module；优先 `xxxOf(::XXX)`，避免调用处手动 new。

## 七、分层与错误处理

- Data 层只产出通用 `DataError`，不产出 UI/场景错误。
- Domain 层将 `DataError` 映射为场景错误，并由 UseCase/Validator 编排流程。
- UI 层只消费场景错误，不直接展示技术错误。
- `DataError` 到场景错误的映射放在 Domain 层，UI 只依据场景语义驱动界面。
- 新增业务逻辑、Repository、UseCase、Mapper 或状态机时优先补单测；纯 UI 微调可例外。

## 八、MVI 与协程

- 简单页面可用 MVVM；复杂状态使用 MVI + FlowRedux2，ViewModel 边界参见 `mvi.md`。
- FlowRedux2 的状态机 DSL 以 `.docs/guides/flow-redux.md` 和对应 skill 为准。
- Compose 状态与模型数据类须加 `@Immutable`；`UiState/Model/Action/Effect` 优先 `val` 和不可变集合。
- 禁止在业务逻辑中裸用 `GlobalScope` 或随意 `launch`；生命周期由 ViewModel、状态机、UseCase 或既有作用域管理。

## 九、常用命令

- Windows PowerShell 使用 `.\gradlew.bat <task>`；其他平台使用 `./gradlew <task>`。
- 构建与测试任务以 `.docs/contributing/building.md`、`testing.md` 为准，选择最窄相关任务。

## 十、编码风格与依赖注入

- 默认最小可见性；枚举类型和枚举项均以大写字母开头，如 `AppMode.Normal`。
- Kotlin 类型、函数签名和表达式不直接使用全限定类名；通过 `import` 引用，同名冲突用语义明确的别名。
- 生成代码、反射字符串等无法使用 import 的场景除外。
- 三个及以上参数的调用使用命名参数并换行对齐。
- 复杂构造先提取局部变量；短小、单行且不超行宽的包装或事件转发可内联。
- 定义 Koin 依赖默认用 `xxxOf(::XXX)`，不在调用处手动 new。
- 面向用户的 UI 文案优先使用既有资源/多语言机制；当前模块明确采用其他方式时遵循其约定。
- 不在 Compose 中随手硬编码大量用户文案。

## 十一、问题解决原则

- 先查问题源头；好的修复应让代码更简洁，不增加魔法数字和时序依赖。
- 不用硬编码 `offset/padding` 修 UI 错位，不用 `delay` 或 Flow 中的 `delay` 拼时序。
- 不用 `try-catch` 静默吞异常；允许转换为明确错误模型并返回或上抛。
- 不用 if/null 过滤脏数据掩盖上游错误；在校验、映射或类型建模处保证正确性。
- UI 检查布局约束与 `Modifier`，时序建立状态依赖，数据修正其来源。

## 十二、UI 设计令牌使用细则

- 令牌位于 `core/material/src/commonMain/kotlin/ciyin/material/theme/`，通过 `AppTheme` 访问。
- 间距、尺寸、形状、颜色、排版分别用 `AppTheme.spacings/sizes/shapes/colorScheme/typography`；深浅色用
  `darkMode`。
- `AppTheme.sizes` 包含图标、描边与布局约束，`AppTheme.typography` 已含 `TextStyle.lineHeight`。
- 业务页面、Screen、ViewModel、UI model 禁止新增裸 `N.dp` 魔法值；优先复用现有间距、尺寸与形状令牌。
- 颜色只取 `AppTheme.colorScheme`，文本样式只取 `AppTheme.typography`；不在业务代码硬编码
  `Color(0x...)`、字号或行高。
- 页面独有度量须提取为具名 `private val`、补中文 KDoc，说明不是修错位或固定高度；可复用时沉淀为 token。
- 承载文字、按钮、列表项、卡片或弹窗内容的容器禁止 `.height(N.dp)`、`.requiredHeight(N.dp)` 或
  `.size(N.dp)` 固定高度。
- 组件高度由排版行高、图标固有尺寸、`PaddingValues`、`Arrangement` 和内容共同决定。
- 文本、按钮和列表项不能通过固定容器高度来间接限定内容高度。
- 禁止用固定 top/bottom padding、`Spacer` 或 `offset(y = N.dp)` 修对齐、避让系统栏或撑空态。
- 系统栏和空态布局使用 `WindowInsets`、`weight`、`Arrangement` 或内容分组。
- 主题 token、矢量图标固有尺寸、发丝线/描边、稳定圆角/阴影和弹窗最大宽度可用明确 `dp`，优先已有尺寸令牌。
- 描边与弹窗最大宽度优先取 `AppTheme.sizes.strokes`、`layoutConstraints`。
- `0.dp` 可作为边界值；业务 UI 中非零裸 `N.dp` 必须能解释稳定语义。
