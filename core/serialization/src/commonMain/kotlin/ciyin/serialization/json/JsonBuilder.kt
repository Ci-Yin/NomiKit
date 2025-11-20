package ciyin.serialization.json

import kotlinx.serialization.Contextual
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.json.ClassDiscriminatorMode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.json.JsonNamingStrategy
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule

/**
 * 由 `Json { ... }` 工厂函数提供的 [Json] 实例的构建器：
 *
 * ```
 * val json = Json { // this: JsonBuilder
 *     encodeDefaults = true
 *     ignoreUnknownKeys = true
 * }
 * ```
 */
@Suppress("unused", "DeprecatedCallableAddReplaceWith")
class JsonBuilder internal constructor(configuration: JsonConfiguration = JsonConfiguration()) {
    /**
     * 指定是否应编码 Kotlin 属性的默认值。
     * 默认为 `false`。
     *
     * 示例：
     * ```
     * @Serializable
     * class Project(val name: String, val language: String = "kotlin")
     *
     * // 打印 {"name":"test-project"}
     * println(Json.encodeToString(Project("test-project")))
     *
     * // 打印 {"name":"test-project","language":"kotlin"}
     * val withDefaults = Json { encodeDefaults = true }
     * println(withDefaults.encodeToString(Project("test-project")))
     * ```
     *
     * 此选项不影响解码。
     */
    var encodeDefaults: Boolean = configuration.encodeDefaults

    /**
     * 指定是否应为可空属性编码 `null` 值，并且在解码期间必须存在于 JSON 对象中。
     *
     * 禁用此标志时，具有 `null` 值的属性不会被编码；
     * 在解码期间，对于没有默认值的可空属性，字段值的缺失被视为 `null`。
     *
     * 默认为 `true`。
     *
     * 可以让解码器将一些无效的输入数据视为缺失字段，以增强此标志的功能。
     * 详情请参阅 [coerceInputValues] 文档。
     *
     * 用法示例：
     * ```
     * @Serializable
     * data class Project(val name: String, val description: String?)
     * val implicitNulls = Json { explicitNulls = false }
     *
     * // 编码
     * // 打印 '{"name":"unknown","description":null}'。null 是显式的
     * println(Json.encodeToString(Project("unknown", null)))
     * // 打印 '{"name":"unknown"}'，null 被省略
     * println(implicitNulls.encodeToString(Project("unknown", null)))
     *
     * // 解码
     * // 打印 Project(name=unknown, description=null)
     * println(implicitNulls.decodeFromString<Project>("""{"name":"unknown"}"""))
     * // 因 "MissingFieldException: Field 'description' is required" 而失败
     * Json.decodeFromString<Project>("""{"name":"unknown"}""")
     * ```
     *
     * 如果要使用此标志并且有具有可空但默认值不为 `null` 的属性的非典型类，请格外小心。
     * 在这种情况下，如果从输出中省略 `null`，编码和解码将不对称。
     * 此类陷阱的示例：
     *
     * ```
     * @Serializable
     * data class Example(val nullable: String? = "non-null default")
     *
     * val json = Json { explicitNulls = false }
     *
     * val original = Example(null)
     * val s = json.encodeToString(original)
     * // 由于 explicitNulls 标志，打印 "{}"
     * println(s)
     * val decoded = json.decodeFromString<Example>(s)
     * // 打印 "non-null default"，因为输入中缺少 `nullable` 字段，所以插入了默认值
     * println(decoded.nullable)
     * println(decoded != original) // true
     * ```
     */
    var explicitNulls: Boolean = configuration.explicitNulls

    /**
     * 指定在输入 JSON 中遇到未知属性时是否应忽略它们，而不是抛出 [SerializationException]。
     * 默认为 `false`。
     *
     * 用法示例：
     * ```
     * @Serializable
     * data class Project(val name: String)
     * val withUnknownKeys = Json { ignoreUnknownKeys = true }
     * // Project(name=unknown)，"version" 被完全忽略
     * println(withUnknownKeys.decodeFromString<Project>("""{"name":"unknown", "version": 2.0}"""))
     * // 因 "Encountered an unknown key 'version'" 而失败
     * Json.decodeFromString<Project>("""{"name":"unknown", "version": 2.0}""")
     * ```
     */
    var ignoreUnknownKeys: Boolean = configuration.ignoreUnknownKeys

    /**
     * 移除 JSON 规范限制 (RFC-4627)，使解析器对格式错误的输入更加宽容。
     * 在宽松模式下，允许使用未加引号的 JSON 键和字符串值。
     *
     * 设置此标志后可接受的无效 JSON 示例：
     * `{key: value}` 可以解析为 `@Serializable class Data(val key: String)`。
     *
     * 其宽松性将来可能会扩展，以便宽松解析器对输入中的无效值更加宽容。
     *
     * 默认为 `false`。
     */
    var isLenient: Boolean = configuration.isLenient

