package ciyin.serialization.json

import kotlinx.serialization.json.ClassDiscriminatorMode
import kotlinx.serialization.json.JsonNamingStrategy

/**
 * 当前 [Json] 实例的配置，可通过 [Json.configuration] 获得
 * 并使用 [JsonBuilder] 构造函数进行配置。
 *
 * 可用于调试目的和自定义的 Json 特定序列化器
 * 通过 [JsonEncoder] 和 [JsonDecoder]。
 *
 * 独立的配置对象没有意义，也不能在 [Json] 之外使用，
 * 也不能从中创建新的 [Json] 实例。
 *
 * 每个属性的详细说明可在 [JsonBuilder] 类中找到。
 *
 * @property encodeDefaults 指定是否应编码具有默认值的属性。
 * @property ignoreUnknownKeys 指定在解码期间是否应忽略 JSON 中存在但在类中不存在的键。
 * @property isLenient 指定解码器是否应对格式错误的输入宽容。
 * @property allowStructuredMapKeys 指定是否允许将非字符串键的映射编码为 JSON 对象。
 * @property prettyPrint 指定输出是否应格式化以便于阅读。
 * @property explicitNulls 指定 `null` 属性值是否应作为 `null` 写入 JSON。
 * 如果为 `false`，则在编码期间将跳过具有 `null` 值的属性。
 * @property prettyPrintIndent 用于漂亮打印的缩进字符串。
 * @property coerceInputValues 指定是否应将不匹配的值强制转换为目标类型（如果可能）。
 * @property useArrayPolymorphism 指定是否应将多态序列化为数组。
 * @property classDiscriminator 用于多态序列化的类鉴别器属性的名称。
 * @property allowSpecialFloatingPointValues 指定是否允许编码和解码特殊浮点值，如 `NaN` 和 `Infinity`。
 * @property useAlternativeNames 指定是否应使用 `@JsonNames` 注解中指定的备用名称。
 * @property namingStrategy 用于在 Kotlin 名称和 JSON 名称之间转换的命名策略。
 * @property decodeEnumsCaseInsensitive 指定在解码枚举时是否应忽略大小写。
 * @property allowTrailingComma 指定是否允许尾随逗号。
 * @property allowComments 指定是否允许注释。
 * @property classDiscriminatorMode 指定类鉴别器的使用方式。
 */
data class JsonConfiguration internal constructor(
    val encodeDefaults: Boolean = false,
    val ignoreUnknownKeys: Boolean = false,
    val isLenient: Boolean = false,
    val allowStructuredMapKeys: Boolean = false,
    val prettyPrint: Boolean = false,
    val explicitNulls: Boolean = true,
    val prettyPrintIndent: String = "    ",
    val coerceInputValues: Boolean = false,
    val useArrayPolymorphism: Boolean = false,
    val classDiscriminator: String = "type",
    val allowSpecialFloatingPointValues: Boolean = false,
    val useAlternativeNames: Boolean = true,
    val namingStrategy: JsonNamingStrategy? = null,
    val decodeEnumsCaseInsensitive: Boolean = false,
    val allowTrailingComma: Boolean = false,
    val allowComments: Boolean = false,
    val classDiscriminatorMode: ClassDiscriminatorMode = ClassDiscriminatorMode.POLYMORPHIC,
)