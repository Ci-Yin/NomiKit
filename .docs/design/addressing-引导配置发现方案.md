# Addressing 引导配置发现方案

> 状态：未标注（待核对）；尚未确认本方案的采纳与落地状态。
> 范围：拟议的引导配置发现、签名校验、缓存恢复与端点提供；目标平台为 Android / Desktop / iOS，Web/Wasm
> 留待后续适配。
> 相关模块：现有 `:business:base`、`:core:serialization`、`:core:platform`、`:component:data-store`、
> `:core:application`；构建逻辑 `buildSrc`；拟新增 `:feature:addressing`。
> 最近核对：2026-11-10；工作区 HEAD `cd08542f`，本次仅核对文档元信息、索引与模块声明，未逐项核验实现。
> 验证记录：当前 [settings.gradle.kts](<../../settings.gradle.kts>) 未声明 `:feature:addressing`
> ；本次未运行 Gradle、单测或平台应用，不能据此判断方案已采纳或完成验收。

正文中的接口、配置与流程代码仍是拟议设计示例，不代表当前可用 API。

## 1. 方案定位

Addressing 用于解决客户端在启动或业务 API 域名失效时，如何找到一份可信的引导配置。它只负责：

- 从构建时注入的多个 HTTPS 候选源获取 `bootstrap.json`；
- 验证配置的结构、签名、版本和有效期；
- 保存最近一次有效配置，并在恢复失败时继续提供旧配置；
- 向业务层提供当前 API 端点。

它不负责业务 API 的鉴权、请求重试、业务错误映射、镜像管理后台或远程更新候选源。第一版的托管服务只需要通过
HTTPS 提供静态 JSON 文件。

本方案保存在 `.docs/design/` 目录，与其他长期维护的设计方案集中管理。

## 2. 当前项目依据与模块决策

当前 `settings.gradle.kts` 已声明 `core`、`component`、`feature`、`business` 和 `app`
层，但没有独立网络模块。相关现有能力如下：

| 能力        | 当前入口                                                                                                                                    | 方案处理                                                             |
|-------------|---------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------|
| HTTP 客户端 | `business:base` 使用 Ktor `HttpClient`；`feature:sdwebui` 已配置各平台 Ktor engine                                                          | 新模块通过接口接收 HTTP 传输能力，不直接依赖业务 `ApiClient`         |
| JSON        | `core:serialization` 的 `ciyin.serialization.json`，底层为 kotlinx.serialization                                                            | 配置模型使用 `@Serializable`；签名校验使用独立的规范化编码器         |
| 本地持久化  | `component:data-store` 的 `DataStoreFactory`、`JsonDataStoreSerializer`、Koin Boot                                                          | 第一版优先复用该工厂保存缓存，避免重复创建同一路径 DataStore         |
| 日志        | `core:platform` 的 logger / Kermit 约定                                                                                                     | 只记录源 ID、错误分类、耗时和配置版本，不记录完整配置或签名          |
| 构建配置    | `buildSrc` 已有 `build-config` 预编译脚本；[AppBuildConfig 注入配置设计与实现](<appbuildconfig-注入配置重构方案.md>) 已提供统一构建注入入口 | 候选源清单使用独立的构建输入和生成任务，避免散落在业务 Kotlin 代码中 |

### 2.1 推荐模块位置

第一版新增 `:feature:addressing`，因为它是可复用的跨平台能力，核心职责是配置发现、验证、故障恢复和端点提供，而不是某一个业务产品的
API。模块不依赖 `app/*`、产品域名、账号配置或业务协议。

模块内部通过接口隔离第三方和平台差异：

- `AddressingFetcher` 只依赖抽象的 HTTP 读取器；Ktor 适配器作为模块实现或宿主装配实现。
- `AddressingStore` 只表达加载、原子保存和清除；默认 DataStore 适配器复用 `component:data-store`。
- `AddressingSignatureVerifier` 只表达 Ed25519 验签；Android/Desktop/iOS 的实现通过平台 source set
  或注入完成。
- `AddressingEndpointProvider` 只向业务层暴露已接受的端点，不改变现有业务 API 客户端的错误模型。

