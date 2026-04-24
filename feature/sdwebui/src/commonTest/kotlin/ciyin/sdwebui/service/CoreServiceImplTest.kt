package ciyin.sdwebui.service

import ciyin.sdwebui.client.Client
import ciyin.sdwebui.support.RecordingClient
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * [CoreServiceImpl] 的单元测试：
 * 验证 Service 通过 [Client] 发出的请求 path/method 与文档一致，
 * 并能正确将服务端 JSON 反序列化为 [ciyin.sdwebui.response.QueueResponse]。
 */
class CoreServiceImplTest {

    private val baseUrl = "http://127.0.0.1:7860"
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun get_queue_should_issue_get_to_queue_status_path() = runTest {
        val client = RecordingClient().apply {
            enqueueSuccess(
                """
                {
                    "msg": "queue_full",
                    "rank": "1",
                    "queue_size": 3,
                    "avg_event_process_time": 1.5,
                    "avg_event_concurrent_process_time": 0.8,
                    "rank_eta": 4.2,
                    "queue_eta": 12.3
                }
                """.trimIndent()
            )
        }
        val service = CoreServiceImpl(baseUrl, client, json)

        val result = service.getQueue()

        assertEquals(1, client.requests.size)
        val request = client.requests.single()
        assertEquals(baseUrl, request.baseUrl)
        assertEquals("queue/status", request.path)
        assertEquals(Client.Method.GET, request.method)
        assertNull(request.body)
        assertNull(request.bodyType)

        val queue = assertNotNull(result.getOrNull())
        assertEquals("queue_full", queue.msg)
        assertEquals(3, queue.queueSize)
        assertEquals(1.5f, queue.avgEventProcessTime)
    }

    @Test
    fun get_queue_should_propagate_failure_as_client_error() = runTest {
        val client = RecordingClient().apply {
            enqueueFailure("boom")
        }
        val service = CoreServiceImpl(baseUrl, client, json)

        val result = service.getQueue()

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is Client.Error)
        assertEquals("boom", error.body)
    }
}
