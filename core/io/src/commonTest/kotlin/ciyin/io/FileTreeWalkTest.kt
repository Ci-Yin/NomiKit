package ciyin.io

import okio.FileSystem
import kotlin.test.*

/**
 * FileTreeWalk 测试类
 *
 * @author 次音(CiYin) QQ:2964221430
 * @github <a href="https://github.com/Ci-Yin">CiYin</a>
 * @since 2025/11/1
 * @version: 1.0
 */
class FileTreeWalkTest {

    private lateinit var tempDir: File
    private lateinit var testStructure: TestFileStructure

    @BeforeTest
    fun setup() {
        // 创建临时测试目录结构
        println(File("FileTreeWalkTest")::class.qualifiedName)
        tempDir = createTempDirectory("FileTreeWalkTest")
        testStructure = createTestFileStructure(tempDir)
    }

    @AfterTest
    fun tearDown() {
        // 清理测试目录
        tempDir.deleteRecursively()
    }

    // ========== 基础遍历测试 ==========

    @Test
    fun testWalkTopDown_basicStructure() {
        val files = testStructure.root.walkTopDown().toList()

        assertTrue(files.isNotEmpty())
        assertEquals(testStructure.root, files.first(), "第一个应该是根目录")

        // 验证父目录在子目录之前
        val rootIndex = files.indexOf(testStructure.root)
        val subDir1Index = files.indexOf(testStructure.subDir1)
        val subDir2Index = files.indexOf(testStructure.subDir2)

        assertTrue(rootIndex < subDir1Index, "根目录应该在子目录1之前")
        assertTrue(rootIndex < subDir2Index, "根目录应该在子目录2之前")
    }

    @Test
    fun testWalkBottomUp_basicStructure() {
        val files = testStructure.root.walkBottomUp().toList()

        assertTrue(files.isNotEmpty())
        assertEquals(testStructure.root, files.last(), "最后一个应该是根目录")

        // 验证父目录在子目录之后
        val rootIndex = files.indexOf(testStructure.root)
        val subDir1Index = files.indexOf(testStructure.subDir1)
        val subDir2Index = files.indexOf(testStructure.subDir2)

        assertTrue(rootIndex > subDir1Index, "根目录应该在子目录1之后")
        assertTrue(rootIndex > subDir2Index, "根目录应该在子目录2之后")
    }

    @Test
    fun testWalk_withDirection() {
        val topDownFiles = testStructure.root.walk(FileWalkDirection.TOP_DOWN).toList()
        val bottomUpFiles = testStructure.root.walk(FileWalkDirection.BOTTOM_UP).toList()

        // 两种方式应该遍历相同数量的文件
        assertEquals(topDownFiles.size, bottomUpFiles.size)

        // 但顺序应该不同
        assertNotEquals(topDownFiles, bottomUpFiles)
    }

    // ========== 单文件测试 ==========

    @Test
    fun testWalk_singleFile() {
        val files = testStructure.file1.walk().toList()

        assertEquals(1, files.size)
        assertEquals(testStructure.file1, files.first())
    }

    @Test
    fun testWalk_nonExistentFile() {
        val nonExistent = File(tempDir, "non-existent")
        val files = nonExistent.walk().toList()

        assertTrue(files.isEmpty(), "不存在的文件应该返回空序列")
    }

    // ========== maxDepth 测试 ==========

    @Test
    fun testMaxDepth_one() {
        // 深度为1只访问根目录和直接子项
        val files = testStructure.root.walkTopDown().maxDepth(1).toList()

        assertTrue(files.contains(testStructure.root))
        assertTrue(files.contains(testStructure.file1))
        assertTrue(files.contains(testStructure.subDir1))
        assertTrue(files.contains(testStructure.subDir2))

        // 不应包含子目录中的文件
        assertFalse(files.contains(testStructure.subFile1))
        assertFalse(files.contains(testStructure.subFile2))
    }

