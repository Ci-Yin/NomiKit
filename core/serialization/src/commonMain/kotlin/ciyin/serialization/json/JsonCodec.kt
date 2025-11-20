package ciyin.serialization.json

import kotlin.reflect.KClass

/**
 * 通用 JSON 编解码接口（跨平台）
 */
interface JsonCodec {
    val builder: JsonBuilder
    fun <T : Any> fromJson(json: String, type: KClass<T>): T
    fun <T : Any> toJson(value: T): String
}
