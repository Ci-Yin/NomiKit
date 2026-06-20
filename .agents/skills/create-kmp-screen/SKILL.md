---
name: create-kmp-screen
description: 在 NomiKit 项目中按 MVI + FlowRedux2 + StateMachineMviViewModel 规范脚手架式生成一个新的 UI screen，包含 Action / Effect / UiState / Model / Mapper / ViewModel / Screen 与可选 component/ 子目录。当用户要求"新建一个 screen / 新建页面 / 加一个 XxxScreen / 写一个 XxxViewModel / 按 main 模板再做一个页面"等场景时使用。
---

# 创建 NomiKit Screen 模板

本 skill 用于在 NomiKit (`com.ciyin.app`) 项目中以"完整集"的方式生成一个新的
`com/ciyin/app/ui/screen/<feature>/` 目录，遵循项目的
**MVI + FlowRedux2 + `StateMachineMviViewModel`** 规范，并满足项目硬性约束：

- 中文 KDoc
- 不写死高度 / 间距 / 字号 / 颜色，统一走 `AppTheme.spacings / typography / colorScheme / sizes`
- `@AppPreview` 注解 + 项目自带的 `AppPreview { }` 容器
- `XxxScreen` 与 `XxxContent` 严格分离

## 一、何时使用

当用户提出以下需求时，立即应用本 skill：

- "新建一个 XxxScreen"、"新增 Xxx 页面"、"加一个页面"
- "参考 main / aiimagedemo / setting 再做一个 XxxScreen"
- "为 Xxx 写 ViewModel + State + Action + Effect"
- "按 MVI / FlowRedux 规范搭一个新页面"
- "脚手架一下 Xxx 页面"

如果用户只是想改某个**已有**页面的局部逻辑（比如改一个按钮、加一个 Action、修一个 bug），**不要**使用本
skill；直接编辑现有文件即可。

## 二、信息收集（生成前必问）

调用 `AskQuestion` 工具一次性收集以下关键信息，**不要**多轮追问。如果用户已经在最初的需求里说清楚某项，则该项跳过。

必填项：

1. **页面英文名**（PascalCase，例如 `Search` / `UpdateApp` / `AiImageDemo`），用于：
    - 目录名小写：`search` / `updateapp` / `aiimagedemo`
    - 类名前缀：`SearchViewModel` / `SearchUiState` / `SearchAction` / `SearchEffect` /
      `SearchScreen`
2. **页面中文名**（用于 KDoc 与 `ScreenScaffold` 的 `title`，例如 "搜索" / "更新应用"）
3. **页面所属层级**：
    - `app/shared/.../ui/screen/<name>/`（顶级页面，最常见）
    - `app/shared/.../ui/screen/<parent>/<name>/`（子模块页面，例如 `setting/account/`）
4. **是否需要 `Mapper` 文件**：当页面要在 Domain 模型 ↔ UI 模型之间转换时为"是"
5. **是否需要 `Model` 文件**：当 UiState 之外还有专门的 UI 子模型 / 枚举时为"是"
   （参考 `main` 的 `MainDemoDestination` / `MainDemoItem`）
6. **是否需要 `component/` 子目录**：当页面需要拆分独立可复用的子 Composable 时为"是"
7. **页面会触发的副作用类型**：
    - 仅状态更新 / 列表展示 → `XxxEffect` 留空（参考 `MainEffect`）
    - 需要导航 / 跳转 → 添加 `data object NavigateBack : XxxEffect` 等具体 Effect
    - 需要 Toast 提示 → ViewModel 实现 `UiEffectHandler` 接口（见第七节）

## 三、目录与文件清单

生成后的目录结构（顶级页面，full 集）：

```
app/shared/src/commonMain/kotlin/com/ciyin/app/ui/screen/<name>/
├── <Name>Action.kt        必选 - 用户动作 sealed interface
├── <Name>Effect.kt        必选 - 副作用 sealed interface（即使为空也要有）
├── <Name>UiState.kt       必选 - data class
├── <Name>Model.kt         可选 - UI 子模型 / 枚举（按需）
├── <Name>Mapper.kt        可选 - Domain ↔ UI 映射（按需）
├── <Name>ViewModel.kt     必选 - StateMachineMviViewModel
├── <Name>Screen.kt        必选 - Composable 入口
└── component/            可选 - 子 Composable
```

包名规则：

- 顶级页面：`com.ciyin.app.ui.screen.<name>`
- 子模块页面：`com.ciyin.app.ui.screen.<parent>.<name>`
- `component/` 子目录：在父包名后追加 `.component`

## 四、生成工作流

任务进度清单（复制到回复中并逐项推进）：

