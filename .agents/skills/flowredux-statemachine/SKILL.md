---
name: flowredux-statemachine
description: 在 NomiKit 项目（com.ciyin.app）中使用 FlowRedux2 状态机进行 ViewModel 编排时的 DSL 速查、关键约束与进阶模式（condition / untilIdentityChanges / collectWhileInState / 子状态机 onEnterStartStateMachine 与 onActionStartStateMachine / 复用 suspend 提取）。本 skill 不负责脚手架式新建 screen（请用 create-kmp-screen），专门用于"已有 StateMachineMviViewModel 想写更复杂的状态流转 / 子状态机 / 多状态 sealed interface"的场景。当用户提到 FlowRedux / FlowRedux2 / inState / onEnter / mutate / override / noChange / condition / untilIdentityChanges / collectWhileInState / 子状态机 / onEnterStartStateMachine / ChangeableState / ChangedState / actionMapper 时使用。
---

# FlowRedux2 状态机进阶 Skill

本 skill 是 NomiKit 项目（包名 `com.ciyin.app.*`，FlowRedux2 版本 `2.0.1`，库坐标
`com.freeletics.flowredux2:flowredux`）使用 FlowRedux2 状态机的**实操参考**：DSL
速查、硬性约束、子状态机模式、错误处理与测试。

> **职责边界**：
> - 「如何从零起一个新的 screen + ViewModel + Action + Effect + UiState」 → 用 `create-kmp-screen`
    skill。
> - 「已有 `XxxViewModel: StateMachineMviViewModel<...>()` 怎么把状态机写好/写复杂/拆子状态机」 → 用本
    skill。
>
> 关于 ViewModel/Screen 文件的创建模板、命名约定、目录结构、Preview 套路，**不在**本 skill 重复，全部以
`create-kmp-screen` 为准。

> **项目当前状态（2026-04 时点）**：
> - 业务侧目前只有 `MainViewModel` 在用 `AbsMviViewModel`，**还没有任何 ViewModel 真正继承
    `StateMachineMviViewModel`**。
> - 本 skill 中的 DSL 模板、约束、模式均**基于本项目 `core/ui-foundation` 中
    `StateMachineMviViewModel` 的真实签名**与 FlowRedux2 2.0.1 源码，而非任何外部历史项目（如
    honeypot /
    Polyvision）的遗留写法。

GitHub 上游：<https://github.com/freeletics/FlowRedux>

## 快速决策：是否需要状态机

```
页面是否有"多步骤流程"或"条件触发的异步逻辑"？
  ├─ 是 → StateMachineMviViewModel（本文档）
  └─ 否 → 有 Action 分发吗？
           ├─ 是 → AbsMviViewModel（on<Action> DSL，如项目里 MainViewModel）
           └─ 否 → AbsMvvmViewModel（updateState + poseEffect）
```

> 三种基类都在 `ciyin.ui.foundation.viewmodel` 包内。

---

## 一、核心类型体系（源自 FlowRedux2 2.0.1 源码）

### 1.1 类层次结构

```
FlowReduxStateMachineFactory<S, A>    ← 用户继承的基类（定义状态机）
    ├── initializeWith { }            ← 顶层扩展函数，设置初始状态
    ├── spec { }                      ← 定义状态转换规范
    ├── installLogger(logger, name)   ← 安装日志器（须在 spec 前调用）
    ├── launchIn(scope) → FlowReduxStateMachine<StateFlow<S>, A>
    └── shareIn(scope)  → FlowReduxStateMachine<SharedFlow<S>, A>

FlowReduxStateMachine<S, A>           ← 运行中的状态机实例
    ├── state: S                      ← 状态流（StateFlow 或 SharedFlow）
    ├── dispatchAction: (A) -> Unit   ← 非挂起的 Action 分发
    └── dispatch(action: A)           ← 挂起函数的 Action 分发

StateMachineMviViewModel<S, A, E>     ← 项目封装的 ViewModel 基类
    ├── state: StateFlow<S>           ← 委托给 stateMachine.state
    ├── dispatchAction: (A) -> Unit   ← 委托给 stateMachine.dispatchAction
    ├── sideEffects: SharedFlow<E>    ← 副作用流
    ├── poseEffect(effect)            ← suspend，异步发送副作用
    └── tryPoseEffect(effect)         ← 尝试发送副作用（非挂起，返回 Boolean）
```

> 项目实现见
`core/ui-foundation/src/commonMain/kotlin/ciyin/ui/foundation/viewmodel/StateMachineMviViewModel.kt`。

### 1.2 状态变更类型体系

```
State<InputState>                     ← 密封基类，只读
    ├── snapshot: InputState          ← 触发时刻的状态快照
    └── ChangeableState<InputState>   ← 可变子类，增加状态变更能力
        ├── mutate { }  → ChangedState<InputState>   修改当前状态
        ├── override { } → ChangedState<S>           替换为新状态（可切换类型）
        └── noChange()  → ChangedState<Nothing>      不改变状态
```

