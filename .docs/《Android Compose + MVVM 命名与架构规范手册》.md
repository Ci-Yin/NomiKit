# 🧱 Android Compose + MVVM 命名与架构规范手册

> 适用于 Android + Kotlin Multiplatform + Jetpack Compose 项目
> 统一模块结构、命名风格、架构层次与文件组织。

---

## 一、模块与包命名结构

> 所有包名均使用 **小写 + 下划线分隔**（如有需要），避免大写或混合形式。
> 模块按职责划分，核心模块可跨功能共享。

```bash
com.ciyin.app
├── core/                          # ⚙️ 核心模块（全局可复用的基础能力）
│    ├── network/                  # 网络封装（Retrofit / Ktor 客户端、拦截器、响应模型）
│    ├── database/                 # 本地存储（Room、DataStore、Preferences）
│    ├── model/                    # 通用数据模型（如 ApiResponse、ErrorResponse）
│    ├── ui/                       # 通用可复用 Compose 组件（跨模块使用）
│    └── util/                     # 工具方法（日期、日志、验证、扩展函数等）
│
├── feature/                       # 🧩 功能模块（按业务域拆分，独立职责）
│    ├── home/                     # 示例：主页模块
│    ├── login/                    # 示例：登录模块
│    ├── profile/                  # 示例：用户信息模块
│    └── setting/                  # 示例：设置模块
│
├── ui/                            # 🎨 应用 UI 层入口（Compose 根结构）
│    ├── app/                      # 应用启动入口
│    │    └── App.kt               # Application 级入口（AppTheme 包裹 Material3）
│    │
│    ├── navigation/               # 导航定义（全局 NavHost、路由常量）
│    │    ├── AppNavGraph.kt
│    │    └── Route.kt
│    │
│    ├── component/                # 全局通用组件（非业务相关）
│    │    ├── Banner.kt
│    │    ├── Card.kt
│    │    ├── LoadingView.kt
│    │    └── EmptyState.kt
│    │
│    ├── screen/                   # 页面结构（按模块划分 UI 层）
│    │    ├── login/               # 登录模块 UI 层
│    │    │    ├── data/           # 数据层（Repository、Model）
│    │    │    │    ├── repository/
│    │    │    │    └── model/
│    │    │    ├── domain/         # 领域层（UseCase）
│    │    │    │    └── usecase/
│    │    │    ├── ui/             # UI 层（Compose 页面 + ViewModel）
│    │    │    │    ├── LoginScreen.kt
│    │    │    │    ├── LoginViewModel.kt
│    │    │    │    ├── LoginUiModel.kt     # ✅ UI 数据模型（界面状态）
│    │    │    │    └── component/          # 子组件（Compose）
│    │    │    │         ├── LoginForm.kt
│    │    │    │         ├── LoginButton.kt
│    │    │    │         └── LogoHeader.kt
│    │    │    └── di/                      # 模块依赖注入（Hilt / Koin）
│    │    │
│    │    └── home/                # 其他模块结构同上
│    │
│    └── theme/                    # 🌈 全局 Compose 主题系统
│         ├── Color.kt             # 颜色定义（Light/Dark 配色）
│         ├── Typography.kt        # 字体定义
│         ├── Shape.kt             # 圆角与形状
│         ├── Theme.kt             # AppTheme 封装入口
│         ├── DarkColorScheme.kt   # 暗色主题方案
│         └── LightColorScheme.kt  # 亮色主题方案
│
├── data/                          # 全局数据层（跨模块共享的数据访问层）
│    ├── repository/               # 通用仓库（账户信息、全局配置）
│    └── datasource/               # 通用数据源（网络 / 本地）
│
├── domain/                        # 全局领域逻辑层（跨模块 UseCase）
│    └── usecase/                  # 复用业务逻辑（如用户验证、缓存策略）
│
└── di/                            # 🧩 全局依赖注入定义（Hilt / Koin Modules）
     ├── AppModule.kt
     ├── NetworkModule.kt
     ├── RepositoryModule.kt
     └── UseCaseModule.kt
```

---

## 二、MVVM 架构命名规范

| 层级                    | 命名示例                                     | 说明                               |
|-----------------------|------------------------------------------|----------------------------------|
| **数据层 (Data)**        | `LoginRepository`, `UserLocalDataSource` | 封装网络与本地数据访问逻辑                    |
| **领域层 (Domain)**      | `LoginUseCase`, `FetchUserInfoUseCase`   | 业务逻辑与操作的封装层                      |
| **UI 层 (UI)**         | `LoginViewModel`, `LoginScreen`          | 页面与状态逻辑                          |
| **UI 数据模型 (UiModel)** | `LoginUiModel`, `UserUiModel`            | ✅ 仅供 UI 层使用的状态模型（包含展示数据、加载与错误状态） |
| **事件类 (UiEvent)**     | `LoginUiEvent`, `HomeUiEvent`            | UI 事件（如点击、刷新、滚动）                 |
| **组件 (Component)**    | `LoginForm`, `VipCard`, `BannerSection`  | 可复用的 Compose 子组件                 |
| **导航 (Navigation)**   | `AppNavGraph`, `HomeNavGraph`            | Compose Navigation 路由定义          |

