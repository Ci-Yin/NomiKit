package ciyin.datastore

import ciyin.koin.KoinPropInstance
import ciyin.koin.KoinProperties

/**
 * DataStore 配置属性
 *
 * 用于配置 DataStore 的行为参数。
 *
 * @property directory DataStore 数据文件存储目录名称
 */
@KoinPropInstance("datastore")
data class DataStoreProperties(
    val directory: String = "datastore"
) {
    companion object {
        /**
         * DataStore 存储目录属性键
         */
        const val DATASTORE_DIRECTORY = "datastore.directory"
    }
}

/**
 * DataStore 存储目录名称属性扩展
 *
 * 用于在 KoinProperties 中设置/获取 DataStore 存储目录名称。
 */
@Suppress("unused")
var KoinProperties.datastore_directory: String?
    get() = this[DataStoreProperties.DATASTORE_DIRECTORY] as String?
    set(value) {
        value?.let { DataStoreProperties.DATASTORE_DIRECTORY(it) }
    }

