package ciyin.feature.kmpsoup

/**
 * 跨平台 HTML 解析入口。
 */
expect object KmpSoup {

    /**
     * 解析 HTML 字符串并返回统一文档模型。
     *
     * @param html HTML 原始字符串。
     * @return 解析后的文档对象。
     * @throws IllegalStateException 当底层解析器发生不可恢复异常时抛出。
     */
    fun parse(html: String): HtmlDocument
}