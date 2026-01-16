_# MVI 架构与 ViewModel 规范

本项目使用 MVI（Model-View-Intent）架构，结合 FlowRedux2 状态机处理复杂业务场景。

## 一、架构概述

### MVI 核心概念

- **Model**: 不可变的 UI 状态 (`State`)
- **View**: 渲染状态并发出意图 (`Composable`)
- **Intent**: 用户意图，触发状态变化 (`Action`)

### 数据流向

```
┌─────────────────────────────────────────────────────────────┐
│                        View (Compose)                        │
│  ┌─────────────────────────────────────────────────────────┐│
│  │ collectAsState(state) ──→ 渲染 UI                        ││
│  │                                                         ││
│  │ onClick ──→ dispatchAction(Action)                      ││
│  └─────────────────────────────────────────────────────────┘│
└──────────────────────────┬──────────────────────────────────┘
                           │ Action
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                       ViewModel                              │
│  ┌─────────────────────────────────────────────────────────┐│
│  │ dispatchAction ──→ 处理 Action ──→ 更新 State            ││
│  │                           │                              ││
│  │                           └──→ 触发 Effect (副作用)       ││
│  └─────────────────────────────────────────────────────────┘│
└──────────────────────────┬──────────────────────────────────┘
                           │ State / Effect
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                        View (Compose)                        │
│  - State 变化触发重组                                         │
│  - Effect 触发一次性事件 (导航、Toast 等)                       │
└─────────────────────────────────────────────────────────────┘
```

## 二、ViewModel 体系结构

### 接口层

```
┌─────────────────────────────────────────────────────────────┐
│                      MviViewModel<S, A, E>                  │
│         (组合接口，包含 State + Action + Effect)              │
└──────────────────────────┬──────────────────────────────────┘
                           │ 继承
       ┌───────────────────┼───────────────────┐
       ▼                   ▼                   ▼
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│StateViewModel│     │ActionViewModel│   │EffectViewModel│
│    <S>      │     │    <A>      │     │    <E>      │
└─────────────┘     └─────────────┘     └─────────────┘
```

| 接口                      | 职责    | 核心成员                                              |
|-------------------------|-------|---------------------------------------------------|
| `StateViewModel<S>`     | 状态管理  | `val state: StateFlow<S>`                         |
| `ActionViewModel<A>`    | 动作分发  | `val dispatchAction: (A) -> Unit`                 |
| `EffectViewModel<E>`    | 副作用触发 | `val sideEffects: SharedFlow<E>`, `poseEffect(E)` |
| `MviViewModel<S, A, E>` | 组合接口  | 继承以上三者                                            |

### 实现类继承关系

```
┌─────────────────────────────────────────────────────────────┐
│                     AbstractViewModel                        │
│            (基础类，提供后台协程作用域)                          │
└──────────────────────────┬──────────────────────────────────┘
                           │
       ┌───────────────────┼───────────────────┐
       ▼                   ▼                   ▼
┌─────────────────┐ ┌─────────────────┐ ┌─────────────────────────┐
│AbsMvvmViewModel │ │ AbsMviViewModel │ │StateMachineMviViewModel │
│    <S, E>       │ │   <S, A, E>     │ │      <S, A, E>          │
│ (MVVM 模式)     │ │ (简易 MVI 模式) │ │  (FlowRedux 状态机)     │
└─────────────────┘ └─────────────────┘ └─────────────────────────┘
```

## 三、ViewModel 基类选择

### 1. `AbstractViewModel` - 基础 ViewModel

最基础的 ViewModel，只提供后台协程作用域和异常处理，适用于最简单的场景：

```kotlin
class SimpleViewModel : AbstractViewModel() {
    private val _state = MutableStateFlow(SimpleState())
    val state: StateFlow<SimpleState> = _state.asStateFlow()

    fun doSomething() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            // 业务逻辑...
            _state.update { it.copy(isLoading = false, data = result) }
        }
    }
}
```

**特性：**

- 提供 `viewModelScope` 和 `backgroundScope`
- 内置协程异常处理 `onBackgroundScopeException`
- 自动清理资源

### 2. `AbsMvvmViewModel<S, E>` - MVVM 模式

实现 `StateViewModel<S>` 和 `EffectViewModel<E>`，适用于简单的状态管理 + 副作用场景：

