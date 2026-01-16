太好了 👍 那我来为你整理出一份完整、现代化、清晰的规范文档：

---

# **Android 开发命名规范（Jetpack Compose + MVVM 架构）**

> 适用于 Kotlin + Compose 项目，统一命名风格、可读性与模块化设计。
> 重点覆盖：模块划分、包结构、Compose 组件、ViewModel、主题、状态、事件等。

---

## 🧱 一、模块与包命名规范

### **1. 模块命名**

采用小写 + 中划线分隔，体现功能边界：

```
:app
:core:network
:core:ui
:feature:home
:feature:player
:feature:profile
```

> ✅ 建议每个 `feature` 模块只负责单一页面或功能域。
> ✅ `core` 模块用于基础组件、通用工具或跨模块资源。

### **2. 单模块命名**

> 当前可以先在 `app` 模块下放置所有内容，每个功能对应一个子文件夹，未来可拆分为独立 Gradle
> 模块（上面第一点），下面的是简单的结构，具体看《Android Compose + MVVM 命名与架构规范手册》里的
`一、模块与包命名结构`。

```
app/
├── core/                   # ⚙️ 核心功能（全局可复用）
│    ├── network/           # 网络封装（Retrofit / Ktor）
│    ├── ui/                # 通用 Compose 组件
│    ├── database/          # 本地存储（Room / DataStore）
│    └── util/              # 工具类（扩展、日志、校验等）
│
├── feature/                # 🧩 功能模块（按业务域划分）
│    ├── home/              # 首页功能
│    ├── login/             # 登录功能
│    ├── profile/           # 用户信息功能
│    └── player/            # 播放器功能
│
└── ui/                     # 🎨 UI 层入口（Compose）
     ├── app/               # 应用入口文件
     │    └── App.kt
     ├── component/         # 全局通用组件
     ├── screen/            # 页面结构（按 feature 子文件夹划分）
     └── theme/             # Compose 主题系统
```

> ✅ **优点**：
>
> * 可以先在 `app` 模块下集中管理所有代码，方便启动项目；
> * 仍然保留 **按 feature 子文件夹划分**，便于未来拆分独立模块；
> * 核心模块（core）与 UI、工具保持统一命名和职责边界。

---

### **3. 包命名**

全部小写、用点号分隔，结构清晰：

```
org.ciyin.app           // 应用入口
org.ciyin.ui       // 通用 Compose 组件
org.ciyin.feature.home  // 首页模块
org.ciyin.feature.player // 播放模块
```

> ❌ 禁止包名使用大写字母或下划线。
> ✅ 每个 feature 模块下建议分为：

```
ui/          // Compose 界面
model/       // 数据类与UI状态
viewmodel/   // 业务逻辑与状态管理
navigation/  // 路由或导航定义
```

---

## 🎨 二、Compose 命名规范

### **1. 组件函数（@Composable）**

* 使用 PascalCase（首字母大写）
* 结尾一般为 `Screen`、`Page`、`Content`、`Section`、`Item`

```kotlin
@Composable
fun HomeScreen()

@Composable
fun VideoPlayerPage()

@Composable
fun UserProfileContent()
```

> ✅ `Screen` 表示独立页面
> ✅ `Content` 表示局部内容块
> ✅ `Item` 表示列表项
> ✅ 避免使用 `Composable` 作为后缀

---

### **2. 状态类命名**

UI 状态类使用 `xxxUiState` 后缀，属性名采用驼峰命名：

```kotlin
data class HomeUiState(
    val isLoading: Boolean = false,
    val videoList: List<Video> = emptyList(),
    val errorMessage: String? = null
)
```

---

### **3. 事件命名**

事件封装类使用 `xxxEvent` 后缀，枚举式命名：

```kotlin
sealed interface HomeEvent {
    data object LoadVideos : HomeEvent
    data class ShowError(val message: String) : HomeEvent
}
```

---

### **4. ViewModel 命名**

ViewModel 以 `xxxViewModel` 结尾：

```kotlin
class HomeViewModel(
    private val repository: HomeRepository
) : ViewModel() { ... }
```

> ✅ ViewModel 中只暴露 `UiState` 与 `Event`
> ✅ 函数命名动词化，如：

