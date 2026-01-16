# FlowRedux2 状态机使用指南

FlowRedux 是一个专为 Kotlin 多平台设计的异步状态机库，通过 DSL 和协程简化状态管理。

> 📖 官方文档：[FlowRedux User Guide](https://freeletics.github.io/FlowRedux/user-guide/1_basics/)

## 一、核心概念

### 1. State（状态）

状态表示应用程序在某一时刻的情况，使用 `sealed interface` 或 `sealed class` 定义：

```kotlin
sealed interface ListState {
    object Loading : ListState
    data class ShowContent(val items: List<Item>) : ListState
    data class Error(val message: String) : ListState
}
```

### 2. Action（动作）

动作是导致状态变化的事件或用户输入：

```kotlin
sealed interface Action {
    object RetryLoadingAction : Action
    data class ItemClicked(val item: Item) : Action
}
```

### 3. FlowReduxStateMachine

状态机的核心 API：

```kotlin
class FlowReduxStateMachine<State, Action> {
    val state: Flow<State>           // 状态流，可订阅状态变化
    suspend fun dispatch(action: Action)  // 分发动作
}
```

## 二、创建状态机

### 1. 继承 `FlowReduxStateMachineFactory`

```kotlin
class ItemListStateMachineFactory(
    private val httpClient: HttpClient
) : FlowReduxStateMachineFactory<ListState, Action>() {

    init {
        // 1. 设置初始状态
        initializeWith { ListState.Loading }

        // 2. 定义状态机规范
        spec {
            inState<ListState.Loading> {
                onEnter {
                    // 进入 Loading 状态时执行
                }
            }
        }
    }
}
```

### 2. 获取 StateMachine 实例

```kotlin
// 方式一：在 CoroutineScope 中启动
val stateMachine = factory.launchIn(viewModelScope)

// 方式二：作为 SharedFlow 共享
val stateMachine = factory.shareIn(viewModelScope, SharingStarted.Lazily)

// 方式三：生产状态机
val stateMachine = factory.produceStateMachine(viewModelScope)
```

## 三、DSL 详解

### 1. `inState<T>` - 状态入口

定义特定状态下的行为，只有当状态机处于该状态时，内部逻辑才会执行：

```kotlin
spec {
    inState<ListState.Loading> {
        // 仅在 Loading 状态时执行
    }

    inState<ListState.ShowContent> {
        // 仅在 ShowContent 状态时执行
    }

    inState<ListState.Error> {
        // 仅在 Error 状态时执行
    }
}
```

### 2. `onEnter` - 进入状态时触发

当进入指定状态时**执行一次**：

```kotlin
inState<ListState.Loading> {
    onEnter {
        try {
            val items = httpClient.loadItems()  // 异步操作
            override { ListState.ShowContent(items) }  // 转换到新状态
        } catch (t: Throwable) {
            override { ListState.Error("加载失败: ${t.message}") }
        }
    }
}
```

**特性：**

- 异步执行（在协程中运行）
- 只执行一次，除非状态转出后再次进入
- 当状态变化时，正在执行的 `onEnter` 会被取消

### 3. `on<Action>` - 处理动作

当收到特定动作时触发：

```kotlin
inState<ListState.Error> {
    on<Action.RetryLoadingAction> {
        override { ListState.Loading }  // 重新进入 Loading 状态
    }
}

inState<ListState.ShowContent> {
    on<Action.ItemClicked> { action ->
        // action.item 包含被点击的项
        poseEffect(Effect.NavigateToDetail(action.item.id))
        noChange()  // 不改变状态
    }
}
```

**特性：**

- 异步执行（在协程中运行）
- 可访问 `action` 参数获取动作数据
- 当状态变化时，正在执行的 `on` 会被取消

### 4. `collectWhileInState` - 订阅 Flow

在特定状态下订阅 Flow，状态离开时自动取消订阅：

```kotlin
inState<ListState.ShowContent> {
    collectWhileInState(websocketUpdatesFlow) { update ->
        mutate {
            copy(items = items + update.newItem)
        }
    }
}
```

**使用场景：**

- 订阅 WebSocket 消息
- 监听数据库变化
- 定时刷新数据

## 四、状态变更 API

### 1. `ChangeableState<T>` 接口

`onEnter` 和 `on<Action>` 的接收者类型，提供状态操作能力：

```kotlin
class ChangeableState<T> {
    val snapshot: T                              // 当前状态快照
    fun override(newState: () -> T): ChangedState<T>   // 完全替换状态
    fun mutate(block: T.() -> T): ChangedState<T>      // 修改当前状态
    fun noChange(): ChangedState<T>                    // 不改变状态
}
```

### 2. `override` vs `mutate`

| 方法             | 用途           | 示例                                   |
|----------------|--------------|--------------------------------------|
| `override { }` | 切换到完全不同的状态类型 | `override { Error("msg") }`          |
| `mutate { }`   | 修改当前状态的属性    | `mutate { copy(count = count + 1) }` |

```kotlin
// ✅ 使用 override 切换状态类型
inState<Loading> {
    onEnter {
        override { ShowContent(items) }  // Loading → ShowContent
    }
}

// ✅ 使用 mutate 修改同一状态的属性
inState<ShowContent> {
    on<Action.LoadMore> {
        mutate { copy(isLoadingMore = true) }  // 仍然是 ShowContent
    }
}

// ❌ 错误：使用 mutate 切换状态类型
inState<Loading> {
    onEnter {
        mutate { Error("msg") }  // 编译错误！
    }
}
```

### 3. `noChange` - 不改变状态

当不需要改变状态时（如只触发副作用）：

```kotlin
inState<ShowContent> {
    on<Action.ShareClicked> {
        poseEffect(Effect.OpenShareDialog)
        noChange()  // 明确表示不改变状态
    }
}
```

## 五、高级 DSL

### 1. `condition` - 条件块

基于状态属性的条件判断：

```kotlin
inState<AuthState> {
    // 只在 isSubmitting 为 true 时执行
    condition({ it.isSubmitting }) {
        onEnter {
            loginUseCase(snapshot.toLoginInput())
                .fold(
                    { error -> poseEffect(Effect.ShowError(error)) },
                    { poseEffect(Effect.NavigateToMain) }
                )
            mutate { copy(isSubmitting = false) }
        }
    }

    // 只在特定 tab 时处理
    condition({ it.currentTab == Tab.Register }) {
        on<Action.SendCode> {
            mutate { copy(sendCodeUiState = SendCodeState.Sending) }
        }
    }
}
```

### 2. `untilIdentityChanged` - 身份变更监听

当状态的某个属性变化时重新执行：

```kotlin
inState<ShowContent> {
    untilIdentityChanged({ it.selectedCategory }) {
        onEnter {
            // 当 selectedCategory 变化时重新加载
            val items = loadItemsForCategory(snapshot.selectedCategory)
            mutate { copy(items = items) }
        }
    }
}
```

### 3. `ExecutionPolicy` - 执行策略

控制多个相同动作的处理方式：

```kotlin
inState<ShowContent> {
    // CANCEL_PREVIOUS（默认）：取消之前的执行，只处理最新的
    on<Action.Search>(executionPolicy = ExecutionPolicy.CANCEL_PREVIOUS) { action ->
        val results = search(action.query)
        mutate { copy(searchResults = results) }
    }

    // ORDERED：按顺序执行
    on<Action.AddItem>(executionPolicy = ExecutionPolicy.ORDERED) { action ->
        // 保证顺序执行
    }

    // UNORDERED：并行执行
    on<Action.PreloadImage>(executionPolicy = ExecutionPolicy.UNORDERED) { action ->
        // 可以并行预加载
    }
}
```

| 策略                | 行为              | 适用场景      |
|-------------------|-----------------|-----------|
| `ORDERED`         | 按顺序执行           | 需要保证顺序的操作 |
| `CANCEL_PREVIOUS` | 取消之前的，只执行最新（默认） | 搜索防抖、表单提交 |
| `UNORDERED`       | 并行执行(无顺序)       | 独立的预加载操作  |

## 六、副作用 (Effects)

副作用是不影响状态但需要执行的操作（如导航、Toast）。

> ⚠️ **注意**: `poseEffect` 是本项目在 `StateMachineMviViewModel` 中扩展的 API，**不是** FlowRedux 原生
> API。只有继承
`StateMachineMviViewModel` 时才可使用。

### 1. 使用 `poseEffect`（推荐）

在 `StateMachineMviViewModel` 中直接使用：

```kotlin
class AuthViewModel : StateMachineMviViewModel<AuthState, AuthAction, AuthEffect>() {

    override val spec: FlowReduxBuilder<AuthState, AuthAction>.() -> Unit = {
        inState<AuthState> {
            on<AuthAction.ItemClicked> { action ->
                // 直接调用 poseEffect（StateMachineMviViewModel 提供）
                poseEffect(AuthEffect.NavigateToDetail(action.item.id))
                noChange()
            }

            condition({ it.isSubmitting }) {
                onEnter {
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

### 2. `poseEffect` vs `tryPoseEffect`

| 方法                      | 说明                          |
|-------------------------|-----------------------------|
| `poseEffect(effect)`    | 异步发送副作用（启动协程）               |
| `tryPoseEffect(effect)` | 尝试发送副作用，返回 `Boolean` 表示是否成功 |

```kotlin
// 异步发送，不关心结果
poseEffect(AuthEffect.ShowMessage("操作成功"))

// 尝试发送，可检查结果
val sent = tryPoseEffect(AuthEffect.ShowMessage("操作成功"))
if (!sent) {
    // 处理发送失败的情况
}
```

## 七、组合状态机

子状态机模式用于将复杂、可复用的逻辑封装为独立的状态机，然后在父状态机中启动和协调。

### 1. 定义子状态机

子状态机继承 `FlowReduxStateMachineFactory`，拥有自己独立的 State 和 Action：

```kotlin
/**
 * 发送验证码子状态机
 * 父状态机可将其作为子状态机运行，并通过 handler 同步状态。
 */
internal class SendCodeStateMachine(
    private val email: String,
    private val codeType: CodeType,
    private val sendVerifyCodeUseCase: SendVerifyCodeUseCase,
    private val onMessage: (String) -> Unit,
    private val initialState: SendCodeState = SendCodeState.Ready.Idle,
) : FlowReduxStateMachineFactory<SendCodeState, Unit>() {  // Unit 表示不接收外部 Action

    init {
        spec {
            initializeWith { initialState }

            inState<SendCodeState.Sending> {
                onEnter {
                    sendVerifyCodeUseCase(email, codeType, snapshot.challengeToken)
                        .fold(
                            { error ->
                                onMessage(error.message)
                                override { SendCodeState.Ready.Idle }
                            },
                            { result ->
                                onMessage(result.message)
                                override { SendCodeState.Cooldown(result.retryAfterSeconds) }
                            }
                        )
                }
            }

            inState<SendCodeState.Cooldown> {
                collectWhileInState(
                    flowBuilder = {
                        flow {
                            var remaining = it.remainingSeconds
                            while (remaining > 0) {
                                delay(1000)
                                remaining--
                                emit(remaining)
                            }
                        }
                    },
                    handler = { remainingSeconds ->
                        if (remainingSeconds <= 0) {
                            override { SendCodeState.Ready.ReSend }
                        } else {
                            mutate { copy(remainingSeconds = remainingSeconds) }
                        }
                    }
                )
            }
        }
    }
}
```

### 2. 创建启动子状态机的扩展函数

在 `ConditionBuilder` 上创建扩展函数，封装子状态机的启动逻辑：

```kotlin
/**
 * 启动发送验证码子状态机的复用函数
 *
 * @param getEmail 获取当前邮箱
 * @param codeType 验证码类型
 * @param sendVerifyCodeUseCase 发送验证码的用例
 * @param poseMessage 发送消息副作用
 * @param getSendCodeUiState 获取当前发送验证码状态
 * @param updateState 更新父状态机状态
 */
fun <State : Any, Action : Any> ConditionBuilder<State, State, Action>.startSendCodeStateMachine(
    getEmail: State.() -> String,
    codeType: CodeType,
    sendVerifyCodeUseCase: SendVerifyCodeUseCase,
    poseMessage: (String) -> Unit,
    getSendCodeUiState: State.() -> SendCodeState,
    updateState: State.(sendCodeUiState: SendCodeState) -> State
) {
    onEnterStartStateMachine(
        // 构建子状态机实例
        stateMachineFactoryBuilder = {
            SendCodeStateMachine(
                email = snapshot.getEmail(),
                codeType = codeType,
                sendVerifyCodeUseCase = sendVerifyCodeUseCase,
                onMessage = { poseMessage(it) },
                initialState = snapshot.getSendCodeUiState()
            )
        },
        // 将父 Action 映射为子 Action（Unit 表示不转发）
        actionMapper = { },
        // 处理子状态机的状态变化，同步到父状态机
        handler = { childState ->
            mutate { updateState(childState) }
        }
    )
}
```

### 3. 在父状态机中使用子状态机

```kotlin
class AuthViewModel : StateMachineMviViewModel<AuthState, AuthAction, AuthEffect>() {

    override val spec: FlowReduxBuilder<AuthState, AuthAction>.() -> Unit = {
        inState<AuthState> {
            // 处理发送验证码 Action
            on<AuthAction.SendCaptcha> {
                if (snapshot.sendCodeUiState !is SendCodeState.Ready) return@on noChange()
                mutate { copy(sendCodeUiState = SendCodeState.Sending()) }
            }

            // 使用 condition 限定子状态机的激活条件
            condition({ it.sendCodeUiState !is SendCodeState.Ready }) {
                // 启动子状态机
                startSendCodeStateMachine(
                    getEmail = { email },
                    codeType = CodeType.Register,
                    sendVerifyCodeUseCase = sendVerifyCodeUseCase,
                    poseMessage = { poseEffect(AuthEffect.ShowMessage(it)) },
                    getSendCodeUiState = { sendCodeUiState },
                    updateState = { copy(sendCodeUiState = it) }
                )
            }
        }
    }
}
```

### 4. 子状态机需要接收 Action 的情况

当子状态机需要响应用户交互时，使用 `actionMapper` 将父 Action 映射为子 Action：

```kotlin
/**
 * 滑动验证码子状态机
 */
internal class CaptchaStateMachine(
    private val initializeState: CaptchaState,
    private val generateCaptchaUseCase: GenerateCaptchaUseCase,
    private val verifyCaptchaUseCase: VerifyCaptchaUseCase,
    private val onMessage: (String) -> Unit,
) : FlowReduxStateMachineFactory<CaptchaState, CaptchaAction>() {
    // ... 状态机逻辑
}

// 启动函数
fun <State : Any, Action : Any> ConditionBuilder<State, State, Action>.startCaptchaStateMachine(
    generateCaptchaUseCase: GenerateCaptchaUseCase,
    verifyCaptchaUseCase: VerifyCaptchaUseCase,
    poseMessage: (String) -> Unit,
    getCaptchaUiState: State.() -> CaptchaState,
    mapAction: (Action) -> CaptchaAction?,  // Action 映射器
    updateState: State.(captchaUiState: CaptchaState) -> State
) {
    onEnterStartStateMachine(
        stateMachineFactoryBuilder = {
            CaptchaStateMachine(
                initializeState = snapshot.getCaptchaUiState(),
                generateCaptchaUseCase = generateCaptchaUseCase,
                verifyCaptchaUseCase = verifyCaptchaUseCase,
                onMessage = { poseMessage(it) }
            )
        },
        // 将父 Action 映射为子 Action
        actionMapper = { action -> mapAction(action) },
        handler = { captchaUiState ->
            mutate { updateState(captchaUiState) }
        }
    )
}

// 在父状态机中使用
condition({ it.captchaUiState.needVerify }) {
    startCaptchaStateMachine(
        generateCaptchaUseCase = generateCaptchaUseCase,
        verifyCaptchaUseCase = verifyCaptchaUseCase,
        poseMessage = { poseEffect(AuthEffect.ShowMessage(it)) },
        getCaptchaUiState = { captchaUiState },
        // 将父 Action 映射为子 Action
        mapAction = { action ->
            when (action) {
                is AuthAction.CaptchaPositionSubmitted ->
                    CaptchaAction.SubmitPosition(action.captchaId, action.x)
                is AuthAction.CloseCaptcha ->
                    CaptchaAction.Close
                else -> null  // 返回 null 表示不转发
            }
        },
        updateState = { copy(captchaUiState = it) }
    )
}
```

### 5. 子状态机设计要点

| 要点            | 说明                                      |
|---------------|-----------------------------------------|
| **独立性**       | 子状态机有自己的 State 和 Action，与父状态机解耦         |
| **初始状态**      | 通过构造参数传入，由父状态机决定                        |
| **状态同步**      | 通过 `handler` 回调将子状态机的状态同步到父状态机          |
| **Action 转发** | 通过 `actionMapper` 将父 Action 选择性地转发给子状态机 |
| **激活条件**      | 使用 `condition` 限定子状态机的激活时机              |
| **副作用传递**     | 通过回调函数（如 `onMessage`）将副作用传递给父状态机处理      |

## 八、测试

### 1. 使用 Turbine 测试

```kotlin
@Test
fun `test loading to show content transition`() = runTest {
        val httpClient = mockk<HttpClient>()
        coEvery { httpClient.loadItems() } returns listOf(Item("Test"))

        val stateMachine = ItemListStateMachineFactory(httpClient)
            .launchIn(this)

        stateMachine.state.test {
            assertEquals(ListState.Loading, awaitItem())
            assertEquals(ListState.ShowContent(listOf(Item("Test"))), awaitItem())
        }
    }

@Test
fun `test retry action in error state`() = runTest {
    val stateMachine = // ... 创建状态机

        stateMachine.state.test {
            // 跳过初始状态
            skipItems(1)

            // 模拟错误状态
            // ...

            // 分发重试动作
            stateMachine.dispatch(Action.RetryLoadingAction)

            assertEquals(ListState.Loading, awaitItem())
        }
}
```

### 2. 测试最佳实践

- 为每个状态转换编写测试
- 测试边界条件和错误情况
- 使用 Mock 隔离外部依赖
- 验证副作用的触发

## 九、DSL 速查表

### FlowRedux 原生 DSL

| DSL                                  | 触发时机      | 用途       |
|--------------------------------------|-----------|----------|
| `inState<T>`                         | 状态匹配时     | 定义状态范围   |
| `onEnter`                            | 进入状态时（一次） | 初始化、数据加载 |
| `on<A>`                              | 收到动作时     | 处理用户交互   |
| `collectWhileInState(flow)`          | 状态持续期间    | 订阅数据流    |
| `condition({ predicate })`           | 条件满足时     | 条件分支     |
| `untilIdentityChanged({ selector })` | 属性变化时重新执行 | 监听属性变化   |

### 状态操作 (FlowRedux 原生)

| 操作                      | 用途       |
|-------------------------|----------|
| `override { NewState }` | 切换到新状态类型 |
| `mutate { copy(...) }`  | 修改当前状态属性 |
| `noChange()`            | 不改变状态    |
| `snapshot`              | 获取当前状态快照 |

### 项目扩展 API (仅 `StateMachineMviViewModel`)

| 操作                      | 用途             |
|-------------------------|----------------|
| `poseEffect(effect)`    | 异步触发副作用        |
| `tryPoseEffect(effect)` | 尝试触发副作用，返回是否成功 |

## 十、注意事项

### 1. 一个 DSL 块中只能改变一次状态

```kotlin
// ❌ 错误：多次状态改变，只有最后一次生效
onEnter {
    mutate { copy(isLoading = true) }   // 被忽略
    mutate { copy(error = null) }        // 生效
}

// ✅ 正确：一次性改变所有属性
onEnter {
    mutate { copy(isLoading = true, error = null) }
}
```

### 2. 异步操作会被取消

当状态变化时，正在执行的 `onEnter` 和 `on<Action>` 会被取消：

```kotlin
inState<Loading> {
    onEnter {
        val result = longRunningOperation()  // 如果状态变化，这里会被取消
        override { ShowContent(result) }
    }
}
```

### 3. 使用 `snapshot` 获取状态

在异步操作中，使用 `snapshot` 获取执行时的状态快照：

```kotlin
onEnter {
    val currentEmail = snapshot.email  // 获取快照
    val result = api.sendCode(currentEmail)
    // ...
}
```

### 4. 避免在 `condition` 中使用复杂逻辑

条件判断应该简单快速：

```kotlin
// ✅ 推荐：简单的属性检查
condition({ it.isSubmitting }) { }

// ❌ 避免：复杂的计算
condition({ it.items.filter { ... }.count() > 10 }) { }
```

## 十一、参考资源

- [FlowRedux 官方文档](https://freeletics.github.io/FlowRedux/)
- [DSL 速查表](https://freeletics.github.io/FlowRedux/dsl-cheatsheet/)
- [测试指南](https://freeletics.github.io/FlowRedux/user-guide/14_testing/)
- [Compose 集成](https://freeletics.github.io/FlowRedux/compose/)