**关键区分**：

- `onEnter` / `on<Action>` / `collectWhileInState` 的 handler 接收者是 **`ChangeableState`**，必须返回
  `ChangedState`
- `onEnterEffect` / `onActionEffect` / `collectWhileInStateEffect` 的 handler 接收者是 **`State`
  **（只读），返回 `Unit`

### 1.3 DSL Builder 嵌套层次

```
FlowReduxBuilder<S, A>               ← spec { } 的接收者
    └── inState<SubState> { }        → InStateBuilder<SubState, S, A>
            ├── condition { }        → ConditionBuilder<SubState, S, A>
            │       └── untilIdentityChanges { } → IdentityBuilder<SubState, S, A>
            └── untilIdentityChanges { } → IdentityBuilder<SubState, S, A>

BaseBuilder<InputState, S, A>         ← 所有 Builder 的抽象基类
    ├── InStateBuilder                 可嵌套 condition / untilIdentityChanges
    ├── ConditionBuilder               可嵌套 untilIdentityChanges
    └── IdentityBuilder                不可再嵌套
```

> 所有 Builder 均继承 `BaseBuilder`，因此都能使用
`on` / `onEnter` / `collectWhileInState` / `onEnterStartStateMachine` 等方法。

---

## 二、ViewModel 模板（项目真实签名）

> **重要**：项目里的 `StateMachineMviViewModel` 通过**两个 abstract 函数**暴露契约，而**不是**abstract
> 属性。任何写成
`override val initialize: FlowReduxStateMachineFactory<...>.() -> Unit = { ... }` 的代码在本项目里*
*编译不过**。

### 2.1 最小 StateMachineMviViewModel

```kotlin
package com.ciyin.app.ui.screen.<feature >

        import ciyin . ui . foundation . viewmodel . StateMachineMviViewModel
        import com . ciyin . app . ui . util . UiEffectHandler
        import com . freeletics . flowredux2 . FlowReduxBuilder
        import com . freeletics . flowredux2 . FlowReduxStateMachineFactory
        import org . koin . core . component . KoinComponent

/**
 * <Feature> 页面的 ViewModel。
 */
class <Feature>ViewModel :
StateMachineMviViewModel < < Feature > UiState, <Feature>Action, <Feature>Effect>(),
KoinComponent, UiEffectHandler {

    override fun FlowReduxStateMachineFactory<<Feature>UiState, <Feature>Action>.initialize() {
    initializeWith { <Feature > UiState() }
}

    override fun FlowReduxBuilder<<Feature>UiState, <Feature>Action>.spec() {
    inState < < Feature > UiState > {
        on < < Feature > Action . BackClick > { _ ->
            poseEffect(< Feature > Effect . NavigateBack)
            noChange()
        }
        // 其它 on<...> { ... } 处理块
    }
}
}
```

### 2.2 必须实现的两个抽象函数

| 函数             | 接收者                                 | 作用       |
|----------------|-------------------------------------|----------|
| `initialize()` | `FlowReduxStateMachineFactory<S,A>` | 设置初始状态   |
| `spec()`       | `FlowReduxBuilder<S, A>`            | 定义状态转换规范 |

### 2.3 initializeWith 参数

```kotlin
initializeWith(
    reuseLastEmittedStateOnLaunch: Boolean = true,  // true：复用上次状态；false：每次重建
initialState: () -> S
)
```

---

## 三、DSL 完整速查

### 3.1 全部 DSL 构件（按 `BaseBuilder` 源码）

