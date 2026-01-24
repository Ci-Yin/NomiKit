package ciyin.datastore

import androidx.datastore.core.okio.OkioSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import okio.BufferedSink
import okio.BufferedSource

/**
 * 基于 kotlinx-serialization JSON 的 DataStore 序列化器
 *
 * 将数据序列化为 JSON 格式存储到 DataStore 中。
 *
 * @param T 数据类型，必须使用 @Serializable 注解
 * @property defaultValue 默认值，当数据文件不存在或解析失败时使用
 * @property serializer kotlinx-serialization 序列化器
 * @property json JSON 实例，可自定义配置
 *
 * 使用示例：
 * ```kotlin
 * @Serializable
 * data class UserSettings(
 *     val theme: String = "light",
 *     val language: String = "zh"
 * )
 *
 * val serializer = JsonDataStoreSerializer(
 *     defaultValue = UserSettings(),
 *     serializer = UserSettings.serializer()
 * )
 * ```
 */
class JsonDataStoreSerializer<T>(
    override val defaultValue: T,
    private val serializer: KSerializer<T>,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }
) : OkioSerializer<T> {

    /**
     * 从数据源读取并反序列化数据
     *
     * @param source 数据源
     * @return 反序列化后的数据对象
     */
    override suspend fun readFrom(source: BufferedSource): T {
        val text = source.readUtf8()
        return if (text.isBlank()) {
            defaultValue
        } else {
            json.decodeFromString(serializer, text)
        }
    }

    /**
     * 序列化数据并写入到目标
     *
     * @param t 要序列化的数据对象
     * @param sink 写入目标
     */
    override suspend fun writeTo(t: T, sink: BufferedSink) {
        val text = json.encodeToString(serializer, t)
        sink.writeUtf8(text)
    }
}

