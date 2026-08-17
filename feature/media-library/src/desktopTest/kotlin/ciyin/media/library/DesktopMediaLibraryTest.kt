package ciyin.media.library

import ciyin.io.File
import ciyin.io.deleteRecursively
import ciyin.io.resolve
import ciyin.io.writeText
import java.nio.file.Files
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/** Desktop 系统媒体库文件提交契约测试。 */
class DesktopMediaLibraryTest {
    /** 发布、检查与幂等删除应形成完整闭环。 */
    @Test
    fun publishExistsAndDeleteRoundTrip() = withLibrary { root, library ->
        val source = root.resolve("source.png").apply { writeText("sample") }
        val request = MediaPublishRequest(
            source = source,
            displayName = "published.png",
            mimeType = "image/png",
            collection = MediaCollection.Images,
            relativeDirectory = "NomiKit/Sample",
        )

        val published = library.publish(request)

        assertEquals("published.png", published.displayName)
        assertEquals(source.length(), published.size)
        assertTrue(published.uri?.startsWith("file:") == true)
        assertTrue(library.exists(published))
        library.delete(published)
        assertFalse(library.exists(published))
        library.delete(published)
    }

    /** 已存在同名目标时不得覆盖原文件。 */
    @Test
    fun existingTargetReturnsAlreadyExists() = withLibrary { root, library ->
        val source = root.resolve("source.png").apply { writeText("first") }
        val request = MediaPublishRequest(
            source = source,
            displayName = "duplicate.png",
            mimeType = "image/png",
            collection = MediaCollection.Images,
        )
        library.publish(request)

        val exception = assertFailsWith<MediaLibraryException> {
            library.publish(request)
        }

        assertIs<MediaLibraryError.AlreadyExists>(exception.error)
    }

    /** 平台引用不得越出任一受支持的用户媒体目录。 */
    @Test
    fun referenceOutsideMediaRootsIsRejected() = withLibrary { root, library ->
        val outside = root.resolve("outside.png").apply { writeText("outside") }
        val media = PublishedMedia(
            platformId = outside.absolutePath,
            uri = outside.toURI(),
            displayName = outside.name,
            mimeType = "image/png",
            size = outside.length(),
        )

        val exception = assertFailsWith<MediaLibraryException> {
            library.exists(media)
        }

        assertIs<MediaLibraryError.PermissionDenied>(exception.error)
    }

    /** 媒体根目录本身不得被持久化引用删除。 */
    @Test
    fun mediaRootReferenceCannotBeDeleted() = withLibrary { root, library ->
        val imagesRoot = root.resolve(MediaCollection.Images.name).apply { mkdirs() }
        val media = PublishedMedia(
            platformId = imagesRoot.absolutePath,
            uri = imagesRoot.toURI(),
            displayName = imagesRoot.name,
            mimeType = "image/png",
            size = 0L,
        )

        val exception = assertFailsWith<MediaLibraryException> {
            library.delete(media)
        }

        assertIs<MediaLibraryError.PermissionDenied>(exception.error)
        assertTrue(imagesRoot.isDirectory)
    }

    /** 媒体目录内的子目录不得按普通文件引用删除。 */
    @Test
    fun directoryReferenceCannotBeDeleted() = withLibrary { root, library ->
        val directory = root.resolve(MediaCollection.Images.name).resolve("album").apply { mkdirs() }
        val media = PublishedMedia(
            platformId = directory.absolutePath,
            uri = directory.toURI(),
            displayName = directory.name,
            mimeType = "image/png",
            size = 0L,
        )

        val exception = assertFailsWith<MediaLibraryException> {
            library.delete(media)
        }

        assertIs<MediaLibraryError.Unsupported>(exception.error)
        assertTrue(directory.isDirectory)
    }

    /** 提交后发生的取消必须删除已经原子移动的目标文件。 */
    @Test
    fun cancellationAfterCommitRollsBackPublishedFile() = withLibrary(
        onCommitted = { throw CancellationException("cancel after commit") },
    ) { root, library ->
        val source = root.resolve("cancel-source.png").apply { writeText("cancel") }
        val request = MediaPublishRequest(
            source = source,
            displayName = "cancelled.png",
            mimeType = "image/png",
            collection = MediaCollection.Images,
        )

        assertFailsWith<CancellationException> {
            library.publish(request)
        }

        assertFalse(root.resolve(MediaCollection.Images.name).resolve("cancelled.png").exists())
    }

    /** 在隔离临时目录中创建并释放 Desktop 媒体库。 */
    private fun withLibrary(
        onCommitted: () -> Unit = {},
        block: suspend (root: File, library: DesktopMediaLibrary) -> Unit,
    ) = runTest {
        val root = File(Files.createTempDirectory("nomikit-media-library-").toString())
        val resolver = DesktopMediaDirectoryResolver { collection ->
            root.resolve(collection.name)
        }
        try {
            block(root, DesktopMediaLibrary(resolver, coroutineContext, onCommitted))
        } finally {
            assertTrue(root.deleteRecursively())
        }
    }
}
