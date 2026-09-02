---
name: data-domain
description: Use when adding or reviewing KMP app/shared data or domain code, including Repository, UseCase, Mapper, DataError, scenario errors, API, Room, DataStore, or feature DI wiring.
---

# Data/Domain Layer

本 skill 约束 app/shared 的 Data/Domain 边界。它只描述当前 NomiKit 的可复用约定；具体
Room、DataStore、FlowRedux2 和 UI 页面细节必须继续遵循对应的 skill。

## 何时使用

- 新增或修改 app/shared/src/commonMain/kotlin/com/ciyin/app/data/<feature>/。
- 新增或修改 app/shared/src/commonMain/kotlin/com/ciyin/app/domain/<feature>/。
- 创建或评审 Repository、DataSource、Api、Mapper、UseCase、Validator、场景错误或 Koin 模块。
- 处理 DataError、Either、本地持久化、网络响应或 Data/Domain 越层问题。

不适用于只修改 Compose 布局、通用 UI 基建或 NomiKit 公共 feature/* 模块；这些任务使用对应
的 UI 或模块 skill。

## 核心边界

依赖方向应保持为：

~~~text
UI/ViewModel -> Domain 契约与用例
Data 实现   -> Domain 契约 + business.base + Ktor/Room/DataStore 等基础设施
Domain      -> business.base 中的通用模型/错误，不依赖 app 的 Data 实现
~~~

- Domain 只定义业务模型、Repository 接口、Validator、UseCase 和场景错误。
- Data 负责网络、本地存储、异常归一化、ApiData/Entity 与 Domain Model 的转换。
- UI 不直接调用 Repository、Dao、Api 或 DataStore。
- Data Repository 可以返回 Domain Model；但 Api、Dao、DataStore 不得接收或返回 Domain Model。
- DAO/DataStore 内部可以保留 Room/DataStore 的原生返回类型；Repository 边界对一次性操作或
  选择了流内错误值的订阅，归一化为 DataError/Either。采用 Flow<T> 的普通状态观察则保留
  终止性异常在流外表达，不能要求每个底层方法都伪装成 Either。
- Domain 不得暴露 ktor、androidx.room、androidx.datastore 或 kotlinx.serialization 类型。

## 目录与命名

实际根路径必须包含 com/ciyin/app：

~~~text
app/shared/src/commonMain/kotlin/com/ciyin/app/
├── domain/<feature>/
│   ├── model/
│   ├── repository/<Feature>Repository.kt
│   ├── error/<UseCase>Error.kt
│   ├── usecase/<Verb><Subject>UseCase.kt
│   ├── util/
│   └── di/<Feature>DomainModule.kt
└── data/<feature>/
    ├── api/<Feature>Api.kt
    ├── datasource/
    │   ├── <Feature>Dao.kt
    │   ├── <Feature>DataStore.kt
    │   └── <Feature>LocalDataSource.kt
    ├── model/
    │   ├── <Xxx>ApiRequest.kt
    │   ├── <Xxx>ApiData.kt
    │   ├── <Xxx>Entity.kt
    │   └── <Feature>Preferences.kt
    ├── mapper/<Feature>Mapper.kt
    ├── repository/<Feature>RepositoryImpl.kt
    └── di/<Feature>DataModule.kt
~~~

约定：

| 类型 | 约定 | 示例 |
| --- | --- | --- |
| Repository 接口 | <Feature>Repository | ProfileRepository |
| Repository 实现 | <Feature>RepositoryImpl | ProfileRepositoryImpl |
| Room DAO | <Feature>Dao | ProfileDao |
| Room 实体 | <Xxx>Entity | ProfileEntity |
| DataStore | <Feature>DataStore | ProfileDataStore |
| 网络请求/响应 | ApiRequest / ApiData | LoginApiRequest |
| Domain 入参 | Input | LoginInput |
| UseCase | 动词在前并以 UseCase 结尾 | GetProfileUseCase |
| 场景错误 | 按 UseCase 命名 | GetProfileError |

Mapper 使用明确的扩展名，例如 toProfileApiRequest()、toDomain()、toEntity()；不要用一个
隐含多层转换的万能 Mapper。

## 错误模型

DataError 只表示跨功能复用的技术失败。当前实现位于
ciyin.business.base.error.DataError，不要重新声明。Data 层不得定义 LoginError、RegisterError
等场景错误。

Domain 为每个需要业务分支的 UseCase 定义类型化错误。错误类型不携带用户可见中文文案；UI
根据错误类型映射到 app/shared/src/commonMain/composeResources/values/strings.xml。

~~~kotlin
package com.ciyin.app.domain.profile.error

import ciyin.business.base.error.DataError

/** 获取资料时可能出现的场景错误。 */
sealed interface GetProfileError {
    /** 当前会话未授权。 */
    data object Unauthorized : GetProfileError

