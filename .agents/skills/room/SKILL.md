---
name: room
description: 指导在 CiYinApplication 中使用 `component:room`（KMP Room + Koin 自动装配、Bundled SQLiteDriver、schema、单测 KSP）。Entity 持久化字段须 `@ColumnInfo`，列名 **snake_case**。在新增实体、DAO、排查 Room 构建问题时使用；**除非用户明确要求，否则不要求也不实现**手写 Migration / AutoMigration / 跨版本数据兼容。涉及 `@Entity` / `@Dao` / `RoomDatabase` / `TypeConverter` / `singleDao` / `singleDatabase` 时自动适用。
disable-model-invocation: true
---

# component:room 使用说明

## 模块位置与职责

- 路径：`component/room/`
- 职责：提供 **Room KMP 运行时依赖**、**Koin 自动装配**（`RoomDatabase.Builder` / `RoomDatabase` / 分库
  Scope）、以及各平台的 `expect/actual` 数据库构建入口。
- 业务侧的 `@Database` / `@Entity` / `@Dao` 仍放在消费方（例如 `app/shared` 的 `com.ciyin.app.data.*`
  ），本模块不包含业务表结构。

## Gradle 与生成物

- 约定插件：`multiplatform-lib-targets`、`ksp`、`kotlin.serialization`、`androidx.room`（见
  `component/room/build.gradle.kts`）。
- `room { schemaDirectory("${projectDir}/schemas") }`：构建可导出 schema 快照；改表结构时按模块约定处理
  `schemas` 目录即可。
- 桌面单测：`kspDesktopTest` 注入 Room compiler（仅用于 `component/room` 内演示/测试）。

## Koin 装配要点

- 入口：`RoomBootInitializer` 注册 `RoomAutoConfiguration`。
- `RoomProperties`：库名、后缀、journal、查询协程上下文、迁移策略等（`@KoinPropInstance("room")`
  ）；默认不启用破坏性迁移，必须由调用方显式开启。
- 迁移配置互斥：启用全量 `fallbackToDestructiveMigration` 时，不再叠加
  `fallbackToDestructiveMigrationOnDowngrade`；Room 2.8.x 的 downgrade-only 调用会重新要求普通升级必须有
  Migration，叠加会导致 3→4 等升级仍崩溃。
- `RoomDatabase.Builder<*>`：默认用 `BundledSQLiteDriver()`，并套用上述配置与查询上下文。
- **按 DAO 注册**：`Module.singleDao(AppDatabase::xxxDao)`，通过 `RoomDatabaseScope` +
  `R::class.qualifiedName` 分库。
- **同名多库实例**：`singleDatabase<AppDatabase>("qualifier")` + 带 qualifier 的
  `get<AppDatabase>(_q(...))`。

各 `expect` 实现在 `androidMain` / `iosMain` / `desktopMain` 的 `ciyin.room` 包内（
`Context.createDatabaseBuilder` / `createInMemoryDatabaseBuilder`）。

## 与 app/shared 的配合

- 参考 `com.ciyin.app.data.core.datasource.AppDatabase`：`@Database` + `@ConstructedBy` +
  `expect object ... RoomDatabaseConstructor` 为 KMP Room 常规写法。
- 业务实体、DAO、Koin `singleDao` 注册放在对应 `data/<feature>/` 的 `datasource` / `di`
  中（与 [data-domain](../data-domain/SKILL.md) 目录约定一致）。

## `@Entity` 与 `@ColumnInfo`（列名 snake_case）

新建或修改 `@Entity` 时，**每个需要落库的构造器属性**都应显式标注 `@ColumnInfo(name = "...")`，`name`
使用 **下划线命名（snake_case）**，与 Kotlin 属性名（通常为 camelCase）解耦，避免依赖 Room 默认驼峰转列规则。

```kotlin
@Entity(tableName = "ai_image_history")
data class ExampleEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: Long,
    @ColumnInfo(name = "created_at_ms") val createdAtMs: Long,
    @ColumnInfo(name = "prompt_text") val promptText: String,
)
```

`@PrimaryKey`、`@Embedded` 内嵌字段的子列等，凡映射到独立 SQL 列的，均按同样规则写清 `name`。仅
`@Ignore` 的非持久化字段不需要 `@ColumnInfo`。

## 数据迁移与版本兼容（默认不做）

**除非用户在当前对话或任务中明确要求「保留已有本地库数据并升级 schema」**，协助修改 Room 时：

- **不要**新增或改写手写 `Migration`；
- **不要**新增或扩展 `@Database(autoMigrations = …)` / `AutoMigration`；
- **不要**为旧数据做额外兼容分支、双写、渐进式读旧表等方案。

允许且默认期望的做法：按需提高 `@Database.version`、改实体/索引；如果没有迁移，Room 应抛出缺少
Migration 的错误，直到调用方显式配置 `room.migration.fallbackToDestructive=true` 或补充 `Migration` /
`AutoMigration` 与测试。

## 储存 `List<*>` 或复杂字段时的 TypeConverter 策略

如果要储存 `List<*>` 的对象时，可以先看看官方的 TypeConverter 或者
`app/shared/src/commonMain/kotlin/com/ciyin/app/data/core/util/Converter.kt` 能不能实现，不能实现的时候就在对应的
`data/*/util/` 下新建一个专属的 TypeConverter，不要手动序列化成json再存进room。

### 落地说明（与上文约束一致）

1. **先查官方**
   ：阅读 [AndroidX Room — 类型转换器](https://developer.android.com/training/data-storage/room/referencing-data#type-converters)
   与当前 Room KMP 文档，确认是否可用内置/推荐方式（例如用**关联表 + `@Relation`**
   表达「一对多」对象列表，而不是把列表塞进单列）。
2. **再查共享 `Converter`**：`com.ciyin.app.data.core.util.Converter` 若已有**完全匹配**的
   `@TypeConverter` 方法对，可在实体或 DAO 上通过 `@TypeConverters(Converter::class)` 复用。**注意**
   ：该文件中部分实现使用 `kotlinx.serialization` 的 JSON；**新增持久化类型时不要照搬「整段 JSON
   字符串落单列」作为默认方案**，除非团队明确接受且与上条「不要手动序列化成 json 再存」的例外一致。
3. **新建专属转换器**：当共享 `Converter` 不适用时，在对应功能的
   `app/shared/src/commonMain/kotlin/com/ciyin/app/data/<feature>/util/` 下新增专用类（例如
   `XxxTypeConverter`），仅承载该 feature 需要的类型与列之间的映射；在 `@Entity` 或 `@Database` 上
   `@TypeConverters(...)` 注册。
4. **优先非 JSON 的映射方式**：能拆表、能 `@Embedded`、能用定界符/位图表达简单标量列表时，优先这些方式；避免把任意
   `List<自定义对象>` 整体打成 JSON blob 作为长期建模手段。

## 自检清单

- [ ] 新实体是否已更新 `@Database` 的 `entities` 与 `version`。
- [ ] 每个持久化属性是否已加 `@ColumnInfo(name = "...")`，且 `name` 为 **snake_case**。
- [ ] **未**在用户明确要求的前提下，不增加手写 `Migration`、`AutoMigration` 或跨版本兼容逻辑（见上文《数据迁移与版本兼容》）。
- [ ] 需要自定义列类型时，`@TypeConverters` 是否指向**最小范围**（实体级优于全库级，除非确有必要）。
- [ ] `List` / 嵌套对象是否已按上文评估官方方案与 `Converter.kt`，不足则是否在 `data/<feature>/util/`
  增加专用 `TypeConverter` 且避免随意 JSON blob。
