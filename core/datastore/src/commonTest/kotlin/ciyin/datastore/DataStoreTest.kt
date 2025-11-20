package ciyin.datastore

import ciyin.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@Serializable
data class TestConfig(
    val username: String = "",
    val age: Int = 0,
    val theme: String = "light",
    val isEnabled: Boolean = false,
    val score: Double = 0.0
)

@OptIn(ExperimentalCoroutinesApi::class)
class DataStoreTest {

    private lateinit var testFile: File
    private lateinit var dataStorage: DataStorage<TestConfig>
    private val defaultConfig = TestConfig(
        username = "default",
        age = 18,
        theme = "light",
        isEnabled = false,
        score = 0.0
    )

    @BeforeTest
    fun setup() {
        // 创建临时测试文件
        testFile = File.createTempFile("test_config", ".json")
        // 确保测试前文件不存在
        if (testFile.exists()) {
            testFile.delete()
        }
    }

    @AfterTest
    fun teardown() {
        // 清理测试文件
        if (testFile.exists()) {
            testFile.delete()
        }
    }

    /** 测试初始状态使用默认数据 */
    @Test
    fun `test initial state with default data`() = runTest {
        dataStorage = DataStorage(
            defaultData = defaultConfig,
            file = testFile,
            autoLoad = false
        )

        assertEquals(defaultConfig, dataStorage.snapshot)
        assertFalse(dataStorage.fileExists)
    }

    /** 测试文件不存在时加载会创建默认数据文件 */
    @Test
    fun `test load creates file with default data when file not exists`() = runTest {
        dataStorage = DataStorage(
            defaultData = defaultConfig,
            file = testFile,
            autoLoad = false
        )

        val result = dataStorage.load()

        assertTrue(result.isSuccess)
        assertEquals(defaultConfig, result.getOrNull())
        // 文件应该被创建（load 内部在文件不存在时会调用 persist）
        assertTrue(dataStorage.fileExists)
    }

    /** 测试持久化保存数据到文件 */
    @Test
    fun `test persist saves data to file`() = runTest {
        dataStorage = DataStorage(
            defaultData = defaultConfig,
            file = testFile,
            autoLoad = false
        )

        dataStorage.set(TestConfig(username = "Alice", age = 25))
        val result = dataStorage.persist()

        assertTrue(result.isSuccess)
        assertTrue(dataStorage.fileExists)
        assertFalse(dataStorage.lastOperationFailed)
    }

    /** 测试 update 修改状态 */
    @Test
    fun `test update modifies state`() = runTest {
        dataStorage = DataStorage(
            defaultData = defaultConfig,
            file = testFile,
            autoLoad = false,
            autoPersist = false
        )

        dataStorage.update { it.copy(username = "Bob", age = 30) }

        assertEquals("Bob", dataStorage.snapshot.username)
        assertEquals(30, dataStorage.snapshot.age)
    }

    /** 测试 updateAndPersist 修改并保存数据 */
    @Test
    fun `test updateAndPersist modifies and saves data`() = runTest {
        dataStorage = DataStorage(
            defaultData = defaultConfig,
            file = testFile,
            autoLoad = false
        )

        val result = dataStorage.updateAndPersist {
            it.copy(username = "Charlie", theme = "dark")
        }

        assertTrue(result.isSuccess)
        assertEquals("Charlie", dataStorage.snapshot.username)
        assertEquals("dark", dataStorage.snapshot.theme)
        assertTrue(dataStorage.fileExists)
    }

    /** 测试 set 替换整个数据对象 */
    @Test
    fun `test set replaces entire data`() = runTest {
        dataStorage = DataStorage(
            defaultData = defaultConfig,
            file = testFile,
            autoLoad = false,
            autoPersist = false
        )

        val newConfig = TestConfig(
            username = "Dave",
            age = 35,
            theme = "dark",
            isEnabled = true,
            score = 99.5
        )
        dataStorage.set(newConfig)

        assertEquals(newConfig, dataStorage.snapshot)
    }

    /** 测试 setAndPersist 替换并保存数据 */
    @Test
    fun `test setAndPersist replaces and saves data`() = runTest {
        dataStorage = DataStorage(
            defaultData = defaultConfig,
            file = testFile,
            autoLoad = false
        )

        val newConfig = TestConfig(username = "Eve", age = 28)
        val result = dataStorage.setAndPersist(newConfig)

        assertTrue(result.isSuccess)
        assertEquals(newConfig, dataStorage.snapshot)
        assertTrue(dataStorage.fileExists)
    }

