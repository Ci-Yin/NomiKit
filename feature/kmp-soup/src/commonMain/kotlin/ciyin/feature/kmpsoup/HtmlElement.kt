package ciyin.feature.kmpsoup

/**
 * 跨平台 HTML 元素抽象。
 */
interface HtmlElement {
    /**
     * 按元素 id 查询。
     *
     * @param id 元素 id。
     * @return 匹配元素，不存在或失败时返回 `null`。
     */
    fun getElementById(id: String): HtmlElement?

    /**
     * 按标签名查询元素集合。
     *
     * @param tag 标签名。
     * @return 查询结果集合，失败时返回空集合。
     */
    fun getElementsByTag(tag: String): HtmlElements

    /**
     * 按类名查询元素集合。
     *
     * @param className 类名。
     * @return 查询结果集合，失败时返回空集合。
     */
    fun getElementsByClass(className: String): HtmlElements

    /**
     * 在当前元素内按 CSS 选择器查询元素集合。
     *
     * @param css CSS 选择器。
     * @return 查询结果集合，失败时返回空集合。
     */
    fun select(css: String): HtmlElements

    /**
     * 在当前元素内按 CSS 选择器查询首个元素。
     *
     * @param css CSS 选择器。
     * @return 首个匹配元素，不存在或失败时返回 `null`。
     */
    fun selectFirst(css: String): HtmlElement?

    /**
     * 获取当前元素及其子树文本。
     *
     * @return 文本内容，失败时返回空字符串。
     */
    fun text(): String

    /**
     * 获取当前元素自身文本。
     *
     * @return 文本内容，失败时返回空字符串。
     */
    fun ownText(): String

    /**
     * 获取属性值。
     *
     * @param name 属性名。
     * @return 属性值，不存在或失败时返回空字符串。
     */
    fun attr(name: String): String

    /**
     * 判断是否包含属性。
     *
     * @param name 属性名。
     * @return 存在返回 `true`，否则返回 `false`。
     */
    fun hasAttr(name: String): Boolean

    /**
     * 获取元素 id。
     *
     * @return id，不存在或失败时返回空字符串。
     */
    fun id(): String

    /**
     * 获取标签名。
     *
     * @return 标签名，失败时返回空字符串。
     */
    fun tagName(): String

    /**
     * 获取完整 class 字符串。
     *
     * @return class 字符串，失败时返回空字符串。
     */
    fun className(): String

    /**
     * 获取 class 名集合。
     *
     * @return class 名集合，失败时返回空集合。
     */
    fun classNames(): Set<String>

    /**
     * 获取父元素。
     *
     * @return 父元素，不存在或失败时返回 `null`。
     */
    fun parent(): HtmlElement?

    /**
     * 获取直接子元素集合。
     *
     * @return 子元素集合，失败时返回空集合。
     */
    fun children(): HtmlElements

    /**
     * 按索引获取子元素。
     *
     * @param index 子元素索引。
     * @return 子元素，越界抛出异常。
     */
    fun child(index: Int): HtmlElement

    /**
     * 获取直接子元素数量。
     *
     * @return 子元素数量，失败时返回 `0`。
     */
    fun childrenSize(): Int

    /**
     * 获取元素内部 HTML。
     *
     * @return HTML 字符串，失败时返回空字符串。
     */
    fun html(): String

    /**
     * 获取元素外层 HTML。
     *
     * @return HTML 字符串，失败时返回空字符串。
     */
    fun outerHtml(): String

    /**
     * 获取当前元素的所有父元素（从近到远）。
     *
     * 对应 Jsoup `Element.parents()`。
     *
     * @return 父元素集合，失败时返回空集合。
     */
    fun parents(): HtmlElements

    /**
     * 获取当前元素的兄弟元素（不包含自身）。
     *
     * 对应 Jsoup `Element.siblingElements()`。
     *
     * @return 兄弟元素集合，失败时返回空集合。
     */
    fun siblingElements(): HtmlElements

    /**
     * 获取当前元素之后的兄弟元素集合。
     *
     * 对应 Jsoup `Element.nextElementSiblings()`。
     *
     * @return 后续兄弟元素集合，失败时返回空集合。
     */
    fun nextElementSiblings(): HtmlElements

    /**
     * 获取当前元素之前的兄弟元素集合。
     *
     * 对应 Jsoup `Element.previousElementSiblings()`。
     *
     * @return 前置兄弟元素集合，失败时返回空集合。
     */
    fun previousElementSiblings(): HtmlElements

    /**
     * 获取第一个兄弟元素（通常是父元素的第一个子元素）。
     *
     * 对应 Jsoup `Element.firstElementSibling()`。
     *
     * @return 第一个兄弟元素。
     */
    fun firstElementSibling(): HtmlElement

