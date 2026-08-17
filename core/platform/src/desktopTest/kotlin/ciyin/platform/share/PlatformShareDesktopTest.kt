package ciyin.platform.share

import ciyin.io.File
import ciyin.platform.Arch
import ciyin.platform.Platform
import com.sun.jna.platform.win32.Guid
import java.nio.file.Files
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/** Desktop 系统分享平台分流与载荷解析测试。 */
class PlatformShareDesktopTest {

    /** JNA GUID 与 IID 子类即使类型不同也必须按字段值判定相同。 */
    @Test
    fun guidAndIidWithSameFieldsAreEqualForComQueryInterface() {
        val interfaceId = Guid.IID("{E521C894-2C26-5946-9E61-2B5E188D01ED}")
        interfaceId.write()
        val requestedId = Guid.GUID(interfaceId.pointer)

        assertTrue(requestedId.hasSameWindowsShareGuidValue(interfaceId))
    }

    /** macOS 必须明确返回 Unsupported 且不调用 Windows launcher。 */
    @Test
    fun macOsReturnsUnsupportedWithoutInvokingWindowsLauncher() = runBlocking {
        var invoked = false

        val result = shareDesktopPlatformContent(
            platform = Platform.MacOS(Arch.X86_64),
            payload = PlatformSharePayload.Text("content"),
            windowsLauncher = WindowsShareLauncher {
                invoked = true
                PlatformShareResult.Opened
            },
        )

        assertEquals(PlatformShareResult.Unsupported, result)
        assertFalse(invoked)
    }

    /** Windows 必须把规范化载荷委托给 launcher。 */
    @Test
    fun windowsDelegatesNormalizedPayloadToLauncher() = runBlocking {
        var receivedPayload: WindowsSharePayload? = null

        val result = shareDesktopPlatformContent(
            platform = Platform.Windows(Arch.X86_64),
            payload = PlatformSharePayload.Text(
                value = "first line\nsecond line",
                title = "  explicit title  ",
            ),
            windowsLauncher = WindowsShareLauncher { payload ->
                receivedPayload = payload
                PlatformShareResult.Opened
            },
        )

        assertEquals(PlatformShareResult.Opened, result)
        val textPayload = assertIs<WindowsSharePayload.Text>(receivedPayload)
        assertEquals("explicit title", textPayload.title)
        assertEquals("first line\nsecond line", textPayload.value)
    }

    /** 未指定标题时必须使用文本首个非空行。 */
    @Test
    fun textTitleUsesFirstNonBlankLine() {
        val payload = PlatformSharePayload.Text("\n  first line  \nsecond line")
            .toWindowsSharePayload()

        assertEquals("first line", assertIs<WindowsSharePayload.Text>(payload).title)
    }

    /** 未指定 displayName 时必须使用实际文件名派生标题。 */
    @Test
    fun fileTitleUsesActualFileNameWhenDisplayNameIsMissing() {
        val path = Files.createTempFile("nomikit-share-derived-title", ".txt")
        try {
            val payload = PlatformSharePayload.File(
                PlatformShareFile(
                    source = PlatformShareFileSource.LocalFile(File(path.toString())),
                    mimeType = "text/plain",
                ),
            ).toWindowsSharePayload()

            assertEquals(
                path.fileName.toString(),
                assertIs<WindowsSharePayload.Files>(payload).title,
            )
        } finally {
            Files.deleteIfExists(path)
        }
    }

    /** 空文件列表必须报告 InvalidPayload。 */
    @Test
    fun emptyFilesReportsInvalidPayload() {
        val exception = assertFailsWith<PlatformShareException> {
            PlatformSharePayload.Files(emptyList()).toWindowsSharePayload()
        }

        assertEquals(PlatformShareFailureReason.InvalidPayload, exception.reason)
    }

    /** LocalFile 必须转换为绝对规范路径。 */
    @Test
    fun localFilePathIsAbsoluteAndNormalized() {
        val directory = java.nio.file.Path.of("build", "platform-share-tests")
        Files.createDirectories(directory)
        val path = directory.resolve("normalized-path.txt")
        Files.writeString(path, "content")
        val unnormalizedPath = directory.resolve("..").resolve(directory.fileName).resolve(path.fileName)
        try {
            val payload = PlatformSharePayload.File(
                PlatformShareFile(
                    source = PlatformShareFileSource.LocalFile(File(unnormalizedPath.toString())),
                    mimeType = "text/plain",
                ),
            ).toWindowsSharePayload()

            val file = assertIs<WindowsSharePayload.Files>(payload).values.single()
            assertTrue(file.path.isAbsolute)
            assertEquals(path.toAbsolutePath().normalize(), file.path)
        } finally {
            Files.deleteIfExists(path)
        }
    }

