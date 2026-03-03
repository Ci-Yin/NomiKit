package ciyin.feature.kmpsoup

import org.jsoup.select.Elements

/**
 * 基于 Jsoup 的元素集合包装实现。
 *
 * @property delegates Jsoup 元素列表快照。
 */
internal class JsoupHtmlElements(
    private val delegates: Elements,
) : HtmlElements, List<HtmlElement> by delegates.map(::JsoupHtmlElement) {

    /**
     * 拼接所有元素文本。
     */
    override fun text(): String {
        return delegates.text()
    }

    /**
     * 获取第一个元素的属性值。
     */
    override fun attr(name: String): String {
        return delegates.attr(name)
    }

    override fun hasAttr(name: String): Boolean {
        return delegates.hasAttr(name)
    }

    override fun eachAttr(name: String): List<String> {
        return delegates.eachAttr(name)
    }

    override fun attr(name: String, value: String): HtmlElements {
        delegates.attr(name, value)
        return this
    }

    override fun removeAttr(name: String): HtmlElements {
        delegates.removeAttr(name)
        return this
    }

    override fun addClass(className: String): HtmlElements {
        delegates.addClass(className)
        return this
    }

    override fun removeClass(className: String): HtmlElements {
        delegates.removeClass(className)
        return this
    }

    override fun toggleClass(className: String): HtmlElements {
        delegates.toggleClass(className)
        return this
    }

    override fun hasClass(className: String): Boolean {
        return delegates.hasClass(className)
    }

    override fun value(): String {
        return delegates.`val`()
    }

    override fun value(value: String): HtmlElements {
        delegates.`val`(value)
        return this
    }

    override fun hasText(): Boolean {
        return delegates.hasText()
    }

    override fun eachText(): List<String> {
        return delegates.eachText()
    }

    override fun html(): String {
        return delegates.html()
    }

    override fun tagName(tagName: String): HtmlElements {
        delegates.tagName(tagName)
        return this
    }

    override fun html(html: String): HtmlElements {
        delegates.html(html)
        return this
    }

    override fun prepend(html: String): HtmlElements {
        delegates.prepend(html)
        return this
    }

    override fun append(html: String): HtmlElements {
        delegates.append(html)
        return this
    }

    override fun before(html: String): HtmlElements {
        delegates.before(html)
        return this
    }

    override fun after(html: String): HtmlElements {
        delegates.after(html)
        return this
    }

    override fun wrap(html: String): HtmlElements {
        delegates.wrap(html)
        return this
    }

    override fun unwrap(): HtmlElements {
        delegates.unwrap()
        return this
    }

    override fun empty(): HtmlElements {
        delegates.empty()
        return this
    }

    override fun remove(): HtmlElements {
        delegates.remove()
        return this
    }

    override fun select(css: String): HtmlElements {
        return delegates.select(css).toHtmlElements()
    }

    override fun selectFirst(css: String): HtmlElement? {
        return delegates.selectFirst(css)?.toHtmlElement()
    }

    override fun expectFirst(css: String): HtmlElement {
        return delegates.expectFirst(css).toHtmlElement()
    }

    override fun not(css: String): HtmlElements {
        return delegates.not(css).toHtmlElements()
    }

    override fun eq(index: Int): HtmlElements {
        return delegates.eq(index).toHtmlElements()
    }

    override fun matches(css: String): Boolean {
        return delegates.`is`(css)
    }

    override fun next(): HtmlElements {
        return delegates.next().toHtmlElements()
    }

    override fun next(css: String): HtmlElements {
        return delegates.next(css).toHtmlElements()
    }

    override fun nextAll(): HtmlElements {
        return delegates.nextAll().toHtmlElements()
    }

    override fun nextAll(css: String): HtmlElements {
        return delegates.nextAll(css).toHtmlElements()
    }

    override fun prev(): HtmlElements {
        return delegates.prev().toHtmlElements()
    }

    override fun prev(css: String): HtmlElements {
        return delegates.prev(css).toHtmlElements()
    }

    override fun prevAll(): HtmlElements {
        return delegates.prevAll().toHtmlElements()
    }

    override fun prevAll(css: String): HtmlElements {
        return delegates.prevAll(css).toHtmlElements()
    }

    override fun parents(): HtmlElements {
        return delegates.parents().toHtmlElements()
    }

    override fun forms(): List<FormElement> {
        return delegates.forms().map { it.toFormElement() }
    }
}
