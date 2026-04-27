---
name: data-domain
description: Scaffold and review Data/Domain layer code for the KMP project (com.ciyin.app). Enforces the project's layered architecture conventions - DataError in Data, scenario errors XxxError + UseCase in Domain, Mapper for cross-layer boundaries, Koin module wiring. Use when adding/modifying any feature module under app/shared/src/commonMain/kotlin/{data,domain}/<feature>/, when creating Repository/UseCase/Mapper/DataSource, when wiring Koin DI for a feature, or when the user mentions data 层 / domain 层 / Repository / UseCase / DataError / 场景错误.
---

# Data/Domain Layered Skill

本 skill 用于在（KMP，包名 `com.ciyin.app.*`）项目中按既定架构生成或评审 `data` / `domain`
层代码。所有产物必须可直接被 `app/shared` 编译通过，并能用现有的 `koin-boot` 自动装配。

## 何时使用

- 新增一个业务模块（feature）：需要同时建 `data/<feature>/` 与 `domain/<feature>/`。
- 给现有模块新增能力：新增 `UseCase`、`Repository` 方法、场景错误。
- 评审 PR：判断是否违反 Data 层只产 `DataError`、Domain 层负责映射场景错误、Mapper 隔离、不可越层等约束。

## 硬性约束（必须遵守）

1. **路径与包名**
    - Data 层：`app/shared/src/commonMain/kotlin/data/<feature>/`，包名 `com.ciyin.app.data.<feature>`
    - Domain 层：`app/shared/src/commonMain/kotlin/domain/<feature>/`，包名
      `com.ciyin.app.domain.<feature>`
    - Domain **不依赖** Data；Data 只能依赖 Domain。UI 层只依赖 Domain。

2. **错误模型**
    - Data 层只产 `ciyin.business.base.error.DataError`（不许出现 UI/场景错误）。
    - Domain 层定义场景错误 `XxxError`（`sealed class`，每条带中文 `message`），简单场景下可不新建自定义错误类，直接使用
      `GenericError`。
    - 兜底：未识别的 `DataError` → `XxxError.Generic(toGenericError())`。
    - 已有基础设施：`DataError`、`GenericError`、`DataError.toGenericError()` 在 `business.base.error`
      包内，**不要重新定义**。

3. **Either / Arrow**
    - Repository / UseCase 一律返回 `Either<错误, 结果>`。
    - 实现内部使用 `arrow.core.raise.either { ... bind() ... }` 或
      `Either.catch { }.mapLeft { ... }`。
    - 网络请求统一通过 `ApiClient.safeCallWithData<T> { post(...) { ... } }` 拿到
      `Either<DataError, T>`，**不要**在业务代码里手写 `try/catch` 包网络异常。

4. **KDoc**：`class/interface/object/enum`、方法、属性必须用中文 `/** ... */` KDoc，不得用 `//` 替代。

5. **不许越层**
    - UI 层不渲染 Domain Model（必要时用 UiModel/UiItem）。
    - Data 层不直接传 Domain Model 给网络/数据库（用 `ApiData` / `Entity` + Mapper）。
    - 为了减少不必要的 Model ，允许 Data 层的 Repository 返回 Domain Model。

## 标准目录与命名

```
data/<feature>/
├── <Feature>Repository.kt              // 仓库
├── api/<Feature>Api.kt                 // 继承 ApiClient
├── datasource/
│   ├── <Feature>Dao.kt                 // Room（@Dao）
│   ├── <Feature>DataStore.kt           // androidx.datastore
│   └── <Feature>LocalDataSource.kt     // 包装上面三类之一/多
├── model/
│   ├── <Xxx>ApiRequest.kt              // @Serializable，请求体
│   ├── <Xxx>ApiData.kt                 // @Serializable，响应体
│   ├── <Xxx>Entity.kt                  // @Entity，DB 实体
│   └── <Feature>Preferences.kt         // @Serializable，DataStore 持久化
├── mapper/<Feature>ApiMapper.kt        // toXxxApiRequest / toDomain / toEntity
└── di/<Feature>DataModule.kt

domain/<feature>/
├── model/                              // <Xxx>Input / <Xxx>Result / <Xxx>Domain
├── error/<UseCaseName>Error.kt         // sealed class
├── usecase/<Verb><Subject>UseCase.kt
├── util/                              // 可选
└── di/<Feature>DomainModule.kt
```