```kotlin
class ProfileViewModel : AbsMvvmViewModel<ProfileState, ProfileEffect>() {

    override val initialState = ProfileState()

    fun loadProfile() {
        viewModelScope.launch {
            updateState { copy(isLoading = true) }
            getUserProfileUseCase()
                .fold(
                    { error ->
                        updateState { copy(isLoading = false, error = error.message) }
                        poseEffect(ProfileEffect.ShowError(error))
                    },
                    { profile ->
                        updateState { copy(isLoading = false, profile = profile) }
                    }
                )
        }
    }
}
```

**特性：**

- 内置 `state: StateFlow<S>` 状态流
- 内置 `sideEffects: SharedFlow<E>` 副作用流
- 提供 `updateState { }` 安全更新状态
- 提供 `poseEffect(effect)` 触发副作用

### 3. `AbsMviViewModel<S, A, E>` - 简易 MVI 模式

实现完整的 `MviViewModel<S, A, E>` 接口，通过 `on<Action>` DSL 处理动作：

```kotlin
class AuthViewModel : AbsMviViewModel<AuthState, AuthAction, AuthEffect>() {

    override val initialState = AuthState()

    init {
        // 使用 on<Action> DSL 注册动作处理器
        on<AuthAction.Submit> { action ->
            updateState { copy(isSubmitting = true) }
            loginUseCase(state.value.toLoginInput())
                .fold(
                    { error ->
                        updateState { copy(isSubmitting = false) }
                        poseEffect(AuthEffect.ShowMessage(error.message))
                    },
                    {
                        updateState { copy(isSubmitting = false) }
                        poseEffect(AuthEffect.NavigateToMain)
                    }
                )
        }

        on<AuthAction.TabSelected> { action ->
            updateState { copy(currentTab = action.tab) }
        }
    }
}
```

**特性：**

- 内置 `dispatchAction: (A) -> Unit` 动作分发
- 使用 `on<SubAction> { }` DSL 注册动作处理器
- 支持 `invoke(action)` 操作符调用

### 4. `StateMachineMviViewModel<S, A, E>` - FlowRedux 状态机

使用 FlowRedux 状态机，适用于复杂业务场景：

```kotlin
class ComplexViewModel : StateMachineMviViewModel<State, Action, Effect>() {

    override val initialize: FlowReduxStateMachineFactory<State, Action>.() -> Unit = {
        initializeWith { State() }
    }

    override val spec: FlowReduxBuilder<State, Action>.() -> Unit = {
        inState<State> {
            on<Action.DoSomething> {
                mutate { copy(isLoading = true) }
            }

            condition({ it.isLoading }) {
                onEnter {
                    // 异步操作...
                    mutate { copy(isLoading = false) }
                }
            }
        }
    }
}
```

**特性：**

- 基于 FlowRedux 状态机
- 声明式状态转换 DSL
- 支持 `condition`、`onEnter` 等高级特性
- 适合复杂的状态编排

### 选择指南

| 场景                 | 推荐 ViewModel               | 说明                         |
|--------------------|----------------------------|----------------------------|
| 纯数据展示，无副作用         | `AbstractViewModel`        | 最简单，手动管理状态                 |
| 简单 CRUD + Toast/导航 | `AbsMvvmViewModel`         | 有状态和副作用，无动作分发              |
| 表单提交、用户交互          | `AbsMviViewModel`          | 完整 MVI，使用 `on<Action>` DSL |
| 多步骤流程、复杂状态         | `StateMachineMviViewModel` | FlowRedux 状态机              |
| 可复用状态逻辑            | `StateMachineMviViewModel` | 子状态机模式                     |

### 快速决策流程

```
需要处理用户动作？
  ├─ 否 → 需要副作用（Toast/导航）？
  │         ├─ 否 → AbstractViewModel
  │         └─ 是 → AbsMvvmViewModel
  └─ 是 → 状态转换复杂（条件分支、子状态机）？
            ├─ 否 → AbsMviViewModel
            └─ 是 → StateMachineMviViewModel
```

## 四、State 定义规范

### 1. 基本结构

```kotlin
@Stable
data class AuthState(
    // 用户输入
    val email: TextFieldState = TextFieldState(),
    val password: TextFieldState = TextFieldState(),

    // UI 状态
    val isLoading: Boolean = false,
    val currentTab: AuthTab = AuthTab.Login,

    // 业务数据
    val emailSuffixes: List<String> = emptyList(),

    // 子状态机状态
    val sendCodeUiState: SendCodeState = SendCodeState.Ready.Idle,
    val captchaUiState: CaptchaState = CaptchaState.Idle,
) {
    // 派生状态（计算属性）
    val isSubmitEnabled: Boolean
        get() = email.text.isNotBlank() && password.text.isNotBlank() && !isLoading

    val fullEmail: String
        get() = "${email.text}${emailSuffixes.getOrElse(0) { "" }}"
}
```