    /** 测试 reset 恢复默认数据 */
    @Test
    fun `test reset restores default data`() = runTest {
        dataStorage = DataStorage(
            defaultData = defaultConfig,
            file = testFile,
            autoLoad = false,
            autoPersist = false
        )

        dataStorage.set(TestConfig(username = "Modified"))
        dataStorage.reset()

        assertEquals(defaultConfig, dataStorage.snapshot)
    }

    /** 测试 resetAndPersist 恢复并保存默认数据 */
    @Test
    fun `test resetAndPersist restores and saves default data`() = runTest {
        dataStorage = DataStorage(
            defaultData = defaultConfig,
            file = testFile,
            autoLoad = false
        )

        dataStorage.set(TestConfig(username = "Modified"))
        val result = dataStorage.resetAndPersist()

        assertTrue(result.isSuccess)
        assertEquals(defaultConfig, dataStorage.snapshot)
    }

    /** 测试 delete 删除文件并重置数据 */
    @Test
    fun `test delete removes file and resets data`() = runTest {
        dataStorage = DataStorage(
            defaultData = defaultConfig,
            file = testFile,
            autoLoad = false
        )

        dataStorage.setAndPersist(TestConfig(username = "ToDelete"))
        assertTrue(dataStorage.fileExists)

        val result = dataStorage.delete()

        assertTrue(result.isSuccess)
        assertFalse(dataStorage.fileExists)
        assertEquals(defaultConfig, dataStorage.snapshot)
    }

    /** 测试 updateProperty 更新单个字段 */
    @Test
    fun `test updateProperty updates single field`() = runTest {
        dataStorage = DataStorage(
            defaultData = defaultConfig,
            file = testFile,
            autoLoad = false,
            autoPersist = false
        )

        dataStorage.updateProperty("username", "Frank")
        assertEquals("Frank", dataStorage.snapshot.username)
        // 其他字段保持不变
        assertEquals(18, dataStorage.snapshot.age)
        assertEquals("light", dataStorage.snapshot.theme)
    }

    /** 测试 updateProperty 支持不同数据类型 */
    @Test
    fun `test updateProperty with different types`() = runTest {
        dataStorage = DataStorage(
            defaultData = defaultConfig,
            file = testFile,
            autoLoad = false,
            autoPersist = false
        )

        dataStorage.updateProperty("age", 40)
        dataStorage.updateProperty("isEnabled", true)
        dataStorage.updateProperty("score", 85.5)

        assertEquals(40, dataStorage.snapshot.age)
        assertTrue(dataStorage.snapshot.isEnabled)
        assertEquals(85.5, dataStorage.snapshot.score)
    }

    /** 测试 updatePropertyAndPersist 更新并保存 */
    @Test
    fun `test updatePropertyAndPersist updates and saves`() = runTest {
        dataStorage = DataStorage(
            defaultData = defaultConfig,
            file = testFile,
            autoLoad = false
        )

        val result = dataStorage.updatePropertyAndPersist("theme", "dark")

        assertTrue(result.isSuccess)
        assertEquals("dark", dataStorage.snapshot.theme)
        assertTrue(dataStorage.fileExists)
    }

    /** 测试 updateProperty 忽略不存在的字段 */
    @Test
    fun `test updateProperty with non-existent field is ignored`() = runTest {
        dataStorage = DataStorage(
            defaultData = defaultConfig,
            file = testFile,
            autoLoad = false,
            autoPersist = false
        )

        val originalConfig = dataStorage.snapshot
        dataStorage.updateProperty("nonExistentField", "value")

        // 数据应该保持不变
        assertEquals(originalConfig, dataStorage.snapshot)
    }

    /** 测试 StateFlow 能够发射更新 */
    @Test
    fun `test state flow emits updates`() = runTest {
        dataStorage = DataStorage(
            defaultData = defaultConfig,
            file = testFile,
            autoLoad = false,
            autoPersist = false
        )

        // 收集初始值
        val initialValue = dataStorage.state.first()
        assertEquals(defaultConfig, initialValue)

        // 执行更新
        dataStorage.update { it.copy(username = "Observer1") }
        val value1 = dataStorage.state.first()
        assertEquals("Observer1", value1.username)

        dataStorage.update { it.copy(username = "Observer2") }
        val value2 = dataStorage.state.first()
        assertEquals("Observer2", value2.username)
    }

