package ciyin.serialization.yaml

import ciyin.io.File
import ciyin.io.readText
import ciyin.io.writeText
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import net.mamoe.yamlkt.Yaml

/**
 * 将 YAML 字符串反序列化为指定类型的对象。
 *
 * 类型需要通过 kotlinx.serialization 提供序列化器，通常使用 `@Serializable` 标注。
 *
 * @param T 目标对象类型。
 * @return 解析后的对象实例。
 */
inline fun <reified T> String.fromYaml(): T {
    return Yaml.decodeFromString<T>(this)
}

/**
 * 将当前对象序列化为 YAML 字符串。
 *
 * 类型需要通过 kotlinx.serialization 提供序列化器，通常使用 `@Serializable` 标注。
 *
 * @param T 当前对象类型。
 * @return YAML 格式字符串。
 */
inline fun <reified T : Any> T.toYamlStr(): String {
    return Yaml.encodeToString<T>(this)
}

/**
 * 将对象序列化为 YAML 并写入文件。
 *
 * 若文件已存在，会覆盖原有内容。
 *
 * @param T 待写入对象类型。
 * @param src 待写入的对象实例。
 */
inline fun <reified T : Any> File.writeYaml(src: T) {
    return writeText(src.toYamlStr())
}

/**
 * 从文件读取 YAML 内容并反序列化为指定类型。
 *
 * @param T 目标对象类型。
 * @return 解析后的对象实例。
 */
inline fun <reified T : Any> File.readYaml(): T {
    return readText().fromYaml()
}

/**
 * 使用显式序列化器将对象序列化为 YAML 并写入文件。
 *
 * 若文件已存在，会覆盖原有内容。
 *
 * @param serializer 对象类型对应的序列化器。
 * @param src 待写入的对象实例。
 */
fun <T : Any> File.writeYaml(
    serializer: SerializationStrategy<T>,
    src: T
) {
    val yaml = Yaml.encodeToString(serializer, src)
    return writeText(yaml)
}

/**
 * 使用显式反序列化器从文件读取 YAML 内容并转为对象。
 *
 * @param deserializer 对象类型对应的反序列化器。
 * @return 解析后的对象实例。
 */
fun <T : Any> File.readYaml(deserializer: DeserializationStrategy<T>): T {
    return Yaml.decodeFromString(deserializer, readText())
}
