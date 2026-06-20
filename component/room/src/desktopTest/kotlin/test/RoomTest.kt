package ciyin.room.test

import androidx.room.RoomDatabase
import ciyin.RoomBootInitializer
import ciyin.core.platform.Context
import ciyin.core.platform.DesktopContext
import ciyin.core.platform.MyukoBuildConfig
import ciyin.system.storage.AppFolderResolver
import ciyin.system.storage.AppInfo
import ciyin.koin.runKoinBoot
import ciyin.room.createInMemoryDatabaseBuilder
import ciyin.room.singleDao
import ciyin.room.singleDatabase
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.koin.core.qualifier._q
import org.koin.dsl.bind
import org.koin.mp.KoinPlatformTools
import kotlin.reflect.KClass

class RoomTest {

    @AfterEach
    fun stopKoin() =
        KoinPlatformTools.defaultContext().stopKoin()


    /**
     * Room singleDao 使用的示范
     *
     */
    @Test
    fun `test singleDao`() {

        val koin = runKoinBoot {
            RoomBootInitializer()
            module {

                single<RoomDatabase.Builder<*>> { parametersHolder ->
                    val databaseClass = parametersHolder.getOrNull<KClass<RoomDatabase>>()!!
                    creatDesktopContext()
                        .createInMemoryDatabaseBuilder(databaseClass)
                }

                singleDao(AppDatabase::searchHistoryDao)
            }
        }

        val searchHistoryDao = koin.get<SearchHistoryDao>()

        runTest {
            searchHistoryDao.insert(SearchHistoryEntity(content = "test", timestamp = "2021-01-01"))
            searchHistoryDao.getAllAsFlow().first().forEach { item ->
                Logger.i { "item = $item" }
            }
        }
    }

    @Test
    fun `test multiple databases by databases class`() {

        val koin = runKoinBoot {
            RoomBootInitializer()
            module {

                single { creatDesktopContext() } bind Context::class
                // 为了不被覆盖加一个 _q<AppDatabase>()
                singleDao(AppDatabase::searchHistoryDao, _q<AppDatabase>())
                singleDao(AppDatabase2::searchHistoryDao)
            }
        }
        // 这里的dao操作的是 `ciyin.room.test.AppDatabase.db` 库
        val searchHistoryDao = koin.get<SearchHistoryDao>(_q<AppDatabase>())
        // 这里的dao操作的是 `ciyin.room.test.AppDatabase2.db` 库
        val searchHistoryDao2 = koin.get<SearchHistoryDao>()

        runTest {
            searchHistoryDao.insert(SearchHistoryEntity(content = "test", timestamp = "2021-01-01"))
            searchHistoryDao.getAllAsFlow().first().forEach { item ->
                Logger.i { "item1 = $item" }
            }
            searchHistoryDao2.insert(
                SearchHistoryEntity(
                    content = "test2",
                    timestamp = "2021-01-01"
                )
            )
            searchHistoryDao2.getAllAsFlow().first().forEach { item ->
                Logger.i { "item2 = $item" }
            }
        }
    }

    @Test
    fun `test multiple databases by name`() {
        val userMainQualifier = _q("user_main")
        val userSubQualifier = _q("user_sub")
        val koin = runKoinBoot {
            RoomBootInitializer()
            module {

                single { creatDesktopContext() } bind Context::class
                // 为了不被覆盖加一个 _q<AppDatabase>()
                singleDatabase<AppDatabase>("user_main")
                singleDatabase<AppDatabase>("user_sub")
                single<SearchHistoryDao>(userMainQualifier) {
                    get<AppDatabase>(userMainQualifier).searchHistoryDao
                }
                single<SearchHistoryDao>(userSubQualifier) {
                    get<AppDatabase>(userSubQualifier).searchHistoryDao
                }
            }
        }
        // 这里的dao操作的是 `user_main.db` 库
        val searchHistoryDao = koin.get<SearchHistoryDao>(userMainQualifier)
        // 这里的dao操作的是 `user_sub.db` 库
        val searchHistoryDao2 = koin.get<SearchHistoryDao>(userSubQualifier)

        runTest {
            searchHistoryDao.insert(SearchHistoryEntity(content = "test", timestamp = "2021-01-01"))
            searchHistoryDao.getAllAsFlow().first().forEach { item ->
                Logger.i { "item1 = $item" }
            }
            searchHistoryDao2.insert(
                SearchHistoryEntity(
                    content = "test2",
                    timestamp = "2021-01-01"
                )
            )
            searchHistoryDao2.getAllAsFlow().first().forEach { item ->
                Logger.i { "item2 = $item" }
            }
        }
    }

    fun creatDesktopContext(): DesktopContext {
        val projectDirectories = AppFolderResolver.resolve(
            AppInfo.OrganizationName(
                qualifier = "com",
                organization = "yy",
                name = if (MyukoBuildConfig.isDebug) "Myuko-debug" else "Myuko",
            ),
        )
        val dataDir = projectDirectories.data
        val cacheDir = projectDirectories.cache
        val logsDir = dataDir.resolve("logs").toFile().apply { mkdirs() }

        Logger.i { "Data directory: $dataDir" }
        // 创建桌面应用上下文，包含窗口状态和文件系统目录信息
        return DesktopContext(
            dataDir = dataDir.toFile(),
            cacheDir = cacheDir.toFile(),
            logsDir = logsDir,
        )

    }

}





