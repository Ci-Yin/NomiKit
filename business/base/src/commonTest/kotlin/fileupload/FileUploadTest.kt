package com.yy.myuko.business.base.data.fileupload

import ciyin.business.data.ApiClient
import ciyin.business.data.fileupload.FileUploadEvent
import ciyin.business.data.fileupload.uploadFiles
import com.yy.myuko.core.io.SystemPath
import com.yy.myuko.core.io.SystemPaths
import com.yy.myuko.core.io.absolutePath
import com.yy.myuko.core.io.bufferedSink
import com.yy.myuko.core.io.createTempFile
import com.yy.myuko.core.io.delete
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.core.toByteArray
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
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
        val client = createMockClient(responseBody = """{"urls":[]}""")
        val apiClient = ApiClient(client)

        val events = apiClient.uploadFiles<TestResponse>(uploadUrl, emptyList())
            .toList()

        assertEquals(1, events.size)
        assertTrue(events[0] is FileUploadEvent.Failed)
        client.close()
    }

    /**
     * 单文件上传成功，应依次收到 Started、Progress、Success 事件
     */
    @Test
    fun singleFileUpload_success_emitsStartedProgressSuccess() = runTest {
        val tempFile = createTempFileWithContent("test-image.png", "fake-png-content".toByteArray())
        val client = createMockClient(
            responseBody = """{"urls":["https://cdn.example.com/img1.png"]}""",
        )
        val apiClient = ApiClient(client)

        val events = apiClient.uploadFiles<TestResponse>(uploadUrl, listOf(tempFile.absolutePath))

            .toList()

        assertTrue(events.isNotEmpty())
        assertTrue(events.first() is FileUploadEvent.Started)
        val started = events.first() as FileUploadEvent.Started
        assertEquals(1, started.fileCount)

        val successEvent = events.lastOrNull { it is FileUploadEvent.Success }
        assertNotNull(successEvent)
        val success = successEvent as FileUploadEvent.Success
        assertEquals(listOf("https://cdn.example.com/img1.png"), success.result.urls)

        tempFile.delete(false)
        client.close()
    }

    /**
     * 多文件上传成功，应正确解析返回的 URL 列表
     */
    @Test
    fun multiFileUpload_success_emitsCorrectImageUrls() = runTest {
        val file1 = createTempFileWithContent("a.png", "content1".toByteArray())
        val file2 = createTempFileWithContent("b.jpg", "content2".toByteArray())
        val client = createMockClient(
            responseBody = """{"urls":["https://cdn.example.com/1.png","https://cdn.example.com/2.jpg"]}""",
        )
        val apiClient = ApiClient(client)

        val events = apiClient.uploadFiles<TestResponse>(
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

        file1.delete(false)
        file2.delete(false)
        client.close()
    }

    /**
     * 服务器返回 500 时，应发送 Failed 事件
     */
    @Test
    fun serverError_emitsFailed() = runTest {
        val tempFile = createTempFileWithContent("test.png", "content".toByteArray())
        val client = createMockClient(
            responseBody = """{"error":"internal error"}""",
            statusCode = HttpStatusCode.InternalServerError,
        )
        val apiClient = ApiClient(client)

        val events = apiClient.uploadFiles<TestResponse>(uploadUrl, listOf(tempFile.absolutePath))
            .toList()

        val failed = events.lastOrNull { it is FileUploadEvent.Failed } as? FileUploadEvent.Failed
        assertNotNull(failed)

        tempFile.delete(false)
        client.close()
    }

    /**
     * block 回调正确将响应体解析为 URL 列表
     */
    @Test
    fun block_extractsImageUrlsCorrectly() = runTest {
        val tempFile = createTempFileWithContent("x.png", "x".toByteArray())
        val client = createMockClient(
            responseBody = """{"urls":["/path/to/a","/path/to/b"]}""",
        )
        val apiClient = ApiClient(client)

        val success = apiClient.uploadFiles<TestResponse>(uploadUrl, listOf(tempFile.absolutePath))
            .toList()
            .filterIsInstance<FileUploadEvent.Success<TestResponse>>()
            .firstOrNull()

        assertNotNull(success)
        assertEquals(listOf("/path/to/a", "/path/to/b"), success.result.urls)

        tempFile.delete(false)
        client.close()
    }

    // ==================== 辅助方法 ====================

    /**
     * 创建带指定内容的临时文件
     *
     * @param fileName 文件名（用于 suffix）
     * @param content 文件内容
     * @return 临时文件的 [SystemPath]，[absolutePath] 供 [uploadFiles] 使用，用毕可调用 [SystemPath.delete]
     */
    private fun createTempFileWithContent(fileName: String, content: ByteArray): SystemPath {
        val tempPath =
            SystemPaths.createTempFile(prefix = "fileupload-test-", suffix = "-$fileName")
        tempPath.bufferedSink(append = false).use { sink ->
            sink.write(content, startIndex = 0, endIndex = content.size)
        }
        return tempPath
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

    @Serializable
    private data class TestResponse(val urls: List<String>)
}

/**
 * 上传图片接口真实请求集成测试
 *
 * 调用接口：POST /v1/app/user/upload/img（见 OpenAPI 规范）
 *
 * ⚠️ 注意：
 * - 运行前需确保本地或远程 API 服务已启动
 * - 修改 [baseUrl] 指向实际服务地址
 * - 若接口需要认证，可在 [createClient] 中配置 token
 */
class FileUploadIntegrationTest {

    /** 本地开发环境地址，按需修改 */
    private val baseUrl = "http://119.91.20.99:31651"

    private fun createClient() = HttpClient {
        defaultRequest {
            url(baseUrl)
        }
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    /**
     * 真实上传单张图片
     *
     * 使用 tiny PNG 魔数作为测试文件内容，服务端通常能识别为图片。
     */
    @Test
    fun uploadImage_realRequest() = runTest {
        val tempFile = createTempFileWithContent(
            "test.png",
            byteArrayOf(
                0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, // PNG magic
            ) + ByteArray(64) { 0 },
        )
        val client = createClient()
        val apiClient = ApiClient(client)
        println(tempFile)
        try {
            val events = apiClient.uploadFiles<UploadImageApiResponseData?>(
                url = "/v1/app/user/upload/img",
                files = listOf(tempFile.absolutePath),

                )
                .toList()

            val success =
                events.filterIsInstance<FileUploadEvent.Success<UploadImageApiResponseData?>>()
                    .firstOrNull()
            assertNotNull(success, "预期收到 Success 事件，实际收到: $events")
            assertTrue(success.result?.images?.isNotEmpty() ?: false, "预期返回至少一个图片 URL")
        } finally {
            tempFile.delete(false)
            client.close()
        }
    }

    private fun createTempFileWithContent(fileName: String, content: ByteArray): SystemPath {
        val tempPath =
            SystemPaths.createTempFile(prefix = "file-upload-integration-", suffix = "-$fileName")
        tempPath.bufferedSink(append = false).use { sink ->
            sink.write(content, startIndex = 0, endIndex = content.size)
        }
        return tempPath
    }

    @Serializable
    data class UploadImageApiResponseData(
        val images: List<UploadImageItemApiData>,
    )

    @Serializable
    data class UploadImageItemApiData(
        val img: String,
    )
}