### 2. State 设计原则

- 使用 `@Stable` 注解优化 Compose 重组
- 所有字段都应有合理的默认值
- 复杂逻辑使用派生属性（计算属性）
- 子状态机状态使用独立的 sealed class

### 3. Compose 运行时状态（LazyListState 等）

在 Compose 中，`LazyListState`、`PagerState`、`TextFieldState`（注意：此处特指
`androidx.compose.foundation.text.input.TextFieldState` 这类运行时状态）等属于 **UI 运行时状态对象**
，它们：

- 依赖 Compose runtime（可变对象、内部含 snapshot 机制）
- 通常不可序列化/不可回放
- 不适合作为“单一事实源”放进 `State`（会破坏 `State` 不可变、可测试、可预测的前提）

因此，本项目推荐的做法是：

1. **`State` 只保存“纯数据”**（业务数据 + UI 展示所需的不可变字段），不直接持有
   `LazyListState/PagerState` 等对象。
2. **“需要滚动/聚焦/切页”等 UI 操作使用 `Effect` 表达**（一次性命令），由 Compose 层消费并执行，保持单向数据流。
3. **运行时状态的保存与恢复在 Compose 层完成**：
    - 优先使用 `rememberSaveable`/`Saver`
    - 或使用项目内的状态管理器（如 `SaverStateManager`）按 ID 保存/恢复（适用于多列表、多 Tab 的场景）

典型示例：重复点击当前 Tab 时“回到顶部”

```kotlin
// Action：用户点击 Tab
sealed interface HomeAction {
    data class SelectCategory(val index: Int) : HomeAction
}

// Effect：请求 UI 执行一次性状态操作
sealed interface HomeEffect {
    data class ScrollToTop(val categoryId: Int) : HomeEffect
}

// ViewModel：只做决策，不直接操作 LazyListState
on<HomeAction.SelectCategory> { action ->
    if (action.index == snapshot.categoryIndex) {
        poseEffect(HomeEffect.ScrollToTop(categoryId = currentCategoryId))
        noChange()
    } else {
        mutate { copy(categoryIndex = action.index) }
    }
}

// Compose：消费 Effect，找到对应 LazyListState 并执行滚动
viewModel.collectSideEffects { effect ->
    when (effect) {
        is HomeEffect.UiState.ScrollToTop -> {
            saverStateManager.get<LazyListState>("HomeContent-${effect.categoryId}")
                ?.animateScrollToItem(0)
        }
    }
}
```

> 何时可以把“滚动位置”放进 `State`？
>
> 仅在你确实需要跨页面/跨进程/跨路由共享或回放时，才考虑把 **纯数据形式** 的位置（例如
`firstVisibleItemIndex/firstVisibleItemScrollOffset`）放入 `State`；不要把 `LazyListState` 本体放进
`State`。

### 4. 子状态机模式

```kotlin
/**
 * 发送验证码的状态机。
 */
@Stable
sealed interface SendCodeState {
    /** 空闲状态，可以发送 */
    sealed interface Ready : SendCodeState {
        data object Idle : Ready
        data object ReSend : Ready
    }

    /** 正在发送验证码 */
    data class Sending(val challengeToken: String? = null) : SendCodeState

    /** 冷却中，防止频繁发送 */
    data class Cooldown(val remainingSeconds: Int) : SendCodeState

    /** 需要滑动验证 */
    data class NeedCaptcha(val challengeId: String) : SendCodeState
}

// 扩展属性便于 UI 使用
val SendCodeState.canSend: Boolean
get() = this is SendCodeState.Ready

fun SendCodeState.getButtonText(): String = when (this) {
    is SendCodeState.Ready.Idle -> "发送验证码"
    is SendCodeState.Sending -> "发送中..."
    is SendCodeState.Cooldown -> "${remainingSeconds}s后重新发送"
    is SendCodeState.NeedCaptcha -> "发送中..."
    is SendCodeState.Ready.ReSend -> "重新发送"
}
```

## 五、Action 定义规范

### 1. 基本结构

