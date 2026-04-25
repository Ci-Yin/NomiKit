package ciyin.sdwebui.service

import ciyin.sdwebui.response.QueueResponse

/**
 * WebUI 队列与任务状态相关 API。
 */
interface CoreService {

    /** 查询任务队列状态：`queue/status`。 */
    suspend fun getQueue(): Result<QueueResponse>
}
