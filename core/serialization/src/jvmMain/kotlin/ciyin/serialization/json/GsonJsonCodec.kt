package ciyin.serialization.json

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import kotlin.reflect.KClass

/**
 * Gson JSON 实现
 */
class GsonJsonCodec(override val builder: JsonBuilder) : JsonCodec {
    private val gson = GsonBuilder().apply {
        // === 基础配置 ===

        // 处理 null 值序列化
        // Gson 的 serializeNulls() 同时控制编码和解码时的 null 处理
        if (builder.explicitNulls || builder.encodeDefaults) {
            serializeNulls()
        }

        // 处理漂亮打印
        if (builder.prettyPrint) {
            setPrettyPrinting()
            // 注意：Gson 不支持自定义缩进，固定为 2 个空格
            // 如果 builder.prettyPrintIndent 不是默认值，可以记录警告
            if (builder.prettyPrintIndent != "    ") {
                println("警告: Gson 不支持自定义缩进，将使用固定的 2 个空格")
            }
        }

        // 处理宽松模式
        if (builder.isLenient) {
            setLenient()
        }

        // 处理特殊浮点值 (NaN, Infinity)
        if (builder.allowSpecialFloatingPointValues) {
            serializeSpecialFloatingPointValues()
        }

        // === 高级配置 ===

        // 禁用 HTML 转义（默认 Gson 会转义 <, >, &, = 等字符）
        disableHtmlEscaping()

        // 处理枚举大小写不敏感（需要自定义 TypeAdapterFactory）
        if (builder.decodeEnumsCaseInsensitive) {
            registerTypeAdapterFactory(CaseInsensitiveEnumTypeAdapterFactory())
        }

        // 处理字段命名策略
        builder.namingStrategy?.let { strategy ->
            // 注意：Gson 的 FieldNamingStrategy 只能访问 Field 对象
            // 而 JsonNamingStrategy 需要 SerialDescriptor 和 elementIndex
            // 这里无法完美映射，只能使用字段名
            setFieldNamingStrategy { field ->
                // 由于无法获取 SerialDescriptor 和 elementIndex，
                // 这里只能保持原字段名或使用简化的命名策略
                // 如果需要完整支持，建议使用 kotlinx.serialization
                field.name
            }
        }

        // 注册自定义类型适配器
        if (builder.coerceInputValues) {
            // 处理强制转换：null -> 默认值，未知枚举 -> 默认值
            registerTypeAdapterFactory(CoercingTypeAdapterFactory())
        }

        // Gson 默认行为说明：
        // - ignoreUnknownKeys: Gson 默认忽略未知字段（无需配置）
        // - allowTrailingComma: Gson 在宽松模式下支持（通过 setLenient()）
        // - allowComments: Gson 在宽松模式下支持（通过 setLenient()）
        // - useAlternativeNames: 需要通过 @SerializedName 注解实现
        // - allowStructuredMapKeys: Gson 默认支持

    }.create()

    override fun <T : Any> fromJson(json: String, type: KClass<T>): T {
        return gson.fromJson(json, type.java)
    }

    override fun <T : Any> toJson(value: T): String {
        return gson.toJson(value)
    }
}

// === 自定义 TypeAdapterFactory 实现 ===

/**
 * 大小写不敏感的枚举 TypeAdapterFactory
 */
private class CaseInsensitiveEnumTypeAdapterFactory : TypeAdapterFactory {
    override fun <T : Any?> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
        val rawType = type.rawType
        if (!rawType.isEnum) return null

        @Suppress("UNCHECKED_CAST")
        return CaseInsensitiveEnumTypeAdapter(rawType as Class<out Enum<*>>) as TypeAdapter<T>
    }
}

private class CaseInsensitiveEnumTypeAdapter<T : Enum<T>>(
    private val enumClass: Class<T>
) : TypeAdapter<T>() {
    private val nameToConstant = enumClass.enumConstants
        .associateBy { it.name.lowercase() }

    override fun write(out: JsonWriter, value: T?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value.name)
        }
    }

    override fun read(`in`: JsonReader): T? {
        if (`in`.peek() == JsonToken.NULL) {
            `in`.nextNull()
            return null
        }

        val value = `in`.nextString()
        return nameToConstant[value.lowercase()]
            ?: throw JsonParseException("Unknown enum value: $value for ${enumClass.simpleName}")
    }
}

/**
 * 强制转换 TypeAdapterFactory
 * 处理 null 值和未知枚举值的强制转换
 */
private class CoercingTypeAdapterFactory : TypeAdapterFactory {
    override fun <T : Any?> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
        val delegate = gson.getDelegateAdapter(this, type)

        return object : TypeAdapter<T>() {
            override fun write(out: JsonWriter, value: T?) {
                delegate.write(out, value)
            }

            override fun read(`in`: JsonReader): T? {
                return try {
                    delegate.read(`in`)
                } catch (e: Exception) {
                    // 如果解析失败，尝试跳过该值并返回 null
                    // 这模拟了 coerceInputValues 的行为
                    `in`.skipValue()
                    null
                }
            }
        }
    }
}