    @Test
    fun testMaxDepth_two() {
        // 深度为2访问根目录、直接子项和孙子项
        val files = testStructure.root.walkTopDown().maxDepth(2).toList()

        assertTrue(files.contains(testStructure.root))
        assertTrue(files.contains(testStructure.subDir1))
        assertTrue(files.contains(testStructure.subFile1))

        // 如果有更深的嵌套，应该不包含
        if (testStructure.nestedDir != null) {
            assertFalse(files.contains(testStructure.nestedFile))
        }
    }

    @Test
    fun testMaxDepth_invalidValue() {
        assertFailsWith<IllegalArgumentException> {
            testStructure.root.walkTopDown().maxDepth(0)
        }

        assertFailsWith<IllegalArgumentException> {
            testStructure.root.walkTopDown().maxDepth(-1)
        }
    }

    // ========== onEnter 回调测试 ==========

    @Test
    fun testOnEnter_skipDirectory() {
        val visited = mutableListOf<File>()

        testStructure.root.walkTopDown()
            .onEnter { file ->
                // 跳过 subDir1
                file != testStructure.subDir1
            }
            .forEach { visited.add(it) }

        assertTrue(visited.contains(testStructure.root))
        assertFalse(visited.contains(testStructure.subDir1), "subDir1应该被跳过")
        assertFalse(visited.contains(testStructure.subFile1), "subDir1中的文件也应该被跳过")
        assertTrue(visited.contains(testStructure.subDir2))
    }

    @Test
    fun testOnEnter_calledBeforeChildren() {
        val visitOrder = mutableListOf<String>()

        testStructure.root.walkTopDown()
            .onEnter { file ->
                visitOrder.add("enter:${file.name}")
                true
            }
            .forEach { file ->
                visitOrder.add("visit:${file.name}")
            }

        // onEnter应该在访问子项之前被调用
        val enterRootIndex = visitOrder.indexOf("enter:${testStructure.root.name}")
        val visitSubDir1Index = visitOrder.indexOf("visit:${testStructure.subDir1.name}")

        assertTrue(enterRootIndex < visitSubDir1Index)
    }

    // ========== onLeave 回调测试 ==========

    @Test
    fun testOnLeave_calledAfterChildren() {
        val visitOrder = mutableListOf<String>()

        testStructure.root.walkTopDown()
            .onLeave { file ->
                visitOrder.add("leave:${file.name}")
            }
            .forEach { file ->
                visitOrder.add("visit:${file.name}")
            }

        // onLeave应该在访问完所有子项之后被调用
        val visitSubFile1Index = visitOrder.indexOf("visit:${testStructure.subFile1.name}")
        val leaveSubDir1Index = visitOrder.indexOf("leave:${testStructure.subDir1.name}")

        assertTrue(visitSubFile1Index < leaveSubDir1Index)
    }

    @Test
    fun testOnLeave_bottomUp() {
        val leaveOrder = mutableListOf<String>()

        testStructure.root.walkBottomUp()
            .onLeave { file ->
                leaveOrder.add(file.name)
            }
            .forEach { }

        // 在自下而上遍历中，onLeave仍然应该在处理完目录后调用
        assertFalse(leaveOrder.isEmpty())
    }

    // ========== onFail 回调测试 ==========

    @Test
    fun testOnFail_accessDenied() {
        // 创建一个无法访问的目录（模拟）
        val protectedDir = File(tempDir, "protected")
        protectedDir.mkdir()

        // 注意：在某些平台上可能无法真正拒绝访问，这个测试可能需要调整
        val failures = mutableListOf<Pair<File, String>>()

        testStructure.root.walkTopDown()
            .onFail { file, exception ->
                failures.add(file to exception.message.orEmpty())
            }
            .forEach { }

        // 这个测试主要验证回调机制是否工作
        // 实际的失败情况取决于文件系统权限
    }

    // ========== 组合回调测试 ==========

