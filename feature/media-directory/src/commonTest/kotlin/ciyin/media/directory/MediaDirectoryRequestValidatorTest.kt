package ciyin.media.directory

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

/** 文件名 / MIME 校验契约测试。 */
class MediaDirectoryRequestValidatorTest {
    /** 空白文件名应被拒绝。 */
    @Test
    fun blankFileNameIsRejected() {
        val exception = assertFailsWith<MediaDirectoryException> {
            validateCommitRequest(
                fileName = " ",
                mimeType = "image/png",
                source = ioSource(),
            )
        }
        assertIs<MediaDirectoryError.Unsupported>(exception.error)
    }

    /** 包含路径分隔符的文件名应被拒绝。 */
    @Test
    fun pathSeparatorsAreRejected() {
        listOf("a/b.png", "a\\b.png", "a:b.png", "a\u0000b.png").forEach { fileName ->
            val exception = assertFailsWith<MediaDirectoryException> {
                validateCommitRequest(
                    fileName = fileName,
                    mimeType = "image/png",
                    source = ioSource(),
                )
            }
            assertIs<MediaDirectoryError.Unsupported>(exception.error)
        }
    }

    /** 空白 MIME 类型应被拒绝。 */
    @Test
    fun blankMimeTypeIsRejected() {
        val exception = assertFailsWith<MediaDirectoryException> {
            validateCommitRequest(
                fileName = "demo.png",
                mimeType = "",
                source = ioSource(),
            )
        }
        assertIs<MediaDirectoryError.Unsupported>(exception.error)
    }

    /** 不存在的源文件应当返回 NotFound。 */
    @Test
    fun missingSourceIsNotFound() {
        val exception = assertFailsWith<MediaDirectoryException> {
            validateCommitRequest(
                fileName = "demo.png",
                mimeType = "image/png",
                source = ciyin.io.File("media-directory-missing-source-${System.nanoTime()}"),
            )
        }
        assertIs<MediaDirectoryError.NotFound>(exception.error)
    }

    /** 测试使用的占位源文件。 */
    private fun ioSource(): ciyin.io.File =
        ciyin.io.File("media-directory-validator-source-${System.nanoTime()}")
}