```kotlin
fun loadVideos()
fun onVideoClick(videoId: String)
fun retryLoad()
```

---

## ⚙️ 三、ViewModel + StateFlow 示例

### **1. ViewModel 命名**

```kotlin
class HomeViewModel(
    private val repository: HomeRepository
) : ViewModel() { ... }
```

### **2. 状态管理示例（MutableStateFlow）**

```kotlin
class HomeViewModel(
    private val repository: HomeRepository
) : ViewModel() {

    // 私有可变状态流
    private val _uiState = MutableStateFlow(HomeUiState())
    // 公开只读状态流
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun loadVideos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val videos = repository.getVideos()
                _uiState.update { it.copy(isLoading = false, videoList = videos) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun onVideoClick(videoId: String) { /* ... */ }
}
```

### **3. Compose 层使用**

```kotlin
@Composable
fun HomeScreen(viewModel: HomeViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()

    HomeContent(
        videoList = state.videoList,
        isLoading = state.isLoading,
        onVideoClick = { id -> viewModel.onVideoClick(id) }
    )
}
```

> ✅ 使用 `collectAsState()` 在 Compose 中观察 StateFlow
> ✅ ViewModel 内部只暴露 **只读 StateFlow**，保证不可变性

---

## 🎨 四、Compose 主题规范

### **1. 主题文件命名**

所有主题相关文件放在 `ui/theme` 包中：

```
Color.kt
Type.kt
Shape.kt
Theme.kt
```

---

### **2. 颜色命名**

遵循语义化命名（非品牌色值命名）：

```kotlin
val Primary = Color(0xFF1E88E5)
val PrimaryDark = Color(0xFF1565C0)
val Secondary = Color(0xFF03DAC5)
val Background = Color(0xFFFFFFFF)
val OnBackground = Color(0xFF000000)
```

> ✅ 语义化而非色值化（不要用 `Blue500`、`Gray100`）。
> ✅ 统一用 `PascalCase` 命名颜色变量。

---

### **3. 字体与形状**

```kotlin
val AppTypography = Typography(
    bodyLarge = TextStyle(fontSize = 16.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.Bold)
)

val AppShapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(12.dp)
)
```

---

### **4. Theme 入口定义**

```kotlin
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
```

---

## ⚙️ 五、枚举与常量命名

### **1. 枚举命名**

每个单词首字母大写（PascalCase）：

```kotlin
enum class PlayerState {
    Idle,
    Loading,
    Playing,
    Paused,
    Error
}
```

> ✅ 枚举名用名词，值用状态词。
> ❌ 不要全大写（如 `IDLE`、`PLAYING`）。

---

### **2. 常量命名**

使用 `UPPER_SNAKE_CASE`，放在 `object` 或 `companion object` 中：

```kotlin
object Config {
    const val DEFAULT_PAGE_SIZE = 20
    const val TIMEOUT_MS = 5000L
}
```

---

## 🧩 六、MVVM 层次规范

| 层级          | 职责        | 命名后缀                 | 示例               |
|-------------|-----------|----------------------|------------------|
| UI（Compose） | 界面展示、事件收集 | `Screen` / `Content` | `HomeScreen`     |
| ViewModel   | 状态管理、业务逻辑 | `ViewModel`          | `HomeViewModel`  |
| UiState     | 状态数据模型    | `UiState`            | `HomeUiState`    |
| Event       | 用户行为或单次事件 | `Event`              | `HomeEvent`      |
| Repository  | 数据来源抽象    | `Repository`         | `UserRepository` |

---

## ✅ 七、示例整合

```kotlin
// HomeUiState.kt
data class HomeUiState(
    val isLoading: Boolean = false,
    val videos: List<Video> = emptyList()
)

// HomeEvent.kt
sealed interface HomeEvent {
    data object LoadVideos : HomeEvent
    data class ShowToast(val message: String) : HomeEvent
}

// HomeViewModel.kt
class HomeViewModel(
    private val repository: HomeRepository
) : ViewModel() {
    var uiState by mutableStateOf(HomeUiState())
        private set

    fun loadVideos() { /* ... */ }
    fun onVideoClick(id: String) { /* ... */ }
}

// HomeScreen.kt
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel()
) {
    val state = viewModel.uiState
    HomeContent(state)
}
```

