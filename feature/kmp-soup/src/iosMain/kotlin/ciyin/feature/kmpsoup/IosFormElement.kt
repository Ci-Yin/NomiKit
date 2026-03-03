package ciyin.feature.kmpsoup

import ciyin.feature.kmpsoup.bridge.KmpSoupBridgeElement

private const val FORM_CONTROL_SELECTOR = "button,input,select,textarea,keygen,object,output"

/**
 * iOS 平台表单元素实现。
 *
 * @property delegate 桥接层表单根元素。
 * @property elementDelegate 通用元素代理实现。
 */
internal class IosFormElement(
    private val delegate: KmpSoupBridgeElement,
    private val elementDelegate: HtmlElement = IosHtmlElement(delegate),
) : FormElement, HtmlElement by elementDelegate {

    /**
     * 获取当前表单可提交控件集合。
     *
     * @return 表单控件列表。
     */
    override fun elements(): List<HtmlElement> = select(FORM_CONTROL_SELECTOR).toList()

    /**
     * 将控件元素追加到表单节点内部。
     *
     * @param element 目标控件元素。
     * @return 当前表单实例。
     */
    override fun addElement(element: HtmlElement): FormElement {
        val outerHtml = safeString { element.outerHtml() }
        if (outerHtml.isEmpty()) {
            return this
        }
        safeUnit {
            delegate.appendHtml(outerHtml)
        }
        return this
    }

    /**
     * 读取表单提交键值对。
     *
     * @return 表单键值对列表。
     */
    override fun formData(): List<Pair<String, String>> {
        val result = mutableListOf<Pair<String, String>>()
        elements().forEach { control ->
            collectFormData(control, result)
        }
        return result
    }

    private fun collectFormData(control: HtmlElement, result: MutableList<Pair<String, String>>) {
        val name = safeString { control.attr("name") }.trim()
        if (name.isEmpty() || control.hasAttr("disabled")) {
            return
        }

        val tag = safeString { control.tagName() }.lowercase()
        val type = safeString { control.attr("type") }.lowercase()

        if (tag == "input" && type in setOf("button", "submit", "reset", "image", "file")) {
            return
        }

        if (tag == "input" && type in setOf("checkbox", "radio") && !control.hasAttr("checked")) {
            return
        }

        if (tag == "select") {
            appendSelectValues(name, control, result)
            return
        }

        val value = safeString { control.value() }
        result += name to value
    }

    private fun appendSelectValues(
        name: String,
        selectElement: HtmlElement,
        result: MutableList<Pair<String, String>>,
    ) {
        val selectedOptions = selectElement.select("option[selected]").toList()
        val options = if (selectedOptions.isNotEmpty()) {
            selectedOptions
        } else {
            val first = selectElement.select("option").firstOrNull()
            if (first != null) listOf(first) else emptyList()
        }

        options.forEach { option ->
            val value = safeString { option.attr("value") }.ifEmpty {
                safeString { option.text() }
            }
            result += name to value
        }
    }
}
