package ciyin.feature.kmpsoup

import ciyin.feature.kmpsoup.bridge.KmpSoupBridgeElement

/**
 * 基于 SwiftSoup 桥接元素的包装实现。
 *
 * @property delegate iOS 桥接元素对象。
 */
internal class IosHtmlElement(
    internal val delegate: KmpSoupBridgeElement,
) : HtmlElement {

    /**
     * 按 id 查询元素。
     */
    override fun getElementById(id: String): HtmlElement? = safeElement {
        delegate.getElementById(id)
    }

    /**
     * 按标签名查询元素集合。
     */
    override fun getElementsByTag(tag: String): HtmlElements = safeElements {
        delegate.getElementsByTag(tag)
    }

    /**
     * 按类名查询元素集合。
     */
    override fun getElementsByClass(className: String): HtmlElements = safeElements {
        delegate.getElementsByClass(className)
    }

    /**
     * 在当前元素内按 CSS 查询元素集合。
     */
    override fun select(css: String): HtmlElements = safeElements {
        delegate.select(css)
    }

    /**
     * 在当前元素内按 CSS 查询首个元素。
     */
    override fun selectFirst(css: String): HtmlElement? = safeElement {
        delegate.selectFirst(css)
    }

    /**
     * 获取当前元素及子树文本。
     */
    override fun text(): String = safeString {
        delegate.text()
    }

    /**
     * 获取当前元素自身文本。
     */
    override fun ownText(): String = safeString {
        delegate.ownText()
    }

    /**
     * 获取属性值。
     */
    override fun attr(name: String): String = safeString {
        delegate.attr(name)
    }

    /**
     * 判断属性是否存在。
     */
    override fun hasAttr(name: String): Boolean = safeBoolean {
        delegate.hasAttr(name)
    }

    /**
     * 获取元素 id。
     */
    override fun id(): String = safeString {
        delegate.id()
    }

    /**
     * 获取标签名。
     */
    override fun tagName(): String = safeString {
        delegate.tagName()
    }

    /**
     * 获取 class 字符串。
     */
    override fun className(): String = safeString {
        delegate.className()
    }

    /**
     * 获取 class 名集合。
     */
    override fun classNames(): Set<String> = normalizeClassNames(className())

    /**
     * 获取父元素。
     */
    override fun parent(): HtmlElement? = safeElement {
        delegate.parent()
    }

    /**
     * 获取子元素集合。
     */
    override fun children(): HtmlElements = safeElements {
        delegate.children()
    }

    /**
     * 按索引获取子元素。
     */
    override fun child(index: Int): HtmlElement {
        if (index < 0) {
            throw IndexOutOfBoundsException("index=$index")
        }
        return safeElement {
            delegate.childAt(index.toLong())
        } ?: throw IndexOutOfBoundsException("index=$index")
    }

    /**
     * 获取子元素数量。
     */
    override fun childrenSize(): Int = safeInt {
        delegate.childSize().toInt()
    }

    /**
     * 获取元素内部 HTML。
     */
    override fun html(): String = safeString {
        delegate.html()
    }

    /**
     * 获取元素外层 HTML。
     */
    override fun outerHtml(): String = safeString {
        delegate.outerHtml()
    }

    /**
     * 获取当前元素的所有父元素（从近到远）。
     */
    override fun parents(): HtmlElements = safeElements {
        delegate.parents()
    }

    /**
     * 获取兄弟元素集合（不包含自身）。
     */
    override fun siblingElements(): HtmlElements = safeElements {
        delegate.siblingElements()
    }

    /**
     * 获取后续兄弟元素集合。
     */
    override fun nextElementSiblings(): HtmlElements = safeElements {
        delegate.nextElementSiblings()
    }

    /**
     * 获取前置兄弟元素集合。
     */
    override fun previousElementSiblings(): HtmlElements = safeElements {
        delegate.previousElementSiblings()
    }

    /**
     * 获取第一个兄弟元素。
     */
    override fun firstElementSibling(): HtmlElement =
        safeElement { delegate.firstElementSibling() } ?: this

    /**
     * 获取最后一个兄弟元素。
     */
    override fun lastElementSibling(): HtmlElement =
        safeElement { delegate.lastElementSibling() } ?: this

    /**
     * 获取兄弟索引。
     */
    override fun elementSiblingIndex(): Int = safeInt {
        delegate.elementSiblingIndex().toInt()
    }

    /**
     * 按属性名查询元素集合。
     */
    override fun getElementsByAttribute(key: String): HtmlElements = select("[$key]")

    /**
     * 按属性名前缀查询元素集合。
     */
    override fun getElementsByAttributeStarting(keyPrefix: String): HtmlElements =
        select("[^$keyPrefix]")

    /**
     * 按属性值精确匹配查询元素集合。
     */
    override fun getElementsByAttributeValue(key: String, value: String): HtmlElements =
        select("[$key=$value]")

    /**
     * 查询不包含指定属性值的元素集合。
     */
    override fun getElementsByAttributeValueNot(key: String, value: String): HtmlElements =
        select(":not([$key=$value])")

    /**
     * 按属性值前缀查询元素集合。
     */
    override fun getElementsByAttributeValueStarting(
        key: String,
        valuePrefix: String
    ): HtmlElements =
        select("[$key^=$valuePrefix]")

    /**
     * 按属性值后缀查询元素集合。
     */
    override fun getElementsByAttributeValueEnding(key: String, valueSuffix: String): HtmlElements =
        select("[$key\$=$valueSuffix]")

    /**
     * 按属性值包含查询元素集合。
     */
    override fun getElementsByAttributeValueContaining(key: String, match: String): HtmlElements =
        select("[$key*=$match]")

    /**
     * 查询文本包含指定字符串的元素集合。
     */
    override fun getElementsContainingText(searchText: String): HtmlElements =
        select(":contains($searchText)")

    /**
     * 查询自身文本包含指定字符串的元素集合。
     */
    override fun getElementsContainingOwnText(searchText: String): HtmlElements =
        select(":containsOwn($searchText)")

    /**
     * 按正则匹配文本的元素集合。
     */
    override fun getElementsMatchingText(regex: String): HtmlElements =
        select(":matches($regex)")

    /**
     * 按正则匹配自身文本的元素集合。
     */
    override fun getElementsMatchingOwnText(regex: String): HtmlElements =
        select(":matchesOwn($regex)")

    /**
     * 获取当前元素及其全部后代（包含自身）。
     */
    override fun getAllElements(): HtmlElements {
        val all = mutableListOf<HtmlElement>()
        all += this
        all += select("*")
        return IosSnapshotHtmlElements(all)
    }

    /**
     * 判断当前元素是否包含指定 class。
     */
    override fun hasClass(className: String): Boolean = safeBoolean {
        delegate.hasClassName(className)
    }

    /**
     * 为当前元素添加 class。
     */
    override fun addClass(className: String): HtmlElement {
        safeUnit {
            delegate.addClassName(className)
        }
        return this
    }

    /**
     * 从当前元素移除 class。
     */
    override fun removeClass(className: String): HtmlElement {
        safeUnit {
            delegate.removeClassName(className)
        }
        return this
    }

    /**
     * 切换当前元素 class。
     */
    override fun toggleClass(className: String): HtmlElement {
        safeUnit {
            delegate.toggleClassName(className)
        }
        return this
    }

    /**
     * 获取表单值。
     */
    override fun value(): String = safeString {
        delegate.value()
    }

    /**
     * 设置表单值。
     */
    override fun value(value: String): HtmlElement {
        safeUnit {
            delegate.setValue(value)
        }
        return this
    }

    /**
     * 获取原始文本（iOS 端回退为标准文本）。
     */
    override fun wholeText(): String = text()

    /**
     * 获取原始自身文本（iOS 端回退为自身文本）。
     */
    override fun wholeOwnText(): String = ownText()

    /**
     * 判断是否包含非空文本。
     */
    override fun hasText(): Boolean = safeBoolean {
        delegate.hasTextContent()
    }

    /**
     * 获取数据节点内容（iOS 端对 script/style 回退为内部 HTML）。
     */
    override fun data(): String {
        val tag = tagName().lowercase()
        return if (tag == "script" || tag == "style") html() else ""
    }

    /**
     * 获取当前元素 CSS 选择器。
     */
    override fun cssSelector(): String {
        val id = id()
        if (id.isNotBlank()) {
            return "#$id"
        }

        val currentTag = tagName().ifBlank { "*" }
        val parentElement = parent()
        if (parentElement == null) {
            return currentTag
        }

        val siblingIndex = elementSiblingIndex() + 1
        return "${parentElement.cssSelector()} > $currentTag:nth-child($siblingIndex)"
    }

    /**
     * 判断当前元素是否匹配 CSS 选择器。
     */
    override fun matches(cssQuery: String): Boolean = safeBoolean {
        delegate.matchesCss(cssQuery)
    }

    /**
     * 查询最近匹配祖先（包含自身）。
     */
    override fun closest(cssQuery: String): HtmlElement? = safeElement {
        delegate.closest(cssQuery)
    }

    /**
     * 按 XPath 查询（iOS 桥接层未提供该能力）。
     */
    override fun selectXpath(xpath: String): HtmlElements {
        return IosSnapshotHtmlElements(emptyList())
    }
}
