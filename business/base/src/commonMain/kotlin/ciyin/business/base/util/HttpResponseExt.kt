package ciyin.business.base.util

import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse

/**
 * 解包 API 响应的 data 字段
 *
 * 注意: 必须配合 installApiValidator() 使用
 * 验证器会在这个方法执行前先检查 HTTP 状态码和业务状态码
 */
suspend inline fun <reified T> HttpResponse.unwrapApiData(): T =
    when {
        Unit is T -> Unit as T
        else -> body<ApiResponse<T>>().data as T
    }


/**
 * 直接获取原始 ApiResponse
 * 用于需要访问完整响应信息的场景
 */
suspend inline fun <reified T> HttpResponse.getApiResponse(): ApiResponse<T> {
    return body<ApiResponse<T>>()
}