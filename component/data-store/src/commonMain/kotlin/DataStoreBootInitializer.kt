package ciyin

import ciyin.datastore.DataStoreAutoConfiguration
import ciyin.koin.KoinBootInitializer

/**
 * DataStore Koinboot 初始化器
 *
 * 用于在 Koinboot 启动时自动配置 DataStore 相关依赖。
 *
 * 使用示例：
 * ```kotlin
 * runKoinBoot {
 *     // 初始化 DataStore 模块
 *     DataStoreBootInitializer(this)
 * }
 * ```
 */
val DataStoreBootInitializer: KoinBootInitializer = {
    autoConfigurations(DataStoreAutoConfiguration)
}

