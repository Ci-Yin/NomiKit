package ciyin.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.okio.OkioSerializer
import androidx.datastore.core.okio.OkioStorage
import ciyin.platform.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.serializer
import okio.FileSystem

/**
 * DataStore 工厂类
 *
 * 用于创建类型安全的 [DataStore] 实例，支持 kotlinx-serialization。
 * 内部维护缓存，确保同一个文件路径只创建一个 DataStore 实例。
 *
 * @property fileSystem 文件系统实例
 * @property dataStorePath 数据存储路径提供者
 */
class DataStoreFactory(
    @PublishedApi internal val fileSystem: FileSystem,
    @PublishedApi internal val dataStorePath: DataStorePath
) {

    private val logger = logger("DataStoreFactory")

    /**
     * 核心修复：将 cache 放入 companion object。
     * 这样即使 DataStoreFactory 被重新实例化（例如 Koin 重启），
     * 缓存依然存在，能够返回之前已经创建的 DataStore 实例，
     * 避免 "Multiple DataStores active" 崩溃。
     */
    private companion object {
        private val cache = mutableMapOf<String, Any>()
    }

    /**
     * 创建 DataStore 实例（同步版本）
     *
     * 如果同一个文件路径已经创建过 DataStore 实例，则返回缓存的实例，避免重复创建。
     *
     * @param T 数据类型，必须是可序列化的
     * @param fileName 数据文件名称（不含路径）
     * @param serializer 数据序列化器
     * @param scope 协程作用域，默认使用 IO 调度器
     * @return [DataStore] 实例
     */
    fun <T> create(
        fileName: String,
        serializer: OkioSerializer<T>,
        scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    ): DataStore<T> {
        val path = dataStorePath.getPath() / fileName
        val key = path.toString()

        logger.i { "create: $path, isHas = ${key in cache}" }

        @Suppress("UNCHECKED_CAST")
        return cache.getOrPut(key) {
            DataStoreFactory.create(
                storage = OkioStorage(
                    fileSystem = fileSystem,
                    serializer = serializer,
                    producePath = { path }
                ),
                scope = scope
            )
        } as DataStore<T>

    }

    /**
     * 创建指定类型的 [DataStore] 实例，该方法基于默认值自动推断类型并设置序列化器。
     *
     * 如果同一个文件路径已经创建过 DataStore 实例，则返回缓存的实例，避免重复创建。
     *
     * @param T 数据类型，必须是可序列化的。
     * @param defaultValue 数据类型的默认值，用于序列化和反序列化失败时的回退。
     * @param scope 协程作用域，默认为使用 IO 调度器的 [CoroutineScope]。
     * @return [DataStore] 实例，用于类型安全的异步数据存储和访问。
     */
    inline fun <reified T> create(
        defaultValue: T,
        serializer: OkioSerializer<T> = jsonDataStoreSerializer(
            defaultValue = defaultValue,
            serializer = serializer()
        ),
        scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    ): DataStore<T> = create(
        fileName = "${T::class.qualifiedName}_preferences.json",
        serializer = serializer,
        scope = scope
    )

}

