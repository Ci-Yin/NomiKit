package ciyin.koin

class KoinBootException(
    message: String? = null,
    cause: Throwable? = null
) : RuntimeException(message, cause)