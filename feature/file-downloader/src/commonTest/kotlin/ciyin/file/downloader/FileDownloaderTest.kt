package ciyin.file.downloader

import ciyin.file.downloader.core.DownloadState
import ciyin.file.downloader.model.DownloadConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull

/**
 * [CommonFileDownloader] 的确定性下载测试。
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FileDownloaderTest {

    /** 普通下载应写入完整响应并发布完成状态。 */
    @Test
    fun normalDownloadWritesFinalFile() = runTest {
        val body = "NomiKit file downloader"
        val fileSystem = FakeFileSystem()
        val client = HttpClient(MockEngine { request ->
            assertEquals(HttpMethod.Get, request.method)
            assertNull(request.headers[HttpHeaders.Range])
            respond(
                content = body,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentLength, body.encodeToByteArray().size.toString()),
            )
        })
        val downloader = createDownloader(client, fileSystem)
        val savePath = "/downloads/readme.md".toPath()

        downloader.download(testConfig(savePath.toString())).join()

        val complete = assertIs<DownloadState.Complete>(downloader.state.value)
        assertEquals(savePath, complete.filePath)
        assertEquals(body, fileSystem.read(savePath) { readUtf8() })
        downloader.cleanup()
        runCurrent()
    }

    /** 已有临时文件时应发送 Range 请求并把响应追加到原内容后。 */
    @Test
    fun resumeDownloadAppendsPartialContent() = runTest {
        val fileSystem = FakeFileSystem()
        val savePath = "/downloads/archive.bin".toPath()
        val tempPath = "/downloads/archive.bin.tmp".toPath()
        fileSystem.createDirectories(tempPath.parent!!)
        fileSystem.write(tempPath) { writeUtf8("abc") }
        val client = HttpClient(MockEngine { request ->
            assertEquals("bytes=3-", request.headers[HttpHeaders.Range])
            respond(
                content = "def",
                status = HttpStatusCode.PartialContent,
                headers = headersOf(
                    HttpHeaders.ContentLength to listOf("3"),
                    HttpHeaders.ContentRange to listOf("bytes 3-5/6"),
                ),
            )
        })
        val downloader = createDownloader(client, fileSystem)

        downloader.download(testConfig(savePath.toString())).join()

        assertIs<DownloadState.Complete>(downloader.state.value)
        assertEquals("abcdef", fileSystem.read(savePath) { readUtf8() })
        assertFalse(fileSystem.exists(tempPath))
        downloader.cleanup()
        runCurrent()
    }

    /** 目标已存在时应遵守覆盖开关，并在允许覆盖后替换内容。 */
    @Test
    fun overwritePolicyProtectsAndReplacesExistingFile() = runTest {
        val fileSystem = FakeFileSystem()
        val savePath = "/downloads/existing.txt".toPath()
        fileSystem.createDirectories(savePath.parent!!)
        fileSystem.write(savePath) { writeUtf8("existing") }
        var requestCount = 0
        val client = HttpClient(MockEngine {
            requestCount += 1
            respond(
                content = "replacement",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentLength, "11"),
            )
        })
        val downloader = createDownloader(client, fileSystem)

        downloader.download(testConfig(savePath.toString(), overwriteExisting = false)).join()

        assertIs<DownloadState.Error>(downloader.state.value)
        assertEquals(0, requestCount)
        assertEquals("existing", fileSystem.read(savePath) { readUtf8() })

        downloader.download(testConfig(savePath.toString(), overwriteExisting = true)).join()

        assertIs<DownloadState.Complete>(downloader.state.value)
        assertEquals(1, requestCount)
        assertEquals("replacement", fileSystem.read(savePath) { readUtf8() })
        downloader.cleanup()
        runCurrent()
    }

    /** 非成功 HTTP 状态应发布错误状态且不生成正式文件。 */
    @Test
    fun httpFailureEmitsErrorState() = runTest {
        val fileSystem = FakeFileSystem()
        val savePath = "/downloads/failure.bin".toPath()
        val client = HttpClient(MockEngine {
            respond(content = "failure", status = HttpStatusCode.InternalServerError)
        })
        val downloader = createDownloader(client, fileSystem)

        downloader.download(testConfig(savePath.toString())).join()

        assertIs<DownloadState.Error>(downloader.state.value)
        assertFalse(fileSystem.exists(savePath))
        downloader.cleanup()
        runCurrent()
    }

    /** 创建绑定测试调度器、MockEngine 与 FakeFileSystem 的下载器。 */
    private fun kotlinx.coroutines.test.TestScope.createDownloader(
        client: HttpClient,
        fileSystem: FileSystem,
    ): CommonFileDownloader {
        val scope = CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler))
        return CommonFileDownloader(
            ktorEngineFactory = MockEngine,
            scope = scope,
            fileSystem = fileSystem,
            httpClient = client,
        )
    }

    /** 创建关闭重试与外网依赖的测试下载配置。 */
    private fun testConfig(
        savePath: String,
        overwriteExisting: Boolean = false,
    ): DownloadConfig = DownloadConfig(
        url = "https://example.test/file",
        savePath = savePath,
        maxRetries = 0,
        retryDelayMs = 0L,
        overwriteExisting = overwriteExisting,
    )
}