    /** 未分类的底层错误。 */
    data class Unknown(val cause: DataError) : GetProfileError
}

/** 将数据层错误转换为获取资料场景错误。 */
fun DataError.toGetProfileError(): GetProfileError = when (this) {
    is DataError.Network.Http -> when (code) {
        401 -> GetProfileError.Unauthorized
        else -> GetProfileError.Unknown(this)
    }
    DataError.Network.Unauthorized -> GetProfileError.Unauthorized
    else -> GetProfileError.Unknown(this)
}
~~~

极简场景可以在 Domain 内部使用 GenericError 或 DataError.toGenericError()，但不得写成不存在的
XxxError.Generic(...)。GenericError 当前只有 GenericError.Failed，它是通用兜底，不是业务错误分类。
DataError.message 和 GenericError.message 只能用于日志/诊断；UI 不得直接渲染它们，未知错误必须
映射到统一的 Compose Resources 兜底文案。

## Either 与 Flow

### 一次性操作

- Repository 一次性方法返回 Either<DataError, T>。
- UseCase 将左值映射为 Either<场景错误, T>。
- API 使用 ApiClient.safeCallWithData<T> 或 safeCall，不在 API 方法里重复包网络 try/catch。
- 本地读写异常必须转换成 DataError.Persistence 或 DataError.Unknown。
- 使用 Either.catch 时必须保留 CancellationException，不能把协程取消转换成业务错误。
- 当前 ApiClient.safeCall 和 safeCallWithData 内部使用 Either.catch，取消传播并不安全；需要取消
  语义时，先修复/替换共享 ApiClient，再在 feature 中使用，不能在 skill 中声称它已经安全。

~~~kotlin
import arrow.core.Either
import ciyin.business.base.error.DataError
import kotlinx.coroutines.CancellationException

/** 将可失败的数据操作转换为 DataError，同时保留协程取消。 */
suspend fun <T> catchDataError(block: suspend () -> T): Either<DataError, T> = try {
    Either.Right(block())
} catch (error: CancellationException) {
    throw error
} catch (error: Exception) {
    Either.Left(DataError.Unknown(error))
}
~~~

~~~kotlin
import arrow.core.Either
import ciyin.business.base.error.DataError
import com.ciyin.app.domain.profile.error.GetProfileError
import com.ciyin.app.domain.profile.error.toGetProfileError
import com.ciyin.app.domain.profile.model.Profile
import com.ciyin.app.domain.profile.repository.ProfileRepository

/** 获取资料用例。 */
class GetProfileUseCase(
    /** 资料仓库。 */
    private val repository: ProfileRepository,
) {
    /** 执行获取资料操作。 */
    suspend operator fun invoke(): Either<GetProfileError, Profile> =
        repository.getProfile().mapLeft(DataError::toGetProfileError)
}
~~~

### 订阅操作

订阅型 UseCase 仍然必须经过 Domain，并以 `Flow<Output>` 作为外层契约。`Output` 的具体类型和
失败承载方式由业务语义决定，不能为了统一形式把所有订阅强制包装成
`Flow<Either<..., ...>>`：