    @Test
    fun testCombinedCallbacks() {
        val events = mutableListOf<String>()

        testStructure.root.walkTopDown()
            .onEnter { file ->
                events.add("enter:${file.name}")
                true
            }
            .onLeave { file ->
                events.add("leave:${file.name}")
            }
            .onFail { file, _ ->
                events.add("fail:${file.name}")
            }
            .forEach { file ->
                events.add("visit:${file.name}")
            }

        // 验证事件顺序
        assertTrue(events.isNotEmpty())

        // 对于每个目录，应该有: enter -> visit -> (children) -> leave
        val rootName = testStructure.root.name
        val enterIndex = events.indexOf("enter:$rootName")
        val visitIndex = events.indexOf("visit:$rootName")
        val leaveIndex = events.indexOf("leave:$rootName")

        assertTrue(enterIndex < visitIndex)
        assertTrue(visitIndex < leaveIndex)
    }

    // ========== 序列操作测试 ==========

    @Test
    fun testSequenceOperations_filter() {
        val txtFiles = testStructure.root.walkTopDown()
            .filter { it.extension == "txt" }
            .toList()

        assertTrue(txtFiles.all { it.extension == "txt" })
    }

    @Test
    fun testSequenceOperations_map() {
        val fileNames = testStructure.root.walkTopDown()
            .map { it.name }
            .toList()

        assertTrue(fileNames.contains(testStructure.root.name))
        assertTrue(fileNames.contains(testStructure.file1.name))
    }

    @Test
    fun testSequenceOperations_count() {
        val totalFiles = testStructure.root.walkTopDown().count()
        val directories = testStructure.root.walkTopDown().count { it.isDirectory }
        val files = testStructure.root.walkTopDown().count { it.isFile }

        assertEquals(totalFiles, directories + files)
    }

    // ========== 边界情况测试 ==========

    @Test
    fun testEmptyDirectory() {
        val emptyDir = File(tempDir, "empty")
        emptyDir.mkdir()

        val files = emptyDir.walkTopDown().toList()

        assertEquals(1, files.size)
        assertEquals(emptyDir, files.first())
    }

    @Test
    fun testSymlinkHandling() {
        // 符号链接的处理取决于平台
        // 这个测试主要验证不会陷入无限循环
        val files = testStructure.root.walkTopDown().take(1000).toList()
        assertTrue(files.size < 1000, "不应该有无限循环")
    }

    // ========== 辅助类和方法 ==========

    private data class TestFileStructure(
        val root: File,
        val file1: File,
        val file2: File,
        val subDir1: File,
        val subDir2: File,
        val subFile1: File,
        val subFile2: File,
        val nestedDir: File?,
        val nestedFile: File?
    )

    private fun createTestFileStructure(parent: File): TestFileStructure {
        /*
         * 创建如下结构:
         * root/
         *   file1.txt
         *   file2.dat
         *   subDir1/
         *     subFile1.txt
         *     nested/
         *       nestedFile.txt
         *   subDir2/
         *     subFile2.txt
         */
        val root = File(parent, "root").apply { mkdir() }
        val file1 = File(root, "file1.txt").apply { write("content1") }
        val file2 = File(root, "file2.dat").apply { write("content2") }

        val subDir1 = File(root, "subDir1").apply { mkdir() }
        val subFile1 = File(subDir1, "subFile1.txt").apply { write("sub content 1") }

        val nestedDir = File(subDir1, "nested").apply { mkdir() }
        val nestedFile = File(nestedDir, "nestedFile.txt").apply { write("nested content") }

        val subDir2 = File(root, "subDir2").apply { mkdir() }
        val subFile2 = File(subDir2, "subFile2.txt").apply { write("sub content 2") }

        return TestFileStructure(
            root = root,
            file1 = file1,
            file2 = file2,
            subDir1 = subDir1,
            subDir2 = subDir2,
            subFile1 = subFile1,
            subFile2 = subFile2,
            nestedDir = nestedDir,
            nestedFile = nestedFile
        )
    }

    private fun createTempDirectory(prefix: String): File {
        return File(FileSystem.SYSTEM_TEMPORARY_DIRECTORY.div(prefix))
    }
}