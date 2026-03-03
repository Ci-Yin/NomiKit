package ciyin.feature.kmpsoup

import org.jsoup.nodes.Element

/**
 * 基于 Jsoup 的元素包装实现。
 *
 * @property delegate Jsoup 元素委托对象。
 */
internal class JsoupHtmlElement(
    internal val delegate: Element,
) : HtmlElement {

    /**
     * 按 id 查询元素。
     */
    override fun getElementById(id: String): HtmlElement? =
        delegate.getElementById(id)?.toHtmlElement()

    /**
     * 按标签名查询元素集合。
     */
    override fun getElementsByTag(tag: String): HtmlElements =
        delegate.getElementsByTag(tag).toHtmlElements()

    /**
     * 按类名查询元素集合。
     */
    override fun getElementsByClass(className: String): HtmlElements =
        delegate.getElementsByClass(className).toHtmlElements()

    /**
     * 在当前元素内按 CSS 查询元素集合。
     */
    override fun select(css: String): HtmlElements =
        delegate.select(css).toHtmlElements()

    /**
     * 在当前元素内按 CSS 查询首个元素。
     */
    override fun selectFirst(css: String): HtmlElement? = delegate.selectFirst(css)?.toHtmlElement()

    /**
     * 获取当前元素及子树文本。
     */
    override fun text(): String = delegate.text()

    /**
     * 获取当前元素自身文本。
     */
    override fun ownText(): String = delegate.ownText()

    /**
     * 获取属性值。
     */
    override fun attr(name: String): String = delegate.attr(name)

    /**
     * 判断属性是否存在。
     */
    override fun hasAttr(name: String): Boolean = delegate.hasAttr(name)

    /**
     * 获取元素 id。
     */
    override fun id(): String = delegate.id()

    /**
     * 获取标签名。
     */
    override fun tagName(): String = delegate.tagName()

    /**
     * 获取 class 字符串。
     */
    override fun className(): String = delegate.className()

    /**
     * 获取 class 名集合。
     */
    override fun classNames(): Set<String> = delegate.classNames()

    /**
     * 获取父元素。
     */
    override fun parent(): HtmlElement? = delegate.parent()?.let(::JsoupHtmlElement)

    /**
     * 获取子元素集合。
     */
    override fun children(): HtmlElements = delegate.children().toHtmlElements()

    /**
     * 按索引获取子元素。
     */
    override fun child(index: Int): HtmlElement {
        return delegate.child(index).toHtmlElement()
    }

    /**
     * 获取子元素数量。
     */
    override fun childrenSize(): Int = delegate.childrenSize()

    /**
     * 获取元素内部 HTML。
     */
    override fun html(): String = delegate.html()

    /**
     * 获取元素外层 HTML。
     */
    override fun outerHtml(): String = delegate.outerHtml()

    /**
     * 获取父元素链。
     */
    override fun parents(): HtmlElements = delegate.parents().toHtmlElements()

    /**
     * 获取兄弟元素集合。
     */
    override fun siblingElements(): HtmlElements =
        delegate.siblingElements().toHtmlElements()

    /**
     * 获取后续兄弟元素集合。
     */
    override fun nextElementSiblings(): HtmlElements =
        delegate.nextElementSiblings().toHtmlElements()

    /**
     * 获取前置兄弟元素集合。
     */
    override fun previousElementSiblings(): HtmlElements =
        delegate.previousElementSiblings().toHtmlElements()

    /**
     * 获取第一个兄弟元素。
     */
    override fun firstElementSibling(): HtmlElement =
        delegate.firstElementSibling().toHtmlElement()

    /**
     * 获取最后一个兄弟元素。
     */
    override fun lastElementSibling(): HtmlElement =
        delegate.lastElementSibling().toHtmlElement()

    /**
     * 获取兄弟索引。
     */
    override fun elementSiblingIndex(): Int = delegate.elementSiblingIndex()

    override fun getElementsByAttribute(key: String): HtmlElements =
        delegate.getElementsByAttribute(key).toHtmlElements()

    override fun getElementsByAttributeStarting(keyPrefix: String): HtmlElements =
        delegate.getElementsByAttributeStarting(keyPrefix).toHtmlElements()

    override fun getElementsByAttributeValue(key: String, value: String): HtmlElements =
        delegate.getElementsByAttributeValue(key, value).toHtmlElements()

    override fun getElementsByAttributeValueNot(key: String, value: String): HtmlElements =
        delegate.getElementsByAttributeValueNot(key, value).toHtmlElements()

    override fun getElementsByAttributeValueStarting(
        key: String,
        valuePrefix: String
    ): HtmlElements =
        delegate.getElementsByAttributeValueStarting(key, valuePrefix).toHtmlElements()

    override fun getElementsByAttributeValueEnding(key: String, valueSuffix: String): HtmlElements =
        delegate.getElementsByAttributeValueEnding(key, valueSuffix).toHtmlElements()

    override fun getElementsByAttributeValueContaining(key: String, match: String): HtmlElements =
        delegate.getElementsByAttributeValueContaining(key, match).toHtmlElements()

    override fun getElementsContainingText(searchText: String): HtmlElements =
        delegate.getElementsContainingText(searchText).toHtmlElements()

    override fun getElementsContainingOwnText(searchText: String): HtmlElements =
        delegate.getElementsContainingOwnText(searchText).toHtmlElements()

    override fun getElementsMatchingText(regex: String): HtmlElements =
        delegate.getElementsMatchingText(regex).toHtmlElements()

    override fun getElementsMatchingOwnText(regex: String): HtmlElements =
        delegate.getElementsMatchingOwnText(regex).toHtmlElements()

    override fun getAllElements(): HtmlElements =
        delegate.allElements.toHtmlElements()

    override fun hasClass(className: String): Boolean =
        delegate.hasClass(className)

    override fun addClass(className: String): HtmlElement {
        delegate.addClass(className)
        return this
    }

    override fun removeClass(className: String): HtmlElement {
        delegate.removeClass(className)
        return this
    }

    override fun toggleClass(className: String): HtmlElement {
        delegate.toggleClass(className)
        return this
    }

    override fun value(): String = delegate.`val`()

    override fun value(value: String): HtmlElement {
        delegate.`val`(value)
        return this
    }

    override fun wholeText(): String = delegate.wholeText()

    override fun wholeOwnText(): String = delegate.wholeOwnText()

    override fun hasText(): Boolean = delegate.hasText()

    override fun data(): String = delegate.data()

    override fun cssSelector(): String = delegate.cssSelector()

    override fun matches(cssQuery: String): Boolean = delegate.`is`(cssQuery)

    override fun closest(cssQuery: String): HtmlElement? =
        delegate.closest(cssQuery)?.let(::JsoupHtmlElement)

    override fun selectXpath(xpath: String): HtmlElements =
        delegate.selectXpath(xpath).toHtmlElements()
}