如果实现时发现 `feature -> component:data-store` 会违反当前项目的依赖约束，则保留上述接口，把
DataStore 实现移到应用装配层；不得为了复用 DataStore 让核心调度依赖 `app/*`。

## 3. 目标与非目标

### 3.1 目标

1. 支持构建时注入 50～100 个候选源，并记录源 ID、初始优先级和故障域。
2. 启动优先加载本地最近一次有效配置，不能因为重新发现阻塞已有业务端点。
3. 使用缓存优先、分批并发、成功即收敛的发现算法。
4. 对远程响应执行 HTTPS、大小、JSON 结构、Ed25519 签名、版本和有效期校验。
5. 配置接受后原子写入缓存，并更新业务端点；失败时保留仍可用的缓存。
6. 通过 `StateFlow` 暴露发现状态，通过 `resolve` / `forceResolve` 暴露一次性解析结果。
7. 对超时、连接失败、HTTP 错误、响应过大、JSON 无效、签名无效、过期和版本回滚分别记录。
8. 让测试可以替换 HTTP、时钟、随机数、签名验证器和存储，不依赖真实镜像服务。

### 3.2 非目标

- 不做远程动态下发候选源清单。
- 不做鉴权、配置管理后台、镜像自动同步服务或跨设备健康状态共享。
- 不做复杂机器学习评分；运行时只维护少量可解释的健康状态。
- 不把配置源可用误判为业务 API 可用；业务 API 健康检查由业务层单独负责。
- 不把完整配置、签名值、业务请求内容写入日志。
- 不在第一版为 Web/Wasm 强行引入未验证的 Ed25519 平台实现；Web 支持作为后续适配项。

## 4. 配置协议

### 4.1 `bootstrap.json` 外部格式

第一版正式格式如下。示例中的时间戳、URL 和签名值仅用于说明字段，不是可发布配置。

```json
{
  "schemaVersion": 1,
  "canonicalizationVersion": 1,
  "configVersion": 42,
  "issuedAt": 1792100000,
  "expiresAt": 1792700000,
  "endpoints": {
    "api": [
      {
        "url": "https://api-a.example.com",
        "priority": 1
      },
      {
        "url": "https://api-b.example.com",
        "priority": 2
      }
    ]
  },
  "signature": {
    "algorithm": "Ed25519",
    "keyId": "config-key-1",
    "value": "BASE64_SIGNATURE"
  }
}
```

对应 Kotlin 模型只表达协议，不携带网络、存储或业务客户端状态：

```kotlin
@Serializable
data class AddressingConfig(
    val schemaVersion: Int,
    val canonicalizationVersion: Int,
    val configVersion: Long,
    val issuedAt: Long,
    val expiresAt: Long,
    val endpoints: AddressingEndpoints,
    val signature: AddressingSignature,
)

@Serializable
data class AddressingEndpoints(
    val api: List<AddressingEndpoint>,
)

@Serializable
data class AddressingEndpoint(
    val url: String,
    val priority: Int,
)

@Serializable
data class AddressingSignature(
    val algorithm: String,
    val keyId: String,
    val value: String,
)
```

### 4.2 字段校验

- `schemaVersion` 必须是客户端支持的版本；未知版本返回 `UnsupportedSchema`。
- `canonicalizationVersion` 必须是客户端实现的版本；未来变更不能静默按旧规则验签。
- `configVersion` 为非负单调发布版本。小于已接受版本直接拒绝。
- `issuedAt <= expiresAt`；发布时间不能明显晚于客户端时钟，允许的时钟偏差通过 `ClockSkewPolicy` 配置。
- `endpoints.api` 不能为空；每个 URL 必须是绝对 HTTPS URL，禁止明文 HTTP、用户名密码、空主机和无效端口。
- `priority` 必须为正数；同一优先级下按 URL 规范化值排序，避免相同配置产生不同签名。
- API URL 去除末尾 `/` 后再进入端点模型，但签名校验必须针对发布方声明的规范化输入完成，不能先静默修改再验签。
- `signature.algorithm` 第一版只接受精确值 `Ed25519`；未知算法不降级为“只信 HTTPS”。
- 响应体设置最大字节数，例如 256 KiB；超过限制返回 `ResponseTooLarge`。

### 4.3 签名范围与规范化