```
任务进度：
- [ ] 步骤 1：收集信息（AskQuestion）
- [ ] 步骤 2：确认目标目录是否已存在（避免覆盖）
- [ ] 步骤 3：按需读取模板文件
- [ ] 步骤 4：用占位符替换写入实际文件
- [ ] 步骤 5：补齐 KDoc，移除生成残留
- [ ] 步骤 6：执行质量自检清单
- [ ] 步骤 7：用 ReadLints 检查新文件
- [ ] 步骤 7.5：提示用户接入导航（不替用户改 NavRouters/App.kt）
```

### 步骤 2：检查目标目录

使用 `Glob` 工具检查 `app/shared/src/commonMain/kotlin/com/ciyin/app/ui/screen/<name>/*.kt`
是否已存在文件。如果已存在，先与用户确认是覆盖、是合并还是改名。

### 步骤 3：读取模板

模板存放在本 skill 的 `templates/` 子目录，按需读取：

| 模板文件                              | 何时读取            |
|-----------------------------------|-----------------|
| `templates/Action.kt.template`    | 必读              |
| `templates/Effect.kt.template`    | 必读              |
| `templates/UiState.kt.template`   | 必读              |
| `templates/Model.kt.template`     | 用户选择"需要 Model"  |
| `templates/Mapper.kt.template`    | 用户选择"需要 Mapper" |
| `templates/ViewModel.kt.template` | 必读              |
| `templates/Screen.kt.template`    | 必读              |

### 步骤 4：占位符替换

模板中的占位符与替换规则：

| 占位符           | 替换为             | 示例                                    |
|---------------|-----------------|---------------------------------------|
| `{{PACKAGE}}` | 完整包名            | `com.ciyin.app.ui.screen.aiimagedemo` |
| `{{NAME}}`    | PascalCase 类名前缀 | `AiImageDemo`                         |
| `{{name}}`    | 全小写目录名          | `aiimagedemo`                         |
| `{{中文名}}`     | 页面中文名           | `文生图演示`                               |

注意：

- 如果选择了"不需要 Mapper / Model"，**不要**生成对应文件；不要生成空白占位文件。
- 子模块页面的包名需要带上父级，例如 `com.ciyin.app.ui.screen.setting.account`。

### 步骤 5：KDoc 补齐与残留清理

每个文件生成后必做：

1. 类、属性、方法、枚举条目都必须有 KDoc（`/** ... */`），中文撰写
2. 删除模板里的 `// TODO 模板示例` 这类生成残留，但保留指引性的 `// TODO(<feature>):` 待用户补全的标记
3. `import` 不要写 `import *`；按 IDE 自然顺序排序：`androidx.*` → `ciyin.*` → `com.*` → 三方 →
   `kotlinx.*` → `org.*`
4. 不要在普通方法、`when` 分支、状态机 DSL 块里写"// 增加点击处理"这种叙述型注释；只在表达**非显然意图
   **时才加注释

### 步骤 6：质量自检清单

提交前**必须**逐条检查：

- [ ] 所有 `class / interface / object / enum` 都有中文 KDoc
- [ ] 所有 `public / internal` 方法、属性都有中文 KDoc
- [ ] 没有写死的 UI 高度（不要出现 `.height(56.dp)` 等魔法数字；用 `AppTheme.spacings` /
  `AppTheme.sizes` 或让内容撑开）
- [ ] 没有 `delay(xxx)` 用来"等一下"修时序
- [ ] 没有 `try { ... } catch(e: Throwable) {}` 之类的吞异常
- [ ] 没有 hard-code 的颜色（用 `AppTheme.colorScheme.*`）
- [ ] 没有 hard-code 的间距 / 字号（用 `AppTheme.spacings` / `AppTheme.typography`）
- [ ] `@Preview` 函数使用项目的 `@AppPreview` 注解 + `AppPreview { }` 容器（详见第八节）
- [ ] `XxxScreen` 与 `XxxContent` 严格分离：`Screen` 只做"接 ViewModel + 收集 Effect +
  路由 Action"，纯 UI 在 `Content` 里
- [ ] `ViewModel` 继承 `StateMachineMviViewModel<S, A, E>`；如需依赖注入再追加 `KoinComponent`
- [ ] 如需 Toast / Dialog，让 `ViewModel` 同时实现 `com.ciyin.app.ui.util.UiEffectHandler`
  （**注意**：当前该接口仅 `toast(text)` / `toast(resource, vararg args)` 两个 API，其他实现尚未完成）
