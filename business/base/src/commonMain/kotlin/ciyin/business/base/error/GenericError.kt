package ciyin.business.base.error

import arrow.core.Either

sealed class GenericError(val message: String) {
    class Failed(message: String?) : GenericError(message ?: "未知错误")
}

fun DataError.toGenericError(): GenericError = GenericError.Failed(message)

fun <B> Either<DataError, B>.mapLeftGenericError(): Either<GenericError, B> {
    return mapLeft { it.toGenericError() }
}


