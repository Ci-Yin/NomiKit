# 分层架构与错误处理规范

本文档定义了项目的分层架构、错误处理策略和测试规范。

## 一、分层架构概述

```
┌────────────────────────────────────────────────────────────┐
│                    Presentation Layer                       │
│  (Screen, ViewModel, State, Action, Effect)                 │
│  - 消费场景错误，展示 UI                                       │
└──────────────────────────┬─────────────────────────────────┘
                           │ 调用
┌──────────────────────────▼─────────────────────────────────┐
│                      Domain Layer                           │
│  (UseCase, Validator, Repository接口, 场景错误)               │
│  - 编排业务流程                                               │
│  - 将错误模型映射为场景错误                                   │
└──────────────────────────┬─────────────────────────────────┘
                           │ 实现
┌──────────────────────────▼─────────────────────────────────┐
│                       Data Layer                            │
│  (Repository实现, DataSource, API, Mapper, Entity)          │
│  - 只产出通用错误 `DomainError` │
│  - 数据获取与持久化                                           │
└────────────────────────────────────────────────────────────┘
```

### 1. 职责边界与调用链

分层的核心目标是：**上层只关心“意图与状态”，下层只关心“数据与实现细节”**，避免越层调用导致的耦合与错误流转混乱。

- **Screen（Compose UI）**：只负责 UI 渲染与事件分发（将用户交互转换为 `UiEvent/UiAction`），不直接调用
  `UseCase/Repository`。
- **ViewModel（Presentation）**：不处理业务逻辑；只负责状态管理与协调 UI 交互：
    - 接收 `UiAction/UiEvent`
    - 调用 `UseCase`
    - 将结果映射为 `UiState`，并在需要时发出 `Effect`
- **UseCase（Domain）**：负责面向“一次用户交互/一次业务意图”的业务编排：
    - 输入校验（可委托给 `Validator`）
    - 串并行调用一个或多个 `Repository`
    - 聚合/转换结果
    - 将通用错误（`DomainError`）映射为场景错误 `XxxError`（预期错误→业务错误；未知错误→`Unknown` 兜底）
- **Repository（Domain 接口 / Data 实现）**：负责具体的数据拉取与存储策略（网络、数据库、缓存、本地设置等）：
    - 对上层提供稳定抽象，隐藏数据源细节
    - 在 Data 层将异常/失败统一转换为通用错误（`DomainError`）并返回

推荐调用链：`Screen → ViewModel → UseCase → Repository(接口) → RepositoryImpl/数据源`

## 二、错误处理规范

### 0. 错误分类与职责边界（通用/领域/场景 × 技术/业务）

错误模型建议用两条维度来划分：

- **技术错误（Technical）**：与业务语义无关的失败原因（网络/超时/解析/持久化/未知异常等）。
- **业务错误（Business）**：与业务规则强相关的失败原因（验证码错误、状态冲突、风控拦截、余额不足等）。

以及错误的“作用域”：

- **通用错误（DomainError）**：可跨领域复用。
- **场景错误（XxxError）**：限定在某个用例/交互/页面语境下消费，用于驱动 UI（Presentation 只消费这一层错误）。

推荐落位规则：

- **Data 层**
    - 只将“异常/失败”转换为通用错误（`DomainError`）（不产出业务/场景错误）。
    - 不直接产出 UI/场景错误（避免把 UI 语义带入底层）。
- **Domain / UseCase 层**
    - 将通用错误（`DomainError`）映射为场景错误 `XxxError`，并编排业务流程：
        - **预期的错误**：把 `DomainError` 解释为业务语义（例如 HTTP 409 → `EmailAlreadyRegistered`）。
        - **未知的错误**：统一映射为 `XxxError.Unknown(DomainError)` 兜底，避免 UI 依赖底层细节。
    - 必要时再把场景错误映射为最终展示用的 `GenericError`（作为 UI 展示兜底模型）。
- **Presentation 层**
    - 只消费 `XxxError`（以及可选的 `GenericError`），负责把错误呈现为状态/副作用，不分支处理底层错误细节。

为什么要这样划分：