签名覆盖除 `signature` 字段外的全部字段。第一版采用显式的 `canonicalizationVersion = 1`：

1. 构建签名对象，字段固定为 `schemaVersion`、`canonicalizationVersion`、`configVersion`、`issuedAt`、
   `expiresAt`、`endpoints`。
2. 对象 key 按 UTF-8 字典序递归排序。
3. API 地址按规范化 URL、再按 `priority` 排序；数组输出顺序固定。
4. 使用 UTF-8、无 BOM、无空白和无换行的 JSON 文本；数字使用 kotlinx.serialization 的整数表示，不允许浮点时间戳。
5. 对上述字节执行 Ed25519 验签；`value` 使用标准 Base64 解码，拒绝非法或过长输入。

实现必须提供固定测试向量，至少包含：同一配置的签名成功、字段改动失败、签名字段改动不影响签名载荷但会因验签失败、数组顺序变化在规范化后保持一致、未知规范化版本拒绝。

### 4.4 密钥策略

客户端内置 `keyId -> publicKey` 白名单。未知 `keyId`
、算法不匹配、密钥格式错误和验签失败均视为不可接受配置。第一版不通过远程配置更新公钥，公钥轮换通过客户端版本发布完成；后续若要轮换，需要预置新旧公钥并定义重叠窗口。

Ed25519 没有现成的 NomiKit 公共封装，不能假设任意 JVM/Native 库都支持当前目标。实现阶段先验证依赖在
Android API 26、Desktop JVM 和 iOS 模拟器/真机的可用性；可行时使用统一 KMP 密码学库，否则定义
`expect/actual` 或注入适配器。不得用自定义纯 Kotlin 算法，也不得用“签名字段存在”替代验签。

## 5. 构建时候选源注入

### 5.1 源清单

候选 URL 放在独立构建输入，例如 `config/bootstrap-sources.json`，不散落在业务代码中，也不与远程
`bootstrap.json` 混合：

```json
{
  "sources": [
    {
      "id": "mirror-001",
      "url": "https://host-a.example/bootstrap.json",
      "priority": 1,
      "faultDomain": "provider-a"
    },
    {
      "id": "mirror-002",
      "url": "https://host-b.example/bootstrap.json",
      "priority": 2,
      "faultDomain": "provider-b"
    }
  ]
}
```

源模型建议如下：

```kotlin
@Serializable
data class AddressingSource(
    val id: String,
    val url: String,
    val priority: Int,
    val faultDomain: String,
)

class AddressingSourceRegistry(
    sources: List<AddressingSource>,
) {
    init {
        require(sources.size <= 100) { "Addressing 候选源不能超过 100 个" }
        require(sources.map { it.id }.distinct().size == sources.size) {
            "Addressing 候选源 ID 不能重复"
        }
        require(sources.map { it.url }.distinct().size == sources.size) {
            "Addressing 候选源 URL 不能重复"
        }
        require(sources.all { it.url.startsWith("https://") && it.faultDomain.isNotBlank() }) {
            "Addressing 候选源必须使用 HTTPS 且提供故障域"
        }
    }

    val sources: List<AddressingSource> = sources
        .sortedWith(compareBy<AddressingSource> { it.priority }.thenBy { it.id })
}
```

清单校验必须拒绝重复 ID、重复 URL、空故障域、非 HTTPS URL 和超过上限的候选数量。建议上限为 100，实际镜像数量
5～10 个；这两个数量不做绑定。

### 5.2 Gradle 生成边界

新增轻量 `GenerateAddressingSourcesTask` 或在现有 `build-config` 方向上增加独立扩展，输入为清单文件，输出为生成的
Kotlin 源文件或资源。任务需要声明明确的 `@InputFile`、`@OutputDirectory`，并接入生成源集。

生成结果只包含已校验的候选源和必要的构建元数据，例如：

```kotlin
/** 构建时注入的 Addressing 候选源。 */
internal object GeneratedAddressingSources {
    val all: List<AddressingSource> = listOf(
        AddressingSource(
            id = "mirror-001",
            url = "https://host-a.example/bootstrap.json",
            priority = 1,
            faultDomain = "provider-a",
        ),
    )
}
```

