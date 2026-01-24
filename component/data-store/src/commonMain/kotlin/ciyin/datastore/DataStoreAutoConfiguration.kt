package ciyin.datastore

import ciyin.koin.configuration.koinAutoConfiguration
import ciyin.koin.configuration.onMissInstances
import ciyin.platform.Context
import ciyin.platform.files
import okio.FileSystem
import okio.Path
import okio.SYSTEM
import org.koin.core.definition.Definition

/**
 * DataStore 自动配置
 *
 * 提供 DataStore 的 Koin 自动配置，包括：
 * - [FileSystem] 文件系统实例
 * - [DataStorePath] 数据存储路径提供者
 * - [DataStoreFactory] 数据存储工厂
 */
internal val DataStoreAutoConfiguration = koinAutoConfiguration {
    module {
        // 用户没有配置 FileSystem 的情况下，自动使用系统默认 FileSystem
        onMissInstances<FileSystem> {
            single<FileSystem> { FileSystem.SYSTEM }
        }

        // 用户没有配置 DataStorePath 的情况下，自动使用平台默认路径
        onMissInstances<DataStorePath> {
            single<DataStorePath>(definition = dataStorePath())
        }

        // 提供 DataStoreFactory
        onMissInstances<DataStoreFactory> {
            single<DataStoreFactory> {
                DataStoreFactory(
                    fileSystem = get(),
                    dataStorePath = get()
                )
            }
        }
    }
}

/**
 * DataStore 存储路径提供者
 *
 * 用于获取 DataStore 数据文件的存储目录路径。
 * 不同平台有不同的实现。
 */
fun interface DataStorePath {
    /**
     * 获取 DataStore 存储目录路径
     *
     * @return 存储目录的 [Path]
     */
    fun getPath(): Path
}

/**
 * 获取平台特定的 DataStorePath 实例
 */
private fun dataStorePath(): Definition<DataStorePath> = {
    val context = get<Context>()
    val directory = getKoin().getProperty(DataStoreProperties.DATASTORE_DIRECTORY)
        ?: DataStoreProperties().directory
    DataStorePath {
        context.files.dataDir.toPath() / directory
    }
}