    /** 测试 autoLoad 在初始化时自动加载数据 */
    @Test
    fun `test autoLoad loads data on initialization`() = runBlocking {
        // 不使用 runTest，因为 DataStore 使用 Dispatchers.IO
        println("=== 开始 autoLoad 测试 ===")

        // 先创建一个文件
        println("步骤1: 创建 initialStore")
        val initialStore = DataStorage(
            defaultData = defaultConfig,
            file = testFile,
            autoLoad = false
        )

        println("步骤2: 保存数据到文件: Persisted, age=50")
        runBlocking {
            initialStore.setAndPersist(TestConfig(username = "Persisted", age = 50))
        }
        println("步骤3: 文件是否存在: ${testFile.exists()}")

        // 创建新实例并自动加载
        println("步骤4: 创建 newDataStore with autoLoad=true")
        val newDataStorage = DataStorage(
            defaultData = defaultConfig,
            file = testFile,
            autoLoad = true
        )

        println("步骤5: 立即检查数据: ${newDataStorage.snapshot}")

        // 轮询等待加载完成
        println("步骤6: 等待加载完成...")
        var attempts = 0
        while (!newDataStorage.isLoadCompleted && attempts < 50) {
            delay(100)
            attempts++
            if (attempts % 10 == 0) {
                println("  等待中... attempt $attempts, isLoadCompleted=${newDataStorage.isLoadCompleted}")
            }
        }

        println("步骤7: 加载完成后检查数据: ${newDataStorage.snapshot}")
        println("步骤8: lastOperationFailed: ${newDataStorage.lastOperationFailed}")
        println("步骤9: isLoadCompleted: ${newDataStorage.isLoadCompleted}")

        assertTrue(newDataStorage.isLoadCompleted, "加载应该已完成")
        assertEquals("Persisted", newDataStorage.snapshot.username)
        assertEquals(50, newDataStorage.snapshot.age)

        println("=== autoLoad 测试完成 ===")
    }

    /** 测试 autoPersist 自动保存变更 */
    @Test
    fun `test autoPersist automatically saves changes`() = runTest {
        dataStorage = DataStorage(
            defaultData = defaultConfig,
            file = testFile,
            autoLoad = false,
            autoPersist = true
        )

        dataStorage.update { it.copy(username = "AutoSaved") }

        // 等待异步持久化完成
        delay(100)

        // 创建新实例验证数据已保存
        val newStore = DataStorage(
            defaultData = defaultConfig,
            file = testFile,
            autoLoad = false
        )
        newStore.load()

        assertEquals("AutoSaved", newStore.snapshot.username)
    }

    /** 测试 onDataLoaded 回调被应用 */
    @Test
    fun `test onDataLoaded callback is applied`() = runTest {
        // 先保存数据
        val initialStore = DataStorage(
            defaultData = defaultConfig,
            file = testFile,
            autoLoad = false
        )
        initialStore.setAndPersist(TestConfig(username = "Original", age = 20))

        // 使用回调加载
        dataStorage = DataStorage(
            defaultData = defaultConfig,
            file = testFile,
            autoLoad = false,
            onDataLoaded = { it.copy(age = it.age + 10) }
        )
        dataStorage.load()

        assertEquals("Original", dataStorage.snapshot.username)
        assertEquals(30, dataStorage.snapshot.age) // 20 + 10
    }

    /** 测试 onBeforePersist 回调被应用 */
    @Test
    fun `test onBeforePersist callback is applied`() = runTest {
        dataStorage = DataStorage(
            defaultData = defaultConfig,
            file = testFile,
            autoLoad = false,
            onBeforePersist = { it.copy(username = it.username.uppercase()) }
        )

        dataStorage.setAndPersist(TestConfig(username = "lowercase"))

        // 重新加载验证
        val newStore = DataStorage(
            defaultData = defaultConfig,
            file = testFile,
            autoLoad = false
        )
        newStore.load()

        assertEquals("LOWERCASE", newStore.snapshot.username)
    }

    /** 测试 createDataStore 工厂函数 */
    @Test
    fun `test createDataStore factory function`() = runTest {
        dataStorage = createDataStorage(
            defaultData = defaultConfig,
            file = testFile,
            autoLoad = false,
            autoPersist = false
        )

        assertNotNull(dataStorage)
        assertEquals(defaultConfig, dataStorage.snapshot)
    }
}