package ciyin.feature.kmpsoup

import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.jsoup.nodes.DocumentType as JsoupDocumentType
import org.jsoup.nodes.FormElement as JsoupFormElement

internal fun Elements.toHtmlElements(): HtmlElements = JsoupHtmlElements(this)

internal fun Element.toHtmlElement(): HtmlElement = JsoupHtmlElement(this)

internal fun JsoupFormElement.toFormElement(): FormElement = JsoupFormElement(this)

internal fun JsoupDocumentType.toDocumentType(): DocumentType = JsoupDocumentType(this)
