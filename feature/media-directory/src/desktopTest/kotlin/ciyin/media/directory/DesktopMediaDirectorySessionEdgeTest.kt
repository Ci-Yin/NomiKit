package ciyin.media.directory

import ciyin.io.File
import ciyin.io.deleteRecursively
import ciyin.io.writeText
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

/**
 * `DesktopMediaDirectorySession` 边界 / IO 归因 / 幂等契约补充测试。
 *
 * 与 [DesktopMediaDirectorySessionTest] 互补，聚焦异常分支：
 * - 目录被外部删除后再次访问的语义。
 * - 非法 location 的归因。
 * - 同一目录的二次 commit / delete 幂等。
 */
class DesktopMediaDirectorySessionEdgeTest {

    /** commit 之后目录被外部删除，validate 应抛 AccessRevoked。 */
    @Test
    fun validateAfterDirectoryDeletedRaisesAccessRevoked() = runTest {
        withSession { context ->
            val access = context.session.requestAccess()
            context.pickerRoot.deleteRecursively()
            val exception = assertFailsWith<MediaDirectoryException> {
                context.session.validate(access.accessToken)
            }
            assertIs<MediaDirectoryError.AccessRevoked>(exception.error)
        }
    }

    /** 同一目录、同一文件名连续 commit 第二次应抛 AlreadyExists，且目标文件不被覆盖。 */
    @Test
    fun duplicateCommitLeavesOriginalIntact() = runTest {
        withSession { context ->
            val source = File(context.root, "source.png").apply { writeText("first") }
            val access = context.session.requestAccess()
            context.session.commit(
                accessToken = access.accessToken,
                source = source,
                fileName = "dup.png",
                mimeType = "image/png",
            )
            val originalSize = File(context.pickerRoot, "dup.png").length()
            val exception = assertFailsWith<MediaDirectoryException> {
                context.session.commit(
                    accessToken = access.accessToken,
                    source = source,
                    fileName = "dup.png",
                    mimeType = "image/png",
                )
            }
            assertIs<MediaDirectoryError.AlreadyExists>(exception.error)
            assertEquals(originalSize, File(context.pickerRoot, "dup.png").length())
        }
    }

    /** delete 传入 NUL 等非法 location 应抛 Unsupported 而不冒到 IO。 */
    @Test
    fun deleteInvalidLocationRaisesUnsupported() = runTest {
        withSession { context ->
            val access = context.session.requestAccess()
            val exception = assertFailsWith<MediaDirectoryException> {
                context.session.delete(access.accessToken, "\u0000")
            }
            assertIs<MediaDirectoryError.Unsupported>(exception.error)
        }
    }

    /** exists 在目录被外部删除后应返回 false，而不是抛错。 */
    @Test
    fun existsReturnsFalseAfterDirectoryDeleted() = runTest {
        withSession { context ->
            val source = File(context.root, "source.png").apply { writeText("data") }
            val access = context.session.requestAccess()
            val entry = context.session.commit(
                accessToken = access.accessToken,
                source = source,
                fileName = "ghost-exists.png",
                mimeType = "image/png",
            )
            assertTrue(context.session.exists(access.accessToken, entry.location))
            context.pickerRoot.deleteRecursively()
            assertFalse(context.session.exists(access.accessToken, entry.location))
        }
    }

    /** delete 两次（幂等）不应抛错，且第二次结束后 exists 仍为 false。 */
    @Test
    fun deleteIsIdempotent() = runTest {
        withSession { context ->
            val source = File(context.root, "source.png").apply { writeText("data") }
            val access = context.session.requestAccess()
            val entry = context.session.commit(
                accessToken = access.accessToken,
                source = source,
                fileName = "idempotent.png",
                mimeType = "image/png",
            )
            context.session.delete(access.accessToken, entry.location)
            context.session.delete(access.accessToken, entry.location)
            assertFalse(context.session.exists(access.accessToken, entry.location))
        }
    }

    /** 临时目录中跑的会话骨架。 */
    private suspend fun withSession(block: suspend (EdgeSessionContext) -> Unit) {
        val root = File(Files.createTempDirectory("nomikit-media-directory-edge-").toString())
        try {
            val sub = File(root, "sub").apply { mkdirs() }
            val launcher = MutablePickerLauncher(sub.path)
            val session = DesktopMediaDirectorySession(launcher)
            block(EdgeSessionContext(root, sub, launcher, session))
        } finally {
            assertTrue(root.deleteRecursively(), "临时目录未清理:$root")
        }
    }

    /** 测试上下文：聚合根目录、launcher 选择目标与会话实例。 */
    private class EdgeSessionContext(
        val root: File,
        val pickerRoot: File,
        val pickerLauncher: MutablePickerLauncher,
        val session: DesktopMediaDirectorySession,
    )

    /** 测试用 launcher：可外部覆写要返回的目录路径。 */
    private class MutablePickerLauncher(
        var path: String,
    ) : DirectoryPickerLauncher {
        override suspend fun pick(): String = path
    }
}
