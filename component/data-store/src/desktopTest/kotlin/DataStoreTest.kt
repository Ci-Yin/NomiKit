package ciyin.datastore.test

import DataStoreBootInitializer
import androidx.datastore.core.DataStore
import ciyin.datastore.DataStoreFactory
import ciyin.datastore.DataStorePath
import ciyin.datastore.DataStoreProperties
import ciyin.datastore.JsonDataStoreSerializer
import ciyin.datastore.datastore_directory
import ciyin.koin.runKoinBoot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.koin.mp.KoinPlatformTools
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * DataStore Koinboot 模块测试类
 *
 * 测试内容包括：
 * 1. DataStoreFactory 的创建和注入
 * 2. JsonDataStoreSerializer 的序列化/反序列化功能
 * 3. DataStore 的读写操作
 * 4. 配置属性的正确应用
 */
class DataStoreTest {

    @TempDir
    lateinit var tempDir: File

    @AfterEach
    fun stopKoin() = KoinPlatformTools.defaultContext().stopKoin()

    /**
     * 测试 DataStore 自动配置
     *
     * 验证 DataStoreFactory 可以通过 Koin 正确注入
     */
    @Test
    fun `test datastore auto configuration`() {
        // 使用自定义的 FileSystem 和 DataStorePath
        val customModule = module {
            single<FileSystem> { FakeFileSystem() }
            single<DataStorePath> { DataStorePath { "/test/datastore".toPath() } }
        }

        val koin = runKoinBoot {
            DataStoreBootInitializer()
            modules(customModule)
        }

        // 验证 DataStoreFactory 已正确注册
        val factory = koin.getOrNull<DataStoreFactory>()
        assertNotNull(factory) { "DataStoreFactory 未被正确注册" }
    }

    /**
     * 测试 DataStore 属性配置
     *
     * 验证可以通过属性配置 DataStore 存储目录
     */
    @Test
    fun `test datastore properties`() {
        val customDirectory = "custom_datastore"

        val koin = runKoinBoot {
            properties {
                datastore_directory = customDirectory
            }
        }

        val directory = koin.getProperty<String>(DataStoreProperties.DATASTORE_DIRECTORY)
        assertEquals(customDirectory, directory)
    }

    /**
     * 测试 JsonDataStoreSerializer 序列化功能
     *
     * 验证使用 kotlinx-serialization 的 JSON 序列化器可以正确序列化/反序列化数据
     */
    @Test
    fun `test json datastore serializer`() = runTest {
        val fakeFileSystem = FakeFileSystem()
        val testPath = "/test/datastore".toPath()

        // 创建测试用的 DataStoreFactory
        val factory = DataStoreFactory(
            fileSystem = fakeFileSystem,
            dataStorePath = { testPath }
        )

        // 创建 DataStore 实例
        val dataStore: DataStore<TestUserPreferences> = factory.create(
            defaultValue = TestUserPreferences()
        )

        // 验证默认值
        val defaultValue = dataStore.data.first()
        assertEquals("light", defaultValue.theme)
        assertEquals("zh", defaultValue.language)
        assertEquals(0, defaultValue.fontSize)

        // 更新数据
        dataStore.updateData { current ->
            current.copy(
                theme = "dark",
                language = "en",
                fontSize = 16
            )
        }

        // 验证更新后的值
        val updatedValue = dataStore.data.first()
        assertEquals("dark", updatedValue.theme)
        assertEquals("en", updatedValue.language)
        assertEquals(16, updatedValue.fontSize)
    }

    /**
     * 测试 DataStore 数据持久化
     *
     * 验证数据可以正确写入文件系统
     */
    @Test
    fun `test datastore persistence`() = runTest {
        val fakeFileSystem = FakeFileSystem()
        val testPath = "/test/datastore".toPath()
        val fileName = "persistent_settings.json"

        val factory = DataStoreFactory(
            fileSystem = fakeFileSystem,
            dataStorePath = { testPath }
        )

        // 第一个 DataStore 实例写入数据
        val dataStore: DataStore<TestUserPreferences> = factory.create(
            fileName = fileName,
            serializer = JsonDataStoreSerializer(
                defaultValue = TestUserPreferences(),
                serializer = TestUserPreferences.serializer()
            )
        )

        dataStore.updateData {
            TestUserPreferences(theme = "dark", language = "ja", fontSize = 20)
        }

        // 验证文件已创建
        val filePath = testPath / fileName
        assert(fakeFileSystem.exists(filePath)) { "DataStore 文件未创建" }
    }


    /**
     * 测试在 KoinBoot 中使用多个 DataStore 实例的功能。
     *
     * 通过创建一个自定义的 Koin 模块，注入两个不同类型的 DataStore 实例，
     * 分别验证它们的独立性与功能正确性。
     *
     * 功能验证的核心流程如下：
     * - 借助 Koin 提供的依赖注入功能，分别获取 `TestUserDataStore` 和 `TestAppConfigDataStore` 实例。
     * - 分别更新两个 DataStore 的数据值。
     * - 校验更新后的数据是否符合预期，并验证两个 DataStore 实例是否互相独立。
     *
     * 本测试用例确保在同一应用中使用多个 DataStore 的场景下，各实例可以独立运作，
     * 且不发生数据冲突或依赖干扰。
     */
    @Test
    fun `test multiple datastore instances in koinboot`() = runTest {

        val customModule = module {
            single<FileSystem> { FakeFileSystem() }
            singleOf(::TestUserDataStore)
            singleOf(::TestAppConfigDataStore)
        }

        val koin = runKoinBoot {
            DataStoreBootInitializer()
            modules(customModule)
        }

        // 分别更新两个 DataStore
        val testUserDataStore: TestUserDataStore = koin.get()
        testUserDataStore.updateData { it.copy(theme = "dark") }
        val testAppConfigDataStore: TestAppConfigDataStore = koin.get()
        testAppConfigDataStore.updateData { it.copy(debugMode = true) }

        // 验证两个 DataStore 独立工作
        assertEquals("dark", testUserDataStore.data.first().theme)
        assertEquals(true, testAppConfigDataStore.data.first().debugMode)
    }

}

/**
 * 测试用的用户设置数据类
 */
@Serializable
data class TestUserPreferences(
    val theme: String = "light",
    val language: String = "zh",
    val fontSize: Int = 0
)

/**
 * 测试用的应用配置数据类
 */
@Serializable
private data class TestAppConfigPreferences(
    val version: String = "1.0.0",
    val debugMode: Boolean = false
)

class TestUserDataStore(
    dataStoreFactory: DataStoreFactory,
) : DataStore<TestUserPreferences> by dataStoreFactory.create(
    defaultValue = TestUserPreferences()
)

class TestAppConfigDataStore(
    dataStoreFactory: DataStoreFactory,
) : DataStore<TestAppConfigPreferences> by dataStoreFactory.create(
    defaultValue = TestAppConfigPreferences()
)