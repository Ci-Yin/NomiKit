package ciyin.business.base.data.fileupload

import ciyin.business.base.data.ApiClient
import ciyin.io.File
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okio.buffer
import okio.use
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * [uploadFiles] 扩展函数的单元测试
 *
 * 覆盖场景：
 * - 空文件列表 → 应发送 Failed 事件
 * - 正常上传 → 应依次收到 Started、Progress、Success 事件
 * - 服务器错误 → 应发送 Failed 事件
 */
class FileUploadTest {

    private val uploadUrl = "https://api.example.com/upload"

    /**
     * 空文件列表时，应发送 Failed 事件
     */
    @Test
    fun emptyFiles_emitsFailed() = runTest {
        val client = createMockClient(responseBody = apiResponse(data = """{"urls":[]}"""))
        val apiClient = ApiClient(client)

        val events = apiClient.uploadFiles<TestResponse>(uploadUrl, emptyList()).toList()

        assertEquals(1, events.size)
        assertTrue(events[0] is FileUploadEvent.Failed)
        client.close()
    }

    /**
     * 单文件上传成功，应依次收到 Started、Progress、Success 事件
     */
    @Test
    fun singleFileUpload_success_emitsStartedProgressSuccess() = runTest {
        val tempFile =
            createTempFileWithContent("test-image.png", "fake-png-content".encodeToByteArray())
        val client = createMockClient(
            responseBody = apiResponse(data = """{"urls":["https://cdn.example.com/img1.png"]}"""),
        )
        val apiClient = ApiClient(client)

        val events = apiClient
            .uploadFiles<TestResponse>(uploadUrl, listOf(tempFile.absolutePath))
            .toList()

        assertTrue(events.isNotEmpty())
        assertTrue(events.first() is FileUploadEvent.Started)
        val started = events.first() as FileUploadEvent.Started
        assertEquals(1, started.fileCount)

        val successEvent = events.lastOrNull { it is FileUploadEvent.Success }
        assertNotNull(successEvent)
        val success = successEvent as FileUploadEvent.Success
        assertEquals(listOf("https://cdn.example.com/img1.png"), success.result.urls)

        tempFile.delete()
        client.close()
    }

    /**
     * 多文件上传成功，应正确解析返回的 URL 列表
     */
    @Test
    fun multiFileUpload_success_emitsCorrectImageUrls() = runTest {
        val file1 = createTempFileWithContent("a.png", "content1".encodeToByteArray())
        val file2 = createTempFileWithContent("b.jpg", "content2".encodeToByteArray())
        val client = createMockClient(
            responseBody = apiResponse(
                data = """{"urls":["https://cdn.example.com/1.png","https://cdn.example.com/2.jpg"]}""",
            ),
        )
        val apiClient = ApiClient(client)

        val events = apiClient
            .uploadFiles<TestResponse>(
                uploadUrl,
                listOf(file1.absolutePath, file2.absolutePath),
            )
            .toList()

        val started =
            events.firstOrNull { it is FileUploadEvent.Started } as? FileUploadEvent.Started
        assertNotNull(started)
        assertEquals(2, started.fileCount)

        val success =
            events.lastOrNull { it is FileUploadEvent.Success } as? FileUploadEvent.Success
        assertNotNull(success)
        assertEquals(
            listOf("https://cdn.example.com/1.png", "https://cdn.example.com/2.jpg"),
            success.result.urls,
        )

        file1.delete()
        file2.delete()
        client.close()
    }

    /**
     * 服务器返回 500 时，应发送 Failed 事件
     */
    @Test
    fun serverError_emitsFailed() = runTest {
        val tempFile = createTempFileWithContent("test.png", "content".encodeToByteArray())
        val client = createMockClient(
            responseBody = """{"error":"internal error"}""",
            statusCode = HttpStatusCode.InternalServerError,
        )
        val apiClient = ApiClient(client)

        val events = apiClient
            .uploadFiles<TestResponse>(uploadUrl, listOf(tempFile.absolutePath))
            .toList()

        val failed = events.lastOrNull { it is FileUploadEvent.Failed } as? FileUploadEvent.Failed
        assertNotNull(failed)

        tempFile.delete()
        client.close()
    }

    /**
     * block 回调正确将响应体解析为 URL 列表
     */
    @Test
    fun block_extractsImageUrlsCorrectly() = runTest {
        val tempFile = createTempFileWithContent("x.png", "x".encodeToByteArray())
        val client = createMockClient(
            responseBody = apiResponse(data = """{"urls":["/path/to/a","/path/to/b"]}"""),
        )
        val apiClient = ApiClient(client)

        val success = apiClient
            .uploadFiles<TestResponse>(uploadUrl, listOf(tempFile.absolutePath))
            .toList()
            .filterIsInstance<FileUploadEvent.Success<TestResponse>>()
            .firstOrNull()

        assertNotNull(success)
        assertEquals(listOf("/path/to/a", "/path/to/b"), success.result.urls)

        tempFile.delete()
        client.close()
    }

    // ==================== 辅助方法 ====================

    /**
     * 创建带指定内容的临时文件
     *
     * @param fileName 文件名（用于 suffix）
     * @param content 文件内容
     * @return 临时文件的 [File]，[absolutePath] 供 [uploadFiles] 使用，用毕可调用 [File.delete]
     */
    private fun createTempFileWithContent(fileName: String, content: ByteArray): File {
        val unique = kotlin.random.Random.nextLong().toString(radix = 36)
        val tempFile = File.createTempFile(
            prefix = "fileupload-test-$unique-",
            suffix = "-$fileName",
        )
        ciyin.io.SystemFileSystem.sink(tempFile.toPath()).buffer().use { sink ->
            sink.write(content)
        }
        return tempFile
    }

    /**
     * 创建使用 MockEngine 的 HttpClient
     */
    private fun createMockClient(
        responseBody: String,
        statusCode: HttpStatusCode = HttpStatusCode.OK,
    ): HttpClient = HttpClient(MockEngine) {
        engine {
            addHandler { _ ->
                respond(
                    content = ByteReadChannel(responseBody),
                    status = statusCode,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }
        }
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    /**
     * 将业务 data 包装为 [ciyin.business.base.util.ApiResponse] 形态，
     * 以满足 [ciyin.business.base.util.unwrapApiData] 的解包协议。
     */
    private fun apiResponse(data: String): String =
        """{"code":0,"msg":"ok","data":$data}"""

    @Serializable
    private data class TestResponse(val urls: List<String>)
}