命名后缀强约束：

| 类型         | 后缀               | 示例                                                                     |
|------------|------------------|------------------------------------------------------------------------|
| Repository | `Repository`     | `AuthRepository`                                                       |
| Room       | `Entity`         | `AuthInfoEntity`                                                       |
| DataStore  | `DataStore`      | `AuthDataStore`                                                        |
| Room 实体    | `Entity`         | `InstalledAppInfoEntity`                                               |
| 网络请求体      | `ApiRequest`     | `LoginApiRequest`                                                      |
| 网络响应体      | `ApiData`        | `LoginApiData`                                                         |
| Domain 入参  | `Input`          | `LoginInput`                                                           |
| Domain 出参  | `Result` 或 其它    | `LoginResult` / `Banner`                                               |
| UseCase    | `UseCase`，动词在前   | `LoginUserUseCase`、`GetBannersUseCase`、`ObserveCurrentUserInfoUseCase` |
| 场景错误       | `<UseCase>Error` | `LoginError`、`GetBannersError`                                         |

Mapper 函数命名：`toXxxApiRequest()`、`toDomain()`、`toEntity()`、`toXxxApiData()`，以扩展函数实现。

## 工作流（新增一个 feature 端到端）

按下列顺序生成；每步完成后再进入下一步。先 Data ，再 Domain ，最后 DI（装配）。

```
任务进度：
- [ ] 1. Domain：定义 Input / Result / Domain Model
- [ ] 2. Domain：定义 Repository（Either<DataError, T>）
- [ ] 3. Domain：定义场景错误 XxxError
- [ ] 5. Domain：定义 UseCase
- [ ] 6. Domain：写 <Feature>DomainModule
- [ ] 7. Data：定义 ApiRequest / ApiData / Entity / Preferences（按需）
- [ ] 8. Data：定义 Api / Entity / DataStore / LocalDataSource（按需）
- [ ] 9. Data：写 Mapper（toXxxApiRequest / toDomain / toEntity）
- [ ] 10. Data：写 Repository
- [ ] 11. Data：写 <Feature>DataModule
- [ ] 12. 在 app/shared/src/commonMain/kotlin/com/ciyin/app/di/ 下添加聚合的模块
```

## 关键模板

### 2. Domain：场景错误

```kotlin
package com.ciyin.app.domain.<feature > . error

        import ciyin . business . base . error . DataError
        import ciyin . business . base . error . GenericError
        import ciyin . business . base . error . toGenericError

        /**
         * <UseCase> 的场景错误。
         */
        sealed class <UseCase>Error(open val message: String) {
    /** 已知业务错误举例 */
    data object SomeBusinessCase : <UseCase>Error("提示文案")
}
```

> 简单场景下可不新建自定义错误类，直接使用 `GenericError`，`DataError.toGenericError()`。

### 4. Domain：UseCase

```kotlin
package com.ciyin.app.domain.<feature > . usecase

        import arrow . core . Either
        import arrow . core . raise . either
        import ciyin . business . base . error . DataError
        import com . ciyin . app . domain .<feature>.< Feature > Repository
        import com . ciyin . app . domain .<feature>.error.< UseCase > Error
        import com . ciyin . app . domain .<feature>.error.to<UseCase> Error
        import com . ciyin . app . domain .<feature>.model.< XxxInput >
import com . ciyin . app . domain .<feature>.model.< XxxResult >

/**
 * <一句话用例描述>。
 */
class <Verb><Subject>UseCase(
private val repository: <Feature>Repository,
) {
    suspend operator fun invoke(input: <XxxInput>): Either<<UseCase>Error, <XxxResult>> = either {
    repository.< verb >(input)
            .mapLeft { it.to<UseCase> Error () }
        .bind()
}
}
```

