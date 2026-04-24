package ciyin.sdwebui.service

import ciyin.sdwebui.response.QueueResponse

interface CoreService {

    suspend fun getQueue(): Result<QueueResponse>
}