实际产品地址、公钥和源清单应由消费应用或发布配置注入；NomiKit 脚手架不能提交真实生产域名、账号信息或私钥。
`AppBuildConfig` 构建注入已落地；本方案的候选源仍需作为独立结构化输入接入同一套构建配置入口，但不要把
50～100 个 URL 压成不透明的字符串属性。

## 6. 模块 API 与职责

### 6.1 外部接口

核心 API 保持小而稳定，使用 `Either` 与 `StateFlow`，不把 Ktor `HttpResponse` 或 DataStore
类型泄漏到发现模块调用方：

```kotlin
interface AddressingDiscovery {
    /** 当前发现状态；状态变化只由发现协调器发布。 */
    val state: StateFlow<DiscoveryState>

    /** 使用缓存和当前冷却策略解析配置。 */
    suspend fun resolve(): Either<DiscoveryError, AddressingConfig>

    /** 忽略源冷却并重新探测，但不清除当前有效缓存。 */
    suspend fun forceResolve(): Either<DiscoveryError, AddressingConfig>
}

interface AddressingFetcher {
    /** 从一个候选源读取原始配置文本，并返回可分类的结果。 */
    suspend fun fetch(source: AddressingSource): FetchResult
}

interface AddressingStore {
    /** 读取最近一次原子保存的配置快照。 */
    suspend fun load(): CachedAddressing?

    /** 只在验证完成后替换整个快照。 */
    suspend fun save(config: AddressingConfig, sourceId: String, acceptedAt: Long)
}

interface AddressingSignatureVerifier {
    /** 使用内置公钥验证规范化载荷和 Ed25519 签名。 */
    fun verify(keyId: String, payload: ByteArray, signature: ByteArray): Boolean
}

interface AddressingEndpointProvider {
    /** 返回当前可用的业务 API 端点，业务请求自行决定健康检查与重试。 */
    fun currentApiEndpoints(): List<AddressingEndpoint>
}
```

### 6.2 状态模型

状态要区分“已有可用缓存”“正在发现”“已发现新配置”和“没有任何可用配置”：

```kotlin
sealed interface DiscoveryState {
    data object Idle : DiscoveryState

    data class UsingCache(
        val configVersion: Long,
        val sourceId: String?,
        val freshness: CacheFreshness,
    ) : DiscoveryState

    data class Resolving(
        val round: Int,
        val attemptedSources: Int,
        val totalSources: Int,
    ) : DiscoveryState

    data class Ready(
        val configVersion: Long,
        val sourceId: String,
        val acceptedAt: Long,
    ) : DiscoveryState

    data class Failed(
        val error: DiscoveryError,
        val cache: CacheAvailability,
        val nextRetryAt: Long?,
    ) : DiscoveryState
}

enum class CacheFreshness { Fresh, StaleWithinGrace }

enum class CacheAvailability { None, Usable }
```

缓存状态和远程发现状态不能合并成单一布尔值。旧配置在短暂过期宽限期内可继续提供端点，但状态必须明确标记为
`StaleWithinGrace`，并继续后台恢复。

### 6.3 错误模型

`DiscoveryError` 用于发现模块自身的场景错误，底层异常只作为原因保留，不向调用方暴露平台异常类型：

```kotlin
sealed interface DiscoveryError {
    data object NoUsableConfiguration : DiscoveryError
    data object UnsupportedSchema : DiscoveryError
    data object InvalidStructure : DiscoveryError
    data object InvalidSignature : DiscoveryError
    data object VersionRollback : DiscoveryError
    data object ExpiredConfiguration : DiscoveryError
    data object ResponseTooLarge : DiscoveryError
    data object AllSourcesFailed : DiscoveryError
    data class Cancelled(val cause: Throwable? = null) : DiscoveryError
    data class Unknown(val cause: Throwable) : DiscoveryError
}
```

单个源的失败需要附加到观测事件中，不能因为一个源失败就终止整轮探测；只有整轮没有得到可接受配置时，才返回
`AllSourcesFailed` 或 `NoUsableConfiguration`。

## 7. 运行时流程

### 7.1 启动流程

