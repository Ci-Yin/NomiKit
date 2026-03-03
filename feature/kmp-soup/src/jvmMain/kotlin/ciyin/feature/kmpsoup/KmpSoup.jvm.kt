package ciyin.feature.kmpsoup

import org.jsoup.Jsoup

/**
 * JVM 平台 HTML 解析入口实现。
 */
actual object KmpSoup {

    /**
     * 使用 Jsoup 解析 HTML。
     *
     * @param html HTML 原始字符串。
     * @return 统一文档对象。
     * @throws IllegalStateException 当 Jsoup 发生不可恢复异常时抛出。
     */
    actual fun parse(html: String): HtmlDocument {
        return try {
            JsoupHtmlDocument(Jsoup.parse(html))
        } catch (throwable: Throwable) {
            throw IllegalStateException("JVM 平台解析 HTML 失败。", throwable)
        }
    }
}