    /** 文件载荷必须保留输入顺序并优先使用 displayName 派生标题。 */
    @Test
    fun filePayloadPreservesOrderAndUsesDisplayName() {
        val firstPath = Files.createTempFile("nomikit-share-first", ".txt")
        val secondPath = Files.createTempFile("nomikit-share-second", ".txt")
        try {
            val payload = PlatformSharePayload.Files(
                values = listOf(
                    PlatformShareFile(
                        source = PlatformShareFileSource.LocalFile(File(firstPath.toString())),
                        mimeType = "text/plain",
                        displayName = "display.txt",
                    ),
                    PlatformShareFile(
                        source = PlatformShareFileSource.LocalFile(File(secondPath.toString())),
                        mimeType = "text/plain",
                    ),
                ),
            ).toWindowsSharePayload()

            val filesPayload = assertIs<WindowsSharePayload.Files>(payload)
            assertEquals("display.txt", filesPayload.title)
            assertEquals(
                listOf(firstPath.toAbsolutePath(), secondPath.toAbsolutePath()),
                filesPayload.values.map(WindowsShareFile::path),
            )
        } finally {
            Files.deleteIfExists(firstPath)
            Files.deleteIfExists(secondPath)
        }
    }

    /** Windows 文件 URI 只允许 file 协议。 */
    @Test
    fun nonFileUriReportsInvalidUri() {
        val exception = assertFailsWith<PlatformShareException> {
            PlatformSharePayload.File(
                PlatformShareFile(
                    source = PlatformShareFileSource.Uri("https://example.com/file.txt"),
                    mimeType = "text/plain",
                ),
            ).toWindowsSharePayload()
        }

        assertEquals(PlatformShareFailureReason.InvalidUri, exception.reason)
    }

    /** 不存在的 LocalFile 必须报告 FileUnavailable。 */
    @Test
    fun missingLocalFileReportsFileUnavailable() {
        val missingFile = File("build/missing-platform-share-${System.nanoTime()}.txt")

        val exception = assertFailsWith<PlatformShareException> {
            PlatformSharePayload.File(
                PlatformShareFile(
                    source = PlatformShareFileSource.LocalFile(missingFile),
                    mimeType = "text/plain",
                ),
            ).toWindowsSharePayload()
        }

        assertEquals(PlatformShareFailureReason.FileUnavailable, exception.reason)
    }

    /** 不存在的 file URI 必须报告 UriUnavailable。 */
    @Test
    fun missingFileUriReportsUriUnavailable() {
        val missingPath = java.nio.file.Path.of(
            "build",
            "missing-platform-share-uri-${System.nanoTime()}.txt",
        ).toAbsolutePath()

        val exception = assertFailsWith<PlatformShareException> {
            PlatformSharePayload.File(
                PlatformShareFile(
                    source = PlatformShareFileSource.Uri(missingPath.toUri().toString()),
                    mimeType = "text/plain",
                ),
            ).toWindowsSharePayload()
        }

        assertEquals(PlatformShareFailureReason.UriUnavailable, exception.reason)
    }

    /** 访问拒绝 HRESULT 必须映射为 PermissionDenied。 */
    @Test
    fun accessDeniedHResultMapsToPermissionDenied() {
        assertEquals(
            expected = PlatformShareFailureReason.PermissionDenied,
            actual = windowsShareFailureReasonForHResult(
                hresult = E_ACCESSDENIED_FOR_TEST,
                fallbackReason = PlatformShareFailureReason.LaunchFailed,
            ),
        )
    }

    /** 通用失败 HRESULT 必须保留调用点指定的 LaunchFailed。 */
    @Test
    fun genericHResultMapsToLaunchFailed() {
        assertEquals(
            expected = PlatformShareFailureReason.LaunchFailed,
            actual = windowsShareFailureReasonForHResult(
                hresult = E_FAIL_FOR_TEST,
                fallbackReason = PlatformShareFailureReason.LaunchFailed,
            ),
        )
    }

    /** Share Contract 缺失 HRESULT 必须识别为不支持。 */
    @Test
    fun missingShareContractHResultIsUnsupported() {
        assertTrue(isUnsupportedWindowsShareHResult(REGDB_E_CLASSNOTREG_FOR_TEST))
        assertTrue(isUnsupportedWindowsShareHResult(E_NOINTERFACE_FOR_TEST))
        assertFalse(isUnsupportedWindowsShareHResult(E_FAIL_FOR_TEST))
    }

    private companion object {
        /** Windows E_ACCESSDENIED HRESULT。 */
        const val E_ACCESSDENIED_FOR_TEST: Int = -2147024891

        /** Windows REGDB_E_CLASSNOTREG HRESULT。 */
        const val REGDB_E_CLASSNOTREG_FOR_TEST: Int = -2147221164

        /** Windows E_NOINTERFACE HRESULT。 */
        const val E_NOINTERFACE_FOR_TEST: Int = -2147467262

        /** Windows E_FAIL HRESULT。 */
        const val E_FAIL_FOR_TEST: Int = -2147467259
    }
}
