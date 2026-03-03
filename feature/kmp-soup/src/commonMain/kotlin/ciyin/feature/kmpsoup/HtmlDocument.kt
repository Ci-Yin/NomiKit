package ciyin.feature.kmpsoup

/**
 * 跨平台 HTML 文档抽象。
 */
interface HtmlDocument : HtmlElement {

    /**
     * 获取文档标题。
     *
     * @return 标题文本，失败时返回空字符串。
     */
    fun title(): String

    /**
     * 获取文档 body 元素。
     *
     * @return body 元素。
     */
    fun body(): HtmlElement

    /**
     * 获取文档的绝对地址。
     *
     * 对应 Jsoup `Document.location()`。
     *
     * @return 文档地址，未知时返回空字符串。
     */
    fun location(): String

    /**
     * 获取文档 head 元素。
     *
     * 对应 Jsoup `Document.head()`。
     *
     * @return head 元素。
     */
    fun head(): HtmlElement

    /**
     * 获取文档的 doctype 信息。
     *
     * 对应 Jsoup `Document.documentType()`。
     *
     * @return doctype。
     */
    fun documentType(): DocumentType?

    /**
     * 获取文档内所有 form 元素。
     *
     * 对应 Jsoup `Document.forms()`。
     *
     * @return 表单元素列表，失败时返回空列表。
     */
    fun forms(): List<FormElement>

    /**
     * 按 CSS 选择器查找并返回首个表单元素。
     *
     * 对应 Jsoup `Document.expectForm(String)`。
     *
     * @param cssQuery CSS 选择器。
     * @return 匹配到的表单元素。
     * @throws IllegalArgumentException 当未找到匹配表单时抛出。
     */
    fun expectForm(cssQuery: String): FormElement

    /**
     * 设置文档标题。
     *
     * 对应 Jsoup `Document.title(String)`。
     *
     * @param title 标题文本。
     */
    fun title(title: String)

    /**
     * 创建一个新的元素实例，但不会自动插入文档。
     *
     * 对应 Jsoup `Document.createElement(String)`。
     *
     * @param tagName 标签名。
     * @return 新创建的元素。
     */
    fun createElement(tagName: String): HtmlElement

    /**
     * 设置 body 文本内容。
     *
     * 对应 Jsoup `Document.text(String)`，不会破坏整体文档结构。
     *
     * @param text 文本内容。
     * @return 当前文档实例，便于链式调用。
     */
    fun text(text: String): HtmlDocument

    // 以下为 Jsoup Document 其余能力的接口占位，仅保留以方便对照，不在当前跨平台抽象中提供：
    //
    // fun connection(): Connection
    // fun charset(charset: Charset)
    // fun charset(): Charset
    // fun outputSettings(): OutputSettings
    // fun outputSettings(outputSettings: OutputSettings): HtmlDocument
    // fun quirksMode(): QuirksMode
    // fun quirksMode(mode: QuirksMode): HtmlDocument
    // fun parser(): Parser
    // fun parser(parser: Parser): HtmlDocument
    // fun connection(connection: Connection): HtmlDocument

}