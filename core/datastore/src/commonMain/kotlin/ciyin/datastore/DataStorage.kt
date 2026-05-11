package ciyin.datastore

import ciyin.io.File
import ciyin.platform.Log
import ciyin.platform.thisLogger
import ciyin.serialization.json.modifyJson
import ciyin.serialization.json.readJson
import ciyin.serialization.json.toJsonPrimitive
import ciyin.serialization.json.writeJson
import ciyin.system.coroutines.IO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer

/**
 * 基于 StateFlow 的响应式数据存储类
 *
 * @param D 数据类型，必须标注 [Serializable]
 * @param defaultData 默认数据
 * @param file 存储文件
 * @param autoLoad 是否自动加载数据，默认 true
 * @param asyncLoad 是否异步加载数据，默认 false
 * @param autoPersist 是否自动持久化数据变更，默认 true
 * @param onDataLoaded 数据加载后的处理回调
 * @param onBeforePersist 数据持久化前的处理回调
 */
open class DataStorage<D : Any>(
    private val defaultData: D,
    private val file: File,
    autoLoad: Boolean = true,
    asyncLoad: Boolean = false,
    private val autoPersist: Boolean = true,
    private val onDataLoaded: (D) -> D = { it },
    private val onBeforePersist: (D) -> D = { it },
) {

    private val _state = MutableStateFlow(defaultData)

    /**
     * 响应式数据流
     */
    val state: StateFlow<D> = _state.asStateFlow()

    /**
     * 当前数据快照
     */
    val snapshot: D get() = _state.value

    /**
     * 文件是否存在
     */
    val fileExists: Boolean get() = file.exists()

    /**
     * 最后一次操作是否出错
     */
    var lastOperationFailed: Boolean = false
        private set

    private val logger = thisLogger()

    @Suppress("UNCHECKED_CAST")
    @OptIn(InternalSerializationApi::class)
    private val serializer: KSerializer<D> = defaultData::class.serializer() as KSerializer<D>

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()

    init {
        if (autoLoad) {
            logger.i { "init: autoLoad=true, 启动加载任务" }
            if (asyncLoad) {
                scope.launch {
                    logger.i { "init: asyncLoad=true, 启动异步加载任务" }
                    load()
                    logger.i { "init: load() 完成，当前数据: $snapshot" }
                }
            } else {
                runBlocking {
                    logger.i { "init: runBlocking, 启动同步加载任务" }
                    load()
                    logger.i { "init: load() 完成，当前数据: $snapshot" }
                }
            }
        } else {
            logger.i { "init: autoLoad=false, 跳过加载" }
        }
    }

    /**
     * 从文件加载数据
     */
    suspend fun load(): Result<D> = mutex.withLock {
        logger.i { "load(): 获取到 mutex 锁" }
        logger.i { "load(): 开始加载，文件存在: $fileExists" }
        runCatching {
            if (!fileExists) {
                logger.i { "文件不存在，使用默认数据" }
                persistUnsafe()
                return@runCatching defaultData
            }

            logger.i { "load(): 从文件读取数据，准备调用 file.readJson" }
            val loadedData = file.readJson(serializer)
            logger.i { "load(): file.readJson 完成，读取到的数据: $loadedData" }

            val processedData = onDataLoaded(loadedData)
            logger.i { "load(): 处理后的数据: $processedData" }

            _state.value = processedData
            lastOperationFailed = false
            logger.i { "数据加载成功，当前 snapshot: $snapshot" }
            processedData
        }.onFailure { error ->
            lastOperationFailed = true
            logger.e(error) { "数据加载失败" }
            _state.value = defaultData
            // 尝试写入默认数据
            persistUnsafe()
        }
    }

    /**
     * 持久化当前数据到文件
     */
    suspend fun persist(): Result<Unit> = mutex.withLock {
        persistUnsafe()
    }

    /**
     * 持久化当前数据到文件（无锁）
     */
    private fun persistUnsafe(): Result<Unit> = runCatching {
        val dataToWrite = onBeforePersist(snapshot)
        file.writeJson(serializer, dataToWrite) {
            prettyPrint = true
        }
        lastOperationFailed = false
        logger.i { "数据持久化成功" }
    }.onFailure { error ->
        lastOperationFailed = true
        Log.error("DataStore", error)
    }

    /**
     * 更新数据（自动持久化）
     *
     * @param transform 数据转换函数
     */
    fun update(transform: (D) -> D) {
        _state.update(transform)
        if (autoPersist) {
            scope.launch { persist() }
        }
    }

    /**
     * 更新数据并等待持久化完成
     *
     * @param transform 数据转换函数
     * @return 持久化结果
     */
    suspend fun updateAndPersist(transform: (D) -> D): Result<Unit> {
        _state.update(transform)
        return persist()
    }

    /**
     * 直接设置数据（自动持久化）
     */
    fun set(data: D) {
        _state.value = data
        if (autoPersist) {
            scope.launch { persist() }
        }
    }

    /**
     * 直接设置数据并等待持久化完成
     */
    suspend fun setAndPersist(data: D): Result<Unit> {
        _state.value = data
        return persist()
    }

    /**
     * 重置为默认数据
     */
    fun reset() {
        update { defaultData }
    }

    /**
     * 重置为默认数据并等待持久化完成
     */
    suspend fun resetAndPersist(): Result<Unit> {
        return updateAndPersist { defaultData }
    }

    /**
     * 删除存储文件
     */
    suspend fun delete(): Result<Boolean> = mutex.withLock {
        runCatching {
            _state.value = defaultData
            if (fileExists) {
                file.delete()
            } else {
                true
            }
        }
    }

    /**
     * 更新单个属性并自动持久化
     *
     * 此方法通过 JSON 序列化/反序列化机制更新对象的指定字段，适用于部分更新不可变数据类。
     * 更新操作是原子性的，会自动触发持久化（如果 autoPersist 为 true）。
     *
     * 注意事项：
     * - 不存在的字段会被忽略，不会抛出异常
     * - 仅支持可序列化为 JsonObject 的类型（如 data class）
     * - 更新失败时会保留原数据并记录错误日志
     *
     * @param key 字段名，直接对应数据类的属性名（不支持嵌套路径）
     * @param value 新值，会自动转换为 JsonPrimitive
     *
     * @throws IllegalArgumentException 如果数据类型序列化后不是 JsonObject
     *
     * 示例：
     * ```kotlin
     * // 假设数据类为 data class Config(val theme: String, val count: Int)
     * dataStore.updateProperty("theme", "dark")
     * dataStore.updateProperty("count", 42)
     * ```
     */
    fun updateProperty(key: String, value: Any) {
        update { data ->
            try {
                data.modifyJsonProperty(serializer, key, value)
            } catch (e: Exception) {
                logger.e(e) { "更新属性失败: $key" }
                data // 失败时返回原数据
            }
        }
    }

    /**
     * 更新单个属性并等待持久化完成
     *
     * 与 [updateProperty] 功能相同，但会等待持久化操作完成并返回结果。
     * 适用于需要确认数据已成功保存到文件的场景。
     *
     * @param key 字段名
     * @param value 新值
     * @return 持久化操作的结果，成功返回 Result.success，失败返回 Result.failure
     *
     * 示例：
     * ```kotlin
     * val result = dataStore.updatePropertyAndPersist("username", "Alice")
     * if (result.isSuccess) {
     *     println("用户名更新并保存成功")
     * } else {
     *     println("保存失败: ${result.exceptionOrNull()}")
     * }
     * ```
     */
    suspend fun updatePropertyAndPersist(key: String, value: Any): Result<Unit> {
        return try {
            val updated = snapshot.modifyJsonProperty(serializer, key, value)
            setAndPersist(updated)
        } catch (e: Exception) {
            logger.e(e) { "更新属性失败: $key" }
            Result.failure(e)
        }
    }
}

/**
 * 扩展函数：修改 JSON 属性
 * 这是一个辅助函数，需要根据实际的 JSON 处理库实现
 */
@OptIn(InternalSerializationApi::class)
private fun <D : Any> D.modifyJsonProperty(
    serializer: KSerializer<D>,
    key: String,
    value: Any
): D {
    return this.modifyJson(
        serializer = serializer,
        updates = mapOf(key to value.toJsonPrimitive())
    )
}

/**
 * 创建 DataStorage 的便捷函数
 */
inline fun <reified D : Any> createDataStorage(
    defaultData: D,
    file: File,
    autoLoad: Boolean = true,
    autoPersist: Boolean = true,
    noinline onDataLoaded: (D) -> D = { it },
    noinline onBeforePersist: (D) -> D = { it },
): DataStorage<D> = DataStorage(
    defaultData = defaultData,
    file = file,
    autoLoad = autoLoad,
    autoPersist = autoPersist,
    onDataLoaded = onDataLoaded,
    onBeforePersist = onBeforePersist,
)