- **保持职责单一**：Data 只关心数据与失败建模；Domain 只关心业务编排与语义映射；Presentation 只关心状态与展示。
- **避免耦合与泄漏**：UI 不需要知道 Dao/网络/缓存等细节；更换数据源策略时，上层不需要跟着改。
- **提高复用与可测试性**：错误映射集中在 Domain，单元测试更稳定。
- **避免 ViewModel 变胖**：错误分支与组合逻辑不会散落在多个 ViewModel 中。

### 1. 通用错误（`DomainError`）

定义跨用例复用的技术性错误，使用 `sealed interface` 建模：

```kotlin
sealed interface DomainError {
    // 网络相关
    sealed interface Network : DomainError {
        object NoConnection : Network      // 离线/无网络
        object Timeout : Network           // 连接/读取超时
        data class Http(
            val code: Int,
            val description: String,
            val cause: Throwable? = null
        ) : Network                        // 非 2xx 响应
        object Unauthorized : Network      // 401/鉴权失效
        object SSL : Network               // 证书/握手问题
        object DNS : Network               // 解析失败
    }

    // 数据/解析
    object Serialization : DomainError     // JSON 解析等
    object Persistence : DomainError       // DB/文件

    // 其他
    data class Unknown(
        val cause: Throwable? = null,
        val message: String? = cause?.message
    ) : DomainError
}
```

### 2. 场景错误 (`XxxError`)

定义特定业务场景关注的错误，更具业务语义：

> **同一领域通常会有多个场景错误类型**，以用例/交互命名更清晰，例如：`LoginError`、`RegisterError`。
>
> 为了避免每个场景错误类型都重复声明 `Generic(GenericError)` 包装，建议统一使用
`Unknown(DomainError)`
> 作为兜底，并在需要展示时通过 `DomainError.toGenericError()` / `XxxError.toDisplayMessage()`
> 等转换为最终文案。

```kotlin
sealed class RegisterError(val message: String) {
    // 输入校验错误
    object EmptyEmail : RegisterError("请输入邮箱")
    object EmptyCaptcha : RegisterError("请输入邮箱验证码")
    object EmptyPassword : RegisterError("请输入密码")
    object EmptyConfirmPassword : RegisterError("请输入确认密码")
    object PasswordMismatch : RegisterError("密码不一致")
    data class PasswordTooShort(val min: Int) : RegisterError("密码长度不能小于${min}位")

    // 服务端业务错误
    object EmailAlreadyRegistered : RegisterError("邮箱已存在")
    class InvalidCaptcha(message: String) : RegisterError(message.ifEmpty { "邮箱验证码错误" })

    // 未知错误兜底：未能被识别为“预期业务错误”的 `DomainError`，统一在这里承接
    data class Unknown(val error: DomainError) : RegisterError(error.toGenericError().message)
}
```

### 3. 错误映射规则

在 `domain` 层集中定义错误到场景错误的映射函数：

- 简单场景：`DomainError -> XxxError`
- 同一领域不同用例：通常是 `DomainError -> LoginError`、`DomainError -> RegisterError` 等分别映射

```kotlin
// domain/auth/error/RegisterError.kt
fun DomainError.toRegisterError(): RegisterError = when (this) {
    is DomainError.Network.Http -> when (code) {
        400 -> RegisterError.InvalidCaptcha(this.description)
        409 -> RegisterError.EmailAlreadyRegistered
        else -> RegisterError.Unknown(this)
    }
    else -> RegisterError.Unknown(this)
}
```

### 4. 错误流转规则

| 层级                   | 职责                                      |
|----------------------|-----------------------------------------|
| **Data 层**           | 只产出通用错误（`DomainError`）                  |
| **Domain/UseCase 层** | 将通用错误（`DomainError`）映射为场景错误（`XxxError`） |
| **Presentation 层**   | 只消费场景错误，用于 UI 展示（不做 `DomainError` 映射）   |

## 三、UseCase 与 Validator

### 1. Validator (校验器)

- **职责**: 负责特定用例的输入校验
- **位置**: 放在用例包内 (`domain.auth.util`)
- **实现**: 返回 `Either<场景错误, Unit>`，使用 `Arrow.Raise` 简化逻辑