    /**
     * 指定生成的 JSON 是否应进行漂亮打印：格式化并优化以方便人类阅读。
     * 默认为 `false`。
     *
     * 用法示例：
     * ```
     * @Serializable
     * class Key(val type: String, val opens: String)
     * val pretty = Json { prettyPrint = true }
     * /*
     *  * 打印
     *  * {
     *  *     "type": "keycard",
     *  *     "opens": "secret door"
     *  * }
     *  */
     * println(pretty.encodeToString(Key("keycard", "secret door")))
     * ```
     */
    var prettyPrint: Boolean = configuration.prettyPrint

    /**
     * 指定与 [prettyPrint] 模式一起使用的缩进字符串。
     * 只允许使用空白字符：' '、'\n'、'\r' 或 '\t'。
     * 默认为 4 个空格。
     *
     * 实验性说明：此 API 是实验性的，因为
     * 目前尚不清楚此选项是否有令人信服的用例。
     */
    var prettyPrintIndent: String = configuration.prettyPrintIndent

    /**
     * 在以下情况下启用对不正确的 JSON 值的强制转换：
     *
     *   1. JSON 值为 `null`，但属性类型不可为空。
     *   2. 属性类型为枚举类型，但 JSON 值包含未知的枚举成员。
     *
     * 强制转换的值被视为缺失；它们将被替换为默认属性值（如果存在），
     * 或者如果 [explicitNulls] 标志设置为 `false` 且属性可空（对于枚举），则替换为 `null`。
     *
     * 用法示例：
     * ```
     * enum class Choice { A, B, C }
     *
     * @Serializable
     * data class Example1(val a: String = "default", b: Choice = Choice.A, c: Choice? = null)
     *
     * val coercingJson = Json { coerceInputValues = true }
     * // 解码 Example1("default", Choice.A, null) 实例
     * coercingJson.decodeFromString<Example1>("""{"a": null, "b": "unknown", "c": "unknown"}""")
     *
     * @Serializable
     * data class Example2(val c: Choice?)
     *
     * val coercingImplicitJson = Json(coercingJson) { explicitNulls = false }
     * // 解码 Example2(null) 实例。
     * coercingImplicitJson.decodeFromString<Example1>("""{"c": "unknown"}""")
     * ```
     *
     * 默认为 `false`。
     */
    var coerceInputValues: Boolean = configuration.coerceInputValues

    /**
     * 用于多态序列化的类描述符属性的名称。
     * 默认为 `type`。
     */
    var classDiscriminator: String = configuration.classDiscriminator

    /**
     * 定义哪些类和对象应在输出中添加类鉴别器。
     * 默认为 [ClassDiscriminatorMode.POLYMORPHIC]。
     *
     * 其他模式通常用于生成供第三方库使用的 JSON，
     * 因此，此设置不影响反序列化过程。
     */
    var classDiscriminatorMode: ClassDiscriminatorMode = configuration.classDiscriminatorMode

    /**
     * 指定 Json 实例是否使用 [JsonNames] 注解。
     *
     * 当根本不使用 [JsonNames] 时禁用此标志有时可能会带来更好的性能，
     * 特别是当使用 [ignoreUnknownKeys] 跳过大量字段时。
     * 默认为 `true`。
     */
    var useAlternativeNames: Boolean = configuration.useAlternativeNames

    /**
     * 指定应用于序列化和反序列化中所有类的所有属性的 [JsonNamingStrategy]。
     *
     * 默认为 `null`。
     *
     * 此策略适用于所有具有 [StructureKind.CLASS] 的实体。
     */
    var namingStrategy: JsonNamingStrategy? = configuration.namingStrategy

    /**
     * 启用以不区分大小写的方式解码枚举值。
     * 编码不受此选项的影响。
     *
     * 它会影响枚举序列名称和备用名称（使用 [JsonNames] 注解指定）。
     * 用法示例：
     * ```
     * enum class E { VALUE_A, @JsonNames("ALTERNATIVE") VALUE_B }
     *
     * @Serializable
     * data class Outer(val enums: List<E>)
     *
     * val json = Json { decodeEnumsCaseInsensitive = true }
     * // 打印 [VALUE_A, VALUE_B]
     * println(json.decodeFromString<Outer>("""{"enums":["Value_A", "alternative"]}""").enums)
     * // 将因 SerializationException: no such enum as 'Value_A' 而失败
     * Json.decodeFromString<Outer>("""{"enums":["Value_A", "alternative"]}""")
     * ```
     *
     * 启用此功能后，将无法再解码具有相同小写形式名称的枚举值。
     * 以下代码将抛出序列化异常：
     * ```
     * enum class CaseSensitiveEnum { One, ONE }
     * val json = Json { decodeEnumsCaseInsensitive = true }
     * // 因 SerializationException: The suggested name 'one' for enum value ONE is already one of the names for enum value One 而失败
     * json.decodeFromString<CaseSensitiveEnum>("ONE")
     * ```
     */
    var decodeEnumsCaseInsensitive: Boolean = configuration.decodeEnumsCaseInsensitive

    /**
     * 允许解析器接受 JSON 对象和数组中的尾随（末尾）逗号，
     * 使 `[1, 2, 3,]` 和 `{"key": "value",}` 之类的输入有效。
     * 不影响编码。
     * 默认为 `false`。
     */
    var allowTrailingComma: Boolean = configuration.allowTrailingComma

    /**
     * 允许解析器接受 JSON 输入中的 C/Java 风格的注释。
     *
     * 注释将被跳过，并且不会存储在任何地方；此设置不会以任何方式影响编码。
     *
     * 更具体地说，注释是不属于 JSON 键或值的子字符串，符合以下条件之一：
     *
     * 1. 以 `//` 字符开头，以换行符 `\n` 结尾。
     * 2. 以 `/*` 字符开头，以 `*/` 字符结尾。不支持嵌套块注释：
     *  无论有多少 `/*` 字符，第一个 `*/` 都将结束注释。
     *
     *  默认为 `false`。
     */
    var allowComments: Boolean = configuration.allowComments

