package ciyin.feature.kmpsoup

/**
 * 跨平台 HTML 表单元素抽象。
 *
 * 对应 Jsoup `FormElement`，用于统一访问表单及其控件。
 *
 * 语义上等价于 Jsoup 的 `FormElement`，在 KMP 层尽量复刻其公开 API。
 */
interface FormElement : HtmlElement {

    /**
     * 获取与当前表单关联的控件元素集合。
     *
     * 语义上对应 Jsoup 中 `FormElement.elements()`，包含当前表单子树中的可提交控件，
     * 以及解析过程中按规则关联到本表单的控件。
     *
     * @return 表单控件集合，失败时返回空集合。
     */
    fun elements(): List<HtmlElement>

    /**
     * 将一个表单控件元素与当前表单建立关联。
     *
     * 不要求控件必须是当前表单的直接子元素。
     *
     * @param element 控件元素。
     * @return 当前表单实例，便于链式调用。
     */
    fun addElement(element: HtmlElement): FormElement

    /**
     * 获取表单拟提交的数据。
     *
     * 语义上对应 Jsoup `FormElement.formData()`，但在跨平台抽象层仅以键值对形式返回，
     * 不直接暴露底层的 `Connection.KeyVal` 类型。
     *
     * @return 表单提交键值对列表，失败时返回空列表。
     */
    fun formData(): List<Pair<String, String>>

    // Jsoup 中还提供了 submit() 与 clone() 能力，这里按原型保留注释，方便后续在具体平台实现：
    //
    // fun submit(): Connection
    // public override fun clone(): FormElement
}

