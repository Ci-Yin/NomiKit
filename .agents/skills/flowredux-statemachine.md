# FlowRedux2 状态机开发技能

> 本文档是 AI 编码助手在 Polyvision 项目中使用 FlowRedux2 状态机时的实操参考。
> 包含模板、约束和决策树，确保生成的代码与项目架构一致。
>
> FlowRedux2 版本：`2.0.1`，库坐标：`com.freeletics.flowredux2:flowredux`
> GitHub：https://github.com/freeletics/FlowRedux

## 快速决策：是否需要状态机

```
页面是否有"多步骤流程"或"条件触发的异步逻辑"？
  ├─ 是 → StateMachineMviViewModel（本文档）
  └─ 否 → 有 Action 分发吗？
           ├─ 是 → AbsMviViewModel（on<Action> DSL）
           └─ 否 → AbsMvvmViewModel（updateState + poseEffect）
```

---

## 一、核心类型体系（源自 FlowRedux2 源码）

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
    ├── poseEffect(effect)            ← 异步发送副作用
    └── tryPoseEffect(effect)         ← 尝试发送副作用
```

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
- `onEnterEffect` / `onActionEffect` / `collectWhileInStateEffect` 的 handler 接收者是 **`State`**
  （只读），返回 `Unit`

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

> 所有 Builder 均继承 `BaseBuilder`，因此都能使用 `on`/`onEnter`/`collectWhileInState`/
`onEnterStartStateMachine` 等方法。

---

## 二、ViewModel 模板

### 2.1 最小 StateMachineMviViewModel

```kotlin
package com.yy.myuko.shared.presentation.screen.xxx

import com.freeletics.flowredux2.FlowReduxBuilder
import com.freeletics.flowredux2.FlowReduxStateMachineFactory
import com.freeletics.flowredux2.initializeWith
import com.yy.myuko.core.ui.foundation.viewmodel.StateMachineMviViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

