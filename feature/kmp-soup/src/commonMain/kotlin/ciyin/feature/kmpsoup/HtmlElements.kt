package ciyin.feature.kmpsoup

/**
 * 跨平台元素集合封装，对应 Jsoup `Elements`。
 *
 * 尽量复刻 Jsoup 中 `Elements` 的公开 API，但不会修改集合本身的结构
 *（例如不提供 `set` / `remove` / `deselect` 这类会改变列表项个数的方法），
 * 以保证 `List<HtmlElement>` 的只读语义不被破坏。
 */
interface HtmlElements : List<HtmlElement> {

    // =========================
    // 基本文本 / 属性访问
    // =========================

    /**
     * 返回所有元素文本拼接结果（空格分隔）。
     *
     * 对应 Jsoup `Elements.text()`。
     *
     * @return 拼接文本，失败时返回空字符串。
     */
    fun text(): String

    /**
     * 获取第一个包含指定属性的元素的属性值。
     *
     * 对应 Jsoup `Elements.attr(String)`。
     *
     * @param name 属性名。
     * @return 属性值，不存在或失败时返回空字符串。
     */
    fun attr(name: String): String

    /**
     * 判断是否存在至少一个元素包含指定属性。
     *
     * 对应 Jsoup `Elements.hasAttr(String)`。
     *
     * @param name 属性名。
     * @return 存在返回 `true`，否则返回 `false`。
     */
    fun hasAttr(name: String): Boolean

    /**
     * 获取所有包含指定属性的元素的属性值列表。
     *
     * 对应 Jsoup `Elements.eachAttr(String)`。
     *
     * @param name 属性名。
     * @return 属性值列表，可能为空列表。
     */
    fun eachAttr(name: String): List<String>

    /**
     * 为所有元素设置指定属性值。
     *
     * 对应 Jsoup `Elements.attr(String, String)`。
     *
     * @param name 属性名。
     * @param value 属性值。
     * @return 当前集合，便于链式调用。
     */
    fun attr(name: String, value: String): HtmlElements

    /**
     * 从所有元素中移除指定属性。
     *
     * 对应 Jsoup `Elements.removeAttr(String)`。
     *
     * @param name 属性名。
     * @return 当前集合，便于链式调用。
     */
    fun removeAttr(name: String): HtmlElements

    // =========================
    // class 操作
    // =========================

    /**
     * 为所有元素添加一个 class。
     *
     * 对应 Jsoup `Elements.addClass(String)`。
     *
     * @param className class 名称。
     * @return 当前集合，便于链式调用。
     */
    fun addClass(className: String): HtmlElements

    /**
     * 从所有元素移除一个 class。
     *
     * 对应 Jsoup `Elements.removeClass(String)`。
     *
     * @param className class 名称。
     * @return 当前集合，便于链式调用。
     */
    fun removeClass(className: String): HtmlElements

    /**
     * 在所有元素上切换一个 class（存在则移除，不存在则添加）。
     *
     * 对应 Jsoup `Elements.toggleClass(String)`。
     *
     * @param className class 名称。
     * @return 当前集合，便于链式调用。
     */
    fun toggleClass(className: String): HtmlElements

    /**
     * 判断是否存在至少一个元素包含指定 class。
     *
     * 对应 Jsoup `Elements.hasClass(String)`。
     *
     * @param className class 名称。
     * @return 存在返回 `true`，否则返回 `false`。
     */
    fun hasClass(className: String): Boolean

    // =========================
    // 表单值访问
    // =========================

    /**
     * 获取第一个表单元素的值。
     *
     * 语义对应 Jsoup `Elements.val()`，命名与 `HtmlElement.value()` 对齐。
     *
     * @return 表单值，未设置或不存在时返回空字符串。
     */
    fun value(): String

    /**
     * 为所有表单元素设置值。
     *
     * 语义对应 Jsoup `Elements.val(String)`，命名与 `HtmlElement.value(String)` 对齐。
     *
     * @param value 要设置的值。
     * @return 当前集合，便于链式调用。
     */
    fun value(value: String): HtmlElements

    // =========================
    // 文本 / HTML 相关
    // =========================

    /**
     * 判断是否存在至少一个元素包含非空白文本。
     *
     * 对应 Jsoup `Elements.hasText()`。
     *
     * @return 存在返回 `true`，否则返回 `false`。
     */
    fun hasText(): Boolean

    /**
     * 获取每个包含文本的元素的文本内容列表。
     *
     * 对应 Jsoup `Elements.eachText()`。
     *
     * @return 文本列表，可能为空列表。
     */
    fun eachText(): List<String>

    /**
     * 获取所有元素内部 HTML 拼接结果（按换行分隔）。
     *
     * 对应 Jsoup `Elements.html()`。
     *
     * @return HTML 字符串，失败时返回空字符串。
     */
    fun html(): String

    /**
     * 更新所有元素的标签名。
     *
     * 对应 Jsoup `Elements.tagName(String)`。
     *
     * @param tagName 新的标签名。
     * @return 当前集合，便于链式调用。
     */
    fun tagName(tagName: String): HtmlElements

    /**
     * 设置所有元素的内部 HTML。
     *
     * 对应 Jsoup `Elements.html(String)`。
     *
     * @param html HTML 字符串。
     * @return 当前集合，便于链式调用。
     */
    fun html(html: String): HtmlElements

    /**
     * 在所有元素内部前面插入指定 HTML。
     *
     * 对应 Jsoup `Elements.prepend(String)`。
     */
    fun prepend(html: String): HtmlElements

    /**
     * 在所有元素内部后面追加指定 HTML。
     *
     * 对应 Jsoup `Elements.append(String)`。
     */
    fun append(html: String): HtmlElements