```kotlin
interface RegisterInputValidator {
    fun validate(input: RegisterInput): Either<RegisterError, Unit>
}

class DefaultRegisterInputValidator : RegisterInputValidator {
    override fun validate(input: RegisterInput): Either<RegisterError, Unit> = either {
        ensure(input.email.isNotBlank()) { RegisterError.EmptyEmail }
        ensure(input.captcha.isNotBlank()) { RegisterError.EmptyCaptcha }
        ensure(input.password.isNotBlank()) { RegisterError.EmptyPassword }
        ensure(input.confirmPassword.isNotBlank()) { RegisterError.EmptyConfirmPassword }
        ensure(input.password == input.confirmPassword) { RegisterError.PasswordMismatch }
        validatePassword(input.password).bind()
    }

    private fun validatePassword(password: String): Either<RegisterError, Unit> = either {
        ensure(password.length >= MIN_PASSWORD_LENGTH) {
            RegisterError.PasswordTooShort(MIN_PASSWORD_LENGTH)
        }
        ensure(!password.contains(' ')) { RegisterError.PasswordContainsSpace }
    }

    companion object {
        private const val MIN_PASSWORD_LENGTH = 6
    }
}
```

### 2. UseCase (用例)

- **职责**: 面向“一次用户交互”的业务编排（校验 → 调用 Repository → 结果聚合/转换 → 错误映射）
- **实现**: 接收 `Repository` 和 `Validator` 依赖，使用 `either { ... }.bind()` 链式处理

```kotlin
class RegisterUserUseCase(
    private val repository: AuthRepository,
    private val validator: RegisterInputValidator
) {
    suspend operator fun invoke(input: RegisterInput): Either<RegisterError, RegisterResult> = either {
        // 1. 校验输入
        validator.validate(input).bind()
        // 2. 调用 Repository，映射错误
        repository.register(input).mapLeft(DomainError::toRegisterError).bind()
    }
}
```

### 3. 订阅型 UseCase（Flow / Observe / Stream）

当 UI 需要**持续监听数据变化**（例如数据库/偏好设置暴露 `Flow`，用于驱动界面自动刷新）时：

- **ViewModel 不应直接订阅 Repository/Dao 的 Flow**，而应通过 `ObserveXxxUseCase` 获取可订阅的
  `Flow`。
- **UseCase 暴露 Flow 的原因**：
    - 保持 `ViewModel` 不处理业务逻辑：VM 只负责 collect 并更新 `UiState/Effect`
    - 统一业务语义与边界：上层依赖“观察用户资料/登录态”等语义，而不是某张表/某个 Dao
    - 集中编排与组合：更容易在 UseCase 中组合多个 Flow、做去重/节流/合并等业务规则
    - 统一错误模型到场景错误的映射：确保 Presentation 只消费 `XxxError`
    - 更易测试：UseCase 的输出序列可用纯单元测试验证
    - 隐藏数据源细节：Repository 可替换 Room/DataStore/缓存策略而不影响上层

实现建议：

- `UseCase` 返回 **cold Flow**，不在 UseCase 内部持有 `CoroutineScope` 启动收集；由 ViewModel
  决定生命周期，并在 VM 层用
  `stateIn/shareIn(viewModelScope, ...)` 做共享与缓存。

示例：

```kotlin
class ObserveUserProfileUseCase(
    private val repository: UserRepository
) {
    operator fun invoke(): Flow<Either<ProfileError, UserProfile>> =
        repository.observeUserProfile()
            .map { it.mapLeft(DomainError::toProfileError) }
}
```

### 4. 何时可以不建 UseCase

**原则上都应该建 UseCase**，保证业务边界与错误映射入口统一，避免把编排与映射分散到多个 ViewModel 中。

极简场景（仅一次 `Repository` 调用、无额外校验/聚合逻辑）时，可以使用“薄 UseCase”：

- `UseCase` 仅做直通调用与错误映射（或在该场景下无需错误映射时，仍保持返回类型统一）
- `ViewModel` 依旧只调用 `UseCase`，不直接调用 `Repository`

## 四、Repository 与 DataSource

### 1. Repository 接口 (Domain 层)

定义数据操作的抽象，返回 `Either<DomainError, ...>`：

```kotlin
// domain/auth/AuthRepository.kt
interface AuthRepository {
    suspend fun register(input: RegisterInput): Either<DomainError, RegisterResult>
    suspend fun login(input: LoginInput): Either<DomainError, LoginResult>
    suspend fun logout(): Either<DomainError, Unit>
}
```