```kotlin
interface AuthAction {
    // 简单动作
    data object Submit : AuthAction
    data object ForgetPassword : AuthAction

    // 带参数的动作
    data class TabSelected(val tab: AuthTab) : AuthAction
    data class EmailSuffixIndexChanged(val index: Int) : AuthAction

    // 复杂动作（包含多个参数）
    data class CaptchaPositionSubmitted(
        val captchaId: String,
        val x: Int
    ) : AuthAction
}
```

### 2. Action 命名约定

| UI 回调            | Action 名称                         |
|------------------|-----------------------------------|
| `onClick`        | 描述性名称，如 `Submit`, `NavigateToXxx` |
| `onValueChanged` | `XxxChanged`, 如 `EmailChanged`    |
| `onSelected`     | `XxxSelected`, 如 `TabSelected`    |
| `onDismiss`      | `DismissXxx` 或 `CloseXxx`         |

### 3. 子状态机 Action

```kotlin
/**
 * 滑动验证子状态机的 Action。
 */
sealed interface CaptchaAction {
    data class SubmitPosition(val captchaId: String, val x: Int) : CaptchaAction
    data object Close : CaptchaAction
}
```

## 六、Effect 定义规范

### 1. 基本结构

```kotlin
internal interface AuthEffect {
    // 导航
    data object NavigateToMain : AuthEffect
    data object NavigateBack : AuthEffect
    data class NavigateToProfile(val userId: String) : AuthEffect

    // 消息提示
    data class ShowMessage(val message: String) : AuthEffect
    data class ShowError(val error: GenericError) : AuthEffect

    // 其他一次性事件
    data class OpenUrl(val url: String) : AuthEffect
    data object CloseDialog : AuthEffect
}
```

### 2. Effect 使用场景

| 场景                 | Effect 类型                       |
|--------------------|---------------------------------|
| 页面跳转               | `NavigateToXxx`                 |
| Toast/Snackbar     | `ShowMessage` / `ShowToast`     |
| 对话框                | `ShowDialog` / `CloseDialog`    |
| 外部链接               | `OpenUrl`                       |
| 系统分享               | `Share`                         |
| UI 状态操作（滚动/聚焦/切页等） | `UiState.Xxx`（例如 `ScrollToTop`） |

### 3. Effect 消费

**方式一：使用 `collectSideEffects` 扩展函数（推荐）**

```kotlin
@Composable
fun AuthScreen(viewModel: AuthViewModel = koinViewModel()) {
    val state by viewModel.stateCollectAsStateWithLifecycle()
    val navigator = LocalNavigator.current
    val toaster = LocalToaster.current

    // 使用扩展函数消费 Effect
    viewModel.collectSideEffects { effect ->
        when (effect) {
            is AuthEffect.NavigateToMain -> navigator.navigateToMain()
            is AuthEffect.ShowMessage -> toaster.show(effect.message)
            else -> Unit
        }
    }

    AuthScreenContent(
        state = state,
        onAction = viewModel.dispatchAction
    )
}
```

**方式二：使用 LaunchedEffect**

```kotlin
@Composable
fun AuthScreen(viewModel: AuthViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsState()
    val navigator = LocalNavigator.current
    val toaster = LocalToaster.current

    // 使用 LaunchedEffect 消费 Effect
    LaunchedEffect(Unit) {
        viewModel.sideEffects.collect { effect ->
            when (effect) {
                is AuthEffect.NavigateToMain -> navigator.navigateToMain()
                is AuthEffect.ShowMessage -> toaster.show(effect.message)
                else -> Unit
            }
        }
    }

    AuthScreenContent(
        state = state,
        onAction = viewModel.dispatchAction
    )
}
```

**扩展函数说明：**

| 扩展函数                                 | 来源                | 说明                        |
|--------------------------------------|-------------------|---------------------------|
| `stateCollectAsStateWithLifecycle()` | `StateViewModel`  | 生命周期感知的状态收集               |
| `collectSideEffects { }`             | `EffectViewModel` | 副作用收集，内部使用 LaunchedEffect |

## 七、FlowRedux2 状态机使用

> 📖 **详细文档**: 请参阅 [flowredux.md](/.docs/guides/flow-redux.md) 获取完整的 FlowRedux2 API 使用指南。

### 1. 基本结构

