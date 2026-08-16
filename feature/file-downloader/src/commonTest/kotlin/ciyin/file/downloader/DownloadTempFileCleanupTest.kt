package ciyin.file.downloader

import ciyin.file.downloader.model.DownloadConfig
import ciyin.file.downloader.util.deleteIdempotently
import ciyin.file.downloader.util.deleteTempFile
import okio.IOException
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** 下载临时文件幂等清理契约测试。 */
class DownloadTempFileCleanupTest {

    /** 已存在的临时文件首次被删除后，重复删除应保持成功。 */
    @Test
    fun existingTempFileCanBeDeletedRepeatedly() {
        val fileSystem = FakeFileSystem()
        val saveFile = "/downloads/app.apk".toPath()
        val tempFile = "/downloads/app.apk.tmp".toPath()
        val config = DownloadConfig(
            url = "https://example.com/app.apk",
            savePath = saveFile.toString(),
        )
        fileSystem.createDirectories(tempFile.parent!!)
        fileSystem.write(tempFile) { writeUtf8("partial download") }

        assertTrue(fileSystem.exists(tempFile))
        config.deleteTempFile(fileSystem)
        assertFalse(fileSystem.exists(tempFile))

        config.deleteTempFile(fileSystem)
        assertFalse(fileSystem.exists(tempFile))
    }

    /** 删除失败后目标已不存在，说明其他清理方已经完成，应按成功处理。 */
    @Test
    fun concurrentDeletionFailureIsTreatedAsSuccess() {
        deleteIdempotently(
            delete = { throw IOException("Deletion failed") },
            exists = { false },
        )
    }

    /** 删除失败且目标仍存在时必须保留真实 I/O 错误。 */
    @Test
    fun persistentDeletionFailureIsRethrown() {
        assertFailsWith<IOException> {
            deleteIdempotently(
                delete = { throw IOException("Deletion failed") },
                exists = { true },
            )
        }
    }
}