### 2. Repository 实现 (Data 层)

组合多种数据源，处理异常/失败并转换为通用错误 `DomainError`：

```kotlin
// data/auth/AuthRepositoryImpl.kt
class AuthRepositoryImpl(
    private val api: UserAuthApi,
    private val authSettings: AuthSettings
) : AuthRepository {

    override suspend fun register(input: RegisterInput): Either<DomainError, RegisterResult> =
        Either.catch {
            val request = input.toRegisterRequest()
            val response = api.register(request)
            response.toRegisterResult()
        }.mapLeft { it.toDomainError() }

    override suspend fun login(input: LoginInput): Either<DomainError, LoginResult> =
        Either.catch {
            val request = input.toLoginRequest()
            val response = api.login(request)
            // 保存 token 到本地
            authSettings.accessToken = response.accessToken
            authSettings.refreshToken = response.refreshToken
            response.toLoginResult()
        }.mapLeft { it.toDomainError() }
}
```

### 3. 数据源命名

根据存储类型选择对应的命名后缀：

| 存储类型                     | 命名后缀        | 示例              | 说明          |
|--------------------------|-------------|-----------------|-------------|
| `multiplatform-settings` | `Settings`  | `AuthSettings`  | 简单键值对存储     |
| `androidx.datastore`     | `DataStore` | `UserDataStore` | 类型安全的复杂数据结构 |
| `Room`                   | `Dao`       | `AuthInfoDao`   | 数据库存储       |

```kotlin
// 远程数据源 (API)
interface UserAuthApi {
    suspend fun register(request: RegisterRequest): RegisterResponse
    suspend fun login(request: LoginRequest): LoginResponse
}

// 本地数据源 - Settings（简单键值对）
class AuthSettings(settings: Settings) {
    var accessToken: String? by settings.nullableString()
    var refreshToken: String? by settings.nullableString()
    
    fun clear() {
        accessToken = null
        refreshToken = null
    }
}

// 本地数据源 - Dao（数据库）
@Dao
interface AuthInfoDao {
    @Query("SELECT * FROM auth_info WHERE id = :id")
    suspend fun getAuthInfo(id: String): AuthInfoEntity?
    
    @Upsert
    suspend fun saveAuthInfo(entity: AuthInfoEntity)
}
```

## 五、DI 模块组织

### 1. 按层和功能模块组织

```kotlin
// data/auth/di/AuthDataModule.kt
val AuthDataModule = module {
    single { AuthSettings(get()) }
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
}

// domain/auth/di/AuthDomainModule.kt
val AuthDomainModule = module {
    single<RegisterInputValidator> { DefaultRegisterInputValidator() }
    single<LoginInputValidator> { DefaultLoginInputValidator() }
    singleOf(::RegisterUserUseCase)
    singleOf(::LoginUserUseCase)
    singleOf(::SendVerifyCodeUseCase)
}

// presentation (通过 koin-boot 自动注册 ViewModel)
```

### 2. 聚合模块

```kotlin
// 可选：聚合所有 Auth 相关模块
val AuthModules = listOf(
    AuthDataModule,
    AuthDomainModule
)
```

## 六、UI 集成 (ViewModel)

### 1. 简单场景：直接调用 UseCase

```kotlin
class ProfileViewModel(
    private val getUserProfileUseCase: GetUserProfileUseCase
) : AbstractViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    fun loadProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            getUserProfileUseCase()
                .fold(
                    { error -> _state.update { it.copy(isLoading = false, error = error.message) } },
                    { profile -> _state.update { it.copy(isLoading = false, profile = profile) } }
                )
        }
    }
}
```

### 2. 复杂场景：使用 FlowRedux 状态机

继承 `StateMachineMviViewModel` 处理复杂业务交互：

