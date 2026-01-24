package ciyin.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.core.okio.OkioSerializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.ScopeDSL

/**
 * 在 Koin 模块中创建 DataStore 单例
 *
 * @param T 数据类型
 * @param name DataStore 名称（用于区分多个 DataStore 实例）
 * @param fileName 数据文件名称
 * @param serializer 数据序列化器
 * @param scope 协程作用域
 *
 * 使用示例：
 * ```kotlin
 * module {
 *     dataStore(
 *         name = "user_settings",
 *         fileName = "user_settings.json",
 *         serializer = JsonDataStoreSerializer(
 *             defaultValue = UserSettings(),
 *             serializer = UserSettings.serializer()
 *         )
 *     )
 * }
 * ```
 */
inline fun <reified T> ScopeDSL.dataStore(
    name: String,
    fileName: String,
    serializer: OkioSerializer<T>,
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) {
    scoped(named(name)) {
        get<DataStoreFactory>().create(
            fileName = fileName,
            serializer = serializer,
            scope = scope
        )
    }
}

/**
 * 从 Koin Scope 获取指定名称的 DataStore 实例
 *
 * @param T 数据类型
 * @param name DataStore 名称
 * @return [DataStore] 实例
 */
inline fun <reified T> Scope.getDataStore(name: String): DataStore<T> = get(named(name))

/**
 * 从 KoinComponent 获取指定名称的 DataStore 实例
 *
 * @param T 数据类型
 * @param name DataStore 名称
 * @return [DataStore] 实例
 */
inline fun <reified T> KoinComponent.getDataStore(name: String): DataStore<T> = get(named(name))

/**
 * 创建 JSON DataStore 序列化器的便捷方法
 *
 * @param T 数据类型
 * @param defaultValue 默认值
 * @param serializer kotlinx-serialization 序列化器
 * @param json JSON 实例配置
 * @return [JsonDataStoreSerializer] 实例
 *
 * 使用示例：
 * ```kotlin
 * val serializer = jsonDataStoreSerializer(
 *     defaultValue = UserSettings(),
 *     serializer = UserSettings.serializer()
 * )
 * ```
 */
fun <T> jsonDataStoreSerializer(
    defaultValue: T,
    serializer: KSerializer<T>,
    json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }
): JsonDataStoreSerializer<T> = JsonDataStoreSerializer(
    defaultValue = defaultValue,
    serializer = serializer,
    json = json
)