    /**
     * 在所有元素前插入指定 HTML。
     *
     * 对应 Jsoup `Elements.before(String)`。
     */
    fun before(html: String): HtmlElements

    /**
     * 在所有元素后插入指定 HTML。
     *
     * 对应 Jsoup `Elements.after(String)`。
     */
    fun after(html: String): HtmlElements

    /**
     * 使用指定 HTML 包裹所有元素。
     *
     * 对应 Jsoup `Elements.wrap(String)`。
     */
    fun wrap(html: String): HtmlElements

    /**
     * 移除所有元素自身节点，但保留其子节点。
     *
     * 对应 Jsoup `Elements.unwrap()`。
     */
    fun unwrap(): HtmlElements

    /**
     * 清空所有元素的子节点。
     *
     * 对应 Jsoup `Elements.empty()`。
     */
    fun empty(): HtmlElements

    /**
     * 将所有元素从 DOM 中移除（但仍保留在当前集合中）。
     *
     * 对应 Jsoup `Elements.remove()`。
     */
    fun remove(): HtmlElements

    // =========================
    // 选择器 / 过滤
    // =========================

    /**
     * 在当前元素集合内部按 CSS 选择器查询。
     *
     * 对应 Jsoup `Elements.select(String)`。
     *
     * @param css CSS 选择器。
     * @return 匹配元素集合，失败时返回空集合。
     */
    fun select(css: String): HtmlElements

    /**
     * 在当前元素集合内部按 CSS 选择器查询首个元素。
     *
     * 对应 Jsoup `Elements.selectFirst(String)`。
     *
     * @param css CSS 选择器。
     * @return 首个匹配元素，不存在时返回 `null`。
     */
    fun selectFirst(css: String): HtmlElement?

    /**
     * 在当前元素集合内部按 CSS 选择器查询首个元素，不存在时抛出异常。
     *
     * 对应 Jsoup `Elements.expectFirst(String)`。
     *
     * @param css CSS 选择器。
     * @return 首个匹配元素。
     * @throws IllegalArgumentException 未匹配到元素时抛出。
     */
    fun expectFirst(css: String): HtmlElement

    /**
     * 从当前集合中过滤掉匹配给定 CSS 选择器的元素。
     *
     * 对应 Jsoup `Elements.not(String)`。
     *
     * @param css CSS 选择器。
     * @return 过滤后的新集合。
     */
    fun not(css: String): HtmlElements

    /**
     * 获取指定索引位置的元素并包装成新的集合。
     *
     * 对应 Jsoup `Elements.eq(int)`。
     *
     * @param index 索引（从 0 开始）。
     * @return 仅包含该元素的新集合，若越界则返回空集合。
     */
    fun eq(index: Int): HtmlElements

    /**
     * 判断是否存在至少一个元素匹配给定 CSS 选择器。
     *
     * 对应 Jsoup `Elements.is(String)`。
     *
     * @param css CSS 选择器。
     * @return 存在匹配返回 `true`，否则返回 `false`。
     */
    fun matches(css: String): Boolean

    // =========================
    // 兄弟 / 父级导航
    // =========================

    /**
     * 获取每个元素的下一个兄弟元素集合。
     *
     * 对应 Jsoup `Elements.next()`。
     */
    fun next(): HtmlElements

    /**
     * 获取匹配指定 CSS 选择器的下一个兄弟元素集合。
     *
     * 对应 Jsoup `Elements.next(String)`。
     */
    fun next(css: String): HtmlElements

    /**
     * 获取所有后续兄弟元素集合。
     *
     * 对应 Jsoup `Elements.nextAll()`。
     */
    fun nextAll(): HtmlElements

    /**
     * 获取所有匹配指定 CSS 选择器的后续兄弟元素集合。
     *
     * 对应 Jsoup `Elements.nextAll(String)`。
     */
    fun nextAll(css: String): HtmlElements

    /**
     * 获取每个元素的上一个兄弟元素集合。
     *
     * 对应 Jsoup `Elements.prev()`。
     */
    fun prev(): HtmlElements

    /**
     * 获取匹配指定 CSS 选择器的上一个兄弟元素集合。
     *
     * 对应 Jsoup `Elements.prev(String)`。
     */
    fun prev(css: String): HtmlElements

    /**
     * 获取所有前置兄弟元素集合。
     *
     * 对应 Jsoup `Elements.prevAll()`。
     */
    fun prevAll(): HtmlElements

    /**
     * 获取所有匹配指定 CSS 选择器的前置兄弟元素集合。
     *
     * 对应 Jsoup `Elements.prevAll(String)`。
     */
    fun prevAll(css: String): HtmlElements

    /**
     * 获取所有元素的父级及祖先元素集合。
     *
     * 对应 Jsoup `Elements.parents()`。
     */
    fun parents(): HtmlElements

    // =========================
    // 表单相关
    // =========================

    /**
     * 从当前集合中提取所有表单元素。
     *
     * 对应 Jsoup `Elements.forms()`。
     *
     * @return 表单元素列表，可能为空列表。
     */
    fun forms(): List<FormElement>

    // =========================
    // 高级节点访问占位（暂不暴露）
    // =========================
    //
    // 下列能力对应 Jsoup `Elements` 中对 Comment / TextNode / DataNode 以及
    // NodeVisitor / NodeFilter 的高级遍历与过滤能力。由于需要在 KMP 层定义
    // 额外的抽象类型，这里先以注释形式保留原型，后续若有需求可补全：
    //
    // fun comments(): List<CommentNode>
    // fun textNodes(): List<TextNodeLike>
    // fun dataNodes(): List<DataNodeLike>
    //
    // fun traverse(visitor: NodeVisitorLike): HtmlElements
    // fun filter(filter: NodeFilterLike): HtmlElements
}
