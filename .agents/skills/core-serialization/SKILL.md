---
name: core-serialization
description: Use the core/serialization Kotlin Multiplatform JSON helper library (package ciyin.serialization.json). Covers using fromJson, toJsonStr, File.readJson/writeJson, modifyJson, toJsonElement, toJsonPrimitive, jsonObjectOf, jsonArrayOf, JsonObject/JsonArray typed accessors, path helpers, merge, and flat Map to nested JsonElement conversion. Use when 用户要在 NomiKit 中使用 ciyin.serialization.json、处理 JSON 序列化/反序列化、构造 JsonElement、读取 JSON 字段、修改可序列化对象字段，或询问这个库的注意事项。
---

# core/serialization 使用指南

`core/serialization` 提供 `ciyin.serialization.json` 包下的一组 KMP JSON helper，底层基于
`kotlinx.serialization.json.Json`。使用它时优先调用这些扩展函数，不要重新引入旧的 `JsonProvider` /
`JsonCodec` / Gson / Moshi 写法。

## 序列化与反序列化

```kotlin
@Serializable
data class User(val id: Int, val name: String)

val user: User = """{"id":1,"name":"Alice"}""".fromJson()
val json: String = user.toJsonStr()

val prettyJson = user.toJsonStr {
    prettyPrint = true
}

val tolerantUser: User = """{"id":1,"name":"Alice","extra":true}""".fromJson {
    ignoreUnknownKeys = true
}
```

注意事项：

- 目标类型必须可被 `kotlinx.serialization` 处理；自定义数据类需要 `@Serializable`。
- 配置用 `JsonBuilder.() -> Unit`，例如 `prettyPrint = true`、`ignoreUnknownKeys = true`。
- 不要使用旧的 `isFormat` 布尔参数；格式化统一通过 `prettyPrint = true`。
- 调用处参数达到 3 个及以上时，按项目规则使用命名参数并换行。

## 文件读写 JSON

```kotlin
file.writeJson(user) {
    prettyPrint = true
}

val restored: User = file.readJson()

file.writeJson(User.serializer(), user)
val restoredBySerializer = file.readJson(User.serializer())
```

注意事项：

- reified 重载适合调用处能直接提供目标类型的场景。
- serializer/deserializer 重载适合运行时已有 `KSerializer<T>` 的场景。
- 读取失败、内容无效、类型不匹配时让异常向外暴露或在调用层转换为明确错误模型，不要静默吞掉。

## 修改可序列化对象字段

```kotlin
val updated = user.modifyJson(
    updates = mapOf(
        "name" to JsonPrimitive("Bob")
    )
)
```

注意事项：

- `modifyJson` 只适合序列化后为 `JsonObject` 的类型，例如普通 data class。
- 它是浅层字段替换；嵌套对象需要传入完整的嵌套 `JsonObject`。
- 默认使用 `Json { ignoreUnknownKeys = true }`，不存在的字段会被忽略。
- 对基本类型、List 等非对象类型调用会抛 `IllegalArgumentException`。

## 构造 JsonElement

```kotlin
val obj = jsonObjectOf(
    "name" to "Alice",
    "tags" to jsonArrayOf("new", "vip"),
    "profile" to mapOf("age" to 18)
)

val element = listOf(1, 2, 3).toJsonElement()
val primitive = "hello".toJsonPrimitive()
```

注意事项：

- `toJsonElement` 支持 null、基本类型、`JsonElement`、Map、Iterable、数组和可序列化对象。
- `toJsonPrimitive` 只支持 String、Number、Boolean、Char；复杂对象请用 `toJsonElement`。
- Map 的 null key 会被忽略；非 null key 会用 `toString()` 作为 JSON 字段名。

## 读取 JsonObject / JsonArray 字段

```kotlin
val id = jsonObject.getInt("id")
val name = jsonObject.getStringOrNull("name")
val fallback = jsonObject.getStringOrDefault("title", "Untitled")
val city = jsonObject.getStringByPath("profile.city")
```

注意事项：

- `getXxxOrNull` 适合可缺失字段；强制 `getXxx` 在缺失或类型不匹配时抛异常。
- `getXxxOrDefault` 只在缺失或无法转成目标类型时返回默认值。
- path helper 使用点号分隔，只支持对象路径，不解析数组下标。
- `merge` 会递归合并两个 `JsonObject`；同名非对象字段由右侧覆盖。
- `hasNonNullKey` 可区分字段不存在、字段为 `JsonNull`、字段有实际值。

## 扁平 Map 转嵌套 JSON

```kotlin
val element = convertMapToJsonElement(
    mapOf(
        "user.name" to "Alice",
        "user.age" to 18,
        "settings.enabled" to true
    )
)
```

注意事项：

- key 中的 `.` 表示嵌套对象路径。
- `"user"` 与 `"user.name"` 这类路径冲突会抛 `IllegalArgumentException`。
- `convertToFinalJsonElement` 支持 null、Map、Iterable、Array、String、Number、Boolean、Enum。