```text
创建 AddressingDiscovery
    |
    +-- 读取缓存
    |      +-- 签名、版本、结构再次验证
    |      +-- Fresh / StaleWithinGrace -> 立即发布 UsingCache
    |      +-- 不可用 -> 记录缓存损坏或过期
    |
    +-- resolve() 单飞协调
           +-- 优先上次成功源
           +-- 选取少量不同故障域的候选
           +-- 分批并发请求
           +-- 响应逐个验证
           +-- 首个可接受配置胜出，取消同轮其余请求
           +-- 原子保存并发布 Ready
```

`resolve()` 不清除旧缓存。已有可用缓存时，应用初始化可以先使用缓存端点，发现任务在后台运行；业务层是否等待新配置由产品生命周期决定，发现模块本身不阻塞业务请求。

### 7.2 分批并发算法

推荐初始参数：

| 参数         |          初始值 | 说明                                   |
|--------------|----------------:|----------------------------------------|
| 内置候选地址 |         50～100 | 只是候选池，不代表每次全部请求         |
| 实际托管位置 |           5～10 | 应覆盖不同服务商/故障域                |
| 首批并发数   |               3 | 尽量选择不同故障域                     |
| 单次请求超时 |            3 秒 | 通过注入策略配置，不在算法中写死       |
| 单轮恢复预算 |       10～15 秒 | 达到预算后停止扩大批次                 |
| 响应体上限   |         256 KiB | 配置应远小于此值                       |
| 失败冷却     | 指数退避 + 抖动 | 例如 1s、2s、4s、8s，上限由策略控制    |
| 旧缓存宽限期 |    初始 24 小时 | 过期缓存仅作为受限可用状态，需产品确认 |

候选选择规则：

1. 先选最近成功源；如果它处于冷却期，选最近成功时间和响应耗时较优的其他源。
2. 首批尽量覆盖不同 `faultDomain`；同一故障域不得占满首批。
3. 一批全部失败后，扩大到下一批；成功验签后立即收敛。
4. 超时、连接错误、HTTP 非 2xx、响应过大、JSON 无效和配置校验失败分别计数。
5. 失败源不会永久删除；冷却结束后仍可参与探测。

调度器必须支持协程取消、单飞和请求预算。多个调用方同时调用 `resolve()` 时共享同一次探测结果；
`forceResolve()` 只跳过冷却和缓存短路，不得并发启动第二个协调循环。

### 7.3 版本、缓存和原子更新

- 远程配置只有在完整验证通过后才可写入。
- 保存内容至少包括配置 JSON、`sourceId`、`acceptedAt`、规范化载荷指纹和 `configVersion`。
- 使用 DataStore 单文件整体替换，禁止先删除旧文件再写新文件；写入失败时旧快照仍应可读。
- 启动读取缓存时再次验签，不能因为缓存来自本地就跳过安全校验。
- 远程 `configVersion` 小于缓存版本直接拒绝。
- 相同 `configVersion` 的规范化载荷不同视为冲突并拒绝，避免同一版本被不同镜像返回不同内容。
- 缓存超过宽限期后不能作为有效业务配置；可保留用于诊断，但 `resolve()` 返回 `NoUsableConfiguration`。

## 8. 业务端点解耦

Addressing 不修改业务 API 的认证、请求重试和数据错误映射。业务层通过 `AddressingEndpointProvider`
或等价的应用适配器取得端点：

```kotlin
class BusinessApi(
    private val httpClient: HttpClient,
    private val endpointProvider: AddressingEndpointProvider,
) {
    suspend fun request(): Either<DataError, ApiResponse> {
        val endpoint = endpointProvider.currentApiEndpoints().firstOrNull()
            ?: return Either.Left(DataError.Network.NoConnection)

        return requestWithBusinessPolicy(
            httpClient = httpClient,
            baseUrl = endpoint.url,
        )
    }
}
```

业务 API 可用性检查、端点轮换、鉴权头、业务响应解析仍留在业务模块。配置源恢复成功只表示得到了可信配置，不表示某个
API 地址当前一定可用。

## 9. 依赖注入与装配

模块应提供显式的构造参数或 Koin Boot 装配，但不能在核心类中手动创建全局 HttpClient、DataStore
或平台密码学对象：

