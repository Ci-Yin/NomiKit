package ciyin.business.error

sealed class DataError(val message: String) {

    sealed class Network(message: String) : DataError(message) {
        data object NoConnection : Network("当前无网络,请检查连接")

        data class Http(val code: Int, val body: String?, val cause: Throwable? = null) :
            Network(body ?: "请求失败,请稍后重试")

        data object Unauthorized : Network("未授权,请重新登录")
        data object Timeout : Network("请求超时,请稍后重试")
        data object SSL : Network("安全连接失败,请稍后再试")
        data object DNS : Network("网络解析失败,请检查网络连接")
    }

    data object Serialization : DataError("数据解析异常")

    data object Persistence : DataError("本地数据存取异常")

    class Unknown(val cause: Throwable? = null, message: String? = cause?.message) :
        DataError(message ?: "未知错误")
}