internal class XxxViewModel :
    StateMachineMviViewModel<XxxState, XxxAction, XxxEffect>(),
    KoinComponent {

    private val xxxUseCase by inject<XxxUseCase>()

    override val initialize: FlowReduxStateMachineFactory<XxxState, XxxAction>.() -> Unit = {
        initializeWith { XxxState() }
    }

    override val spec: FlowReduxBuilder<XxxState, XxxAction>.() -> Unit = {
        inState<XxxState> {
            // Action 处理和状态逻辑
        }
    }
}
```

### 2.2 必须实现的两个抽象属性

| 属性           | 类型                                              | 作用       |
|--------------|-------------------------------------------------|----------|
| `initialize` | `FlowReduxStateMachineFactory<S, A>.() -> Unit` | 设置初始状态   |
| `spec`       | `FlowReduxBuilder<S, A>.() -> Unit`             | 定义状态转换规范 |

### 2.3 initializeWith 参数

```kotlin
initializeWith(
    reuseLastEmittedStateOnLaunch: Boolean = true,  // true：复用上次状态；false：每次重建
initialState: () -> S
)
```

---

## 三、DSL 完整速查

### 3.1 全部 DSL 构件（按 BaseBuilder 源码）

```kotlin
spec {
    // ═══════════════════ 状态入口 ═══════════════════
    inState<SubState> {                             // 匹配状态类型（KClass.isInstance）

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

| API                     | 说明                                       |
|-------------------------|------------------------------------------|
| `poseEffect(effect)`    | 异步发送副作用（启动协程，通过 `viewModelScope.launch`） |
| `tryPoseEffect(effect)` | 尝试发送，返回 `Boolean`（不启动协程）                 |

> 仅在 `StateMachineMviViewModel` 及其 `spec` 块中可用。子状态机通过构造参数回调传递副作用。

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

**原理**：`on`/`onEnter` 的返回值是 `ChangedState<S>`，Kotlin 函数只有一个返回值，多次调用只取最后一个。

### 约束 2：mutate vs override

```kotlin
// mutate：修改同一类型状态（lambda 的 this 是 InputState）
inState<ShowContent> {
    on<Action.LoadMore> {
        mutate { copy(isLoadingMore = true) }  // ✅ 仍然是 ShowContent
    }
}

// override：切换到不同状态类型
inState<Loading> {
    onEnter {
        override { ShowContent(items) }  // ✅ Loading → ShowContent
    }
}

// ❌ 错误：mutate 不能切换类型
inState<Loading> { onEnter { mutate { ShowContent(items) } } }  // 编译错误
```

### 约束 3：状态变化时 onEnter/on 会被取消

```kotlin
inState<Loading> {
    onEnter {
        val result = longRunningCall()  // 状态变化时被 CancellationException
        override { ShowContent(result) }
    }
}
```

### 约束 4：condition 谓词必须简单快速

```kotlin
// ✅ 推荐：简单属性检查
condition({ it.isSubmitting }) { ... }
condition({ it.videoDetail is VideoDetailState.Loading }) { ... }

// ❌ 避免：复杂计算
condition({ it.items.filter { ... }.count() > 10 }) { ... }
```

### 约束 5：snapshot 是快照，mutate/override 内的 this 是最新状态

```kotlin
on<Action.Load> {
    val param = snapshot.email       // ✅ 触发时的快照，用于发起请求
    val result = api.load(param)     // 异步操作期间状态可能已变
    mutate { copy(data = result) }   // ✅ mutate 内的 this 是 reduce 时的最新状态
}
```

**源码依据**：`mutate` 内部创建 `UnsafeMutateState(reducer)`，在 `reduxStore` 的状态变更队列中执行
`reducer(currentState)`。

### 约束 6：同一 inState 可定义多个 onEnter（并行执行）

```kotlin
inState<HomeState> {
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

---

## 五、子状态机

### 5.1 两种形态

本项目中子状态机有两种形态，根据复杂度选择：

**形态 A：完整状态机类**（复杂、可复用的独立逻辑）

```kotlin
internal class PipStateMachine(
    private val pipController: PipController,
    private val onEffect: (PipEffect) -> Unit,
    private val initialState: PipState = PipState(),
) : FlowReduxStateMachineFactory<PipState, PipAction>() {
    init {
        spec {
            initializeWith { initialState }
            inState<PipState> { /* 完整的状态转换逻辑 */ }
        }
    }
}
```

**形态 B：BaseBuilder 扩展函数**（简单的一次性加载逻辑）

```kotlin
// 不创建子状态机类，直接在父状态机的 Builder 上定义 onEnter
fun <Action : Any> BaseBuilder<EpisodeState.Active, EpisodeState, Action>.startVideoDetailLoadingForActive(
    loadVideoDetailWithPlayersUseCase: LoadVideoDetailWithPlayersUseCase,
) {
    onEnter {
        val loading = snapshot.videoDetail as VideoDetailState.Loading
        loadVideoDetailWithPlayersUseCase(loading.videoId).fold(
            ifLeft = { error -> mutate { copy(videoDetail = VideoDetailState.Error(...)) } },
            ifRight = { detail -> mutate { copy(videoDetail = VideoDetailState.Success(...)) } }
        )
    }
}
```

### 5.2 子状态机类模板

```kotlin
package com.yy.myuko.shared.presentation.screen.xxx.statemachine.yyy

import com.freeletics.flowredux2.FlowReduxStateMachineFactory
import com.freeletics.flowredux2.initializeWith

/**
 * Yyy 子状态机。
 *
 * 负责 [描述职责]。
 *
 * @property onEffect 副作用回调
 * @property initialState 初始状态
 */
internal class YyyStateMachine(
    private val onEffect: (YyyEffect) -> Unit,
    private val someUseCase: SomeUseCase,
    private val initialState: YyyState = YyyState(),
) : FlowReduxStateMachineFactory<YyyState, YyyAction>() {

    init {
        spec {
            initializeWith { initialState }

            inState<YyyState> {
                on<YyyAction.DoSomething> { action ->
                    someUseCase(action.param).fold(
                        { error ->
                            onEffect(YyyEffect.ShowError(error.message))
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
 * 在 [ParentState.Active] 状态下启动 Yyy 子状态机。
 */
fun <Action : Any> BaseBuilder<ParentState.Active, ParentState, Action>.startYyyStateMachineForActive(
    someUseCase: SomeUseCase,
    onYyyEffect: (YyyEffect) -> Unit,
) {
    onEnterStartStateMachine(
        stateMachineFactoryBuilder = {
            YyyStateMachine(
                onEffect = onYyyEffect,
                someUseCase = someUseCase,
                initialState = snapshot.yyy,     // 从父状态传入初始值
            )
        },
        actionMapper = { action ->
            when (action) {
                is ParentAction.YyyDoSomething -> YyyAction.DoSomething(action.param)
                else -> null                     // 不转发
            }
        },
        handler = { childState ->
            mutate { copy(yyy = childState) }    // 同步子状态到父状态
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

- 子状态机通过 `factory.launchIn(scope)` 在 `SupervisorJob` 中启动
- `handler` 每次子状态变化时被调用，在 `isInState` 条件下执行
- `actionMapper` 将父 Action 通过 `mapNotNull` 过滤后 `dispatch` 给子状态机
- 父状态离开 inState/condition 范围时，子状态机的 scope 被取消

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

> 本项目目前未使用此 API，但适用于「用户点击某按钮后才启动子流程」的场景。

### 5.6 扩展函数接收者选择

| 接收者类型                                            | 使用场景                             | 项目示例                               |
|--------------------------------------------------|----------------------------------|------------------------------------|
| `BaseBuilder<Parent, ParentRoot, Action>`        | 在 `inState` 中直接启动                | `startScreenModeStateMachine`      |
| `ConditionBuilder<Parent, ParentRoot, Action>`   | 在 `condition { }` 内启动            | `startSendCodeStateMachine`        |
| `IdentityBuilder<Parent, ParentRoot, Action>`    | 在 `untilIdentityChanges { }` 内启动 | `startCategoryContentStateMachine` |
| `BaseBuilder<Parent.Active, ParentRoot, Action>` | 限定为特定子状态                         | `startPipStateMachineForActive`    |

### 5.7 资源管理模式（生命周期绑定）

当子状态机需要管理外部资源（如 PipController）的生命周期时：

```kotlin
inState<ChildState> {
    onEnterEffect {
        try {
            awaitCancellation()  // 挂起直到状态离开
        } finally {
            controller.release()  // 状态离开时自动释放
        }
    }
}
```

### 5.8 子状态机的 handler 中做额外计算

handler 不仅可以简单同步子状态，还可以根据子状态变化联动修改父状态的其他字段：

```kotlin
handler = { childState ->
    val currentState = snapshot
    val enteringPip = childState.isInPip && !currentState.pip.isInPip
    val exitingPip = !childState.isInPip && currentState.pip.isInPip

    val newDanmaku = when {
        enteringPip -> currentState.danmaku.copy(isEnabled = false)
        exitingPip -> currentState.danmaku.copy(isEnabled = currentState.pip.danmakuBefore ?: true)
        else -> currentState.danmaku
    }
    mutate { copy(pip = childState, danmaku = newDanmaku) }
}
```

### 5.9 子状态机设计检查清单

- [ ] 子状态机继承 `FlowReduxStateMachineFactory<ChildState, ChildAction>`
- [ ] 在 `init { spec { initializeWith { initialState } ... } }` 中定义
- [ ] 通过构造参数 `initialState` 接收父状态传入的初始值
- [ ] 副作用通过回调函数 `onEffect: (ChildEffect) -> Unit` 传递给父
- [ ] 创建 `BaseBuilder` / `ConditionBuilder` / `IdentityBuilder` 上的扩展函数封装启动逻辑
- [ ] `actionMapper` 返回 `null` 表示不转发该 Action
- [ ] 如果不需要接收 Action，Action 类型用 `Unit`，`actionMapper = { }`
- [ ] `handler` 中通过 `mutate { copy(child = childState) }` 同步
- [ ] 启动扩展函数和状态机类放在同一文件

---

## 六、错误处理模式

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
// 返回类型必须明确为 ChangedState<XxxState>
private suspend fun ChangeableState<XxxState>.fetchData(): ChangedState<XxxState> =
    useCase(snapshot.param).fold(
        { error ->
            poseEffect(Effect.ShowMessage(error.message))
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

子状态机不能直接调用 `poseEffect`，需通过回调：

```kotlin
// 子状态机
class YyyStateMachine(
    private val onMessage: (String) -> Unit,  // 消息回调
) : FlowReduxStateMachineFactory<YyyState, Unit>() {
    init {
        spec {
            inState<YyyState.Loading> {
                onEnter {
                    useCase().fold(
                        { error ->
                            onMessage(error.message)           // 通过回调传递
                            override { YyyState.Error(...) }
                        },
                        { ... }
                    )
                }
            }
        }
    }
}

// 父状态机启动时
onEnterStartStateMachine(
    stateMachineFactoryBuilder = {
        YyyStateMachine(onMessage = { poseEffect(Effect.ShowMessage(it)) })
    },
    ...
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
        onEnterEffect { Toaster.show(snapshot.message) }  // 进入错误态自动提示
        on<Action.Retry> { override { PageState.Loading } }
    }
    inState<PageState.Content> {
        on<Action.Refresh> { override { PageState.Loading } }
    }
}
```

### 7.2 条件触发异步操作（单一 data class 状态）

```kotlin
data class FormState(val email: String = "", val isSubmitting: Boolean = false)

spec {
    inState<FormState> {
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
    data class Idle(val screenMode: ScreenMode = ScreenMode.Normal) : PageState
    data class Active(val videoId: Long, val screenMode: ScreenMode, ...) : PageState
}

spec {
    inState<PageState> {
        on<Action.Load> { action ->
            override {
                PageState.Active(videoId = action.videoId, screenMode = screenMode)
            }
        }
        // 全局子状态机（跨 Idle/Active 共享的，如屏幕模式）
        startScreenModeStateMachine(...)
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
        flowBuilder = { state ->              // 参数是进入状态时的快照
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
condition({ it.categories.isNotEmpty() }) {
    untilIdentityChanges({ it.categoryIndex }) {
        // categoryIndex 变化时，内部所有 side effect 被取消并重新启动
        onEnterStartStateMachine(
            stateMachineFactoryBuilder = {
                CategoryContentStateMachine(
                    categoryId = snapshot.categories[snapshot.categoryIndex].id,
                    initialState = snapshot.categoryContents[categoryId] ?: Idle
                )
            },
            ...
        )
    }
}
```

### 7.6 条件互斥模式

```kotlin
inState<ScreenMode> {
    condition({ it.isFull }) {
        on<Action.PressBack> { override { snapshot.changeFull(false) } }
    }
    condition({ !it.isFull }) {
        onActionEffect<Action.PressBack> { onEffect(Effect.NavigateBack) }
    }
}
```

### 7.7 多个 onEnter 并行加载

```kotlin
inState<HomeState> {
    onEnter { loadCategories() }          // 并行
    onEnter { loadGuides() }              // 并行
    onEnter { loadNotices() }             // 并行
    onEnter { loadContinueWatching() }    // 并行
}
```

### 7.8 collectWhileInState 监听平台状态

```kotlin
inState<PipState> {
    collectWhileInState(pipController.isInPipMode) { isInPip ->
        if (isInPip == snapshot.isInPictureInPictureMode) return@collectWhileInState noChange()
        if (isInPip) {
            onEffect(PipEffect.SetDanmakuEnabled(false))
            mutate { copy(isInPictureInPictureMode = true) }
        } else {
            onEffect(PipEffect.SetDanmakuEnabled(snapshot.danmakuEnabledBeforePip ?: true))
            mutate { copy(isInPictureInPictureMode = false, danmakuEnabledBeforePip = null) }
        }
    }
}
```

---

## 八、Compose 集成模板

```kotlin
@Composable
fun XxxScreen(viewModel: XxxViewModel = koinViewModel()) {
    val state by viewModel.stateCollectAsStateWithLifecycle()
    val navigator = LocalNavigator.current

    viewModel.collectSideEffects { effect ->
        when (effect) {
            is XxxEffect.NavigateBack -> navigator.pop()
            is XxxEffect.ShowMessage -> Toaster.show(effect.message)
        }
    }

    XxxScreenContent(
        state = state,
        onAction = viewModel.dispatchAction
    )
}

@Composable
private fun XxxScreenContent(
    state: XxxState,
    onAction: (XxxAction) -> Unit
) {
    // 纯 UI 渲染，无 ViewModel 依赖
}

@AppPreview
@Composable
private fun XxxScreenPreview() = AppPreview {
    XxxScreenContent(
        state = XxxState(),
        onAction = {}
    )
}
```

---

## 九、文件组织规范

### 9.1 简单页面（无子状态机）

```
presentation/screen/xxx/
    ├── XxxScreen.kt          # Compose UI
    ├── XxxViewModel.kt       # ViewModel
    ├── XxxState.kt           # State（可选：简单时放 ViewModel 同文件）
    ├── XxxAction.kt          # Action
    └── XxxEffect.kt          # Effect
```

### 9.2 复杂页面（有子状态机）

```
presentation/screen/xxx/
    ├── XxxScreen.kt
    ├── XxxViewModel.kt
    ├── XxxState.kt
    ├── XxxAction.kt
    ├── XxxEffect.kt
    └── statemachine/
        ├── YyyStateMachine.kt         # 简单逻辑：只有 BaseBuilder 扩展函数
        ├── yyy/
        │   ├── YyyStateMachine.kt     # 复杂逻辑：状态机类 + 启动扩展函数（同文件）
        │   ├── YyyState.kt
        │   ├── YyyAction.kt
        │   └── YyyEffect.kt
        └── zzz/
            ├── ZzzStateMachine.kt
            ├── ZzzState.kt
            └── ZzzAction.kt
```

### 9.3 命名约定

| 类型        | 命名格式                         | 示例                              |
|-----------|------------------------------|---------------------------------|
| State     | `XxxState` / `XxxUiState`    | `EpisodeState`                  |
| Action    | `XxxAction`                  | `EpisodeAction`                 |
| Action 子类 | 无 `Action` 后缀的描述性名称          | `EpisodeAction.Load`            |
| Effect    | `XxxEffect`                  | `EpisodeEffect`                 |
| 子状态机      | `YyyStateMachine`            | `PipStateMachine`               |
| 启动函数      | `startYyyStateMachineForXxx` | `startPipStateMachineForActive` |
| 子 State   | `YyyState`                   | `PipState`                      |
| 子 Action  | `YyyAction`                  | `PipAction`                     |
| 子 Effect  | `YyyEffect`                  | `PipEffect`                     |

---

## 十、测试

### 10.1 使用 shareIn + Turbine 测试状态机

```kotlin
@Test
fun `test loading to content transition`() = runTest {
        val factory = MyStateMachineFactory(mockUseCase)
        factory.initializeWith { MyState.Loading }  // 可覆盖初始状态
        val sm = factory.shareIn(backgroundScope)   // 用 shareIn 避免值合并

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

生成 FlowRedux 相关代码后，逐项检查：

- [ ] `initialize` 中调用了 `initializeWith { ... }`
- [ ] 子状态机在 `init { spec { initializeWith { ... } ... } }` 中定义
- [ ] 每个 `onEnter` / `on<Action>` 块只有一次状态变更（返回一个 `ChangedState`）
- [ ] `mutate` 只用于修改同类型状态，跨类型用 `override`
- [ ] `condition` 谓词是简单属性检查
- [ ] 所有异步操作的错误通过 `Either.fold` 处理
- [ ] 副作用在 ViewModel 中通过 `poseEffect` 发送，在子状态机中通过回调传递
- [ ] 子状态机的 `actionMapper` 对无关 Action 返回 `null`
- [ ] 子状态机的 `handler` 通过 `mutate { copy(child = childState) }` 同步
- [ ] `handler` 中如需跳过更新，使用 `noChange()` 而非省略
- [ ] 不在 spec 内使用 `state.value`，应使用 `snapshot`
- [ ] `onEnterEffect` / `onActionEffect` 用于纯副作用（不改变状态的操作）
- [ ] 新增的 `class/interface/object/enum` 有中文 KDoc 注释
- [ ] `LazyListState` / `PagerState` 等 Compose 运行时状态不在 State 中
- [ ] 启动扩展函数和状态机类放在同一个 `.kt` 文件中
