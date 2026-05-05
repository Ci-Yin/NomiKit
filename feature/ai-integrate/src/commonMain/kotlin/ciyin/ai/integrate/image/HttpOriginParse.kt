package ciyin.ai.integrate.image

/**
 * 将 [baseUrl] 解析为 SD WebUI 客户端所需的 `(host, port, useHttps)`。
 *
 * 支持常见 `http(s)://host(:port)(/path)?` 形态；非法或空输入抛出 [IllegalArgumentException]。
 */
internal fun parseHttpOrigin(baseUrl: String): Triple<String, Int, Boolean> {
    val normalized = baseUrl.trim().removeSuffix("/")
    require(normalized.isNotEmpty()) { "baseUrl 不能为空" }

    val useHttps = normalized.startsWith("https:", ignoreCase = true)
    val hasHttpScheme = normalized.startsWith("http:", ignoreCase = true) ||
            normalized.startsWith("https:", ignoreCase = true)
    require(hasHttpScheme) { "baseUrl 须包含 http:// 或 https:// 方案：$baseUrl" }

    val withoutScheme = normalized.substringAfter("://", missingDelimiterValue = normalized)
    require(withoutScheme.isNotEmpty()) { "baseUrl 缺少主机部分：$baseUrl" }

    val hostPort = withoutScheme.substringBefore('/')
    require(hostPort.isNotEmpty()) { "baseUrl 缺少主机：$baseUrl" }

    val host = hostPort.substringBefore(':')
    require(host.isNotEmpty()) { "baseUrl 主机名为空：$baseUrl" }

    val portPart = hostPort.substringAfter(':', missingDelimiterValue = "")
    val port = portPart.toIntOrNull()
        ?: if (useHttps) 443 else 80

    return Triple(host, port, useHttps)
}