- 普通状态观察优先使用 `Flow<T>`。流的异常作为终止性的 Flow 失败交给上层处理，UseCase 不
  自行收集、重启或创建 CoroutineScope。
- 如果失败需要作为流内事件逐条传递，可以使用
  `Flow<Either<DataError, T>> -> Flow<Either<场景错误, T>>`，或定义包含错误状态的领域类型；
  必须在契约或 KDoc 中说明为什么需要这种错误承载方式，以及流在错误后的终止/恢复语义。

普通只读资料流的契约示例：

~~~kotlin
import com.ciyin.app.domain.profile.model.Profile
import kotlinx.coroutines.flow.Flow

/** 只读资料流的仓库契约。 */
interface ProfileObservationRepository {
    /** 观察当前资料。 */
    fun observeProfile(): Flow<Profile>
}

/** 观察资料用例。 */
class ObserveProfileUseCase(
    /** 资料流仓库。 */
    private val repository: ProfileObservationRepository,
) {
    /** 获取当前资料流。 */
    operator fun invoke(): Flow<Profile> = repository.observeProfile()
}
~~~

Data 层将原生 `Flow<ProfileEntity>` 映射为 `Flow<Profile>`，不得把 Entity 泄漏到 Domain。ViewModel
再决定 `stateIn`/`shareIn` 的生命周期，并根据产品需要处理终止性异常。

只有需要把失败作为流内值时，才采用显式错误契约：

~~~kotlin
import arrow.core.Either
import ciyin.business.base.error.DataError
import com.ciyin.app.domain.profile.error.GetProfileError
import com.ciyin.app.domain.profile.error.toGetProfileError
import com.ciyin.app.domain.profile.model.Profile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** 需要逐条表达资料读取失败时的仓库契约。 */
interface ProfileObservationWithErrorsRepository {
    /** 观察资料，并将失败作为流内值传递；错误后的恢复由上层策略决定。 */
    fun observeProfile(): Flow<Either<DataError, Profile>>
}

/** 映射资料流中的场景错误。 */
class ObserveProfileWithErrorsUseCase(
    /** 资料仓库。 */
    private val repository: ProfileObservationWithErrorsRepository,
) {
    /** 获取经过场景错误映射的资料流。 */
    operator fun invoke(): Flow<Either<GetProfileError, Profile>> =
        repository.observeProfile()
            .map { it.mapLeft(DataError::toGetProfileError) }
}
~~~

当 Data 层选择上述错误值契约时，必须在流边界转换异常并保留取消：

~~~kotlin
import arrow.core.Either
import ciyin.business.base.error.DataError
import com.ciyin.app.domain.profile.model.Profile
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/** 将资料数据流错误转换为流内值；此示例发出错误后结束。 */
fun observeProfileWithErrors(): Flow<Either<DataError, Profile>> =
    dao.observeProfile()
        .map { Either.Right(it.toDomain()) }
        .catch { error ->
            if (error is CancellationException) throw error
            emit(Either.Left(DataError.Persistence))
        }
~~~

## Repository 契约与实现

Domain 必须先声明接口，Data 再提供实现。接口返回 Domain Model，不暴露 ApiData、Entity、
Dao 或 Ktor 类型。

~~~kotlin
package com.ciyin.app.domain.profile.repository

import arrow.core.Either
import ciyin.business.base.error.DataError
import com.ciyin.app.domain.profile.model.Profile

/** 资料领域仓库契约。 */
interface ProfileRepository {
    /** 获取当前用户资料。 */
    suspend fun getProfile(): Either<DataError, Profile>
}
~~~

~~~kotlin
package com.ciyin.app.data.profile.repository

import arrow.core.Either
import arrow.core.raise.either
import ciyin.business.base.error.DataError
import com.ciyin.app.data.profile.api.ProfileApi
import com.ciyin.app.data.profile.mapper.toDomain
import com.ciyin.app.domain.profile.model.Profile
import com.ciyin.app.domain.profile.repository.ProfileRepository