```kotlin
spec {
    // ═══════════════════ 状态入口 ═══════════════════
    inState<SubState> {                              // 匹配状态类型（KClass.isInstance）

        // ─── 进入状态触发 ───
        onEnter { ... }                              // suspend ChangeableState<SubState>.() -> ChangedState<S>
        onEnterEffect { ... }                        // suspend State<SubState>.() -> Unit（不改变状态）

        // ─── Action 处理 ───
        on<SubAction>(                               // suspend ChangeableState<SubState>.(SubAction) -> ChangedState<S>
            executionPolicy = ExecutionPolicy.CancelPrevious
        ) { action -> ... }

        onActionEffect<SubAction>(                   // suspend State<SubState>.(SubAction) -> Unit
            executionPolicy = ExecutionPolicy.CancelPrevious
        ) { action -> ... }

        // ─── Flow 订阅 ───
        collectWhileInState(                         // 直接传 Flow
            flow = someFlow,
            executionPolicy = ExecutionPolicy.Ordered
        ) { item -> ... }                            // suspend ChangeableState<SubState>.(T) -> ChangedState<S>

        collectWhileInState(                         // 基于初始状态创建 Flow
            flowBuilder = { inputState -> flow { ... } },
            executionPolicy = ExecutionPolicy.Ordered
        ) { item -> ... }

        collectWhileInStateEffect(flow) { item ->    // Effect 版本（不改变状态）
            // suspend State<SubState>.(T) -> Unit
        }

        // ─── 条件块（仅 InStateBuilder 可用） ───
        condition({ it.someProperty }) {             // 额外条件谓词
            // ConditionBuilder 内部，可用所有 BaseBuilder 方法 + untilIdentityChanges
        }

        // ─── 身份监控（InStateBuilder / ConditionBuilder 可用） ───
        untilIdentityChanges({ it.selectedId }) {    // 选择器值变化时取消并重启内部操作
            // IdentityBuilder 内部，可用所有 BaseBuilder 方法
        }

        // ─── 子状态机（进入状态时启动） ───
        onEnterStartStateMachine(
            stateMachineFactoryBuilder = { /* State<InputState>.() -> Factory */ },
            actionMapper = { parentAction -> childAction? },  // 可选：省略则不转发 Action
            cancelOnState = { childState -> false },          // 可选：子状态满足条件时取消
            handler = { childState -> mutate { copy(...) } }  // ChangeableState<InputState>.(ChildState) -> ChangedState<S>
        )

        // ─── 子状态机（Action 触发启动） ───
        onActionStartStateMachine<TriggerAction, ChildState>(
            stateMachineFactoryBuilder = { action -> /* State<InputState>.(TriggerAction) -> Factory */ },
            actionMapper = { parentAction -> childAction? },
            cancelOnState = { childState -> false },
            handler = { childState -> mutate { copy(...) } }
        )
    }

    // 可定义多个 inState 块，匹配不同的状态子类型
    inState<SubState2> { ... }

    // 匹配所有状态（基类）
    inState<RootState> { ... }
}
```

### 3.2 状态变更 API（三选一）

| API                       | 接收者                  | 用途            | 返回类型              |
|---------------------------|----------------------|---------------|-------------------|
| `mutate { copy(...) }`    | `ChangeableState<T>` | 修改当前状态的部分字段   | `ChangedState<T>` |
| `override { NewState() }` | `ChangeableState<T>` | 完全替换状态（可切换类型） | `ChangedState<S>` |
| `noChange()`              | `ChangeableState<T>` | 不改变状态         | `ChangedState<S>` |
| `snapshot`                | `State<T>`           | 获取触发时刻的状态快照   | `T`（只读）           |

### 3.3 ExecutionPolicy（执行策略）

| 策略                    | 内部实现                 | 适用场景        |
|-----------------------|----------------------|-------------|
| `CancelPrevious`（默认）  | `flatMapLatest`      | 搜索防抖、表单提交   |
| `Ordered`             | `flatMapConcat`      | 需要保证顺序的操作   |
| `Unordered`           | `flatMapMerge`       | 独立的并行操作     |
| `Throttled(duration)` | 节流 + `flatMapConcat` | 高频事件（如进度上报） |

### 3.4 副作用 API（项目扩展，非 FlowRedux 原生）

| API                     | 说明                                                               |
|-------------------------|------------------------------------------------------------------|
| `poseEffect(effect)`    | **suspend**，通过 `Dispatchers.Default + emit` 异步发送副作用，可能因下游慢而挂起    |
| `tryPoseEffect(effect)` | 非挂起，调用 `tryEmit`，返回 `Boolean`（`MutableSharedFlow` 的 buffer 满会失败） |

> 仅在 `StateMachineMviViewModel` 内可用（`spec()` 块内可直接调用，因为是 ViewModel 实例的方法）。**子状态机
> `FlowReduxStateMachineFactory` 子类没有 `poseEffect`**，需要通过构造参数回调把副作用回传给父
> ViewModel —— 见第五章。

---

## 四、关键约束（必须遵守）

### 约束 1：一个 DSL 块只能改变一次状态

```kotlin
// ❌ 错误：多次状态变更，只有最后一次生效
on<Action.Submit> {
    mutate { copy(isLoading = true) }   // 被忽略！
    mutate { copy(error = null) }       // 生效
}

// ✅ 正确：一次性合并所有变更
on<Action.Submit> {
    mutate { copy(isLoading = true, error = null) }
}
```

**原理**：`on` / `onEnter` 的返回值是 `ChangedState<S>`，Kotlin 函数只有一个返回值，多次调用只取最后一个。

### 约束 2：mutate vs override