- [ ] FlowRedux2 强制约束：所有 `on<Action> { }` / `onEnter { }` 必须套在 `inState<XxxUiState> { }` 内
- [ ] FlowRedux2 强制约束：一个 `on<Action> { }` / `onEnter { }` 块里只允许调用一次
  `mutate { }` / `override { }` / `noChange()`，多次调用只有最后一次生效
- [ ] `Action` / `Effect` 仅由 `XxxScreen` 顶层和 `XxxContent`（通过 `(Action) -> Unit` 的 `onAction`
  ）消费，
  子 Composable 不直接 `import XxxAction / XxxEffect`
- [ ] 文件结尾保留**且仅保留**一个换行
- [ ] 没有 IDE 自动生成的无用 import 与 `import *`

### 步骤 7：Lint 检查

调用 `ReadLints`，传入新生成的文件路径，确认没有引入新的红色错误；若有则修复。

### 步骤 7.5：提示用户接入导航

生成文件不等于"用户能跳进去"。生成完后，**用文字提醒**用户去做以下三步（不要替用户改这三处）：

1. **声明路由**：在 `app/shared/src/commonMain/kotlin/com/ciyin/app/ui/app/navigation/NavRouters.kt`
   增加：
   ```kotlin
   @Serializable
   object {{NAME}}Router : NavRouter
   ```
   并在文件末尾的 `polymorphic(NavKey::class) { ... }` 块里追加：
   ```kotlin
   subclass({{NAME}}Router::class, {{NAME}}Router.serializer())
   ```
2. **挂到导航树**：在 `app/shared/src/commonMain/kotlin/com/ciyin/app/ui/app/App.kt` 的
   `entryProvider` 里追加：
   ```kotlin
   entry<{{NAME}}Router> {
       {{NAME}}Screen(
           onBack = { navBackStack.back() },
       )
   }
   ```
3. **可选 — 让首页能发现**：如果是想被首页 demo 列表点击进入的页面，需要：
    - 在 `app/shared/.../ui/screen/main/MainModel.kt` 的 `sealed interface MainDemoDestination` 加分支：
      `data object {{NAME}} : MainDemoDestination`
    - 在 `app/shared/.../ui/screen/main/MainViewModel.kt` 的 `initState.demos` 里追加一项
      `MainDemoItem`
    - 在 `App.kt` 内 `MainScreen.onOpenDemo` 的 `when` 分支里把
      `MainDemoDestination.{{NAME}}` 映射到 `navBackStack.navigate({{NAME}}Router)`

## 五、ViewModel 模板硬规则（StateMachineMviViewModel + FlowRedux2）

本 skill **统一**使用 `ciyin.ui.foundation.viewmodel.StateMachineMviViewModel<S, A, E>`。
（项目里也存在更轻量的 `AbsMviViewModel<S, A, E>`，例如 `MainViewModel` / `AppViewModel` 用的就是它，
但本 skill **不**生成 `AbsMviViewModel` 形态。）

`StateMachineMviViewModel` 必须重写两个抽象成员：

```kotlin
class {
    { NAME }
}ViewModel :
StateMachineMviViewModel < { { NAME } } UiState, { { NAME } } Action, { { NAME } } Effect >(),
KoinComponent /* 仅在需要 by inject<...>() 时再加 */ {

    override fun FlowReduxStateMachineFactory< { { NAME } }UiState, { { NAME } } Action >.initialize() {
    initializeWith { { { NAME } } UiState () }
}

    override fun FlowReduxBuilder< { { NAME } }UiState, { { NAME } } Action >.spec() {
    inState < { { NAME } } UiState > {
        // 处理点击
        on < { { NAME } } Action . SomeClick > { _ ->
            mutate { copy(/* ... */) }
        }
    }
}
}
```

强约束：

- `on<Action> { }` / `onEnter { }` / `condition({ }) { }` 必须**全部**套在 `inState<XxxUiState> { }`
  里。
- 每个 `on<Action> { }` / `onEnter { }` 块里只允许调用**一次** `mutate { }` / `override { }` /
  `noChange()`。
  多次调用只有最后一次生效——这是 FlowRedux2 的硬约束。
- 触发副作用统一用 `poseEffect(XxxEffect.Foo)`（来自父类 `EffectViewModel`）。
- 如需 Toast / Dialog 等"非状态"反馈，让 ViewModel 同时实现 `com.ciyin.app.ui.util.UiEffectHandler`，
  在 `on<Action>` 里直接 `toast("...")`。当前 `UiEffectHandler` 实现尚未完成（见第七节"已知现状"）。

## 六、Screen 模板硬规则