```kotlin
fun addressingModule(
    sources: AddressingSourceRegistry,
    fetcher: AddressingFetcher,
    store: AddressingStore,
    verifier: AddressingSignatureVerifier,
    clock: AddressingClock,
    policy: AddressingDiscoveryPolicy = AddressingDiscoveryPolicy.Default,
): Module = module {
    single { sources }
    single<AddressingFetcher> { fetcher }
    single<AddressingStore> { store }
    single<AddressingSignatureVerifier> { verifier }
    single<AddressingClock> { clock }
    single { policy }
    single<AddressingDiscovery> {
        DefaultAddressingDiscovery(
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
}
```

实际 Koin API 以当前 `component:koin` 约定为准，示例用于说明依赖边界。默认存储适配器可以使用
`DataStoreFactory.create(fileName = "bootstrap.json", ...)`，并确保全局只创建一个同路径 DataStore。

## 10. 平台与安全边界

### 10.1 HTTP

- Ktor `HttpClient` 只负责请求和读取受限响应；业务 `ApiClient` 的 `DataError` 映射不直接复用到发现模块。
- 平台 engine 由宿主或模块按现有 `androidMain`、`desktopMain`、`iosMain` 依赖约定提供。
- 关闭自动重定向，或至少验证最终 URL 仍为 HTTPS 且不跨到不允许的协议/主机策略；重定向行为需要记录。
- 使用独立请求超时和响应体上限，不沿用业务 API 的默认配置。

### 10.2 签名

- 公钥只读内置；不从 `bootstrap.json` 或候选清单中取得。
- 验签前先限制 Base64 长度和 JSON 大小，避免无界输入。
- 失败原因对外使用粗粒度错误，日志可以记录分类和源 ID，但不记录签名原文。
- 密钥轮换、撤销和生产私钥保管不在客户端模块内，放在发布流程设计。

### 10.3 时间

通过 `AddressingClock` 注入当前 epoch 秒，测试使用可控时钟。系统时间明显回拨时不自动接受已过期配置；允许的未来偏差和缓存宽限期必须进入策略对象并可观测。

## 11. 实施分阶段

### 阶段 0：契约和依赖门槛

1. 新增 `:feature:addressing` 的目录、`settings.gradle.kts` 声明和基础 Gradle 配置。
2. 确认模块实际依赖方向；若不能依赖 `component:data-store`，先建立 `AddressingStore` 应用适配边界。
3. 评估 Ed25519 KMP 依赖在 Android API 26、Desktop 和 iOS 的编译与运行可行性，形成固定选择；未验证的平台不宣称支持。
4. 固定协议模型、规范化版本和测试向量。

### 阶段 1：纯逻辑核心

1. 实现配置模型、源注册表、规范化编码、配置验证和版本/时间策略。
2. 实现 `AddressingSignatureVerifier` 接口和 fake verifier 测试。
3. 实现候选源健康状态、排序、故障域选择和退避策略。
4. 为以上逻辑补 `commonTest`，全部使用固定时钟和 fake 依赖。

### 阶段 2：传输、缓存和协调器

1. 实现 Ktor fetcher、响应大小限制、错误分类和超时策略。
2. 实现 DataStore 原子缓存适配器；验证路径级单例和重启恢复。
3. 实现 `DefaultAddressingDiscovery` 的单飞、分批并发、成功收敛、取消和状态流。
4. 使用 Ktor MockEngine、FakeFileSystem 或现有 DataStore 测试方式覆盖故障矩阵。

### 阶段 3：构建注入和业务接入

1. 新增源清单校验和生成任务，生成宿主侧 `AddressingSourceRegistry`。
2. 在 `app:sample` 或最小示例入口接入端点提供器，验证业务 API 不需要了解发现源。
3. 记录首次成功源、配置版本、耗时和失败分类的最小诊断事件。
4. 用一份签名固定的测试配置模拟镜像同步，不连接真实生产服务。

### 阶段 4：发布前验证

1. 使用多个本地 MockEngine 源模拟超时、DNS、HTTP 500、错误 JSON、错误签名、旧版本和有效响应。
2. 在 Android、Desktop、iOS 目标运行签名向量与核心调度测试；Web/Wasm 只在提供正式 WebCrypto 适配后加入。
3. 进行离线启动、缓存过期、时钟回拨、并发 `resolve()`、取消和磁盘写入失败测试。
4. 由发布脚本检查同一 `bootstrap.json` 的规范化摘要在 5～10 个镜像位置一致。

