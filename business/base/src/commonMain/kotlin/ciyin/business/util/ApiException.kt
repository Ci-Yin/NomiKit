package ciyin.business.util

/**
 * API 业务异常
 */
sealed class ApiException(
    val code: Int,
    override val message: String,
    override val cause: Throwable? = null
) : Exception(message, cause) {

    /** `400` 请求参数错误 */
    class BadRequest(message: String) : ApiException(400, message)

    /** `401` 未认证 */
    class Unauthorized(message: String) : ApiException(401, message)

    /** `403` 无权限 */
    class Forbidden(message: String) : ApiException(403, message)

    /** `404` 资源不存在 */
    class NotFound(message: String) : ApiException(404, message)

    /** `419` 需要完成人机验证 */
    class ChallengeRequired(
        message: String,
        val challengeId: String,
        val challengeType: String
    ) : ApiException(419, message)

    /** `429` 请求过于频繁 */
    class TooManyRequests(message: String, val retryAfter: Int? = null) : ApiException(429, message)

    /** `500` 服务器内部错误 */
    class InternalServerError(message: String) : ApiException(500, message)

    /** `502` 网关错误 */
    class BadGateway(message: String) : ApiException(502, message)

    /** `503` 服务不可用 */
    class ServiceUnavailable(message: String) : ApiException(503, message)

    /** `504` 网关超时 */
    class GatewayTimeout(message: String) : ApiException(504, message)

    /** 业务错误码(非 HTTP 标准状态码) */
    class BusinessError(code: Int, message: String) : ApiException(code, message)
}