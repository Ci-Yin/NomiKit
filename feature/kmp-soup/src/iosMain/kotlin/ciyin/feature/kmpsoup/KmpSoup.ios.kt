package ciyin.feature.kmpsoup

import ciyin.feature.kmpsoup.bridge.KmpSoupBridgeParser

/**
 * iOS 平台 HTML 解析入口实现。
 */
actual object KmpSoup {

    /**
     * 使用 SwiftSoup 桥接层解析 HTML。
     *
     * @param html HTML 原始字符串。
     * @return 统一文档对象。
     * @throws IllegalStateException 当桥接层发生不可恢复异常时抛出。
     */
    actual fun parse(html: String): HtmlDocument {
        return try {
            val bridgeDocument = KmpSoupBridgeParser.parseHtml(html)
                ?: throw IllegalStateException("iOS 平台解析 HTML 失败。")
            bridgeDocument.toHtmlDocument()
        } catch (throwable: Throwable) {
            throw IllegalStateException("iOS 平台解析 HTML 失败。", throwable)
        }
    }
}