/** 资料仓库的数据层实现。 */
internal class ProfileRepositoryImpl(
    private val api: ProfileApi,
) : ProfileRepository {
    /** 获取当前用户资料。 */
    override suspend fun getProfile(): Either<DataError, Profile> = either {
        api.getProfile().bind().toDomain()
    }
}
~~~

## 网络 API

ApiClient 当前提供：

- safeCallWithData<T>：响应是项目标准信封 ApiResponse<T> 时使用；
- safeCall：裸 JSON、文件流、二进制或第三方非信封响应时使用，调用方再解析 HttpResponse
  并转换解析错误。

safeCallWithData 只负责解析 ApiResponse<T>.data，不会自动验证业务 code。当前 NomiKit 源码没有
通用的业务状态码验证器；使用 safeCallWithData 前必须确认宿主 HttpClient 已注册 HTTP 和业务
状态码校验。若没有验证器，应使用 safeCall + getApiResponse<T>()，按宿主协议显式检查 code、msg
和 data，再将失败转换为 DataError.Network.Http 或 DataError.Serialization。

API 模型必须使用 @Serializable。FeatureApi 构造需要 HttpClient，而当前仓库没有通用的业务
HttpClient 自动绑定；DataModule 生效前必须由宿主或测试模块注册它。

~~~kotlin
import arrow.core.Either
import ciyin.business.base.data.ApiClient
import ciyin.business.base.error.DataError
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.serialization.Serializable

/** 资料接口响应体。 */
@Serializable
data class ProfileApiData(
    /** 昵称。 */
    val nickname: String,
)

/** 资料相关网络接口。 */
class ProfileApi(
    client: HttpClient,
) : ApiClient(client) {
    /** 请求当前用户资料。 */
    suspend fun getProfile(): Either<DataError, ProfileApiData> = safeCallWithData {
        // 仅当宿主已注册 HTTP/业务状态码校验时使用此分支。
        get("/v1/profile")
    }
}
~~~

没有状态码验证器时，改用 safeCall + getApiResponse；只有确认接口不是标准信封时才直接解析
裸响应，并在方法 KDoc 中写明响应格式和解析责任。

## 本地数据源

### DataStore

**REQUIRED SUB-SKILL:** 使用 data-store skill 处理 DataStoreFactory、JsonDataStoreSerializer、
DataStorePath、Scope DSL 和 DataStore 测试。

业务偏好模型必须标记 @Serializable。为避免模型重命名导致文件名改变，业务代码优先传入
稳定的显式 fileName：

~~~kotlin
import androidx.datastore.core.DataStore
import ciyin.datastore.DataStoreFactory
import ciyin.datastore.JsonDataStoreSerializer
import kotlinx.serialization.Serializable

/** 资料偏好。 */
@Serializable
data class ProfilePreferences(
    /** 昵称。 */
    val nickname: String = "",
)

/** 资料 DataStore 包装。 */
class ProfileDataStore(
    factory: DataStoreFactory,
) : DataStore<ProfilePreferences> by factory.create(
    fileName = "profile_preferences.json",
    serializer = JsonDataStoreSerializer(
        defaultValue = ProfilePreferences(),
        serializer = ProfilePreferences.serializer(),
    ),
)
~~~

同一逻辑文件只能通过一个 DataStoreFactory.create 路径创建。需要 Scope 内多实例时使用
ciyin.datastore.dataStore(name, fileName, serializer)，并为每个不同的数据类型/序列化器分配
唯一 fileName；name 不会改变 DataStoreFactory 按完整路径去重的规则。不要绕过工厂直接创建多个
活跃实例。
具体配置和测试使用 data-store skill。

### Room

**REQUIRED SUB-SKILL:** 使用 room skill 处理 Entity、Dao、AppDatabase、schema、TypeConverter
和迁移策略。

新增实体、DAO 后必须同步更新 AppDatabase.entities 和抽象 DAO 属性，并按 Room skill 的规则
处理 schema/version。持久化字段显式使用 snake_case 的 @ColumnInfo：

