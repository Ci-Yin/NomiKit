package ciyin.feature.kmpsoup

import org.jsoup.nodes.Document

/**
 * 基于 Jsoup 的文档包装实现。
 *
 * @property delegate Jsoup 文档委托对象。
 */
internal class JsoupHtmlDocument(
    private val delegate: Document,
) : HtmlDocument, HtmlElement by delegate.toHtmlElement() {

    override fun title(): String = delegate.title()

    override fun body(): HtmlElement = delegate.body().toHtmlElement()

    override fun location(): String = delegate.location()

    override fun head(): HtmlElement = delegate.head().toHtmlElement()

    override fun documentType(): DocumentType? = delegate.documentType()?.toDocumentType()

    override fun forms(): List<FormElement> = delegate.forms().map { it.toFormElement() }

    override fun expectForm(cssQuery: String): FormElement {
        return delegate.expectForm(cssQuery).toFormElement()
    }

    override fun title(title: String) {
        delegate.title(title)
    }

    override fun createElement(tagName: String): HtmlElement =
        delegate.createElement(tagName).toHtmlElement()

    override fun text(text: String): HtmlDocument {
        delegate.text(text)
        return this
    }

}
