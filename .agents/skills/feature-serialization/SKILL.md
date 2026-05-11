---
name: feature-serialization
description: Use the feature/serialization Kotlin Multiplatform serialization helper library (package ciyin.serialization.*). Covers using non-official serialization format adapters, currently YAML via ciyin.serialization.yaml with fromYaml, toYamlStr, File.readYaml/writeYaml, and serializer overloads. Use when 用户要在 NomiKit 中使用 feature/serialization、YAML 序列化/反序列化、非官方序列化格式封装，或询问该库的使用注意事项。
---

# feature/serialization 使用指南

`feature/serialization` 是 NomiKit 对非官方序列化格式库的轻量封装层。它不是业务模型层，也不是
`core/serialization` 的替代品；当前只集成 YAML，未来可以继续在这里接入 TOML、XML 等其它格式。

优先从 `ciyin.serialization.*` 包使用本模块提供的扩展函数，不要在业务代码里直接耦合底层第三方库 API。

## YAML 序列化与反序列化

当前 YAML 能力在 `ciyin.serialization.yaml` 包下，底层基于 `net.mamoe.yamlkt.Yaml`。

```kotlin
import ciyin.serialization.yaml.fromYaml
import ciyin.serialization.yaml.toYamlStr
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Int,
    val name: String,
    val tags: List<String>
)

val yaml = """
    id: 1
    name: Alice
    tags:
    - admin
    - active
""".trimIndent()

val user: User = yaml.fromYaml()
val output: String = user.toYamlStr()
```

注意事项：

- 目标类型必须可被 `kotlinx.serialization` 处理；自定义数据类需要 `@Serializable`。
- `fromYaml` 适合从 YAML 字符串读取对象，`toYamlStr` 适合把对象输出为 YAML 字符串。
- 当前封装没有暴露 YAML builder/config 参数；需要格式配置时先确认模块 API 是否已经支持，不要在调用处绕过封装直接依赖
  yamlkt。
- YAML 解析失败、字段缺失、类型不匹配时让异常向外暴露，或在调用层转换成明确错误模型，不要静默吞掉。

## 文件读写 YAML

```kotlin
import ciyin.serialization.yaml.readYaml
import ciyin.serialization.yaml.writeYaml

file.writeYaml(user)

val restored: User = file.readYaml()

file.writeYaml(User.serializer(), user)
val restoredBySerializer = file.readYaml(User.serializer())
```

注意事项：

- reified 重载适合调用处能直接提供目标类型的场景。
- serializer/deserializer 重载适合运行时已有 `KSerializer<T>`、泛型类型不方便 reified 推断的场景。
- `writeYaml` 会覆盖目标文件内容；调用前由业务层决定是否需要备份、确认或冲突处理。
- 文件能力依赖项目的 `ciyin.io.File`，不要混用平台文件类型。

## 与 JSON helper 的边界

- JSON 相关能力使用 `core/serialization` 的 `ciyin.serialization.json`。
- YAML 和未来的非官方格式能力使用 `feature/serialization` 的 `ciyin.serialization.<format>`。
- 不要为了少写 import 把 YAML API 放进 `ciyin.serialization.json`，也不要让业务代码同时直接依赖
  yamlkt 和本封装。

## 扩展到更多格式时的使用口径

未来接入新的非官方格式时，保持与 YAML 一致的调用风格：

```kotlin
val model: Model = text.fromXxx()
val text: String = model.toXxxStr()

file.writeXxx(model)
val restored: Model = file.readXxx()
```

注意事项：

- 新格式放在独立包名下，例如 `ciyin.serialization.toml`、`ciyin.serialization.xml`。
- 对外暴露项目自己的扩展函数，调用处不直接使用底层非官方库类型，除非 API 明确需要。
- 同一格式优先保持 `String`、对象、`File` 三类入口一致，降低调用侧迁移成本。
