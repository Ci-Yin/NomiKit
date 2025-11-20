package ciyin.serialization.json

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlin.reflect.KClass

/**
 * Moshi JSON 实现
 */
class MoshiJsonCodec(override val builder: JsonBuilder) : JsonCodec {
    private val moshi = Moshi.Builder().apply {
        // 处理 null 值序列化
        if (!builder.explicitNulls) {
            add(SkipNullValuesAdapterFactory())
        }

        // 处理宽松模式
        if (builder.isLenient) {
            add(LenientAdapterFactory())
        }

        // 处理默认值 - Moshi 默认会序列化所有字段，包括默认值
        // 如果不需要编码默认值，需要自定义 adapter
        if (!builder.encodeDefaults) {
            add(SkipDefaultValuesAdapterFactory())
        }

        // 处理特殊浮点值
        if (builder.allowSpecialFloatingPointValues) {
            // Moshi 需要自定义 adapter 来处理 NaN 和 Infinity
            add(SpecialFloatAdapterFactory())
        }

        // 添加 Kotlin 支持（应该在最后）
        addLast(KotlinJsonAdapterFactory())
    }.build()

    override fun <T : Any> fromJson(json: String, type: KClass<T>): T {
        val typeRef = object : TypeToken<T>() {}.type
        val adapter = moshi.adapter<T>(typeRef)
        return adapter.fromJson(json)!!
    }

    override fun <T : Any> toJson(value: T): String {
        val adapter = moshi.adapter(value::class.java) as JsonAdapter<T>
        return if (builder.prettyPrint) {
            adapter.indent(builder.prettyPrintIndent).toJson(value)
        } else {
            adapter.toJson(value)
        }
    }
}

/**
 * TypeToken - 用于捕获泛型类型信息
 *
 * 这是一个经典的技巧，用于在运行时获取泛型类型信息
 * 类似于 Gson 的 TypeToken 和 Moshi 的 Types.newParameterizedType
 */
abstract class TypeToken<T> {
    val type: Type = (javaClass.genericSuperclass as ParameterizedType)
        .actualTypeArguments[0]
}

// 自定义 AdapterFactory 示例（需要根据实际需求实现）

/**
 * 跳过 null 值的 AdapterFactory
 */
private class SkipNullValuesAdapterFactory : JsonAdapter.Factory {
    override fun create(type: Type, annotations: Set<Annotation>, moshi: Moshi): JsonAdapter<*>? {
        return null // 简化实现，实际需要包装原有 adapter
    }
}

/**
 * 宽松模式的 AdapterFactory
 */
private class LenientAdapterFactory : JsonAdapter.Factory {
    override fun create(type: Type, annotations: Set<Annotation>, moshi: Moshi): JsonAdapter<*>? {
        return null // 简化实现
    }
}

/**
 * 跳过默认值的 AdapterFactory
 */
private class SkipDefaultValuesAdapterFactory : JsonAdapter.Factory {
    override fun create(type: Type, annotations: Set<Annotation>, moshi: Moshi): JsonAdapter<*>? {
        return null // 简化实现
    }
}

/**
 * 处理特殊浮点值的 AdapterFactory
 */
private class SpecialFloatAdapterFactory : JsonAdapter.Factory {
    override fun create(type: Type, annotations: Set<Annotation>, moshi: Moshi): JsonAdapter<*>? {
        if (type != Double::class.java && type != Float::class.java) return null

        return object : JsonAdapter<Number>() {
            override fun fromJson(reader: JsonReader): Number? {
                if (reader.peek() == JsonReader.Token.STRING) {
                    val value = reader.nextString()
                    return when (value) {
                        "NaN" -> Double.NaN
                        "Infinity" -> Double.POSITIVE_INFINITY
                        "-Infinity" -> Double.NEGATIVE_INFINITY
                        else -> value.toDouble()
                    }
                }
                return reader.nextDouble()
            }

            override fun toJson(writer: JsonWriter, value: Number?) {
                if (value == null) {
                    writer.nullValue()
                    return
                }
                val doubleValue = value.toDouble()
                when {
                    doubleValue.isNaN() -> writer.value("NaN")
                    doubleValue.isInfinite() -> writer.value(
                        if (doubleValue > 0) "Infinity" else "-Infinity"
                    )

                    else -> writer.value(doubleValue)
                }
            }
        }
    }
}