package ciyin.feature.kmpsoup

import ciyin.feature.kmpsoup.bridge.KmpSoupBridgeDocument

/**
 * 基于 SwiftSoup 桥接文档的包装实现。
 *
 * @property delegate iOS 桥接文档对象。
 */
internal class IosHtmlDocument(
    private val delegate: KmpSoupBridgeDocument,
    private val elementDelegate: HtmlElement = IosDetachedHtmlElement("document"),
) : HtmlDocument, HtmlElement by elementDelegate {

    /**
     * 按 CSS 查询元素集合。
     */
    override fun select(css: String): HtmlElements = safeElements {
        delegate.select(css)
    }

    /**
     * 按 CSS 查询首个元素。
     */
    override fun selectFirst(css: String): HtmlElement? = safeElement {
        delegate.selectFirst(css)
    }

    /**
     * 按 id 查询元素。
     */
    override fun getElementById(id: String): HtmlElement? = safeElement {
        delegate.getElementById(id)
    }

    /**
     * 按标签名查询元素集合。
     */
    override fun getElementsByTag(tag: String): HtmlElements = safeElements {
        delegate.getElementsByTag(tag)
    }

    /**
     * 按类名查询元素集合。
     */
    override fun getElementsByClass(className: String): HtmlElements = safeElements {
        delegate.getElementsByClass(className)
    }

    /**
     * 获取标题。
     */
    override fun title(): String = safeString {
        delegate.title()
    }

    /**
     * 获取 body 元素。
     */
    override fun body(): HtmlElement {
        return safeElement {
            delegate.body()
        } ?: IosDetachedHtmlElement("body")
    }

    /**
     * 获取文档地址。
     */
    override fun location(): String = ""

    /**
     * 获取 head 元素。
     */
    override fun head(): HtmlElement {
        return selectFirst("head")
            ?: throw IllegalStateException("iOS 平台未能找到 head 元素。")
    }

    /**
     * 获取文档 doctype 信息。
     */
    override fun documentType(): DocumentType? = parseDocumentTypeFromHtml(outerHtml())

    /**
     * 获取文档内所有 form 元素。
     */
    override fun forms(): List<FormElement> {
        return select("form").mapNotNull { element -> element.asFormOrNull() }
    }

    /**
     * 按 CSS 查找并返回首个表单元素。
     */
    override fun expectForm(cssQuery: String): FormElement {
        val first = selectFirst(cssQuery)
            ?: throw IllegalArgumentException("未找到匹配 CSS [$cssQuery] 的表单元素。")
        return first.asFormOrNull()
            ?: throw IllegalArgumentException("匹配 CSS [$cssQuery] 的元素不是 form。")
    }

    /**
     * 设置文档标题。
     */
    override fun title(title: String) {
        safeUnit {
            delegate.setTitle(title)
        }
    }

    /**
     * 创建一个新的元素实例（不自动插入文档）。
     */
    override fun createElement(tagName: String): HtmlElement = IosDetachedHtmlElement(tagName)

    /**
     * 设置 body 文本内容。
     */
    override fun text(text: String): HtmlDocument {
        safeUnit {
            delegate.setText(text)
        }
        return this
    }

    /**
     * 获取文档内部 HTML。
     */
    override fun html(): String = safeString {
        delegate.html()
    }

    /**
     * 获取文档外层 HTML。
     */
    override fun outerHtml(): String = safeString {
        delegate.outerHtml()
    }

    private fun HtmlElement.asFormOrNull(): FormElement? {
        if (!safeString { tagName() }.equals("form", ignoreCase = true)) {
            return null
        }
        return when (this) {
            is IosFormElement -> this
            is IosHtmlElement -> IosFormElement(this.delegate)
            else -> null
        }
    }
}