~~~kotlin
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** 资料数据库实体。 */
@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey
    @ColumnInfo(name = "user_id")
    /** 用户编号。 */
    val userId: String,
    @ColumnInfo(name = "display_name")
    /** 展示名称。 */
    val displayName: String,
)
~~~

DataModule 使用 singleDao(AppDatabase::profileDao) 注册。实际数据库聚合类当前位于
com.ciyin.app.data.core.datasource.AppDatabase。除非用户明确要求，不要擅自新增 Migration、
AutoMigration 或破坏性迁移配置。具体 Room 规则使用 room skill。

## Mapper

Mapper 负责明确的边界转换：

~~~kotlin
/** Domain 入参转换为网络请求体。 */
fun ProfileInput.toProfileApiRequest(): ProfileApiRequest = ProfileApiRequest(
    nickname = nickname,
)

/** 网络响应转换为 Domain Model。 */
fun ProfileApiData.toDomain(): Profile = Profile(
    nickname = nickname,
)

/** Domain Model 转换为数据库实体。 */
fun Profile.toEntity(): ProfileEntity = ProfileEntity(
    userId = userId,
    displayName = nickname,
)
~~~

不要把 ApiData、Entity 或 Domain Model 直接传给错误的边界；简单场景可以省略不产生价值的
中间模型，但必须保留网络/数据库技术类型不泄漏到 Domain。

## Koin DI 与聚合

Domain Module 只注册 UseCase/Validator；Data Module 注册 API、DataStore、DAO、LocalDataSource
和 Repository 实现。接口绑定必须显式写出：

~~~kotlin
val ProfileDomainModule = module {
    singleOf(::GetProfileUseCase)
}

val ProfileDataModule = module {
    singleOf(::ProfileApi)
    singleOf(::ProfileDataStore)
    singleDao(AppDatabase::profileDao)
    singleOf(::ProfileRepositoryImpl) bind ProfileRepository::class
}
~~~

singleDao、ProfileDataStore 和 ProfileApi 只在对应能力实际使用时注册。确保 HttpClient
和 Room/DataStore Boot 初始化器已经存在，否则不要声称 Koin 图完整。

当前 app/shared/src/commonMain/kotlin/com/ciyin/app/di/modules/Modules.kt 已有 MainModules /
SettingsModules 占位，并由 KoinInitializer.kt 显式调用。优先把 feature 模块加入合适的占位：

~~~kotlin
val MainModules: KoinBootInitializer = {
    modules(ProfileDataModule, ProfileDomainModule)
}
~~~

只有没有合适占位时才新增 FeatureModules，并在同一改动中显式调用。不要手改生成的
AppBootInitializer；新增或修改聚合后必须验证运行时可以解析 UseCase 和 Repository 接口。

## 新增 Feature 工作流

1. Domain：定义 Model、Repository 接口和输入校验。
2. Domain：定义场景错误及 DataError.toXxxError() 映射。
3. Domain：实现 UseCase/Validator。
4. Data：定义 ApiRequest、ApiData、Entity、Preferences（按需）。
5. Data：实现 Api、Dao、DataStore、LocalDataSource（按需）。
6. Data：实现 Mapper 和 RepositoryImpl。
7. Data/Domain：分别建立并测试 Koin Module。
8. App：把模块加入现有聚合 initializer，必要时注册宿主 HttpClient。
9. 验证编译、测试、Koin 解析和 git diff --check。

## Review 清单

- [ ] 文件位于 app/shared/src/commonMain/kotlin/com/ciyin/app/{data,domain}/<feature>/。
- [ ] Domain 声明 Repository 接口，Data 使用独立的 RepositoryImpl 实现并绑定接口。
- [ ] Domain 没有导入 com.ciyin.app.data.*、ApiData、Entity、Dao 或 Ktor 类型。
- [ ] Data 没有定义场景错误；Repository 对外的一次性操作或显式错误值契约返回 DataError/Either，
      普通订阅按 Flow<Output> 暴露，DAO/DataStore 的原生类型仅保留在 Data 内部。