```kotlin
class AuthViewModel : StateMachineMviViewModel<AuthState, AuthAction, AuthEffect>(), KoinComponent {

    private val loginUseCase by inject<LoginUserUseCase>()

    override val initialize: FlowReduxStateMachineFactory<AuthState, AuthAction>.() -> Unit = {
        initializeWith { AuthState() }
    }

    override val spec: FlowReduxBuilder<AuthState, AuthAction>.() -> Unit = {
        inState<AuthState> {
            // 处理 Action
            on<AuthAction.Submit> {
                mutate { copy(isSubmitting = true) }
            }

            // 条件状态处理
            condition({ it.isSubmitting }) {
                onEnter {
                    // 执行异步操作
                    loginUseCase(snapshot.toLoginInput())
                        .fold(
                            { error -> poseEffect(AuthEffect.ShowMessage(error.message)) },
                            { poseEffect(AuthEffect.NavigateToMain) }
                        )
                    mutate { copy(isSubmitting = false) }
                }
            }
        }
    }
}
```

### 2. 核心 DSL 速查

| DSL                                  | 用途          |
|--------------------------------------|-------------|
| `inState<S>`                         | 定义状态类型的处理块  |
| `on<A>`                              | 处理特定 Action |
| `onEnter`                            | 进入状态时执行（一次） |
| `condition({ predicate })`           | 条件状态处理      |
| `collectWhileInState(flow)`          | 订阅 Flow     |
| `untilIdentityChanged({ selector })` | 属性变化时重新执行   |

| 状态操作                    | 用途                                           |
|-------------------------|----------------------------------------------|
| `override { NewState }` | 切换到新状态类型                                     |
| `mutate { copy(...) }`  | 修改当前状态属性                                     |
| `noChange()`            | 不改变状态                                        |
| `snapshot`              | 获取当前状态快照                                     |
| `poseEffect(effect)`    | 触发副作用 ⚠️ *项目扩展，仅 `StateMachineMviViewModel`* |

### 3. 重要规则

> **⚠️ 一个 DSL 块中只能改变一次状态，多次调用只有最后一次生效**

```kotlin
// ❌ 错误：多次改变状态
on<AuthAction.Submit> {
    mutate { copy(isLoading = true) }  // 被忽略！
    mutate { copy(error = null) }       // 生效
}

// ✅ 正确：一次性改变所有需要的状态
on<AuthAction.Submit> {
    mutate { copy(isLoading = true, error = null) }
}
```

## 八、Screen 组件结构

### 1. 推荐结构

```kotlin
// AuthScreen.kt
@Composable
fun AuthScreen(
    viewModel: AuthViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    // 处理 Effect
    HandleEffects(viewModel)

    AuthScreenContent(
        state = state,
        onAction = viewModel.dispatchAction
    )
}

@Composable
private fun HandleEffects(viewModel: AuthViewModel) {
    val navigator = LocalNavigator.current
    val toaster = LocalToaster.current

    LaunchedEffect(Unit) {
        viewModel.sideEffects.collect { effect ->
            when (effect) {
                is AuthEffect.NavigateToMain -> navigator.navigateToMain()
                is AuthEffect.ShowMessage -> toaster.show(effect.message)
                AuthEffect.NavigateBack -> navigator.pop()
            }
        }
    }
}

@Composable
private fun AuthScreenContent(
    state: AuthState,
    onAction: (AuthAction) -> Unit
) {
    Scaffold {
        // UI 实现
        Button(onClick = { onAction(AuthAction.Submit) }) {
            Text("提交")
        }
    }
}

@AppPreview
@Composable
private fun AuthScreenPreview() = AppPreview {
    AuthScreenContent(
        state = AuthState(emailSuffixes = listOf("@gmail.com")),
        onAction = {}
    )
}
```

### 2. 分离原则

| 组件                 | 职责                          |
|--------------------|-----------------------------|
| `XxxScreen`        | 连接 ViewModel，处理 Effect      |
| `XxxScreenContent` | 纯 UI 渲染，接收 state 和 onAction |
| `HandleEffects`    | 集中处理副作用                     |

## 九、最佳实践

1. **State 不可变**: 始终通过 `copy()` 创建新状态
2. **Action 单一职责**: 每个 Action 只表达一个用户意图
3. **Effect 一次性**: Effect 用于一次性事件，不要存储在 State 中
4. **分离 UI 和逻辑**: Content 组件应该是纯函数，便于预览和测试
5. **利用派生状态**: 复杂的 UI 逻辑放在 State 的计算属性中
6. **子状态机复用**: 通用的状态逻辑抽取为可复用的子状态机_
