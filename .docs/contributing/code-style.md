# 代码风格与开发规范

> [!IMPORTANT]
> 确保 IDE 设置中 `Editor -> Code Style -> Enable EditorConfig support` 是勾选的。

## 一、代码风格

### 1. 命名规范

| 类型   | 规范                      | 示例                                                          |
|------|-------------------------|-------------------------------------------------------------|
| 函数   | 动词/动词短语，驼峰命名            | `fetchUser()`, `calculateTotal()`, `submitForm()`           |
| 布尔函数 | `is/has/can/should` 前缀  | `isValid()`, `hasPermission()`, `canRetry()`                |
| 断言函数 | `need/require/check` 前缀 | `needLogin()`, `requireAuth()`, `checkPermission()`         |
| 工厂函数 | `create/of/from` 前缀     | `createUser()`, `fromJson()`, `Color.of(hex)`               |
| 转换函数 | `to` 前缀（创建新对象）          | `toUiModel()`, `toDomain()`, `toList()`                     |
| 视图函数 | `as` 前缀（返回当前对象的代理/包装）   | `asStateFlow()`, `asSequence()`, `asList()`                 |
| 变量   | 名词/名词短语，驼峰命名            | `userName`, `orderList`, `currentIndex`                     |
| 布尔变量 | `is/has/can/should` 前缀  | `isLoading`, `hasError`, `canSubmit`                        |
| 私有状态 | `_` 前缀（配合公开只读属性）        | `private val _state`, `val state = _state.asStateFlow()`    |
| 枚举值  | 驼峰命名（因与单例对象类似）          | `enum class AppPlatformType { Android, Desktop, Ios, Web }` |
| 静态对象 | 驼峰命名（因与单例对象类似）          | `val DataModule = module {}`                                |
| 常量   | 全大写 + 下划线分隔             | `const val MAX_RETRY_COUNT = 3`                             |
| 接口   | 无 `I` 前缀                | `AuthRepository` 而非 `IAuthRepository`                       |
| 实现类  | `Impl` 后缀或具体描述          | `AuthRepositoryImpl` 或 `DefaultAuthRepository`              |
| 密封子类 | 不重复父类后缀                 | `AuthAction.Submit` 而非 `AuthAction.SubmitAction`            |

> **💡 密封类子类命名**
>
> `UiState`/`UiEvent`/`Action`/`Effect` 等密封类的子类**不要重复父类后缀**，避免冗长：
>
> ```kotlin
> // ✅ 推荐：简洁明了
> @Stable
> sealed interface SendCodeUiState {
>     /**
>      * 就绪状态，可以发送验证码。
>      */
>     sealed interface Ready : SendCodeUiState {
>         /**
>          * 初始空闲状态，可以发送验证码。
>          */
>         data object Idle : Ready
> 
>         /**
>          * 冷却结束后的重新发送状态。
>          */
>         data object ReSend : Ready
> }
>  // 使用时: sendCodeUiState is SendCodeUiState.Ready.ReSend
>
> // ❌ 避免：后缀重复冗余
> sealed interface AuthAction {
>     data object SubmitAction : AuthAction           // 多余的 Action 后缀
>     data class TabSelectedAction(val tab: AuthTab) : AuthAction
> }
> ```

### 2. 文件夹与文件命名

| 类型   | 规范               | 示例                                    | 说明                         |
|------|------------------|---------------------------------------|----------------------------|
| 文件夹名 | 单数形式，表示模块或功能领域划分 | `mapper/`、`model/`、`api/`             | 文件夹本身就是多个文件的集合，无需用复数强调     |
| 文件名  | 根据内容决定单复数        | `Colors.kt`、`UserMappers.kt`          | 包含多个相关扩展函数或转换器时使用复数        |
| 单一职责 | 单数形式             | `AuthRepository.kt`、`LoginUseCase.kt` | 文件内仅定义一个主要类型时使用单数          |
| 集合型  | 复数形式             | `Strings.kt`、`Dimensions.kt`          | 文件内包含多个同类扩展函数、常量或工具方法时使用复数 |

> **💡 文件夹与文件命名原则**
>
> - **文件夹名使用单数**：文件夹天然是文件的集合，再用复数会显得冗余。命名应体现模块或功能领域的划分，如
    `mapper/`、`di/`、
    `api/`。