# 八、Compose 命名反例 vs 推荐写法对照表

---

## 1️⃣ Compose 组件函数命名

| ❌ 不推荐                | ✅ 推荐                  | 说明                                                         |
|----------------------|-----------------------|------------------------------------------------------------|
| `fun home_screen()`  | `fun HomeScreen()`    | ❌ 下划线 + 小写不符合 Compose 函数规范；✅ PascalCase 可读性高，符合 Compose 风格 |
| `fun showbanner()`   | `fun BannerSection()` | ❌ 动词开头语义不清；✅ 名词 + Section/Item 明确组件用途                      |
| `fun userform()`     | `fun UserForm()`      | ❌ 小写驼峰，界面组件易混淆；✅ PascalCase，易于识别 Compose 组件                |
| `fun screen_login()` | `fun LoginScreen()`   | ❌ 下划线命名，不直观；✅ 首字母大写 + Screen 后缀清晰                          |

---

## 2️⃣ ViewModel / UiState / Event 命名

| ❌ 不推荐                     | ✅ 推荐                          | 说明                                               |
|---------------------------|-------------------------------|--------------------------------------------------|
| `class loginmodel`        | `class LoginViewModel`        | ❌ 小写 + 模糊，非标准 ViewModel；✅ 模块名 + ViewModel 后缀清楚职责 |
| `data class loginState`   | `data class LoginUiState`     | ❌ 小写 + 不清楚用途；✅ UiState 后缀表明是 UI 状态模型             |
| `sealed class loginevent` | `sealed interface LoginEvent` | ❌ 小写 + 类名不规范；✅ PascalCase + Event 后缀，明确事件角色      |
| `fun load()`              | `fun loadUserData()`          | ❌ 动作不明确；✅ 方法名语义清晰，指明操作对象                         |

---

## 3️⃣ 颜色 / 主题 / Typography 命名

| ❌ 不推荐                             | ✅ 推荐                                     | 说明                                  |
|-----------------------------------|------------------------------------------|-------------------------------------|
| `val blue500 = Color(0xFF2196F3)` | `val Primary = Color(0xFF2196F3)`        | ❌ 数值化命名，语义不清；✅ 语义化命名，易理解用途          |
| `val dark_bg = Color(0xFF000000)` | `val BackgroundDark = Color(0xFF000000)` | ❌ 下划线 + 不标准；✅ PascalCase + 明确用途     |
| `val font1 = TextStyle(...)`      | `val TitleMedium = TextStyle(...)`       | ❌ 无语义，维护困难；✅ 按 Typography 类型命名，语义清晰 |

---

## 4️⃣ 包名与模块名

| ❌ 不推荐                    | ✅ 推荐                      | 说明                                 |
|--------------------------|---------------------------|------------------------------------|
| `org.ciyin.FeatureLogin` | `org.ciyin.feature.login` | ❌ 大写 + 驼峰，不符合包名规范；✅ 小写 + 点分隔，按模块划分 |
| `org.ciyin.UiComponent`  | `org.ciyin.ui`            | ❌ 模糊，跨模块使用不明；✅ core.ui 表示通用组件      |

---

## 5️⃣ 枚举与常量命名

| ❌ 不推荐                                       | ✅ 推荐                                                 | 说明                            |
|---------------------------------------------|------------------------------------------------------|-------------------------------|
| `enum class player_state { idle, loading }` | `enum class PlayerState { Idle, Loading }`           | ❌ 小写 + 下划线；✅ 每个单词首字母大写，清晰明了   |
| `const val timeout = 5000L`                 | `const val TIMEOUT_MS = 5000L`                       | ❌ 小写 + 无单位；✅ 全大写 + 下划线 + 单位清楚 |
| `object config { val pagesize = 20 }`       | `object Config { const val DEFAULT_PAGE_SIZE = 20 }` | ❌ 属性名不规范；✅ 全局常量规范，易维护         |

---

✅ **小结**：

* Compose 函数：PascalCase + 名词 + Screen/Content/Item
* ViewModel/UiState/Event：模块名 + 后缀
* 颜色/字体/形状：语义化 + PascalCase
* 枚举值：每个单词首字母大写
* 常量：UPPER_SNAKE_CASE

---


