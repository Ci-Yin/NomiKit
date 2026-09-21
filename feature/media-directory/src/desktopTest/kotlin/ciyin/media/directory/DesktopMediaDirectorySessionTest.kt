package ciyin.media.directory

import ciyin.io.File
import ciyin.io.deleteRecursively
import ciyin.io.writeText
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Desktop 用户媒体目录会话契约测试。
 *
 * 测试通过注入自定义 [DirectoryPickerLauncher] 模拟用户选择目录的返回路径，
 * 从而绕开真实的 JFileChooser UI 弹窗，并在隔离的临时目录中验证会话完整生命周期。
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DesktopMediaDirectorySessionTest {

    /** 选目录、提交、存在性、删除、幂等删除应形成完整闭环。 */
    @Test
    fun pickCommitExistsAndDeleteRoundTrip() = withSession { context ->
        val source = File(context.root, "source.png").apply { writeText("sample") }
        val access = context.session.requestAccess()
        context.session.validate(access.accessToken)

        val entry = context.session.commit(
            accessToken = access.accessToken,
            source = source,
            fileName = "committed.png",
            mimeType = "image/png",
        )
        assertEquals("committed.png", entry.fileName)
        assertTrue(context.session.exists(access.accessToken, entry.location))
        context.session.delete(access.accessToken, entry.location)
        assertFalse(context.session.exists(access.accessToken, entry.location))
        // 幂等删除：第二次删除不应抛错。
        context.session.delete(access.accessToken, entry.location)
    }

    /** 目录被外部删除后 validate 应抛 AccessRevoked。 */
    @Test
    fun validateAfterDirectoryDeletionRaisesAccessRevoked() = withSession { context ->
        val externalDirectory = File(context.root, "external").apply { mkdirs() }
        val access = context.session.requestAccess()
        // 在 context.pickerRoot 之外另选一个目录用于探针，确保覆盖外部删除场景。
        context.replacePickerRoot(externalDirectory)
        val externalAccess = context.session.requestAccess()
        externalDirectory.deleteRecursively()
        val exception = assertFailsWith<MediaDirectoryException> {
            context.session.validate(externalAccess.accessToken)
        }
        assertIs<MediaDirectoryError.AccessRevoked>(exception.error)
        assertTrue(File(context.root, "sub").isDirectory)
    }

    /** 同名提交两次应抛 AlreadyExists。 */
    @Test
    fun duplicateCommitReturnsAlreadyExists() = withSession { context ->
        val source = File(context.root, "source.png").apply { writeText("first") }
        val access = context.session.requestAccess()
        context.session.commit(
            accessToken = access.accessToken,
            source = source,
            fileName = "dup.png",
            mimeType = "image/png",
        )
        val exception = assertFailsWith<MediaDirectoryException> {
            context.session.commit(
                accessToken = access.accessToken,
                source = source,
                fileName = "dup.png",
                mimeType = "image/png",
            )
        }
        assertIs<MediaDirectoryError.AlreadyExists>(exception.error)
    }

    /** 注入的自定义 launcher 返回的路径应被会话原样使用作为 access token。 */
    @Test
    fun customPickerLauncherIsUsed() = withSession { context ->
        val access = context.session.requestAccess()
        // launcher 返回的目录路径应作为 canonicalPath 原样透传到 Access。
        assertEquals(context.pickerRoot.path, access.canonicalPath)
        assertEquals(context.pickerRoot.name, access.displayName)
    }

    /**
     * 在隔离临时目录中创建会话。
     *
     * 测试时通过 [SessionContext.replacePickerRoot] 切换 launcher 返回的根目录，
     * 模拟"用户先后选择两个不同目录"的场景。
     */
    private fun withSession(block: suspend (SessionContext) -> Unit) = runTest {
        val root = File(Files.createTempDirectory("nomikit-media-directory-").toString())
        try {
            val sub = File(root, "sub").apply { mkdirs() }
            val pickerLauncher = MutablePickerLauncher(sub.path)
            val session = DesktopMediaDirectorySession(pickerLauncher)
            block(
                SessionContext(
                    root = root,
                    pickerRoot = sub,
                    pickerLauncher = pickerLauncher,
                    session = session
                )
            )
        } finally {
            assertTrue(root.deleteRecursively())
        }
    }

    /** 测试上下文：聚合根目录、launcher 选择目标与会话实例。 */
    private class SessionContext(
        val root: File,
        val pickerRoot: File,
        val pickerLauncher: MutablePickerLauncher,
        val session: DesktopMediaDirectorySession,
    ) {
        /** 把 launcher 后续返回的目录切换到指定目录，便于在一次会话内复用不同 picker 目标。 */
        fun replacePickerRoot(target: File) {
            target.mkdirs()
            pickerLauncher.path = target.path
            pickerRoot.let { /* no-op */ }
        }
    }

    /** 测试用 launcher：可外部覆写要返回的目录路径。 */
    private class MutablePickerLauncher(
        var path: String,
    ) : DirectoryPickerLauncher {
        override suspend fun pick(): String = path
    }
}
