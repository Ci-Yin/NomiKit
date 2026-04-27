package ciyin.business.base.data

import arrow.core.Either
import arrow.core.flatMap
import ciyin.business.base.error.DataError
import ciyin.business.base.util.ApiException
import ciyin.business.base.util.unwrapApiData
import ciyin.platform.thisLogger
import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.statement.HttpResponse
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException

open class ApiClient(
    @PublishedApi internal val httpClient: HttpClient,
) {

    protected val logger = thisLogger()

    /**
     * 内部使用的日志记录器，用于记录 ApiClient 类的日志信息。
     * 为了使 inline 的函数能够记录日志，又保证属性的作用域，使用 PublishedApi + internal
     */
    @PublishedApi
    @Suppress("PropertyName")
    internal val _logger: Logger
        get() = logger

    /**
     * 安全执行 HTTP 请求,将异常转换为 DataError
     */
    suspend fun safeCall(
        block: suspend HttpClient.() -> HttpResponse
    ): Either<DataError, HttpResponse> = Either.catch {
        withContext(Dispatchers.IO) { block(httpClient) }
    }.onLeft { _logger.e("safeCall: ${it.message}", it) }
        .mapLeft(::mapExceptionToDataError)

    /**
     * 安全执行 HTTP 请求,将异常转换为 DataError, 返回结果data
     */
    suspend inline fun <reified T> safeCallWithData(
        noinline block: suspend HttpClient.() -> HttpResponse
    ): Either<DataError, T> = safeCall(block).flatMap { response ->
        Either.catch {
            response.unwrapApiData<T>()
        }
            .onLeft { _logger.e("safeCallWithData", it) }
            .mapLeft {
                when (it) {
                    is SerializationException -> DataError.Serialization
                    else -> DataError.Unknown(it)
                }
            }
    }

    /**
     * 统一的异常映射
     */
    @PublishedApi
    internal fun mapExceptionToDataError(e: Throwable): DataError = when (e) {
        // API 业务异常 - 由 ApiValidator 抛出
        is ApiException -> DataError.Network.Http(e.code, e.message, e)
        // 网络异常 - Ktor 原生异常
        is ConnectTimeoutException -> DataError.Network.NoConnection
        is HttpRequestTimeoutException -> DataError.Network.Timeout
        is UnresolvedAddressException -> DataError.Network.DNS
        is ClientRequestException -> DataError.Network.Http(
            e.response.status.value,
            e.response.status.description,
            e
        )

        is RedirectResponseException -> DataError.Network.Http(
            e.response.status.value,
            e.response.status.description,
            e
        )
        // 序列化异常
        is SerializationException -> DataError.Serialization

        // 其他
        else -> DataError.Unknown(e, e.message ?: "未知错误")
    }
}