    /**
     * 获取最后一个兄弟元素。
     *
     * 对应 Jsoup `Element.lastElementSibling()`。
     *
     * @return 最后一个兄弟元素。
     */
    fun lastElementSibling(): HtmlElement

    /**
     * 获取当前元素在兄弟元素列表中的索引（从 0 开始）。
     *
     * 对应 Jsoup `Element.elementSiblingIndex()`。
     *
     * @return 兄弟索引，失败时返回 `0`。
     */
    fun elementSiblingIndex(): Int

    /**
     * 按属性名查询包含该属性的元素集合。
     *
     * 对应 Jsoup `Element.getElementsByAttribute(String)`。
     *
     * @param key 属性名。
     * @return 匹配元素集合，失败时返回空集合。
     */
    fun getElementsByAttribute(key: String): HtmlElements

    /**
     * 按属性名前缀查询元素集合。
     *
     * 对应 Jsoup `Element.getElementsByAttributeStarting(String)`。
     *
     * @param keyPrefix 属性名前缀。
     * @return 匹配元素集合，失败时返回空集合。
     */
    fun getElementsByAttributeStarting(keyPrefix: String): HtmlElements

    /**
     * 按属性值精确匹配查询元素集合（忽略大小写）。
     *
     * 对应 Jsoup `Element.getElementsByAttributeValue(String, String)`。
     *
     * @param key 属性名。
     * @param value 属性值。
     * @return 匹配元素集合，失败时返回空集合。
     */
    fun getElementsByAttributeValue(key: String, value: String): HtmlElements

    /**
     * 查询不包含指定属性值的元素集合（忽略大小写）。
     *
     * 对应 Jsoup `Element.getElementsByAttributeValueNot(String, String)`。
     *
     * @param key 属性名。
     * @param value 属性值。
     * @return 不匹配该值的元素集合，失败时返回空集合。
     */
    fun getElementsByAttributeValueNot(key: String, value: String): HtmlElements

    /**
     * 按属性值前缀查询元素集合（忽略大小写）。
     *
     * 对应 Jsoup `Element.getElementsByAttributeValueStarting(String, String)`。
     *
     * @param key 属性名。
     * @param valuePrefix 属性值前缀。
     * @return 匹配元素集合，失败时返回空集合。
     */
    fun getElementsByAttributeValueStarting(key: String, valuePrefix: String): HtmlElements

    /**
     * 按属性值后缀查询元素集合（忽略大小写）。
     *
     * 对应 Jsoup `Element.getElementsByAttributeValueEnding(String, String)`。
     *
     * @param key 属性名。
     * @param valueSuffix 属性值后缀。
     * @return 匹配元素集合，失败时返回空集合。
     */
    fun getElementsByAttributeValueEnding(key: String, valueSuffix: String): HtmlElements

    /**
     * 按属性值包含子串查询元素集合（忽略大小写）。
     *
     * 对应 Jsoup `Element.getElementsByAttributeValueContaining(String, String)`。
     *
     * @param key 属性名。
     * @param match 需包含的子串。
     * @return 匹配元素集合，失败时返回空集合。
     */
    fun getElementsByAttributeValueContaining(key: String, match: String): HtmlElements

    /**
     * 查询文本内容包含指定字符串的元素集合（不区分大小写）。
     *
     * 对应 Jsoup `Element.getElementsContainingText(String)`。
     *
     * @param searchText 查询文本。
     * @return 匹配元素集合，失败时返回空集合。
     */
    fun getElementsContainingText(searchText: String): HtmlElements

    /**
     * 查询自身文本内容包含指定字符串的元素集合（不区分大小写）。
     *
     * 对应 Jsoup `Element.getElementsContainingOwnText(String)`。
     *
     * @param searchText 查询文本。
     * @return 匹配元素集合，失败时返回空集合。
     */
    fun getElementsContainingOwnText(searchText: String): HtmlElements

    /**
     * 按正则匹配整棵子树文本内容的元素集合。
     *
     * 对应 Jsoup `Element.getElementsMatchingText(String)`。
     *
     * @param regex 正则表达式字符串。
     * @return 匹配元素集合，失败时返回空集合。
     */
    fun getElementsMatchingText(regex: String): HtmlElements

    /**
     * 按正则匹配自身文本内容的元素集合。
     *
     * 对应 Jsoup `Element.getElementsMatchingOwnText(String)`。
     *
     * @param regex 正则表达式字符串。
     * @return 匹配元素集合，失败时返回空集合。
     */
    fun getElementsMatchingOwnText(regex: String): HtmlElements

