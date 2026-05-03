---
name: data-store
description: 指导在 CiYinApplication 中使用 `component:data-store`（KMP androidx DataStore + Okio + kotlinx-serialization JSON、`DataStoreFactory` 路径级单例缓存、Koin 自动装配与 Scope DSL）。在新增/修改 DataStore 持久化、`JsonDataStoreSerializer`、`DataStoreBootInitializer`、自定义 `DataStorePath`/`FileSystem`、或排查「Multiple DataStores active」等问题时使用。
disable-model-invocation: true
---

# component:data-store 使用说明

## 模块位置与职责

- 路径：`component/data-store/`
- 职责：在 KMP 上封装 **DataStore（core + Okio）**、**JSON 序列化**（`kotlinx-serialization`）、*
  *按文件路径去重的工厂**（避免同一文件多个活跃 DataStore），以及 **Koin Boot 自动配置**与可选 **Scope
  内 DSL**。
- 业务侧的 `@Serializable` 偏好模型与 `DataStore<T>` 薄包装仍放在消费方（例如 `app/shared` 或
  `app/sample` 的 `data/*`），本模块不包含业务 schema。

## Gradle

- 消费方：`implementation(projects.component.dataStore)`（见 `app/shared`、`app/sample` 的
  `build.gradle.kts`）。
- 本模块插件：`multiplatform-lib-targets`、`kotlin.serialization`；依赖含 `libs.datastore.core`、
  `libs.datastore.core.okio`，并依赖 `component:koin`、`core:serialization`、`core:coroutines`。

## 核心 API

### DataStoreFactory

- 构造依赖：`FileSystem` + `DataStorePath`（通常由自动配置注入）。
- `create(fileName, serializer, scope)`：按 **完整文件路径** 缓存；同一路径只创建一个 `DataStore`，用于规避
  **Multiple DataStores active**（缓存放在 companion，Koin 重建 `DataStoreFactory` 仍可命中同一实例）。
- `create(defaultValue, serializer, scope)`（`reified T`）：默认文件名为
  `` `${T::class.qualifiedName}_preferences.json` ``；模型须 `@Serializable`，并传 `defaultValue` 与
  `KSerializer`（可用 `jsonDataStoreSerializer`）。

### JsonDataStoreSerializer

- 实现 `OkioSerializer<T>`；空白文件回退 `defaultValue`；默认 `Json`：`ignoreUnknownKeys`、
  `encodeDefaults`、`prettyPrint`。
- 需要与线上/磁盘格式严格一致时，可传入自定义 `Json` 实例构造 `JsonDataStoreSerializer`。

### Koin 自动装配（DataStoreBootInitializer）

- 入口：`DataStoreBootInitializer`（`ciyin` 包顶层），在 `runKoinBoot { ... }` 中与各 Boot 一并注册。
- `DataStoreAutoConfiguration`（internal）：若未声明则提供 `FileSystem.SYSTEM`、默认 `DataStorePath`（
  `Context.files.dataDir` + 目录名）、`DataStoreFactory`。
- 目录名：默认子目录 `datastore`；可通过 Koin property **`datastore.directory`**（常量
  `DataStoreProperties.DATASTORE_DIRECTORY`）覆盖；或使用 `@KoinPropInstance("datastore")` 的
  `DataStoreProperties` 注入整套默认配置（与 `component:koin` 属性机制一致）。
- 单测/自定义场景：可手动 `single<DataStorePath> { ... }`、`single<FileSystem> { FakeFileSystem() }`
  再拉 `DataStoreBootInitializer()`，参考
  `component/data-store/src/desktopTest/kotlin/DataStoreTest.kt`。

### KoinExt（Scope 内多实例）

- `ScopeDSL.dataStore(name, fileName, serializer, scope)`：在 **Koin Scope** 内按 `named(name)` 注册
  `scoped` 的 `DataStore<T>`。
- `Scope.getDataStore<T>(name)` / `KoinComponent.getDataStore<T>(name)`：按名解析。
- `jsonDataStoreSerializer(...)`：构建 `JsonDataStoreSerializer` 的便捷函数。

## 与 app 的配合

- 全局单类型偏好：常见写法是
  `class XxxDataStore : DataStore<XxxPreferences> by getKoin().get<DataStoreFactory>().create(defaultValue = XxxPreferences())`
  （与 sample 中 `AiImageDataStore` / `AiChatDataStore` 一致）。
- 目录与模块边界、Repository/DataStore 命名约定见 [data-domain](../data-domain/SKILL.md)。

## 自检清单

- [ ] 同一逻辑文件是否只通过 **一个** `DataStoreFactory.create` 路径创建（避免绕过工厂导致多实例）。
- [ ] `@Serializable` 模型变更是否考虑向后兼容（`ignoreUnknownKeys` 可缓解读旧数据，字段默认值与迁移策略仍要评估）。
- [ ] 桌面/单测是否需要替换 `FileSystem` 或 `DataStorePath` 以隔离磁盘路径。

## 延伸阅读

- Koin Boot 接入范式：`.docs/guides/koinboot-third-party-library-adaptation-guide.md`（文中含
  DataStore 小节示例）。
