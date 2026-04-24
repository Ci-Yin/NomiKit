package ciyin.sdwebui

import ciyin.sdwebui.support.RecordingClient
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertSame

/**
 * [SdWebUi.Builder] 的单元测试：
 * - 验证默认值构建链路畅通且各 Service 可懒加载；
 * - 验证 host / port / useHttps / client 注入后，会真正影响下游 Service 发出的请求。
 */
class SdWebUiBuilderTest {

    @Test
    fun default_builder_should_lazy_provide_all_services() {
        val sdWebUi = SdWebUi.Builder().build()

        assertNotNull(sdWebUi.core)
        assertNotNull(sdWebUi.stableDiffusion)
        assertNotNull(sdWebUi.controlNet)
        assertNotNull(sdWebUi.reActor)
    }

    @Test
    fun service_accessors_should_return_singletons_per_sdwebui_instance() {
        val sdWebUi = SdWebUi.Builder().build()

        assertSame(sdWebUi.core, sdWebUi.core, "core 必须是 lazy 单例")
        assertSame(sdWebUi.stableDiffusion, sdWebUi.stableDiffusion)
        assertSame(sdWebUi.controlNet, sdWebUi.controlNet)
        assertSame(sdWebUi.reActor, sdWebUi.reActor)
    }

    @Test
    fun custom_host_port_and_https_should_compose_into_base_url() = runTest {
        val client = RecordingClient().apply {
            enqueueSuccess(
                """
                {
                    "msg": "ok",
                    "rank": null,
                    "queue_size": 0,
                    "avg_event_process_time": null,
                    "avg_event_concurrent_process_time": null,
                    "rank_eta": null,
                    "queue_eta": null
                }
                """.trimIndent()
            )
        }
        val sdWebUi = SdWebUi.Builder()
            .host("custom.example.com")
            .port(9999)
            .useHttps(true)
            .client(client)
            .build()

        sdWebUi.core.getQueue()

        val request = client.requests.single()
        assertEquals("https://custom.example.com:9999", request.baseUrl)
        assertEquals("queue/status", request.path)
    }

    @Test
    fun default_host_port_should_target_local_sdwebui() = runTest {
        val client = RecordingClient().apply { enqueueSuccess() }
        val sdWebUi = SdWebUi.Builder()
            .client(client)
            .build()

        sdWebUi.stableDiffusion.refreshCheckpoints()

        val request = client.requests.single()
        assertEquals("http://127.0.0.1:7860", request.baseUrl)
    }
}