---

## 三、Compose 命名规范

| 类型                | 命名规则              | 示例                                |
|-------------------|-------------------|-----------------------------------|
| **Composable 函数** | 首字母大写，语义化命名       | `LoginScreen()`, `BannerCard()`   |
| **状态类**           | 以 `UiModel` 结尾    | `HomeUiModel`, `PlayerUiModel`    |
| **事件类**           | 以 `UiEvent` 结尾    | `LoginUiEvent`, `ProfileUiEvent`  |
| **ViewModel**     | 模块名 + `ViewModel` | `LoginViewModel`, `HomeViewModel` |
| **子组件文件夹**        | `component/`      | 统一放小型可组合 UI 组件                    |
| **主题类**           | 以 `Theme` 结尾      | `AppTheme`, `PlayerTheme`         |
| **资源文件**          | 小写下划线分隔           | `ic_logo.png`, `bg_banner.jpg`    |

---

## 四、命名风格对照表

| 类型                | 风格                    | 示例                                       |
|-------------------|-----------------------|------------------------------------------|
| **包名 / 模块名**      | 全小写                   | `feature.login`, `core.network`          |
| **类名 / 接口名**      | 大驼峰（PascalCase）       | `LoginRepository`, `AppTheme`            |
| **函数名 / 方法名**     | 小驼峰（camelCase）        | `loadUserData()`, `onLoginClick()`       |
| **变量名**           | 小驼峰（camelCase）        | `userList`, `loginState`                 |
| **常量名**           | 全大写 + 下划线             | `DEFAULT_TIMEOUT`, `MAX_RETRY_COUNT`     |
| **枚举值**           | 每个单词首字母大写（PascalCase） | `NetworkType.Wifi`, `NetworkType.Mobile` |
| **文件名**           | 与类名一致                 | `LoginViewModel.kt`, `HomeScreen.kt`     |
| **资源名 (Compose)** | 小写 + 下划线              | `ic_launcher.xml`, `bg_main.jpg`         |

---

## 五、Compose + MVVM 协作关系图

```
┌──────────────────────────────┐
│         UI 层 (Compose)      │
│  LoginScreen.kt              │
│  └── 监听 ViewModel 状态流    │
│  └── 响应 UiEvent 事件        │
└───────────────▲──────────────┘
                │
     StateFlow / UiModel 更新
                │
┌───────────────┴──────────────┐
│      ViewModel 层 (逻辑层)   │
│  LoginViewModel.kt           │
│  └── 调用 UseCase 执行业务逻辑 │
│  └── 更新 UiModel 状态         │
└───────────────▲──────────────┘
                │
        调用仓库与领域层逻辑
                │
┌───────────────┴──────────────┐
│       领域层 (UseCase)       │
│  LoginUseCase.kt             │
│  └── 封装业务规则与操作逻辑   │
└───────────────▲──────────────┘
                │
       访问数据层（Repository）
                │
┌───────────────┴──────────────┐
│        数据层 (Repository)   │
│  LoginRepository.kt          │
│  └── 调用网络 / 数据源         │
└──────────────────────────────┘
```

---

## 六、文件组织建议

| 目录                         | 作用     | 示例文件                                                     |
|----------------------------|--------|----------------------------------------------------------|
| `data/repository/`         | 负责获取数据 | `LoginRepository.kt`, `UserRepository.kt`                |
| `domain/usecase/`          | 封装业务逻辑 | `LoginUseCase.kt`, `FetchUserProfileUseCase.kt`          |
| `ui/screen/.../ui/`        | 页面逻辑   | `LoginScreen.kt`, `LoginViewModel.kt`, `LoginUiModel.kt` |
| `ui/screen/.../component/` | UI 子组件 | `LoginForm.kt`, `LoginHeader.kt`                         |
| `core/ui/`                 | 通用组件库  | `LoadingView.kt`, `ErrorView.kt`                         |
| `ui/theme/`                | 主题与样式  | `Theme.kt`, `Color.kt`, `Typography.kt`                  |
| `ui/navigation/`           | 路由与导航图 | `AppNavGraph.kt`, `Route.kt`                             |

---