## 12. 验证计划与验收标准

### 12.1 单元测试

- 配置模型：缺字段、未知字段、非法 URL、空端点、时间范围和响应大小。
- 签名：固定 Ed25519 向量、字段篡改、版本篡改、规范化顺序和未知 keyId。
- 版本：回滚拒绝、同版本不同内容拒绝、合法新版本接受。
- 源注册表：去重、排序、故障域分布、冷却到期恢复。
- 调度：首批 3 并发、批次扩展、成功即取消、总预算、单飞和协程取消。
- 缓存：原子替换、旧快照保留、缓存再次验签、宽限期和过期不可用。
- 状态：`UsingCache -> Resolving -> Ready`、缓存失败后 `Failed`、恢复失败不清除旧配置。

### 12.2 Gradle 验证

模块落地后运行最窄任务，具体任务名以 `multiplatform-lib-targets` 实际启用目标为准：

```powershell
.\gradlew.bat :feature:addressing:allTests --console=plain
.\gradlew.bat :feature:addressing:compileKotlinDesktop --console=plain
.\gradlew.bat :feature:addressing:compileAndroidMain --console=plain
```

如果本机未启用 Desktop/iOS 或任务名因 Kotlin 版本不同而变化，记录实际可用任务，不用全量 `check` 代替窄验证。

### 12.3 功能验收

- 断网启动时，存在未过期缓存即可立即提供端点；发现失败不会删除缓存。
- 缓存过期但处于宽限期时，状态显示 `StaleWithinGrace`，后台继续恢复；超过宽限期后不再提供为有效配置。
- 首批请求不超过 3 个，尽量覆盖不同故障域；收到第一个有效配置后取消剩余探测。
- 任意镜像返回 HTTP 200 但签名无效时，不能更新缓存或端点。
- 低版本或同版本不同内容不能回滚已接受配置。
- `forceResolve()` 不清空现有配置、不启动重复协调循环。
- 业务 API 模块只依赖端点提供器，不读取候选清单、不解析签名、不处理镜像故障。
- 日志不包含完整配置、签名值、公钥或业务鉴权信息。

## 13. 观测与发布运维

建议记录结构化事件：`sourceId`、故障域、请求耗时、HTTP 状态分类、解析/签名/版本失败分类、配置版本、缓存新鲜度和下一次重试时间。日志中不记录完整
URL 的查询参数，必要时只记录源 ID 或主机摘要。

发布侧维护两份独立输入：

1. 候选源清单：随客户端构建注入，稳定发布，不能由远程配置修改。
2. `bootstrap.json`：由发布脚本生成、签名并同步到 5～10 个镜像位置。

同步脚本需要在发布前验证：所有镜像内容摘要相同、HTTPS 可访问、文件大小合规、签名可由固定测试公钥验证。客户端不承担镜像同步和后台管理职责。

## 14. 后续演进

- 动态镜像池：只有在固定候选源和恢复指标稳定后再考虑；必须先设计签名覆盖和回滚策略。
- 更复杂的源评分：先用最近成功、耗时、连续失败和冷却时间，真实数据不足时不引入不可解释评分。
- 公钥轮换：预置多 key、定义重叠窗口和撤销策略。
- Web/Wasm：采用正式 WebCrypto 适配后再扩展平台目标。
- 业务健康检查：在业务 API 模块中实现，不把健康状态混入 Addressing 的配置有效性。

## 15. 待实现前确认项

1. NomiKit 第一版是否只承诺 Android、Desktop、iOS；Web/Wasm 是否明确延期。
2. 旧缓存的 24 小时宽限期是否符合产品离线策略，或需要更短/更长的策略。
3. 生产公钥的注入方式：构建生成、源码常量还是平台安全存储；私钥必须留在发布环境。
4. 端点 URL 是否允许路径、查询参数和端口；当前建议只接受 HTTPS 绝对 URL，并在验签前后使用同一规范化规则。
5. 业务 API 客户端是否能在每次请求时读取端点，还是需要由应用层提供可替换的 `HttpClient`/路由适配器。
6. `component:data-store` 是否可以被新 `addressing` feature 模块直接依赖；若不允许，缓存适配器必须留在宿主装配层。
