package ciyin.room.test

import ciyin.RoomBootInitializer
import ciyin.koin.runKoinBoot
import ciyin.platform.Context
import ciyin.platform.DesktopContext
import ciyin.platform.context.CommonContextFiles
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

/**
 * Room singleDao / singleDatabase 的桌面端集成测试
 *
 * 使用 in-memory SQLite 验证：
 * - 单数据库的 singleDao 注入
 * - 多数据库（按类 Qualifier）分库
 * - 多数据库（按自定义名称 Qualifier）分库
 */
class RoomTest {

    @AfterEach
    fun stopKoin() = KoinPlatformTools.defaultContext().get().close()

    /**
     * 验证 singleDao 可从 in-memory 数据库正确注入 DAO 并执行查询
     */
    @Test
    fun testSingleDao() = runTest {
        val koin = runKoinBoot {
            RoomBootInitializer()
            module {
                // 提供 DesktopContext（用于 RoomAutoConfiguration 的 onMissInstances）
                single { createTestDesktopContext() } bind Context::class
                // RoomAutoConfiguration 的 onMissInstances 会自动提供 Builder；
                // 此处通过 parametersOf 传入 AppDatabase class 即可创建 in-memory 数据库
                singleDao(AppDatabase::searchHistoryDao)
            }
        }

        val searchHistoryDao = koin.get<SearchHistoryDao>()
        searchHistoryDao.insert(SearchHistoryEntity(content = "test", timestamp = "2021-01-01"))
        val items = searchHistoryDao.getAllAsFlow().first()
        items.forEach { item ->
            Logger.i { "item = $item" }
        }
    }

    /**
     * 验证同一数据库类的多个实例（按类 Qualifier 分库）
     */
    @Test
    fun testMultipleDatabasesByClass() = runTest {
        val koin = runKoinBoot {
            RoomBootInitializer()
            module {
                single { createTestDesktopContext() } bind Context::class
                // 两个不同的类 → 各自独立的 .db
                singleDao(AppDatabase::searchHistoryDao, _q<AppDatabase>())
                singleDao(AppDatabase::searchHistoryDao)
            }
        }

        // 操作 AppDatabase.db
        val searchHistoryDao = koin.get<SearchHistoryDao>(_q<AppDatabase>())
        searchHistoryDao.insert(SearchHistoryEntity(content = "test1", timestamp = "2021-01-01"))
        val items1 = searchHistoryDao.getAllAsFlow().first()
        items1.forEach { item -> Logger.i { "item1 = $item" } }

        // 操作 AppDatabase2.db
        val searchHistoryDao2 = koin.get<SearchHistoryDao>()
        searchHistoryDao2.insert(SearchHistoryEntity(content = "test2", timestamp = "2021-01-02"))
        val items2 = searchHistoryDao2.getAllAsFlow().first()
        items2.forEach { item -> Logger.i { "item2 = $item" } }
    }

    /**
     * 验证同一数据库类的多个实例（按自定义名称 Qualifier 分库）
     */
    @Test
    fun testMultipleDatabasesByName() = runTest {
        val userMainQualifier = _q("user_main")
        val userSubQualifier = _q("user_sub")

        val koin = runKoinBoot {
            RoomBootInitializer()
            module {
                single { createTestDesktopContext() } bind Context::class
                // 用 singleDatabase 按名称注册两个独立的 AppDatabase 实例
                singleDatabase<AppDatabase>("user_main")
                singleDatabase<AppDatabase>("user_sub")
                // 从各自的 scope 取 DAO
                single<SearchHistoryDao>(userMainQualifier) {
                    get<AppDatabase>(userMainQualifier).searchHistoryDao
                }
                single<SearchHistoryDao>(userSubQualifier) {
                    get<AppDatabase>(userSubQualifier).searchHistoryDao
                }
            }
        }

        // 操作 user_main.db
        val searchHistoryDao = koin.get<SearchHistoryDao>(userMainQualifier)
        searchHistoryDao.insert(
            SearchHistoryEntity(
                content = "main-user",
                timestamp = "2021-01-01"
            )
        )
        val items1 = searchHistoryDao.getAllAsFlow().first()
        items1.forEach { item -> Logger.i { "item1 = $item" } }

        // 操作 user_sub.db
        val searchHistoryDao2 = koin.get<SearchHistoryDao>(userSubQualifier)
        searchHistoryDao2.insert(
            SearchHistoryEntity(
                content = "sub-user",
                timestamp = "2021-01-02"
            )
        )
        val items2 = searchHistoryDao2.getAllAsFlow().first()
        items2.forEach { item -> Logger.i { "item2 = $item" } }
    }

    /**
     * 创建用于 in-memory 测试的 [DesktopContext]。
     * 所有目录均指向系统临时目录下的子目录，测试结束后由 JVM 自行清理。
     *
     * 路径转换链：
     * [java.io.File] → [.toPath()][java.io.File.toPath] → [okio.Path]
     * → [.toFile()][okio.Path.toFile] → [ciyin.io.File]
     */
    private fun createTestDesktopContext(): DesktopContext {
        val baseDir = java.io.File(System.getProperty("java.io.tmpdir"))
        val dataDir = java.io.File(baseDir, "room-test-data-${System.currentTimeMillis()}")
            .also { it.mkdirs() }
        val cacheDir = java.io.File(baseDir, "room-test-cache-${System.currentTimeMillis()}")
            .also { it.mkdirs() }
        Logger.i { "Test data directory: $dataDir" }
        Logger.i { "Test cache directory: $cacheDir" }
        return DesktopContext(
            CommonContextFiles(
                dataDir = ciyin.io.File(dataDir.absolutePath),
                cacheDir = ciyin.io.File(cacheDir.absolutePath),
                defaultBaseMediaCacheDir = ciyin.io.File(cacheDir.absolutePath),
            )
        )
    }
}
