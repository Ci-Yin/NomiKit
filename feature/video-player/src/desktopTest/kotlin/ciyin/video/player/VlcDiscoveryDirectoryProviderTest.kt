package ciyin.video.player

import java.nio.file.Files
import kotlin.io.path.createDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import uk.co.caprica.vlcj.factory.NativeLibraryMappingException

/** VLC 原生库目录解析测试。 */
class VlcDiscoveryDirectoryProviderTest {
    /** 显式属性、环境变量和打包资源按既定优先级返回。 */
    @Test
    fun resolvesExistingDirectoriesInPriorityOrder() {
        val root = Files.createTempDirectory("vlc-discovery-test")
        try {
            val systemDirectory = root.resolve("system").createDirectory()
            val environmentDirectory = root.resolve("environment").createDirectory()
            val composeResources = root.resolve("resources").createDirectory()
            val composeLibraryDirectory = composeResources.resolve("lib").createDirectory()

            assertEquals(
                listOf(
                    systemDirectory.toFile().absoluteFile,
                    environmentDirectory.toFile().absoluteFile,
                    composeLibraryDirectory.toFile().absoluteFile,
                ),
                resolveVlcLibraryDirectories(
                    systemPropertyPath = systemDirectory.toString(),
                    environmentPath = environmentDirectory.toString(),
                    composeResourcesPath = composeResources.toString(),
                ),
            )
        } finally {
            root.toFile().deleteRecursively()
        }
    }

    /** 空白、重复和不存在的目录不会进入 VLCJ 搜索列表。 */
    @Test
    fun ignoresInvalidAndDuplicateDirectories() {
        val root = Files.createTempDirectory("vlc-discovery-test")
        try {
            val libraryDirectory = root.resolve("library").createDirectory().toFile().absoluteFile

            assertEquals(
                listOf(libraryDirectory),
                resolveVlcLibraryDirectories(
                    systemPropertyPath = "  ${libraryDirectory.path}  ",
                    environmentPath = libraryDirectory.path,
                    composeResourcesPath = root.resolve("missing").toString(),
                ),
            )
            assertTrue(
                resolveVlcLibraryDirectories(
                    systemPropertyPath = " ",
                    environmentPath = null,
                    composeResourcesPath = null,
                ).isEmpty(),
            )
        } finally {
            root.toFile().deleteRecursively()
        }
    }

    /** VLC 原生链接错误会转换为明确的不可用结果。 */
    @Test
    fun mapsLinkageErrorToUnavailableResult() {
        val error = UnsatisfiedLinkError("libvlc missing")

        val result = initializeVlcPlayer { throw error }

        assertTrue(result is VlcPlayerInitializationResult.Unavailable)
        assertEquals(error, result.cause)
    }

    /** VLCJ 包装的原生库映射失败同样返回不可用结果。 */
    @Test
    fun mapsNativeLibraryMappingExceptionToUnavailableResult() {
        val error = NativeLibraryMappingException(
            "libvlc mapping failed",
            UnsatisfiedLinkError("libvlc missing"),
        )

        val result = initializeVlcPlayer { throw error }

        assertTrue(result is VlcPlayerInitializationResult.Unavailable)
        assertEquals(error, result.cause)
    }

    /** 非原生库初始化异常保持抛出，避免掩盖播放器实现错误。 */
    @Test
    fun propagatesNonLinkageFailures() {
        assertFailsWith<IllegalStateException> {
            initializeVlcPlayer { error("factory failed") }
        }
    }
}