```kotlin
// mutate：修改同一类型状态（lambda 的 this 是 InputState）
inState<PageState.Content> {
    on<Action.LoadMore> {
        mutate { copy(isLoadingMore = true) }  // ✅ 仍然是 PageState.Content
    }
}

// override：切换到不同状态类型
inState<PageState.Loading> {
    onEnter {
        override { PageState.Content(items) }  // ✅ Loading → Content
    }
}

// ❌ 错误：mutate 不能切换类型
inState<PageState.Loading> {
    onEnter { mutate { PageState.Content(items) } }   // 编译错误
}
```

### 约束 3：状态变化时 onEnter / on 会被取消

```kotlin
inState<PageState.Loading> {
    onEnter {
        val result = longRunningCall()  // 状态变化时被 CancellationException
        override { PageState.Content(result) }
    }
}
```

### 约束 4：condition 谓词必须简单快速

```kotlin
// ✅ 推荐：简单属性检查
condition({ it.isSubmitting }) { ... }
condition({ it.detail is DetailState.Loading }) { ... }

// ❌ 避免：复杂计算
condition({ it.items.filter { ... }.count() > 10 }) { ... }
```

### 约束 5：snapshot 是快照，mutate / override 内的 this 是最新状态

```kotlin
on<Action.Load> {
    val param = snapshot.email       // ✅ 触发时的快照，用于发起请求
    val result = api.load(param)     // 异步操作期间状态可能已变
    mutate { copy(data = result) }   // ✅ mutate 内的 this 是 reduce 时的最新状态
}
```

> **源码依据**：`mutate` 内部创建 `UnsafeMutateState(reducer)`，在 `reduxStore` 的状态变更队列中执行
`reducer(currentState)`。

### 约束 6：同一 inState 可定义多个 onEnter（并行执行）

```kotlin
inState<HomeUiState> {
    onEnter { loadCategories() }   // 并行执行
    onEnter { loadGuides() }       // 并行执行
    onEnter { loadNotices() }      // 并行执行
}
```

每个 `onEnter` 是独立的 `SideEffect`，在进入状态时同时启动。

### 约束 7：不要在 spec 内使用 state.value

```kotlin
// ❌ 错误：绕过状态机的状态管理
on<Action.Load> {
    val items = state.value.items  // 不应该直接访问 state.value
}

// ✅ 正确：使用 snapshot
on<Action.Load> {
    val items = snapshot.items
}
```

### 约束 8：副作用要选对 API

- `poseEffect` 是 `suspend`，下游 buffer 满会被挂起；适合**保证一定送达**的场景。
- `tryPoseEffect` 非挂起，buffer 满会**直接丢弃并返回 `false`**；适合**高频但允许丢失**的场景（如进度上报）。
- 切勿在 spec 中 `viewModelScope.launch { poseEffect(...) }` —— 已经在 `viewModelScope` 内。

---

## 五、子状态机

> **何时拆子状态机**：当一段状态流转**有自己独立的生命周期**（进入特定父状态时启动 / 离开时取消）、*
*有自己独立的状态机模型
> `(ChildState, ChildAction)`**、并且**与父状态可以解耦**时再拆。
> 简单的"一次性加载"建议先用形态 B（扩展函数），不要立刻上完整子状态机类。

### 5.1 两种形态

**形态 A：完整子状态机类**（复杂、可复用的独立逻辑）

```kotlin
internal class <Child>StateMachine(
private val onEffect: (<Child>Effect) -> Unit,
private val someUseCase: SomeUseCase,
private val initialState: <Child>State = <Child>State(),
) : FlowReduxStateMachineFactory<<Child>State, <Child>Action>() {
    init {
        spec {
            initializeWith { initialState }
            inState < < Child > State > { /* 完整的状态转换逻辑 */ }
        }
    }
}
```

**形态 B：BaseBuilder 扩展函数**（简单的一次性加载逻辑，**推荐起步**）

```kotlin
// 不创建子状态机类，直接在父状态机的 Builder 上定义 onEnter
fun <Action : Any> BaseBuilder<<Parent>State.Active, <Parent>State, Action>.start<Child>LoadingForActive(
loadDetailUseCase: LoadDetailUseCase,
) {
    onEnter {
        val loading = snapshot.detail as DetailState.Loading
        loadDetailUseCase(loading.id).fold(
            ifLeft = { error -> mutate { copy(detail = DetailState.Error(error.message)) } },
            ifRight = { detail -> mutate { copy(detail = DetailState.Success(detail)) } }
        )
    }
}
```

### 5.2 完整子状态机类模板

