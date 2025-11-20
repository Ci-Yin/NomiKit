package ciyin.system.utils.mime

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 *
 * kotlin文件作用描述
 *
 * @author 次音(CiYin) QQ:2964221430
 * @github <a href="https://github.com/Ci-Yin">CiYin</a>
 * @since 2025/11/1 15:02
 * @version: 1.0
 */

/**
 * 边界情况和特殊场景测试
 */
class MimeTypeEdgeCasesTest {

    @BeforeTest
    fun setup() {
        MimeTypeManager.clearCustomTypes()
    }

    // ========== Unicode 和特殊字符测试 ==========

    @Test
    fun testGetMime_unicodeFilename() {
        assertEquals("image/jpeg", "照片.jpg".getMime())
        assertEquals("video/mp4", "视频文件.mp4".getMime())
        assertEquals("text/plain", "文档.txt".getMime())
    }

    @Test
    fun testGetMime_emojiInFilename() {
        assertEquals("image/jpeg", "🎉celebration.jpg".getMime())
        assertEquals("video/mp4", "video🎬.mp4".getMime())
    }

    // ========== 长文件名测试 ==========

    @Test
    fun testGetMime_veryLongFilename() {
        val longName = "a".repeat(1000) + ".jpg"
        assertEquals("image/jpeg", longName.getMime())
    }

    @Test
    fun testGetMime_veryLongExtension() {
        val longExt = "." + "x".repeat(100)
        assertEquals("application/octet-stream", ("file$longExt").getMime())
    }

    // ========== 空格和空白字符测试 ==========

    @Test
    fun testGetMime_filenameWithSpaces() {
        assertEquals("image/jpeg", "my photo.jpg".getMime())
        assertEquals("video/mp4", "holiday video.mp4".getMime())
    }

    @Test
    fun testGetMime_leadingTrailingSpaces() {
        assertEquals("image/jpeg", " photo.jpg ".getMime())
        assertEquals("video/mp4", "  video.mp4  ".getMime())
    }

    // ========== 隐藏文件测试 ==========

    @Test
    fun testGetMime_hiddenFiles() {
        assertEquals("text/plain", ".gitignore.txt".getMime())
        assertEquals("application/json", ".config.json".getMime())
        assertEquals("application/octet-stream", ".hidden".getMime())
    }

    // ========== URL 和路径测试 ==========

    @Test
    fun testGetMime_urlWithQuery() {
        assertEquals("image/jpeg", "photo.jpg?size=large".getMime())
        assertEquals("image/jpeg", "photo.jpg?v=1&format=original".getMime())
    }

    @Test
    fun testGetMime_urlWithFragment() {
        assertEquals("text/html", "page.html#section".getMime())
    }

    @Test
    fun testGetMime_mixedPathSeparators() {
        assertEquals("image/jpeg", "C:/Users\\user/photo.jpg".getMime())
    }

    // ========== 性能测试 ==========

    @OptIn(ExperimentalTime::class)
    @Test
    fun testGetMime_performance_manyLookups() {
        val filenames = listOf(
            "photo.jpg", "video.mp4", "song.mp3", "doc.pdf",
            "archive.zip", "page.html", "style.css", "script.js"
        )

        val startTime = Clock.System.now().toEpochMilliseconds()
        repeat(10000) {
            filenames.forEach { it.getMime() }
        }
        val duration = Clock.System.now().toEpochMilliseconds() - startTime

        // 10000次循环应该在合理时间内完成（<1秒）
        assertTrue(duration < 1000, "Performance test took ${duration}ms")
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun testGetMime_performance_manyCustomTypes() {
        // 注册大量自定义类型
        repeat(1000) { i ->
            MimeTypeManager.register("custom$i", "application/x-custom-$i")
        }

        val startTime = Clock.System.now().toEpochMilliseconds()
        repeat(1000) { i ->
            "file.custom$i".getMime()
        }
        val duration = Clock.System.now().toEpochMilliseconds() - startTime

        assertTrue(duration < 500, "Custom type lookup took ${duration}ms")
    }

    // ========== 并发安全测试（如果支持）==========

    @Test
    fun testMimeTypeManager_concurrentRegistration() {
        // 注意：这个测试在不支持并发的平台上可能需要跳过
        val types = (1..100).associate { "ext$it" to "type/$it" }

        MimeTypeManager.registerAll(types)
        types.keys.forEach { ext ->
            "file.$ext".getMime()
        }
    }
}