订阅型 UseCase（暴露 cold Flow，VM 用 `stateIn` 共享）：

```kotlin
class Observe<Subject>UseCase(
private val repository: <Feature>Repository,
) {
    operator fun invoke(): Flow<<Domain>> = repository.observe<Subject>()
}
```

### 5. Domain：DI Module

```kotlin
package com.ciyin.app.domain.<feature > . di

        import com . ciyin . app . domain .<feature>.usecase.< Verb ><Subject> UseCase
        import org . koin . core . module . dsl . singleOf
        import org . koin . dsl . module

val <Feature> DomainModule = module {
    singleOf(::<Verb><Subject> UseCase)
}
```

### 6. Data：Api（继承 `ApiClient`）

```kotlin
package com.ciyin.app.data.<feature > . api

        import arrow . core . Either
        import ciyin . business . base . data . ApiClient
        import ciyin . business . base . error . DataError
        import io . ktor . client . HttpClient
        import io . ktor . client . request . post
        import io . ktor . client . request . setBody
        import io . ktor . http . ContentType
        import io . ktor . http . contentType

/**
 * <feature> 相关网络接口。
 */
class <Feature>Api(client: HttpClient) : ApiClient(client) {

    /**
     * <接口语义>。
     */
    suspend fun <verb>(request: <Xxx>ApiRequest): Either<DataError, <Xxx>ApiData> =
    safeCallWithData {
        post("/v1/...") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }
}
```

> **绝不在 `<Feature>Api` 里手写 `try/catch`**，统一用 `safeCall` / `safeCallWithData`。

#### `safeCall` vs `safeCallWithData` 选用规则

`ApiClient` 提供两种统一封装，用法不同，**不要混用**：

- **`safeCallWithData<T> { ... }: Either<DataError, T>`**
    - 适用：服务端返回 **统一信封 `ApiResponse<T>`**（即 `{ code, message, data: T }` 这种结构）。
    - 内部：先做 `safeCall` 拿到 `HttpResponse`，再调用 `response.unwrapApiData<T>()`：
        - `body<ApiResponse<T>>().data` 取出 `data` 字段直接返回。
        - 当 `T = Unit` 时返回 `Unit`。
        - HTTP 状态码 / 业务状态码的校验由全局 `installApiValidator()` 在响应链中完成；如未识别会抛
          `ApiException`，被映射为 `DataError.Network.Http(code, message, cause)`。
    - 序列化失败 → `DataError.Serialization`；其它异常 → `DataError.Unknown`。

- **`safeCall { ... }: Either<DataError, HttpResponse>`**
    - 适用：**非信封响应**（裸 JSON、文件流、二进制、第三方 API 等）。
    - 拿到 `HttpResponse` 后由调用方自行 `body<T>()` / `bodyAsChannel()`；其中可能抛出的反序列化异常需要自己再用
      `Either.catch { }.mapLeft { ... }` 转换成 `DataError`。

> 强提示：项目自有后端默认走信封，**优先用 `safeCallWithData`**；只有当确认接口确实不带信封时才改用
`safeCall`，并在该方法 KDoc 中注明原因。

### 7. Data：本地数据源

- **Room**：直接 `@Dao interface XxxDao { ... }`，方法返回 `suspend` 或 `Flow`。在 DataModule 用
  `singleDao(AppDatabase::xxxDao)` 注册。

- **DataStore**（结构化数据）：

```kotlin
class <Feature>DataStore(
dataStoreFactory: DataStoreFactory,
) : DataStore<<Feature>Preferences> by dataStoreFactory.create(
defaultValue = < Feature > Preferences ()
)
```

### 8. Data：Mapper

```kotlin
package com.ciyin.app.data.<feature > . mapper

        /** Domain → ApiRequest */
        fun <XxxInput> .to<Xxx> ApiRequest (): <Xxx>ApiRequest = <Xxx>ApiRequest(...)

/** ApiData → Domain */
fun <Xxx> ApiData.toDomain(): <XxxResult> = <XxxResult>(...)

/** Domain ↔ Entity */
fun <Xxx> Domain.toEntity(): <Xxx>Entity = <Xxx>Entity(...)
fun <Xxx> Entity.toDomain(): <Xxx>Domain = <Xxx>Domain(...)
```