```kotlin
package com.ciyin.app.ui.screen.<feature > . statemachine .<child>

import com . freeletics . flowredux2 . FlowReduxStateMachineFactory
        import com . freeletics . flowredux2 . initializeWith

        /**
         * <Child> 子状态机。
         *
         * 负责 [描述职责]。
         *
         * @property onEffect 副作用回调（向父 ViewModel 透传）
         * @property someUseCase 业务依赖
         * @property initialState 初始状态（一般由父状态传入）
         */
        internal class <Child>StateMachine(
private val onEffect: (<Child>Effect) -> Unit,
private val someUseCase: SomeUseCase,
private val initialState: <Child>State = <Child>State(),
) : FlowReduxStateMachineFactory<<Child>State, <Child>Action>() {

    init {
        spec {
            initializeWith { initialState }

            inState < < Child > State > {
                on < < Child > Action . DoSomething > { action ->
                    someUseCase(action.param).fold(
                        { error ->
                            onEffect(< Child > Effect . ShowError (error.message))
                            noChange()
                        },
                        { result ->
                            mutate { copy(data = result) }
                        }
                    )
                }
            }
        }
    }
}
```

### 5.3 启动扩展函数模板

```kotlin
/**
 * 在 [<Parent>State.Active] 状态下启动 <Child> 子状态机。
 */
fun <Action : Any> BaseBuilder<<Parent>State.Active, <Parent>State, Action>.start<Child>StateMachineForActive(
someUseCase: SomeUseCase,
on<Child> Effect :(< Child > Effect) -> Unit,
) {
    onEnterStartStateMachine(
        stateMachineFactoryBuilder = {
            <Child > StateMachine(
            onEffect = on<Child> Effect,
            someUseCase = someUseCase,
            initialState = snapshot.< child >,    // 从父状态传入初始值
        )
        },
        actionMapper = { action ->
            when (action) {
                is <Parent>Action.< Child > DoSomething-><Child > Action.DoSomething(action.param)
                else -> null                        // 不转发
            }
        },
        handler = { childState ->
            mutate { copy(< child > = childState) }   // 同步子状态到父状态
        }
    )
}
```

### 5.4 onEnterStartStateMachine 完整签名（源码）

```kotlin
public fun <SubStateMachineState : Any, SubStateMachineAction : Any> onEnterStartStateMachine(
    stateMachineFactoryBuilder: State<InputState>.() -> FlowReduxStateMachineFactory<SubStateMachineState, SubStateMachineAction>,
    actionMapper: (A) -> SubStateMachineAction?,           // 父 Action → 子 Action（null 不转发）
    cancelOnState: (SubStateMachineState) -> Boolean = { false },  // 子状态满足条件时取消
    name: String? = null,                                   // 日志标识
    handler: suspend ChangeableState<InputState>.(SubStateMachineState) -> ChangedState<S>,
)

// 不需要转发 Action 的简化版本（Action 类型为 A，无 actionMapper）
public fun <SubStateMachineState : Any> onEnterStartStateMachine(
    stateMachineFactoryBuilder: State<InputState>.() -> FlowReduxStateMachineFactory<SubStateMachineState, A>,
    cancelOnState: (SubStateMachineState) -> Boolean = { false },
    name: String? = null,
    handler: suspend ChangeableState<InputState>.(SubStateMachineState) -> ChangedState<S>,
)
```

**内部实现要点**（源自 `OnEnterStartStateMachine.kt`）：

- 子状态机通过 `factory.launchIn(scope)` 在 `SupervisorJob` 中启动。
- `handler` 每次子状态变化时被调用，在 `isInState` 条件下执行。
- `actionMapper` 将父 Action 通过 `mapNotNull` 过滤后 `dispatch` 给子状态机。
- 父状态离开 `inState` / `condition` 范围时，子状态机的 scope 被取消。

### 5.5 onActionStartStateMachine（Action 触发启动）

```kotlin
// 当收到特定 Action 时启动子状态机（而非进入状态时）
public inline fun <reified SubAction : A, SubStateMachineState : Any, SubStateMachineAction : Any> onActionStartStateMachine(
    noinline stateMachineFactoryBuilder: State<InputState>.(SubAction) -> FlowReduxStateMachineFactory<SubStateMachineState, SubStateMachineAction>,
    noinline actionMapper: (A) -> SubStateMachineAction?,
    noinline cancelOnState: (SubStateMachineState) -> Boolean = { false },
    name: String? = null,
    noinline handler: suspend ChangeableState<InputState>.(SubStateMachineState) -> ChangedState<S>,
)
```

> 适用于「用户点击某按钮后才启动子流程」的场景（如点击"开始下载"才启动下载子状态机）。

### 5.6 扩展函数接收者选择

| 接收者类型                                            | 使用场景                             |
|--------------------------------------------------|----------------------------------|
| `BaseBuilder<Parent, ParentRoot, Action>`        | 在 `inState` 中直接启动                |
| `ConditionBuilder<Parent, ParentRoot, Action>`   | 在 `condition { }` 内启动            |
| `IdentityBuilder<Parent, ParentRoot, Action>`    | 在 `untilIdentityChanges { }` 内启动 |
| `BaseBuilder<Parent.Active, ParentRoot, Action>` | 限定为特定子状态                         |

