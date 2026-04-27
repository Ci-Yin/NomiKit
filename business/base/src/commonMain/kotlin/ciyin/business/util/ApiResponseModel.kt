package ciyin.business.util

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * API 标准响应体
 */
@Serializable
data class ApiResponse<T>(
    val code: Int,
    val msg: String,
    val timestamp: Long? = null,
    val requestId: String? = null,
    val data: T? = null
)

/**
 * 419 Challenge 响应数据
 */
@Serializable
internal data class ChallengeData(
    @SerialName("challenge_id")
    val challengeId: String,
    @SerialName("challenge_type")
    val challengeType: String
)

@Serializable
internal data class RetryAfterData(
    @SerialName("retry_after")
    val retryAfter: Int,
)