### 9. Data：Repository

```kotlin
package com.ciyin.app.data.<feature >

        import arrow . core . Either
        import arrow . core . raise . either
        import ciyin . business . base . error . DataError
        import com . ciyin . app . data .<feature>.api.< Feature > Api
        import com . ciyin . app . data .<feature>.mapper.toDomain
import com . ciyin . app . data .<feature>.mapper.to<Xxx> ApiRequest
        import com . ciyin . app . domain .<feature>.model.< XxxInput >
import com . ciyin . app . domain .<feature>.model.< XxxResult >

/**
 * <Feature>Repository 的 Data 层实现。
 */
class <Feature>Repository(
private val api: <Feature>Api,
) {

    suspend fun <verb>(input: <XxxInput>): Either<DataError, <XxxResult>> = either {
    val apiData = api.< verb >(input.to<Xxx> ApiRequest ()).bind()
    apiData.toDomain()
}
}
```

涉及本地异常需要捕获的路径，使用
`Either.catch { }.mapLeft { /* DataError.Persistence / DataError.Unknown(it) */ }`。

### 10. Data：DI Module

```kotlin
package com.ciyin.app.data.<feature > . di

        import ciyin . component . room . singleDao
        import com . ciyin . app . data .<feature>.< Feature > Repository
        import com . ciyin . app . data .<feature>.api.< Feature > Api
        import com . ciyin . app . data .<feature>.datasource.< Feature > DataStore
        import com . ciyin . app . data .<feature>.datasource.< Feature > LocalDataSource
        import com . ciyin . app . data . core . datasource . AppDatabase
        import org . koin . core . module . dsl . singleOf
        import org . koin . dsl . module

val <Feature> DataModule = module {
    singleOf(::<Feature> Api)
    singleOf(::<Feature> DataStore)
    singleOf(::<Feature> LocalDataSource)
    singleDao(AppDatabase::<feature> Dao)               // 仅当用了 Room
    singleOf(::<Feature> Repository)
}
```

### 11. 聚合 Initializer

> **重要**：项目已经在 `app/shared/src/commonMain/kotlin/com/ciyin/app/di/modules/Modules.kt` 中预留了
> `MainModules` / `SettingsModules` 两个 `KoinBootInitializer` 占位（当前里面是注释行 `//
> modules(MainDomainModule)`），并在 `KoinInitializer.kt::initKoin` 里被显式调用。新增 feature 时应*
*优先复用这些占位**，而不是再创建一个新的全局 initializer，否则 `initKoin` 还要同步增改。
>
> 选择规则：
> - **如果新 feature 属于 `Main`/`Settings` 等已有占位的语义范畴** → 直接把模块塞进对应的
    initializer。
> - **如果是全新业务域**（且没有合适的占位）→ 才新增一个 `<Feature>Modules`，并在
    `KoinInitializer.kt::initKoin` 里追加调用。

#### 写法 A：复用现有占位（**推荐**）

```kotlin
// app/shared/src/commonMain/kotlin/com/ciyin/app/di/modules/Modules.kt

import ciyin.koin.KoinBootInitializer
import com.ciyin.app.data.core.di.AppDataModule
import com.ciyin.app.data.<feature > . di .<Feature> DataModule
        import com . ciyin . app . domain .<feature>.di.< Feature > DomainModule

val AppModules: KoinBootInitializer = {
    modules(AppDataModule)
}

val MainModules: KoinBootInitializer = {
    modules(< Feature > DataModule, <Feature>DomainModule)   // ← 把 feature 挂到这里
}

val SettingsModules: KoinBootInitializer = {
    // modules(SettingsDataModule)
}
```

`KoinInitializer.kt` **无需任何改动**，因为它本来就调用了 `MainModules()` / `SettingsModules()`：

```kotlin
fun initKoin(context: Context) {
    runKoinBoot {
        AppBootInitializer()
        AppModules()
        MainModules()       // ← 已在调用，feature 自动生效
        SettingsModules()
        appDeclaration {
            koin.declare(context, secondaryTypes = listOf(Context::class))
        }
    }
}
```