### 5.7 资源管理模式（生命周期绑定）

当子状态机需要管理外部资源（如某个 controller / sensor）的生命周期时：

```kotlin
inState<ChildState> {
    onEnterEffect {
        try {
            awaitCancellation()    // 挂起直到状态离开
        } finally {
            controller.release()   // 状态离开时自动释放
        }
    }
}
```

### 5.8 子状态机的 handler 中做额外计算

`handler` 不仅可以简单同步子状态，还可以根据子状态变化联动修改父状态的其他字段：

```kotlin
handler = { childState ->
    val current = snapshot
    val entering = childState.isActive && !current.child.isActive
    val exiting = !childState.isActive && current.child.isActive

    val newSibling = when {
        entering -> current.sibling.copy(isPaused = true)
        exiting -> current.sibling.copy(isPaused = false)
        else -> current.sibling
    }
    mutate { copy(child = childState, sibling = newSibling) }
}
```

### 5.9 子状态机设计检查清单

- [ ] 子状态机继承 `FlowReduxStateMachineFactory<<Child>State, <Child>Action>`
- [ ] 在 `init { spec { initializeWith { initialState } ... } }` 中定义
- [ ] 通过构造参数 `initialState` 接收父状态传入的初始值
- [ ] 副作用通过回调函数 `onEffect: (<Child>Effect) -> Unit` 传递给父
- [ ] 创建 `BaseBuilder` / `ConditionBuilder` / `IdentityBuilder` 上的扩展函数封装启动逻辑
- [ ] `actionMapper` 返回 `null` 表示不转发该 Action
- [ ] 如果子状态机不需要接收 Action，把 Action 类型用 `Unit`，省略 `actionMapper`
- [ ] `handler` 中通过 `mutate { copy(child = childState) }` 同步
- [ ] 启动扩展函数和子状态机类放在**同一文件**

---

## 六、错误处理模式

> 业务错误模型 `DataError` / 场景错误 `XxxError` 的定义与映射规则见 `data-domain` skill 与
`.docs/contributing/layered.md`。本节只讲"已经拿到 `Either<XxxError, T>` 之后在状态机里怎么写"。

### 6.1 标准 Either 处理（Arrow）

```kotlin
on<Action.Load> {
    useCase(snapshot.param).fold(
        ifLeft = { error ->
            poseEffect(Effect.ShowMessage(error.message))
            mutate { copy(isLoading = false, error = error.message) }
        },
        ifRight = { data ->
            mutate { copy(isLoading = false, data = data) }
        }
    )
}
```

### 6.2 提取 suspend 函数复用

当多个 Action 或 onEnter 共享相同的数据加载逻辑时，提取到 ViewModel 的私有方法：

```kotlin
// 返回类型必须明确为 ChangedState<XxxUiState>
private suspend fun ChangeableState<XxxUiState>.fetchData(): ChangedState<XxxUiState> =
    useCase(snapshot.param).fold(
        { error ->
            poseEffect(XxxEffect.ShowMessage(error.message))
            mutate { copy(isPullToRefresh = false) }
        },
        { data ->
            mutate { copy(data = data, isPullToRefresh = false) }
        }
    )

// 在 spec 中复用
onEnter { fetchData() }
on<Action.Refresh> { fetchData() }
on<Action.Load> { fetchData() }
```

### 6.3 子状态机中的错误处理

子状态机**不能**直接调用 `poseEffect`（它不是 ViewModel），需通过回调：

```kotlin
internal class <Child>StateMachine(
private val onMessage: (String) -> Unit,    // 消息回调
) : FlowReduxStateMachineFactory<<Child>State, Unit>() {
    init {
        spec {
            inState < < Child > State . Loading > {
                onEnter {
                    useCase().fold(
                        { error ->
                            onMessage(error.message)            // 通过回调传递
                            override { <Child > State.Error(error.message) }
                        },
                        { result -> override { <Child > State.Success(result) } }
                    )
                }
            }
        }
    }
}

// 父状态机启动时
onEnterStartStateMachine(
    stateMachineFactoryBuilder = {
        <Child > StateMachine(onMessage = { poseEffect(< Parent > Effect . ShowMessage (it)) })
    },
    handler = { childState -> mutate { copy(child = childState) } },
    actionMapper = { null },
)
```

---

## 七、常见状态流转模式

### 7.1 加载-展示-错误（sealed interface 多状态）

```kotlin
sealed interface PageState {
    data object Loading : PageState
    data class Content(val items: List<Item>) : PageState
    data class Error(val message: String) : PageState
}

spec {
    inState<PageState.Loading> {
        onEnter {
            useCase().fold(
                { override { PageState.Error(it.message) } },
                { override { PageState.Content(it) } }
            )
        }
    }
    inState<PageState.Error> {
        onEnterEffect { /* 进入错误态自动提示，例如调用 UiEffectHandler.toast(snapshot.message) */ }
        on<Action.Retry> { override { PageState.Loading } }
    }
    inState<PageState.Content> {
        on<Action.Refresh> { override { PageState.Loading } }
    }
}
```