- [ ] 一次性操作的 UseCase 返回 Either<场景错误, T>；订阅操作明确使用 Flow<Output> 外层契约，
      并说明 Output 的错误承载方式与流在异常后的终止/恢复语义。
- [ ] 所有 DataError 到场景错误的映射集中在 Domain，并覆盖未知错误。
- [ ] API 使用正确的 safeCallWithData / safeCall，没有假设仓库不存在的 validator。
- [ ] 已确认 safeCall/safeCallWithData 的取消语义；若共享 ApiClient 会吞取消，先修复共享层。
- [ ] DataError.message/GenericError.message 没有直接渲染到 UI，未知错误使用资源化兜底文案。
- [ ] 跨层转换通过 Mapper，技术模型没有泄漏到 Domain 或 UI。
- [ ] DataStore 模型有 @Serializable，文件路径稳定且没有重复活跃实例。
- [ ] Room Entity/DAO、AppDatabase、schema/version 和 singleDao 已同步。
- [ ] DataStore、Room、KoinBoot 的细节规则已遵循对应 REQUIRED SUB-SKILL。
- [ ] FeatureDataModule 显式绑定 Repository 接口，UseCase 可以从 Koin 解析。
- [ ] 用户可见文案没有写入 Domain Error、ViewModel 或 UI Model，已使用 Compose Resources。
- [ ] 新增或修改的 Kotlin 声明符合中文 KDoc 规则。
- [ ] 至少有错误映射、UseCase、Mapper、Repository/API 和 Koin 图的最小测试。

## 直接拒绝的反模式

- 在 Data 中产出 LoginError、RegisterError 等场景错误。
- 在 Domain 中导入 com.ciyin.app.data.* 或暴露 Ktor/Room/DataStore 类型。
- API 内重复手写网络异常 try/catch，或吞掉异常后返回假成功。
- Repository 实现没有实现 Domain 接口，或 Koin 只注册了具体类而未绑定接口。
- UseCase 内创建 CoroutineScope、使用 GlobalScope 或自行收集 Flow。
- 无业务理由地把所有订阅强制包装成 Flow<Either<场景错误, T>>，或未说明流内错误的终止/恢复语义。
- 用 delay、空值过滤或 UI 偏移量掩盖数据/时序错误。
- 把用户可见中文文案硬编码到 Domain Error、ViewModel 或 UI Model。
- 重新定义 DataError、GenericError，或引用不存在的 XxxError.Generic。

## 最小测试矩阵

| 对象 | 至少验证 |
| --- | --- |
| Mapper | API/Entity 与 Domain 字段转换、空值边界 |
| 错误映射 | 已知 HTTP/授权错误和未知 DataError |
| UseCase | 成功、错误映射、Validator 拒绝 |
| Repository/API | MockEngine 响应、解析失败、本地异常 |
| Koin | FeatureDataModule + FeatureDomainModule 可解析 UseCase 和 Repository 接口 |
| Flow | 普通 Flow<T> 验证首次/后续值、终止性异常和取消传播；错误值契约额外验证错误映射及错误后的终止/恢复 |
| DataStore | 序列化往返、显式文件名、重复路径只创建一个实例 |
| Room | Entity/DAO 编译、singleDao 解析、schema/version 变更检查 |

推荐从最窄任务开始，例如 .\gradlew.bat :app:shared:compileKotlinDesktop 和
.\gradlew.bat :app:shared:desktopTest；若任务名因当前 KMP 插件不同，以
.\gradlew.bat :app:shared:tasks 中的实际任务为准。

## 现有符号

- ciyin.business.base.error.DataError
- ciyin.business.base.error.GenericError
- ciyin.business.base.error.toGenericError
- ciyin.business.base.data.ApiClient
- ciyin.room.singleDao
- ciyin.datastore.DataStoreFactory
- ciyin.datastore.JsonDataStoreSerializer
- ciyin.koin.KoinBootInitializer
- com.ciyin.app.data.core.datasource.AppDatabase