```kotlin
class AuthViewModel : StateMachineMviViewModel<AuthState, AuthAction, AuthEffect>(), KoinComponent {

    private val loginUserUseCase by inject<LoginUserUseCase>()
    private val registerUserUseCase by inject<RegisterUserUseCase>()

    override val initialize: FlowReduxStateMachineFactory<AuthState, AuthAction>.() -> Unit = {
        initializeWith { AuthState(emailSuffixes = listOf("@gmail.com")) }
    }

    override val spec: FlowReduxBuilder<AuthState, AuthAction>.() -> Unit = {
        inState<AuthState> {
            on<AuthAction.TabSelected> {
                if (snapshot.isSubmitting) return@on noChange()
                mutate { copy(currentTab = it.tab) }
            }

            on<AuthAction.Submit> {
                mutate { copy(isSubmitting = true) }
            }

            // 处理提交逻辑
            condition({ it.isSubmitting }) {
                onEnter {
                    when (snapshot.currentTab) {
                        AuthTab.Login -> handleLogin()
                        AuthTab.Register -> handleRegister()
                    }
                    mutate { copy(isSubmitting = false) }
                }
            }
        }
    }

    private suspend fun FlowReduxBuilder<AuthState, AuthAction>.StateScope<AuthState>.handleLogin() {
        loginUserUseCase(snapshot.toLoginInput())
            .fold(
                { error -> poseEffect(AuthEffect.ShowMessage(error.message)) },
                { poseEffect(AuthEffect.NavigateToMain) }
            )
    }
}
```

> **注意**: FlowRedux 状态机的一个动作 DSL 中只能改变一次状态。

## 七、测试策略

### 1. Validator 单元测试

验证校验规则的正确性：

```kotlin
class RegisterInputValidatorTest {
    private val validator = DefaultRegisterInputValidator()

    @Test
    fun `validate returns EmptyEmail when email is blank`() {
        val input = RegisterInput(email = "", password = "123456", confirmPassword = "123456")
        val result = validator.validate(input)

        assertEquals(RegisterError.EmptyEmail, result.leftOrNull())
    }

    @Test
    fun `validate returns PasswordMismatch when passwords differ`() {
        val input = RegisterInput(email = "test@gmail.com", password = "123456", confirmPassword = "654321")
        val result = validator.validate(input)

        assertEquals(RegisterError.PasswordMismatch, result.leftOrNull())
    }
}
```

### 2. UseCase 单元测试

模拟 Repository 返回不同错误模型，断言正确映射为场景错误：

```kotlin
class RegisterUserUseCaseTest {
    private val repository = mockk<AuthRepository>()
    private val validator = DefaultRegisterInputValidator()
    private val useCase = RegisterUserUseCase(repository, validator)

    @Test
    fun `invoke returns EmailAlreadyRegistered when API returns 409`() = runTest {
        val input = validRegisterInput()
        coEvery { repository.register(any()) } returns DomainError.Network.Http(409, "").left()

        val result = useCase(input)

        assertEquals(RegisterError.EmailAlreadyRegistered, result.leftOrNull())
    }

    @Test
    fun `invoke returns success when registration succeeds`() = runTest {
        val input = validRegisterInput()
        val expected = RegisterResult(message = "注册成功")
        coEvery { repository.register(any()) } returns expected.right()

        val result = useCase(input)

        assertEquals(expected, result.getOrNull())
    }
}
```

### 3. Repository 单元测试

模拟 DataSource 异常，断言正确产出技术错误包装：

```kotlin
class AuthRepositoryImplTest {
    private val api = mockk<UserAuthApi>()
    private val authSettings = mockk<AuthSettings>(relaxed = true)
    private val repository = AuthRepositoryImpl(api, authSettings)

    @Test
    fun `register returns Network error when API throws IOException`() = runTest {
        coEvery { api.register(any()) } throws IOException("Network error")

        val result = repository.register(validRegisterInput())

        val error = result.leftOrNull()
        assertTrue(error is DomainError.Network)
    }
}
```

### 4. ViewModel 测试

验证 UI 状态变化、错误消息展示、成功路径：

```kotlin
class AuthViewModelTest {
    private val loginUseCase = mockk<LoginUserUseCase>()
    private val viewModel: AuthViewModel // 使用 Fake UseCase 初始化

    @Test
    fun `submit login shows error message on failure`() = runTest {
        coEvery { loginUseCase(any()) } returns LoginError.InvalidCredentials.left()

        viewModel.dispatchAction(AuthAction.Submit)

        // 验证 Effect
        val effect = viewModel.sideEffects.first()
        assertTrue(effect is AuthEffect.ShowMessage)
    }

    @Test
    fun `submit login navigates to main on success`() = runTest {
        coEvery { loginUseCase(any()) } returns LoginResult().right()

        viewModel.dispatchAction(AuthAction.Submit)

        val effect = viewModel.sideEffects.first()
        assertEquals(AuthEffect.NavigateToMain, effect)
    }
}
```

