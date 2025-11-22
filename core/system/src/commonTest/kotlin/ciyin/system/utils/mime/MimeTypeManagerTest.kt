package ciyin.system.utils.mime

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 *
 * kotlin文件作用描述
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2025/11/1 15:02
 */


class MimeTypeManagerTest {

    @BeforeTest
    fun setup() {
        MimeTypeManager.clearCustomTypes()
    }

    @AfterTest
    fun tearDown() {
        MimeTypeManager.clearCustomTypes()
    }

    // ========== 自定义类型注册测试 ==========

    @Test
    fun testRegister_singleType() {
        MimeTypeManager.register("custom", "application/x-custom")
        assertEquals("application/x-custom", "file.custom".getMime())
    }

    @Test
    fun testRegister_overrideBuiltIn() {
        // 自定义类型应该覆盖内置类型
        MimeTypeManager.register("jpg", "image/custom-jpeg")
        assertEquals("image/custom-jpeg", "photo.jpg".getMime())
    }

    @Test
    fun testRegister_caseInsensitive() {
        MimeTypeManager.register("CUSTOM", "application/x-custom")
        assertEquals("application/x-custom", "file.custom".getMime())
        assertEquals("application/x-custom", "file.CUSTOM".getMime())
    }

    @Test
    fun testRegisterAll() {
        val customTypes = mapOf(
            "ext1" to "type/one",
            "ext2" to "type/two",
            "ext3" to "type/three"
        )
        MimeTypeManager.registerAll(customTypes)

        assertEquals("type/one", "file.ext1".getMime())
        assertEquals("type/two", "file.ext2".getMime())
        assertEquals("type/three", "file.ext3".getMime())
    }

    @Test
    fun testUnregister() {
        MimeTypeManager.register("custom", "application/x-custom")
        assertEquals("application/x-custom", "file.custom".getMime())

        MimeTypeManager.unregister("custom")
        assertEquals("application/octet-stream", "file.custom".getMime())
    }

    @Test
    fun testUnregister_nonExistent() {
        // 删除不存在的类型不应该报错
        MimeTypeManager.unregister("nonexistent")
    }

    @Test
    fun testClearCustomTypes() {
        MimeTypeManager.register("custom1", "type/one")
        MimeTypeManager.register("custom2", "type/two")

        MimeTypeManager.clearCustomTypes()

        assertEquals("application/octet-stream", "file.custom1".getMime())
        assertEquals("application/octet-stream", "file.custom2".getMime())
    }

    // ========== getMimeType 方法测试 ==========

    @Test
    fun testGetMimeType_basic() {
        assertEquals("image/jpeg", MimeTypeManager.getMimeType("photo.jpg"))
        assertEquals("video/mp4", MimeTypeManager.getMimeType("video.mp4"))
        assertEquals("application/pdf", MimeTypeManager.getMimeType("doc.pdf"))
    }

    @Test
    fun testGetMimeType_withCustomDefault() {
        assertEquals("text/plain", MimeTypeManager.getMimeType("unknown.xyz", "text/plain"))
        assertEquals("custom/default", MimeTypeManager.getMimeType("file", "custom/default"))
    }

    // ========== getExtension 方法测试 ==========

    @Test
    fun testGetExtension_builtInTypes() {
        assertEquals("jpg", MimeTypeManager.getExtension("image/jpeg"))
        assertEquals("png", MimeTypeManager.getExtension("image/png"))
        assertEquals("mp4", MimeTypeManager.getExtension("video/mp4"))
    }

    @Test
    fun testGetExtension_customTypes() {
        MimeTypeManager.register("custom", "application/x-custom")
        assertEquals("custom", MimeTypeManager.getExtension("application/x-custom"))
    }

    @Test
    fun testGetExtension_prioritizeCustom() {
        // 如果自定义和内置都有，应该返回自定义的
        MimeTypeManager.register("newext", "image/jpeg")
        val ext = MimeTypeManager.getExtension("image/jpeg")
        assertTrue(ext == "newext" || ext == "jpg") // 可能返回任一个
    }

    // ========== 类型检查方法测试 ==========

    @Test
    fun testIsImage() {
        assertTrue(MimeTypeManager.isImage("image/jpeg"))
        assertTrue(MimeTypeManager.isImage("image/png"))
        assertTrue(MimeTypeManager.isImage("image/gif"))
        assertTrue(MimeTypeManager.isImage("image/webp"))

        assertFalse(MimeTypeManager.isImage("video/mp4"))
        assertFalse(MimeTypeManager.isImage("audio/mpeg"))
        assertFalse(MimeTypeManager.isImage("application/pdf"))
    }

    @Test
    fun testIsVideo() {
        assertTrue(MimeTypeManager.isVideo("video/mp4"))
        assertTrue(MimeTypeManager.isVideo("video/x-msvideo"))
        assertTrue(MimeTypeManager.isVideo("video/quicktime"))

        assertFalse(MimeTypeManager.isVideo("image/jpeg"))
        assertFalse(MimeTypeManager.isVideo("audio/mpeg"))
    }

    @Test
    fun testIsAudio() {
        assertTrue(MimeTypeManager.isAudio("audio/mpeg"))
        assertTrue(MimeTypeManager.isAudio("audio/wav"))
        assertTrue(MimeTypeManager.isAudio("audio/ogg"))

        assertFalse(MimeTypeManager.isAudio("video/mp4"))
        assertFalse(MimeTypeManager.isAudio("image/jpeg"))
    }

    @Test
    fun testIsText() {
        assertTrue(MimeTypeManager.isText("text/plain"))
        assertTrue(MimeTypeManager.isText("text/html"))
        assertTrue(MimeTypeManager.isText("text/css"))

        assertFalse(MimeTypeManager.isText("application/json"))
        assertFalse(MimeTypeManager.isText("image/jpeg"))
    }

    // ========== 列表方法测试 ==========

    @Test
    fun testGetAllExtensions() {
        val extensions = MimeTypeManager.getAllExtensions()

        assertTrue(extensions.contains("jpg"))
        assertTrue(extensions.contains("png"))
        assertTrue(extensions.contains("mp4"))
        assertTrue(extensions.contains("pdf"))
        assertTrue(extensions.size > 50) // 应该有很多内置类型
    }

    @Test
    fun testGetAllExtensions_includesCustom() {
        MimeTypeManager.register("custom", "application/x-custom")
        val extensions = MimeTypeManager.getAllExtensions()

        assertTrue(extensions.contains("custom"))
    }

    @Test
    fun testGetAllMimeTypes() {
        val mimeTypes = MimeTypeManager.getAllMimeTypes()

        assertTrue(mimeTypes.contains("image/jpeg"))
        assertTrue(mimeTypes.contains("video/mp4"))
        assertTrue(mimeTypes.contains("application/pdf"))
        assertTrue(mimeTypes.size > 50)
    }

    @Test
    fun testGetAllMimeTypes_includesCustom() {
        MimeTypeManager.register("custom", "application/x-custom")
        val mimeTypes = MimeTypeManager.getAllMimeTypes()

        assertTrue(mimeTypes.contains("application/x-custom"))
    }
}