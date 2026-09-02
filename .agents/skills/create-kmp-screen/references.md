# NomiKit Screen 实例索引

本文件配套 `SKILL.md` 使用：当你不确定"某个具体场景应该照抄哪个 screen"时，先查这张表。
所有路径都以仓库根 `D:/Studio/AndroidStudioProjects/NomiKit/` 为根。

## 一、按场景速查

| 你想做的事                                                   | 照抄哪里                                                                              | 为什么                                                                                                                     |
|---------------------------------------------------------|-----------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------|
| 一个"列表 + 点击跳转"的页面                                        | `app/shared/.../ui/screen/main/`                                                  | 完整 MVI 五件套（Action / Effect / UiState / Model / ViewModel / Screen），还演示了 `XxxScreen` + `XxxContent` + 同文件 `@AppPreview` 标准结构 |
| 真正基于 FlowRedux2 状态机的 ViewModel                          | `app/shared/.../ui/app/AppViewModel.kt`                                           | 项目里目前最贴近"状态机思维"的活实例，包含 `KoinComponent` + `SingleStateMachine.spec()` 写法。**注意**：它继承的是 `AbsMviViewModel`，不是本 skill 的目标基类  |
| 异步调用 AI 能力 / 流式收集 / 结果渲染                                | `app/shared/.../ui/screen/aiimagedemo/`                                           | 演示了 `AiImage.generate(...).collect { ... }`、`ScreenScaffold + topBar`、`OutlinedTextField` 输入态。**只**作 UI 模板，不做 MVI 模板    |
| `ScreenScaffold` 容器、`@AppPreview` 注解 + `AppPreview { }` | `app/shared/.../ui/screen/main/MainScreen.kt`                                     | 同一个 Compose 文件同时提供入口、纯 Content 和 Preview；两个同名 `AppPreview` 的 import 分别来自注解与项目主题容器 |
| 最简版"占位"页面，几乎没逻辑                                         | `app/shared/.../ui/screen/setting/SettingScreen.kt`                               | 仅一个 `Box + Text`，可作"先占位、后续再补 ViewModel"的临时方案；**不**作 MVI 模板                                                              |
| 把新页面接入导航                                                | `app/shared/.../ui/app/navigation/NavRouters.kt` + `app/shared/.../ui/app/App.kt` | 新增 `XxxRouter`、在 `polymorphic` 里登记，再到 `entryProvider` 里加 `entry<XxxRouter> { ... }`                                     |
| 让首页能发现新 demo                                            | `app/shared/.../ui/screen/main/MainModel.kt` + `MainViewModel.kt`                 | `MainDemoDestination` 加分支、`initState.demos` 加一项，`App.kt` 里 `MainScreen.onOpenDemo` 的 `when` 加映射                         |

## 二、关键基础设施速查（生成时直接 import 即可）

| 你需要……                                                       | 直接用                                                                          | 来源                                                               |
|-------------------------------------------------------------|------------------------------------------------------------------------------|------------------------------------------------------------------|
| MVI 基类（FlowRedux2）                                          | `StateMachineMviViewModel<S, A, E>`                                          | `ciyin.ui.foundation.viewmodel.StateMachineMviViewModel`         |
| MVI 基类（轻量、不用 FlowRedux2）                                    | `AbsMviViewModel<S, A, E>`                                                   | `ciyin.ui.foundation.viewmodel.AbsMviViewModel`（**本 skill 不生成**） |
| Compose 中创建 ViewModel                                       | `viewModel(::XxxViewModel)`                                                  | `ciyin.ui.foundation.viewmodel.viewModel`                        |
| 收集 state                                                    | `vm.state.collectAsStateWithLifecycle()`                                     | `androidx.lifecycle.compose.collectAsStateWithLifecycle`         |
| 收集副作用（首选）                                                   | `vm.collectSideEffects { effect -> ... }`                                    | `ciyin.ui.foundation.viewmodel.collectSideEffects`               |
| 顶层容器                                                        | `ScreenScaffold(title, topBar, content)`                                     | `com.ciyin.app.ui.component.ScreenScaffold`                      |
| Preview 注解                                                  | `@AppPreview`                                                                | `org.jetbrains.compose.ui.tooling.preview.AppPreview`            |
| Preview 容器                                                  | `AppPreview { ... }`                                                         | `com.ciyin.app.ui.component.AppPreview`                          |
| 主题对象                                                        | `AppTheme.spacings / typography / colorScheme / sizes / shapes`              | `ciyin.material.theme.AppTheme`                                  |
| Toast / Dialog（接口存在但实现尚未完成）                                 | `UiEffectHandler` 接口，仅 `toast(text)` / `toast(resource, vararg)`             | `com.ciyin.app.ui.util.UiEffectHandler`                          |
| FlowRedux2 DSL（`mutate / override / noChange / poseEffect`） | `FlowReduxBuilder<S, A>`                                                     | `com.freeletics.flowredux2.FlowReduxBuilder`                     |
| FlowRedux2 工厂（`initializeWith`）                             | `FlowReduxStateMachineFactory<S, A>`                                         | `com.freeletics.flowredux2.FlowReduxStateMachineFactory`         |
| 导航（声明 / 登记 / 跳转）                                            | `NavRouter` + `NavSavedStateConfig` + `entryProvider { entry<...> { ... } }` | `com.ciyin.app.ui.app.navigation.*` + `androidx.navigation3.*`   |

