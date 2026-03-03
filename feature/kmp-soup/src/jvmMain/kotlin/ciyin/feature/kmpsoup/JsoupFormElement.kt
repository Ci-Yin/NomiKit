package ciyin.feature.kmpsoup

import org.jsoup.nodes.FormElement as JsoupFormElement

/**
 * 基于 Jsoup 的表单元素包装实现。
 *
 * @property delegate Jsoup 表单元素委托对象。
 */
internal class JsoupFormElement(
    internal val delegate: JsoupFormElement,
) : FormElement, HtmlElement by delegate.toHtmlElement() {

    /**
     * 获取与当前表单关联的控件元素集合。
     */
    override fun elements(): List<HtmlElement> = delegate.elements().toHtmlElements()

    /**
     * 将一个控件元素与当前表单建立关联。
     */
    override fun addElement(element: HtmlElement): FormElement {
        if (element is JsoupHtmlElement) {
            delegate.addElement(element.delegate)
        }
        return this
    }

    /**
     * 获取表单拟提交的数据。
     */
    override fun formData(): List<Pair<String, String>> =
        delegate.formData().map { it.key() to it.value() }
}

