package com.ciyin.app.util

import ciyin.io.File
import ciyin.lang.unit
import ciyin.platform.Log
import ciyin.platform.logger
import ciyin.serialization.json.modifyJson
import ciyin.serialization.json.readJson
import ciyin.serialization.json.toJsonPrimitive
import ciyin.serialization.json.writeJson
import ciyin.system.coroutines.IO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer

/**
 * 一个通用的数据存储类，用于处理特定类型数据的读写操作
 *
 * @param D 泛型参数，代表存储的数据类型，且数据类必须要有 [kotlinx.serialization.Serializable] 注解
 * @param defaultData 初始化数据，当数据文件为空或读取失败时使用
 * @param file 文件操作对象，用于数据的读写
 * @param isReadData 是否在初始化时读取数据，默认为true
 * @param isAsyncReadData 是否在初始化时异步读取数据，默认为false
 * @param onAfterRead 读取数据后的回调
 * @param onBeforeWrite 写入数据前的回调
 */
class DataStore<D : Any>(
    defaultData: D,
    var file: File,
    isReadData: Boolean = true,
    isAsyncReadData: Boolean = false,
    val onAfterRead: D.() -> D = { this },
    val onBeforeWrite: D.() -> D = { this },
) {

    /**
     * 获取全部配置数据
     */
    var data = defaultData
        private set

    /**
     * 判断配置文件出错
     */
    var isError = false
        private set

    /**
     * 判断配置文件存在
     */
    val isExists get() = file.exists()

    private val logger = logger("DataStore")

    @OptIn(InternalSerializationApi::class)
    private val serializer = data::class.serializer() as KSerializer<D>

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        if (isReadData) {
            scope.launch {
                if (isAsyncReadData) readBacking() else read()
            }
        }
    }

    /**
     * 读取配置数据
     *
     * @param context 读取数据后的回调
     */
    @OptIn(InternalSerializationApi::class)
    fun read(context: D.() -> D = { this }) = runCatching {
        file.readJson(serializer)
    }
        .onSuccess { data = it }
        .onFailure { logger.e(it) { "读取配置文件出错" } }
        .let {
            isError = it.isFailure
            if (isError || isExists) write() //配置文件不存在时写入默认数据
            data = onAfterRead(data)
            data = context(data)
        }


    /**
     * 后台写入配置数据
     *
     * @param context 读取数据后的回调
     */
    fun readBacking(context: D.() -> D = { this }) = scope.launch { read(context) }.unit()


    /**
     * 写入配置数据
     *
     * @param context 写入数据前的回调
     * @return 是否写入成功
     */
    fun write(context: D.() -> D = { this }): Boolean {
        data = onBeforeWrite(this.data)
        data = context(data)
        return try {
            file.writeJson(serializer, data) {
                prettyPrint = true
            }
            true
        } catch (e: Exception) {
            Log.error("DataStore", e)
            false
        }
    }


    /**
     * 后台写入配置数据
     */
    fun writeBacking(context: D.() -> D = { this }) = scope.launch { write(context) }.unit()

    /**
     * 设置单条数据，并后台写入配置数据
     */
    @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
    fun setBacking(key: String, value: Any) {
        set(key, value)
        writeBacking()
    }

    /**
     * 设置单条数据，并写入配置数据
     */
    @Suppress("UNCHECKED_CAST")
    @OptIn(InternalSerializationApi::class)
    operator fun set(key: String, value: Any): Boolean = runCatching {
        data.modifyJson(
            serializer = data::class.serializer() as KSerializer<D>,
            updates = mapOf(key to value.toJsonPrimitive())
        )
    }.isSuccess

    /**
     * 获取单条数据
     */
    fun <V> get(key: String): V {
//        return ReflectUtil.accessibleGet(data, key)
        TODO()
    }

}