    /**
     * 获取当前元素及其所有后代元素集合（包含自身）。
     *
     * 对应 Jsoup `Element.getAllElements()`。
     *
     * @return 所有元素集合，失败时返回空集合。
     */
    fun getAllElements(): HtmlElements

    /**
     * 判断当前元素是否包含指定 class。
     *
     * 对应 Jsoup `Element.hasClass(String)`。
     *
     * @param className class 名称。
     * @return 存在返回 `true`，否则返回 `false`。
     */
    fun hasClass(className: String): Boolean

    /**
     * 为当前元素添加一个 class。
     *
     * 对应 Jsoup `Element.addClass(String)`。
     *
     * @param className class 名称。
     * @return 当前元素，便于链式调用。
     */
    fun addClass(className: String): HtmlElement

    /**
     * 从当前元素移除一个 class。
     *
     * 对应 Jsoup `Element.removeClass(String)`。
     *
     * @param className class 名称。
     * @return 当前元素，便于链式调用。
     */
    fun removeClass(className: String): HtmlElement

    /**
     * 切换当前元素的某个 class：存在则移除，不存在则添加。
     *
     * 对应 Jsoup `Element.toggleClass(String)`。
     *
     * @param className class 名称。
     * @return 当前元素，便于链式调用。
     */
    fun toggleClass(className: String): HtmlElement

    /**
     * 获取表单控件的值。
     *
     * 对应 Jsoup `Element.val()`。
     *
     * @return 值字符串，未设置时返回空字符串。
     */
    fun value(): String

    /**
     * 设置表单控件的值。
     *
     * 对应 Jsoup `Element.val(String)`。
     *
     * @param value 新的值。
     * @return 当前元素，便于链式调用。
     */
    fun value(value: String): HtmlElement

    /**
     * 获取当前元素及其子树的原始文本（不做空白规范化）。
     *
     * 对应 Jsoup `Element.wholeText()`。
     *
     * @return 原始文本，失败时返回空字符串。
     */
    fun wholeText(): String

    /**
     * 获取当前元素自身的原始文本（不包含子元素）。
     *
     * 对应 Jsoup `Element.wholeOwnText()`。
     *
     * @return 原始文本，失败时返回空字符串。
     */
    fun wholeOwnText(): String

    /**
     * 判断当前元素或其子元素是否包含非空白文本。
     *
     * 对应 Jsoup `Element.hasText()`。
     *
     * @return 存在非空白文本返回 `true`，否则返回 `false`。
     */
    fun hasText(): Boolean

    /**
     * 获取元素内部的“数据”内容，如脚本、样式、注释等。
     *
     * 对应 Jsoup `Element.data()`。
     *
     * @return 数据内容，失败时返回空字符串。
     */
    fun data(): String

    /**
     * 获取一个唯一定位当前元素的 CSS 选择器。
     *
     * 对应 Jsoup `Element.cssSelector()`。
     *
     * @return CSS 选择器字符串。
     */
    fun cssSelector(): String

    /**
     * 判断当前元素是否匹配给定 CSS 选择器。
     *
     * 对应 Jsoup `Element.is(String)`。
     *
     * @param cssQuery CSS 选择器。
     * @return 匹配返回 `true`，否则返回 `false`。
     */
    fun matches(cssQuery: String): Boolean

    /**
     * 查找最近的、匹配给定 CSS 选择器的祖先元素（包含自身）。
     *
     * 对应 Jsoup `Element.closest(String)`。
     *
     * @param cssQuery CSS 选择器。
     * @return 最近匹配元素，不存在时返回 `null`。
     */
    fun closest(cssQuery: String): HtmlElement?

    /**
     * 查询当前元素匹配给定 XPath 表达式的元素集合。
     *
     * 对应 Jsoup `Element.selectXpath(String)`。
     *
     * @param xpath XPath 表达式。
     * @return 匹配元素集合，失败时返回空集合。
     */
    fun selectXpath(xpath: String): HtmlElements

    // 下面为 Jsoup Element 其余高级查询能力在 KMP 层的占位说明，暂不在公共接口中暴露：
    //
    // fun selectNodes(cssQuery: String): Nodes<Node>
    // fun selectNodes(evaluator: Evaluator): Nodes<Node>
    // fun <T : Node> selectNodes(cssQuery: String, type: Class<T>): Nodes<T>
    // fun <T : Node> selectNodes(evaluator: Evaluator, type: Class<T>): Nodes<T>
    // fun <T : Node> selectFirstNode(cssQuery: String, type: Class<T>): T?
    // fun <T : Node> selectFirstNode(evaluator: Evaluator, type: Class<T>): T?
    // fun <T : Node> expectFirstNode(cssQuery: String, type: Class<T>): T
    // fun <T : Node> selectXpath(xpath: String, nodeType: Class<T>): List<T>
}