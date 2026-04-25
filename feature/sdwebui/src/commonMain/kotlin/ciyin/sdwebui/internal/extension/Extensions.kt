package ciyin.sdwebui.internal.extension

import io.ktor.http.*

/**
 * 根据主机、端口与协议拼接 WebUI 根 URL（含默认端口省略规则）。
 */
internal fun buildUrl(host: String, port: Int, useHttps: Boolean): String {
    val builder = URLBuilder(
        protocol = if (useHttps) URLProtocol.HTTPS else URLProtocol.HTTP,
        host = host,
        port = port,
    )
    return builder.build().toString()
}