## 三、典型组合：从零起一个新页面要碰的文件

下面以"新增 `Search` 页面"为例，列出**全部**会被改动 / 新增的文件，便于自检遗漏：

```
新增：
  app/shared/src/commonMain/kotlin/com/ciyin/app/ui/screen/search/SearchAction.kt
  app/shared/src/commonMain/kotlin/com/ciyin/app/ui/screen/search/SearchEffect.kt
  app/shared/src/commonMain/kotlin/com/ciyin/app/ui/screen/search/SearchUiState.kt
  app/shared/src/commonMain/kotlin/com/ciyin/app/ui/screen/search/SearchViewModel.kt
  app/shared/src/commonMain/kotlin/com/ciyin/app/ui/screen/search/SearchScreen.kt
  （可选）app/shared/src/commonMain/kotlin/com/ciyin/app/ui/screen/search/SearchModel.kt
  （可选）app/shared/src/commonMain/kotlin/com/ciyin/app/ui/screen/search/SearchMapper.kt
  （可选）app/shared/src/commonMain/kotlin/com/ciyin/app/ui/screen/search/SearchExt.kt
    - 仅放 internal/public 的非 Compose 扩展函数和扩展属性；private 与 @Composable 扩展留在原文件
  （可选）app/shared/src/commonMain/kotlin/com/ciyin/app/ui/screen/search/component/*.kt
    - 只放 UI model + callbacks 驱动的独立视觉单元；每个含 @Composable 的文件都要有同文件 Preview

需要用户手工接入（本 skill 不替用户改）：
  app/shared/src/commonMain/kotlin/com/ciyin/app/ui/app/navigation/NavRouters.kt
    - 增加 @Serializable object SearchRouter : NavRouter
    - 在 polymorphic(NavKey::class) { ... } 里 subclass(SearchRouter::class, SearchRouter.serializer())
  app/shared/src/commonMain/kotlin/com/ciyin/app/ui/app/App.kt
    - entryProvider 里增加 entry<SearchRouter> {
        SearchScreen(
            title = stringResource(Res.string.search_title),
            backContentDescription = stringResource(Res.string.action_back),
            onBack = { navBackStack.back() },
        )
      }

可选（让首页 demo 列表能跳进来）：
  app/shared/src/commonMain/kotlin/com/ciyin/app/ui/screen/main/MainModel.kt
    - sealed interface MainDemoDestination 增加 data object Search : MainDemoDestination
  app/shared/src/commonMain/kotlin/com/ciyin/app/ui/screen/main/MainViewModel.kt
    - initState.demos 列表追加 MainDemoItem(destination = MainDemoDestination.Search, ...)
  app/shared/src/commonMain/kotlin/com/ciyin/app/ui/app/App.kt
    - MainScreen.onOpenDemo 的 when 分支追加 MainDemoDestination.Search -> navBackStack.navigate(SearchRouter)
```

## 四、不要照抄的反模式

- `AiImageDemoViewModel`：它继承的是 `androidx.lifecycle.ViewModel`，**完全没用 MVI / FlowRedux2**
  ，是早期为最小可用打底的临时实现。**不要**作为 ViewModel 模板。
- `SettingScreen`：没有 ViewModel，没有 `ScreenScaffold`，是占位。**不要**作为完整 Screen 模板。
- 任何把 `LazyListState` / `PagerState` 直接放进 `UiState` 的写法 —— 详见
  `.docs/contributing/mvi.md` 第四节第三点。
- 任何在 `Screen` 顶层之外（例如子 Composable / `component/` 下）`import XxxAction / XxxEffect` 的写法。
- 任何名为 `XxxPreview.kt` 的独立预览文件；Preview 必须和被预览的 Compose 实现处于同一 Kotlin 文件。
- 将 `@Composable` 扩展函数、private 扩展函数或页面状态机放进 `XxxExt.kt`。