## 八、层间数据隔离规范

> **核心原则**: 各层使用独立的数据模型，通过 Mapper 进行转换，避免跨层直接使用对象。

### 1. 数据模型分层

```
┌─────────────────────────────────────────────────────────────┐
│  Presentation 层                                            │
│  使用: State, UiItem, UiModel                               │
│        ↑ .toUiModel() / .toUiItem()                        │
├─────────────────────────────────────────────────────────────┤
│  Domain 层                                                  │
│  使用: Domain Model (Banner, Film, HomeVideo 等)            │
│        ↑ .toDomain()                                       │
├─────────────────────────────────────────────────────────────┤
│  Data 层                                                    │
│  使用: ApiData/ApiItem (网络), Entity (数据库)               │
└─────────────────────────────────────────────────────────────┘
```

### 2. 禁止跨层直接使用对象

| 规则                        | 说明                                                      |
|---------------------------|---------------------------------------------------------|
| **UI 层不直接使用 Domain 对象渲染** | Domain Model 需通过 Mapper 转换为 UiModel/UiItem 后再用于 UI 渲染   |
| **Data 层不直接使用 Domain 对象** | 网络请求使用 ApiData，数据库使用 Entity，通过 Mapper 与 Domain Model 互转 |

### 3. Mapper 示例

```kotlin
// Data 层: ApiData → Domain Model
// data/home/mappers/HomeApiMapper.kt
fun HotPlayApiData.toDomain(): HotPlayResult { /*...*/
}
fun GuessYouLikeVideoApiItem.toDomain(): HomeVideo { /*...*/
}

// Data 层: Domain Model → Entity (持久化)
// data/auth/mappers/AuthEntityMapper.kt
fun AuthInfo.toEntity(): AuthInfoEntity { /*...*/
}
fun AuthInfoEntity.toDomain(): AuthInfo { /*...*/
}

// Presentation 层: Domain Model → UiModel/UiItem (可选，简单场景可省略)
// presentation/screen/home/mappers/HomeUiMapper.kt
fun HomeVideo.toUiItem(): HomeVideoUiItem { /*...*/
}
fun Film.toUiModel(): FilmUiModel { /*...*/
}
```

### 4. 何时可以省略 UI 层 Mapper

- Domain Model 结构简单，与 UI 展示需求一致
- 不需要额外的 UI 特有字段（如格式化文本、计算属性）
- State 中直接持有 Domain Model 列表

```kotlin
// ✅ 简单场景：State 直接持有 Domain Model
data class HomeState(
    val recommendations: List<HomeVideo> = emptyList(),  // Domain Model
    val isLoading: Boolean = false
)

// ✅ 复杂场景：需要 UI 特有字段时使用 UiItem
data class HomeState(
    val films: List<FilmUiItem> = emptyList(),  // UiItem
)

data class FilmUiItem(
    val id: Int,
    val title: String,
    val formattedScore: String,      // UI 特有：格式化后的评分
    val episodeProgress: String,     // UI 特有：如 "更新至第12集"
    val isNew: Boolean,              // UI 特有：计算属性
)
```

## 九、最佳实践总结

1. **Data 层**: 只负责数据获取，异常转换为通用错误 `DomainError`，使用 ApiData/Entity
2. **Domain 层**: 编排业务流程，将通用错误 `DomainError`映射为场景错误，定义核心业务模型
3. **Presentation 层**: 只消费场景错误，专注 UI 展示，必要时使用 UiModel/UiItem
4. **依赖关系**: Domain 层不依赖任何层，Presentation 层和 Data 层只依赖 Domain 层，不会依赖彼此
5. **数据隔离**: 各层使用独立数据模型，通过 Mapper 转换，避免跨层耦合
6. **错误映射集中管理**: 在场景错误文件中定义映射函数
7. **测试覆盖每一层**: Validator、UseCase、Repository、ViewModel 都应有对应测试