### 7.2 条件触发异步操作（单一 data class 状态）

```kotlin
data class FormUiState(val email: String = "", val isSubmitting: Boolean = false)

spec {
    inState<FormUiState> {
        on<Action.Submit> {
            mutate { copy(isSubmitting = true) }
        }
        condition({ it.isSubmitting }) {
            onEnter {
                submitUseCase(snapshot.email).fold(
                    { error ->
                        poseEffect(Effect.ShowMessage(error.message))
                        mutate { copy(isSubmitting = false) }
                    },
                    {
                        poseEffect(Effect.NavigateToMain)
                        mutate { copy(isSubmitting = false) }
                    }
                )
            }
        }
    }
}
```

### 7.3 Idle → Active 两阶段模式

```kotlin
sealed interface PageState {
    data class Idle(val keyword: String = "") : PageState
    data class Active(val id: Long, val keyword: String, val data: Data) : PageState
}

spec {
    inState<PageState> {
        on<Action.Activate> { action ->
            override {
                PageState.Active(
                    id = action.id,
                    keyword = (snapshot as? PageState.Idle)?.keyword.orEmpty(),
                    data = Data()
                )
            }
        }
        // 全局子状态机（跨 Idle / Active 共享的能力）
        // startSharedStateMachine(...)
    }
    inState<PageState.Active> {
        // Active 专属的业务逻辑和子状态机
    }
}
```

### 7.4 倒计时模式（collectWhileInState + flowBuilder）

```kotlin
inState<State.Cooldown> {
    collectWhileInState(
        flowBuilder = { state ->                  // 参数是进入状态时的快照
            flow {
                var remaining = state.remainingSeconds
                while (remaining > 0) {
                    delay(1000)
                    remaining--
                    emit(remaining)
                }
            }
        },
        handler = { remaining ->
            if (remaining <= 0) override { State.Ready }
            else mutate { copy(remainingSeconds = remaining) }
        }
    )
}
```

### 7.5 untilIdentityChanges 监控重启模式

```kotlin
condition({ it.tabs.isNotEmpty() }) {
    untilIdentityChanges({ it.selectedTabIndex }) {
        // selectedTabIndex 变化时，内部所有 side effect 被取消并重新启动
        onEnter {
            val tab = snapshot.tabs[snapshot.selectedTabIndex]
            val data = loadTabUseCase(tab.id).getOrNull()
            mutate { copy(currentTabData = data) }
        }
    }
}
```

### 7.6 条件互斥模式

```kotlin
inState<DialogUiState> {
    condition({ it.isVisible }) {
        on<Action.PressBack> { mutate { copy(isVisible = false) } }
    }
    condition({ !it.isVisible }) {
        onActionEffect<Action.PressBack> { poseEffect(Effect.NavigateBack) }
    }
}
```

### 7.7 多个 onEnter 并行加载

```kotlin
inState<HomeUiState> {
    onEnter { loadCategories() }          // 并行
    onEnter { loadGuides() }              // 并行
    onEnter { loadNotices() }             // 并行
    onEnter { loadContinueWatching() }    // 并行
}
```

### 7.8 collectWhileInState 监听外部状态

```kotlin
inState<HomeUiState> {
    collectWhileInState(networkStatusFlow) { online ->
        if (online == snapshot.isOnline) return@collectWhileInState noChange()
        if (online) {
            poseEffect(HomeEffect.RefreshAfterReconnect)
            mutate { copy(isOnline = true) }
        } else {
            mutate { copy(isOnline = false) }
        }
    }
}
```

---

## 八、Compose 集成

> 完整 `XxxScreen` / `XxxContent` / `@AppPreview` 模板见 `create-kmp-screen` skill。本节只列**和状态机相关
**的 import 与收集套路，避免与 `create-kmp-screen` 重复。

```kotlin
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ciyin.ui.foundation.viewmodel.collectSideEffects
import ciyin.ui.foundation.viewmodel.viewModel

@Composable
fun <Feature> Screen(
    viewModel: <Feature>ViewModel = viewModel(::<Feature>ViewModel),
onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    viewModel.collectSideEffects { effect ->
        when (effect) {
                <Feature > Effect.NavigateBack-> onBack()
            // 其它 effect 分支
        }
    }

    <Feature > Content(
    state = state,
    onAction = viewModel.dispatchAction,
)
}
```

> **注意**：项目里的 `Toaster` / `LocalNavigator` / `koinViewModel()` **不存在**。Toast 类副作用应在
`<Feature>ViewModel` 实现 `UiEffectHandler` 后调用 `toast(text)`，或通过 `Effect` 让 Screen 处理。