```kotlin
@Composable
fun {
    { NAME }
}Screen(
onBack: () -> Unit,
viewModel: { { NAME } } ViewModel = viewModel (::{ { NAME } } ViewModel),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    viewModel.collectSideEffects { effect ->
        when (effect) {
            { { NAME } } Effect . NavigateBack -> onBack()
            // 其余 Effect 分支
        }
    }

    { { NAME } } Content (
            state = state,
    onAction = viewModel.dispatchAction,
    )
}

@Composable
private fun {
    { NAME }
}Content(
state: { { NAME } } UiState,
onAction: ({ { NAME } } Action) -> Unit,
) = ScreenScaffold(
title = "{{中文名}}",
topBar = {
    IconButton(onClick = { onAction({ { NAME } } Action . BackClick) }) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
    }
},
) {
    // 纯 UI
}

@AppPreview
@Composable
private fun {
    { NAME }
}ScreenPreview() = AppPreview {
    { { NAME } } Content (
            state = { { NAME } } UiState (/* 给点预览数据 */),
    onAction = {},
    )
}
```

强约束：

- `viewModel` 形参类型必须显式写出，使用 `viewModel(::{{NAME}}ViewModel)`（来自
  `ciyin.ui.foundation.viewmodel.viewModel`），**不要**写 `koinViewModel()`，**不要**写
  `viewModel { ... }`。
- 状态收集统一用 `viewModel.state.collectAsStateWithLifecycle()`（来自
  `androidx.lifecycle.compose.collectAsStateWithLifecycle`）。
- 副作用消费**首选** `viewModel.collectSideEffects { ... }`（项目自带扩展，见
  `core/ui-foundation/.../viewmodel/EffectViewModel.kt`）；**次选**
  `LaunchedEffect + sideEffects.collect`。
- 容器统一用 `com.ciyin.app.ui.component.ScreenScaffold`，不要直接 `Scaffold`。
- `@AppPreview` 注解从 `org.jetbrains.compose.ui.tooling.preview.AppPreview` 导入；`AppPreview { }`
  容器从 `com.ciyin.app.ui.component.AppPreview` 导入（**两者同名**，一个是注解、一个是 Composable，必须都
  import）。

## 七、UiEffectHandler 现状（重要）

`com.ciyin.app.ui.util.UiEffectHandler` **存在但尚未完善**。当前可用 API：

```kotlin
fun toast(text: String)
suspend fun toast(resource: StringResource, vararg args: Any)
```

但**当前两个 `toast(...)` 内部实现都是空的**（`Toaster.show(...)` 被注释掉了）。
其他想象中的 `dialog(...)` / `toastLoading(...)` / `toastSuccess(...)` / `toastError(...)` /
`toastWarning(...)` / `dialogLogin(...)` 也**都被注释掉**，目前无法直接调用。

因此 skill 在生成模板时：

- 如果用户说"我要弹 Toast / Dialog"：让 ViewModel 实现 `UiEffectHandler`，在 `on<Action>` 里直接调用
  `toast("...")`，但同时**显式提醒用户**"该接口尚未接 Toaster / Dialoger，所以现在调用看不到效果"。
- 如果用户没有明确要 Toast：保持 ViewModel 不实现 `UiEffectHandler`，避免引入"看似能用、其实没接"的依赖。

## 八、模板与项目实例对照

不要凭空想，要照着项目里已有的 screen 模仿。常见参照见 `references.md`：

```markdown
更详细的实例索引（什么场景照抄哪个 screen）见 [references.md](references.md)。
```

## 九、不要做的事

- 不要把 `Action` 的处理逻辑写在 `Screen` 里（`Screen` 只能做"路由 Action / 收集 Effect"）
- 不要把 `LazyListState`、`PagerState`、`TextFieldState` 这类 Compose runtime 状态放进 `UiState`
  （详见 `.docs/contributing/mvi.md` 第四节第三点：要走 `Effect` + Compose 层 `rememberSaveable`）
- 不要给 `Screen` 入口的 `viewModel` 形参加默认值 `koinViewModel()`；遵循项目实际写法
  `viewModel: XxxViewModel = viewModel(::XxxViewModel)`
- 不要把"一次性事件"（导航、Toast 触发）放进 `UiState`，要走 `Effect` 或（未来完善后的）`UiEffectHandler`
- 不要在生成的 KDoc 里写英文
- 不要用 `// region/endregion` 折叠区块
- 不要在 `XxxAction` / `XxxEffect` 上加任何子 Composable 依赖；只有 `XxxScreen` 与 `XxxContent` 才能
  `import` 它们

## 十、附加资源

- MVI 与 ViewModel 规范：`.docs/contributing/mvi.md`
- FlowRedux2 DSL 速查：`.docs/guides/flow-redux.md`
- 项目实例索引：[references.md](references.md)