#### 写法 B：新增全局 initializer（仅在没有合适占位时用）

```kotlin
// app/shared/src/commonMain/kotlin/com/ciyin/app/di/modules/Modules.kt
val <Feature> Modules: KoinBootInitializer = {
    modules(< Feature > DataModule, <Feature>DomainModule)
}

// app/shared/src/commonMain/kotlin/com/ciyin/app/di/KoinInitializer.kt
fun initKoin(context: Context) {
    runKoinBoot {
        AppBootInitializer()
        AppModules()
        MainModules()
        SettingsModules()
        <Feature > Modules()      // ← 必须同步追加调用，否则不生效
        appDeclaration { /* ... */ }
    }
}
```

> **反模式**：定义了 `<Feature>Modules` 却忘了在 `initKoin` 中调用——Koin 容器里查不到对应实例，运行期才会报
`NoBeanDefinitionFoundException`。新增 initializer 必须**同一 PR 内**改 `KoinInitializer.kt`。

## 评审清单（Review）

收到对 `data/` 或 `domain/` 的改动时，按下列项检查；任一失败必须指出并要求修正。

- [ ] 路径与包名是否符合 `data/<feature>` / `domain/<feature>` 与
  `com.ciyin.app.{data,domain}.<feature>`
- [ ] `data/` 是否引用了 `domain.error.XxxError` —— **禁止**
- [ ] `domain/` 是否引用了 `data.*` 类（`ApiData`/`Entity`/`Dao`）—— **禁止**
- [ ] 所有可失败方法是否返回 `Either<错误, T>`，是否有人写 `throw`/裸 `try-catch` 吞异常
- [ ] 网络是否走 `ApiClient.safeCall(WithData)`，未自行包 `try/catch`
- [ ] 场景错误是否提供了 `fun DataError.toXxxError()` 映射函数
- [ ] 是否每个 `class/interface/object/enum`、方法、属性都有中文 KDoc
- [ ] 数据源命名是否对应：Room→`Dao`，DataStore→`DataStore`
- [ ] 跨层数据是否通过 Mapper 转换（`toXxxApiRequest`/`toDomain`/`toEntity`）
- [ ] `<Feature>DomainModule` 与 `<Feature>DataModule` 是否都建好，并通过
  `<Feature>Modules: KoinBootInitializer` 聚合
- [ ] 是否触发了"亡羊补牢"反模式：`try-catch` 吞错、`if (x != null)` 静默过滤、`delay` 解决时序、写死 UI
  高度（详见 AGENTS.md 第十节）

## 反模式（直接拒绝）

- ❌ 在 `data/` 中产出 `LoginError`、`RegisterError` 这种场景错误。
- ❌ 在 `domain/` 中 `import com.ciyin.app.data.*`。
- ❌ Repository 实现里直接 `throw DataError.Unknown(...)`（应 `raise(...)` 或返回 `Either.Left`）。
- ❌ Api 类里手写 `try { ... } catch (e: Exception) { ... }`。
- ❌ ViewModel 直接调用 `Repository`/`Dao`（必须经 UseCase）。
- ❌ Domain 暴露 `kotlinx.serialization` / `androidx.room` / `ktor` 类型。
- ❌ 重新声明 `DataError` / `GenericError`（已存在于 `business.base.error`）。
- ❌ UseCase 内自启 `CoroutineScope` 收集 Flow（应返回 cold Flow，由 VM `stateIn`）。

## 速查：可直接复用的现成符号

- `ciyin.business.base.error.DataError`
- `ciyin.business.base.error.GenericError`
- `ciyin.business.base.error.toGenericError`
- `ciyin.business.base.data.ApiClient`（提供 `safeCall` / `safeCallWithData`）
- `ciyin.room.singleDao`
- `ciyin.datastore.DataStoreFactory`
- `ciyin.koin.KoinBootInitializer`
- 数据库聚合类：`com.ciyin.app.data.core.AppDatabase`
