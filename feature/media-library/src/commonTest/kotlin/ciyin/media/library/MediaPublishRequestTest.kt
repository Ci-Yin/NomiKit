package ciyin.media.library

import ciyin.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

/** 系统媒体库公共请求与错误契约测试。 */
class MediaPublishRequestTest {
    /** 空显示名称应在访问源文件前返回不支持错误。 */
    @Test
    fun blankDisplayNameIsRejected() {
        val exception = assertFailsWith<MediaLibraryException> {
            request(displayName = " ").validate()
        }

        assertIs<MediaLibraryError.Unsupported>(exception.error)
    }

    /** 显示名称不得包含任何平台路径分隔符或卷分隔符。 */
    @Test
    fun unsafeDisplayNamesAreRejected() {
        listOf("a/b.png", "a\\b.png", "a:b.png", "a\u0000b.png").forEach { displayName ->
            val exception = assertFailsWith<MediaLibraryException> {
                request(displayName = displayName).validate()
            }

            assertIs<MediaLibraryError.Unsupported>(exception.error)
        }
    }

    /** 空 MIME 类型应返回不支持错误。 */
    @Test
    fun blankMimeTypeIsRejected() {
        val exception = assertFailsWith<MediaLibraryException> {
            request(mimeType = "").validate()
        }

        assertIs<MediaLibraryError.Unsupported>(exception.error)
    }

    /** 目录穿越应在访问源文件前被拒绝。 */
    @Test
    fun relativeDirectoryTraversalIsRejected() {
        val exception = assertFailsWith<MediaLibraryException> {
            request(relativeDirectory = "albums/../private").validate()
        }

        assertIs<MediaLibraryError.Unsupported>(exception.error)
    }

    /** 空白相对目录不得被静默解释成未指定目录。 */
    @Test
    fun blankRelativeDirectoryIsRejected() {
        val exception = assertFailsWith<MediaLibraryException> {
            request(relativeDirectory = "   ").validate()
        }

        assertIs<MediaLibraryError.Unsupported>(exception.error)
    }

    /** 缺失源文件应返回 NotFound。 */
    @Test
    fun missingSourceIsNotFound() {
        val exception = assertFailsWith<MediaLibraryException> {
            request().validate()
        }

        assertIs<MediaLibraryError.NotFound>(exception.error)
    }

    /** PublishedMedia 应保持可持久化字段的值语义。 */
    @Test
    fun publishedMediaHasValueSemantics() {
        val media = PublishedMedia(
            platformId = "platform-id",
            uri = "content://media/1",
            displayName = "sample.png",
            mimeType = "image/png",
            size = 4L,
        )

        assertEquals(media, media.copy())
        assertEquals(4, MediaCollection.entries.size)
    }

    /** 构造一个默认指向缺失源文件的请求。 */
    private fun request(
        displayName: String = "sample.png",
        mimeType: String = "image/png",
        relativeDirectory: String? = null,
    ): MediaPublishRequest = MediaPublishRequest(
        source = File("missing-media-library-test-source"),
        displayName = displayName,
        mimeType = mimeType,
        collection = MediaCollection.Images,
        relativeDirectory = relativeDirectory,
    )
}