---

## 九、文件组织规范

> 简单页面（无子状态机）的目录结构与命名以 `create-kmp-screen` skill 为准。本节只补充**有子状态机时**
> 的扩展。

### 9.1 复杂页面（有子状态机）

```
app/shared/src/commonMain/kotlin/com/ciyin/app/ui/screen/<feature>/
    ├── <Feature>Screen.kt
    ├── <Feature>ViewModel.kt
    ├── <Feature>UiState.kt
    ├── <Feature>Action.kt
    ├── <Feature>Effect.kt
    ├── <Feature>Model.kt          # 可选
    ├── <Feature>Mapper.kt         # 可选
    └── statemachine/
        ├── <Child1>Builder.kt     # 简单逻辑：只有 BaseBuilder 扩展函数（形态 B）
        ├── <child2>/
        │   ├── <Child2>StateMachine.kt   # 复杂逻辑：状态机类 + 启动扩展函数（同一文件）
        │   ├── <Child2>State.kt
        │   ├── <Child2>Action.kt
        │   └── <Child2>Effect.kt
        └── <child3>/
            ├── <Child3>StateMachine.kt
            ├── <Child3>State.kt
            └── <Child3>Action.kt
```

### 9.2 子状态机命名

| 类型       | 命名格式                             | 示例                                        |
|----------|----------------------------------|-------------------------------------------|
| 子状态机     | `<Child>StateMachine`            | `SearchHistoryStateMachine`               |
| 启动扩展函数   | `start<Child>StateMachineFor<X>` | `startSearchHistoryStateMachineForActive` |
| 子 State  | `<Child>State`                   | `SearchHistoryState`                      |
| 子 Action | `<Child>Action`                  | `SearchHistoryAction`                     |
| 子 Effect | `<Child>Effect`                  | `SearchHistoryEffect`                     |

> Screen / ViewModel / UiState / Action / Effect 等顶层命名见 `create-kmp-screen` skill。

---

## 十、测试

### 10.1 使用 shareIn + Turbine 测试状态机

```kotlin
@Test
fun `test loading to content transition`() = runTest {
        val factory = MyStateMachineFactory(mockUseCase)
        factory.initializeWith { MyState.Loading }    // 可覆盖初始状态
        val sm = factory.shareIn(backgroundScope)     // 用 shareIn 避免值合并

        sm.state.test {
            assertEquals(MyState.Loading, awaitItem())
            sm.dispatch(MyAction.Load)
            assertEquals(MyState.Content(data), awaitItem())
        }
    }
```

### 10.2 测试 ChangeableState 的 reduce

```kotlin
// 直接测试 handler 函数返回的 ChangedState
val state = ChangeableState(MyState(count = 0))
val result: ChangedState<MyState> = handler(state)
val actual: MyState = result.reduce(state.snapshot)
assertEquals(MyState(count = 1), actual)
```

---

## 十一、代码审查检查清单

生成 FlowRedux2 相关代码后，逐项检查；任一失败必须指出并要求修正：

- [ ] ViewModel 通过**实现两个 abstract 函数** `initialize()` / `spec()` 暴露契约（**不是** abstract
  属性）
- [ ] 子状态机在 `init { spec { initializeWith { ... } ... } }` 中定义
- [ ] 每个 `onEnter` / `on<Action>` 块只有一次状态变更（返回一个 `ChangedState`）
- [ ] `mutate` 只用于修改同类型状态，跨类型用 `override`
- [ ] `condition` 谓词是简单属性检查
- [ ] 所有异步操作的错误通过 `Either.fold` 处理
- [ ] 副作用在 ViewModel 中通过 `poseEffect` / `tryPoseEffect` 发送，在子状态机中通过回调传递
- [ ] 子状态机的 `actionMapper` 对无关 Action 返回 `null`
- [ ] 子状态机的 `handler` 通过 `mutate { copy(child = childState) }` 同步
- [ ] `handler` 中如需跳过更新，使用 `noChange()` 而非省略
- [ ] 不在 spec 内使用 `state.value`，应使用 `snapshot`
- [ ] `onEnterEffect` / `onActionEffect` 用于纯副作用（不改变状态的操作）
- [ ] 新增的 `class/interface/object/enum` 有中文 KDoc 注释
- [ ] `LazyListState` / `PagerState` 等 Compose 运行时状态不在 `UiState` 中（详见
  `.docs/contributing/mvi.md`）
- [ ] 启动扩展函数和子状态机类放在同一个 `.kt` 文件中
- [ ] 没有 `viewModelScope.launch { poseEffect(...) }`、没有调用项目里不存在的
  `Toaster` / `LocalNavigator` / `koinViewModel()`