    /**
     * 移除 JSON 规范对特殊浮点值（如 `NaN` 和 `Infinity`）的限制
     * 并启用它们作为不带引号的浮点文字的序列化和反序列化。
     * 启用时，请确保接收方能够编码和解码这些特殊值。
     * 此选项影响编码和解码。
     * 默认为 `false`。
     *
     * 用法示例：
     * ```
     * val floats = listOf(1.0, 2.0, Double.NaN, Double.NEGATIVE_INFINITY)
     * val json = Json { allowSpecialFloatingPointValues = true }
     * // 打印 [1.0,2.0,NaN,-Infinity]
     * println(json.encodeToString(floats))
     * // 打印 [1.0, 2.0, NaN, -Infinity]
     * println(json.decodeFromString<List<Double>>("[1.0,2.0,NaN,-Infinity]"))
     * ```
     */
    var allowSpecialFloatingPointValues: Boolean = configuration.allowSpecialFloatingPointValues

    /**
     * 通过将映射的序列化形式从 JSON 对象（键值对）更改为
     * 类似 `[k1, v1, k2, v2]` 的扁平数组，从而使结构化对象能够序列化为映射键。
     * 默认为 `false`。
     */
    var allowStructuredMapKeys: Boolean = configuration.allowStructuredMapKeys

    /**
     * 将多态序列化切换为默认数组格式。
     * 这是用于旧版 JSON 格式的选项，通常不应使用。
     * 默认为 `false`。
     *
     * 仅当 [classDiscriminatorMode] 处于默认的 [ClassDiscriminatorMode.POLYMORPHIC] 状态时，才能使用此选项。
     */
    var useArrayPolymorphism: Boolean = configuration.useArrayPolymorphism

    /**
     * 包含在生成的 [Json] 实例中使用的上下文和多态序列化器的模块。
     *
     * @see SerializersModule
     * @see Contextual
     * @see Polymorphic
     */
    var serializersModule: SerializersModule = EmptySerializersModule()

    @OptIn(ExperimentalSerializationApi::class)
    internal fun build(): JsonConfiguration {
        if (useArrayPolymorphism) {
            require(classDiscriminator == defaultDiscriminator) {
                "指定数组多态时，不应指定类鉴别器"
            }
            require(classDiscriminatorMode == ClassDiscriminatorMode.POLYMORPHIC) {
                "仅当 classDiscriminatorMode 处于默认的 POLYMORPHIC 状态时，才能使用 useArrayPolymorphism 选项。"
            }
        }

        if (!prettyPrint) {
            require(prettyPrintIndent == defaultIndent) {
                "使用默认打印模式时，不应指定缩进"
            }
        } else if (prettyPrintIndent != defaultIndent) {
            // JSON 规范允许的空白字符
            val allWhitespaces =
                prettyPrintIndent.all { it == ' ' || it == '\t' || it == '\r' || it == '\n' }
            require(allWhitespaces) {
                "只允许使用空格、制表符、换行符和回车符作为漂亮打印符号。但当前为 $prettyPrintIndent"
            }
        }

        return JsonConfiguration(
            encodeDefaults,
            ignoreUnknownKeys,
            isLenient,
            allowStructuredMapKeys,
            prettyPrint,
            explicitNulls,
            prettyPrintIndent,
            coerceInputValues,
            useArrayPolymorphism,
            classDiscriminator,
            allowSpecialFloatingPointValues,
            useAlternativeNames,
            namingStrategy,
            decodeEnumsCaseInsensitive,
            allowTrailingComma,
            allowComments,
            classDiscriminatorMode
        )
    }
}

private const val defaultIndent = "    "
private const val defaultDiscriminator = "type"