> - **文件名视内容而定**：
    >   - 单一类型定义：使用单数，如 `User.kt`、`AuthRepository.kt`
    >

- 多个相关函数/扩展集合：使用复数，如 `Colors.kt`（Color 的多个扩展函数）、`UserMappers.kt`（User
  及其内部类的多个转换函数）

>
> ```kotlin
> /** ✅ 文件夹：单数形式
> feature/home/
> ├── di/           // 而非 dis/
> ├── mapper/       // 而非 mappers/
> ├── model/        // 而非 models/
> └── api/          // 而非 apis/
> */
> // ✅ 文件：根据内容决定
> // Colors.kt - 包含多个 Color 相关扩展函数
> fun Color.withAlpha(alpha: Float): Color = TODO()
> fun Color.toHexString(): String = TODO()
> val Color.isDark: Boolean 
>   get() = TODO()
>
> // UserMappers.kt - 包含 User 及相关类的多个转换函数
> fun User.toUiModel(): UserUiModel = TODO()
> fun UserAddress.toUiModel(): UserAddressUiModel = TODO()
> fun UserPreference.toEntity(): UserPreferenceEntity = TODO()
> ```

### 3. Kotlin 官方风格

其他规范参考 [Kotlin 官方代码风格指南](https://kotlinlang.org/docs/coding-conventions.html)

## 二、开发规范

### 1. UI 架构

- 使用 **MVI + MVVM** 混合模式
- 简单页面使用 MVVM
- 复杂状态管理使用 MVI + FlowRedux 状态机

### 2. 可见性原则

```kotlin
// ✅ 推荐：非通用函数使用 private 或 internal
private fun formatAuthError(error: AuthError): String {
    TODO()
}

// ✅ 推荐：通过扩展方法挂载到相关对象
fun AuthError.toDisplayMessage(): String {
    TODO()
}

// ❌ 避免：直接定义顶级 public 函数污染全局
fun formatAuthError(error: AuthError): String {
    TODO()
}
```

### 3. Lambda 参数命名

```kotlin
// ✅ 单层 lambda：使用 it 简洁明了
users.filter { it.isActive }
    .map { it.name }

// ✅ 嵌套 lambda：显式声明参数，避免 it 指代不明
users.forEach { user ->
    user.orders.forEach { order ->
        println("${user.name} ordered ${order.id}")
    }
}

// ❌ 避免：嵌套 lambda 中使用 it
users.forEach {
    it.orders.forEach {
        println(it.id)  // 此时 it 指代 order，外层 user 无法访问
    }
}
```

### 4. 状态管理原则

```kotlin
// ✅ 推荐：内部状态通过 StateFlow/SharedFlow 暴露，外部订阅观察
class DownloadManager {
    private val _queueState = MutableStateFlow<List<Task>>(emptyList())
    val queueState: StateFlow<List<Task>> = _queueState.asStateFlow()

    private val _taskEvents = MutableSharedFlow<TaskEvent>()
    val taskEvents = _taskEvents.asSharedFlow()

    fun addTask(task: Task) {
        _queueState.update { it + task }
        scope.launch { _taskEvents.emit(TaskEvent.Added(task)) }
    }
}

// 使用：订阅状态变化
downloadManager.queueState.collect { tasks -> updateUI(tasks) }
downloadManager.taskEvents.collect { event -> handleEvent(event) }

// ❌ 避免：调用方法修改内部属性，再通过属性获取值（命令式）
class DownloadManager {
    var tasks: List<Task> = emptyList()  // 可变属性
    var lastEvent: TaskEvent? = null

    fun addTask(task: Task) {
        tasks = tasks + task      // 直接修改属性
        lastEvent = TaskEvent.Added(task)
    }
}

// 使用（问题：状态变化不可观察，调用者需轮询或手动刷新）
downloadManager.addTask(task)
val currentTasks = downloadManager.tasks  // 无法感知后续变化
```

### 5. 避免滥用高阶函数

```kotlin
// ✅ 推荐：简洁直接
val activeUsers = users.filter { it.isActive }

// ❌ 避免：不必要的嵌套
val activeUsers = users.let { list ->
    list.filter { user ->
        user.let { u -> u.isActive }
    }
}
```

## 三、命名规范总览表

### UI 层命名

| 层级         | 后缀         | 示例                              | 说明                                            |
|------------|------------|---------------------------------|-----------------------------------------------|
| **UI 状态**  | `UiState`  | `ProfileUiState`                | 页面状态，随用户交互改变                                  |
| **UI 模型**  | `UiModel`  | `FilmUiModel`, `ProfileUiModel` | 视图展示数据，纯数据承载（一个类一个文件(除非强关联性), 放在 `model/` 目录） |
| **UI 事件**  | `UiEvent`  | `ProfileUiEvent`                | 用户交互产生的原始事件                                   |
| **UI 动作**  | `Action`   | `AuthAction`                    | 直接作用于 ViewModel 的动作                           |
| **UI 副作用** | `Effect`   | `AuthEffect`                    | 单次动作（弹窗、导航、Toast）                             |
| **列表项模型**  | `UiItem`   | `FeedUiItem`                    | 列表或网格项数据                                      |
| **映射器**    | `UiMapper` | `HomeUiMapper`                  | 扩展函数 `.toUiModel()`（放在 `mapper/` 目录）          |
| **DI 模块**  | `UiModule` | `HomeUiModule`                  | Ui 层 DI 配置（放在 `di/` 目录）                       |

> 加上 `Ui` 前缀 是为了区分 UI 层与业务层，避免命名冲突。而Action 和 Effect 不需要加 `Ui` 前缀, 因为
> Action 和 Effect 是
> UI 层与业务层交互的桥梁。

> **💡 UiState vs UiModel**
>
> - **UiState**：页面状态，包含 UI 交互相关的状态（如 `isLoading`、`isExpanded`、`currentTab`），会随用户交互改变。
> - **UiModel**：纯数据承载类，用于视图展示，从 Domain Model 转换而来，包含 UI 特有的格式化字段。
>
> ```kotlin
> // UiState：页面状态，包含交互状态
> data class ProfileUiState(
>     val isLoading: Boolean = false,
>     val isEditing: Boolean = false,
>     val profile: ProfileUiModel? = null,  // 持有 UiModel
>     val error: String? = null
> )
>
> // UiModel：纯数据展示，从 Domain Model 转换
> data class ProfileUiModel(
>     val id: String,
>     val displayName: String,           // 格式化后的显示名
>     val avatarUrl: String,
>     val membershipBadge: String,       // UI 特有：会员徽章文案
>     val formattedJoinDate: String,     // UI 特有：格式化日期
> )
> ```

> **💡 UI 事件 vs UI 动作**
>
> - **UI 事件 (Event)**：用户交互产生的原始事件（点击、滑动、返回等）。事件**不直接作用于 ViewModel**，需要经过
    UI
    层处理后决定是否转换为动作。有些事件（如返回操作、展开/收起）只涉及 UI 层状态变化，不需要与 ViewModel
    交互。
>
> - **UI 动作 (Action)**：经过 UI 层处理后，**直接分发给 ViewModel** 的指令。Action 会触发 ViewModel
    中的状态变化或业务逻辑。
>
> **UI 事件的另一个用途**：作为子组件回调父组件的**统一入参类型**，避免多个回调参数导致的歧义和混乱。
>
> ```kotlin
> // ❌ 避免：多个回调参数，容易混淆
> @Composable
> fun FilmCard(
>     film: Film,
>     onFilmClick: (Film) -> Unit,
>     onFavoriteClick: (Film) -> Unit,
>     onShareClick: (Film) -> Unit,
>     onMoreClick: (Film) -> Unit,
> ) {  /*TODO()*/ }
>
> // ✅ 推荐：使用 UiEvent 统一封装
> sealed interface FilmCardEvent {
>     data class Click(val film: Film) : FilmCardEvent
>     data class Favorite(val film: Film) : FilmCardEvent
>     data class Share(val film: Film) : FilmCardEvent
>     data class More(val film: Film) : FilmCardEvent
> }
>
> @Composable
> fun FilmCard(
>     film: Film,
>     onEvent: (FilmCardEvent) -> Unit,  // 单一回调，语义清晰
> ) { /*TODO()*/ }
>
> // 父组件处理事件
> FilmCard(
>     film = film,
>     onEvent = { event ->
>         when (event) {
>             is FilmCardEvent.Click -> viewModel.dispatchAction(Action.OpenDetail(event.film))
>             is FilmCardEvent.Favorite -> viewModel.dispatchAction(Action.ToggleFavorite(event.film))
>             is FilmCardEvent.Share -> shareFilm(event.film)  // 仅 UI 层处理
>             is FilmCardEvent.More -> showMoreOptions(event.film)
>         }
>     }
> )
> ```

### Domain 层命名

| 层级                | 后缀                             | 示例                                        | 说明                          |
|-------------------|--------------------------------|-------------------------------------------|-----------------------------|
| **Repository 接口** | `Repository`                   | `AuthRepository`                          | 定义在 Domain 层的数据操作抽象         |
| **用例**            | `UseCase`                      | `LoginUserUseCase`, `RegisterUserUseCase` | 业务用例，编排流程                   |
| **校验器**           | `Validator` / `InputValidator` | `RegisterInputValidator`                  | 输入校验策略                      |
| **领域模型**          | 无特定后缀                          | `Banner`,                                 | 业务实体                        |
| **输入模型**          | `Input`                        | `LoginInput`, `RegisterInput`             | UseCase 输入参数                |
| **输出模型**          | `Result`                       | `RegisterResult`, `LoginResult`           | UseCase 输出结果                |
| **DI 模块**         | `DomainModule`                 | `HomeDomainModule`                        | Domain 层 DI 配置（放在 `di/` 目录） |

### Data 层命名

| 层级                | 后缀                                                | 示例                                              | 说明                                                           |
|-------------------|---------------------------------------------------|-------------------------------------------------|--------------------------------------------------------------|
| **Repository 实现** | `RepositoryImpl`                                  | `HomeRepositoryImpl`                            | Repository 的具体实现                                             |
| **API 类**         | `Api`                                             | `HomeApi`, `UserAuthApi`                        | 网络请求类（放在 `api/` 目录）                                          |
| **本地数据源**         | `Settings` / `Dao`  / `DataStore`                 | `AuthSettings`, `AuthInfoDao` , `UserDataStore` | 本地数据操作（放在 `datasource/` 目录）, `Dao` 为数据库存储, `Settings` 为键值对存储 |
| **API 响应数据**      | `ApiData` / `ApiItem`                             | `HotPlayApiData`, `GuessYouLikeVideoApiItem`    | API 响应 DTO（放在 `model/` 目录）                                   |
| **本地实体**          | `Entity(Dao的数据类)` / `Preferences(DataStore 的数据类)` | `AuthInfoEntity` , `UserPreferences`            | Room 实体类（放在 `model/` 目录）                                     |
| **映射器**           | `ApiMapper`                                       | `HomeApiMapper`                                 | 扩展函数 `.toDomain()`（放在 `mappers/` 目录）                         |
| **DI 模块**         | `DataModule`                                      | `HomeDataModule`                                | Data 层 DI 配置（放在 `di/` 目录）                                    |

> **💡 Settings vs DataStore**
>
> - **Settings**：基于 `multiplatform-settings`，用于简单的键值对存储（如用户偏好设置）
> - **DataStore**：基于 `androidx.datastore`，用于类型安全的复杂数据结构存储，配合 `@Serializable` 使用
>
> ```kotlin
> // Settings：简单键值对存储
> class AuthSettings(settings: Settings) {
>     var accessToken: String?
>           get() = settings.nullableString("access_token")
>           set(value) = settings.putNullableString("access_token", value)
>     var refreshToken: String?
>           get() = settings.nullableString("refresh_token")
>           set(value) = settings.putNullableString("refresh_token", value)
> }
>
> // DataStore：类型安全的复杂数据存储
> @Serializable
> data class UserPreferences(
>     val theme: String = "light",
>     val language: String = "zh",
>     val notifications: NotificationPreferences = NotificationPreferences()
> )
>
> @Serializable
> data class NotificationPreferences(
>     val enabled: Boolean = true,
>     val sound: Boolean = true
> )
> 
> // 使用 DataStoreFactory 创建
> class UserDataStore(
>    dataStoreFactory: DataStoreFactory,
> ): DataStore<UserPreferences> by dataStoreFactory.create(
>      defaultValue = UserPreferences()
> )
> ```

### 错误命名

| 层级       | 后缀             | 示例                            | 说明           |
|----------|----------------|-------------------------------|--------------|
| **通用错误** | `DataError`    | `DataError.Network.Http`      | 跨用例复用的技术性错误  |
| **场景错误** | `XxxError`     | `RegisterError`, `LoginError` | 特定业务场景的错误    |
| **通用错误** | `GenericError` | `GenericError`                | 最终展示给用户的通用错误 |

## 四、UiAction 命名约定

`UiAction` 密封类中的子类，如果是回调类型的 `onXXX`，只保留 `XXX`：

```kotlin
// ✅ 推荐
sealed interface AuthAction {
    data class TabSelected(val tab: AuthTab) : AuthAction  // 对应 onTabSelected
    data object Submit : AuthAction                         // 对应 onSubmit
    data class EmailSuffixIndexChanged(val index: Int) : AuthAction
}

// ❌ 避免
sealed interface AuthAction {
    data class OnTabSelected(val tab: AuthTab) : AuthAction  // 多余的 On 前缀
}
```

## 五、Effect 定义规范

Effect 用于一次性副作用事件：

```kotlin
// ✅ 推荐：简洁的副作用定义
internal interface AuthEffect {
    data object NavigateToMain : AuthEffect
    data class ShowMessage(val message: String) : AuthEffect
    data class ShowError(val error: GenericError) : AuthEffect
}

// 常见 Effect 类型：
// - NavigateToXxx: 导航
// - ShowMessage/ShowToast: 提示消息
// - ShowDialog: 显示对话框
// - OpenUrl: 打开外部链接
```

## 六、Compose Preview 规范

生成 Compose Preview 函数时，使用项目内的 `AppPreview` 注解：

```kotlin
import com.yy.myuko.core.ui.foundation.widgets.AppPreview

@AppPreview
@Composable
private fun AuthScreenPreview() = AppPreview {
    AuthScreen(
        state = AuthState(emailSuffixes = listOf("@gmail.com")),
        onAction = {}
    )
}
```

## 七、资源引用规范

使用 JetBrains Compose Resources，而非 androidx 的 Resources：

```kotlin
// ✅ 推荐：使用 JetBrains Compose Resources
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.painterResource

Text(text = stringResource(Res.string.app_name))
Icon(painter = painterResource(Res.drawable.ic_home), contentDescription = null)
```

```kotlin
// ❌ 避免：使用 androidx 资源
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
```

## 八、文档注释规范

为新功能添加标准文档注释：

```kotlin
/**
 * 用户登录用例。
 *
 * 负责编排登录流程：校验输入 → 调用 Repository → 错误映射。
 *
 * @param repository 认证仓库，提供登录能力
 * @param validator 输入校验器
 */
class LoginUserUseCase(
    private val repository: AuthRepository,
    private val validator: LoginInputValidator
) {
    /**
     * 执行登录操作。
     *
     * @param input 登录输入参数
     * @return [Either.Left] 包含 [LoginError]，[Either.Right] 包含登录结果
     */
    suspend operator fun invoke(input: LoginInput): Either<LoginError, LoginResult> = either {
        validator.validate(input).bind()
        repository.login(input).mapLeft { it.toLoginError() }.bind()
    }
}
```

## 九、单元测试规范

- 为新功能增加单元测试
- 测试文件放在对应的 `test` 源集中
- 使用描述性的测试方法名

```kotlin
class RegisterInputValidatorTest {

    private val validator = DefaultRegisterInputValidator()

    @Test
    fun `validate should return error when email is empty`() = runTest {
        val input = RegisterInput(email = "", password = "123456", confirmPassword = "123456")
        val result = validator.validate(input)

        assertTrue(result.isLeft())
        assertEquals(RegisterError.EmptyEmail, result.leftOrNull())
    }

    @Test
    fun `validate should return success when input is valid`() = runTest {
        val input = RegisterInput(email = "test@gmail.com", password = "123456", confirmPassword = "123456")
        val result = validator.validate(input)

        assertTrue(result.isRight())
    }
}
```
