package ciyin.feature.kmpsoup

/**
 * 脱离文档的占位元素实现，用于在 iOS 无法提供完整 DOM 能力时兜底。
 */
internal class IosDetachedHtmlElement(
    private val tag: String = "",
) : HtmlElement {

    override fun getElementById(id: String): HtmlElement? = null

    override fun getElementsByTag(tag: String): HtmlElements = IosSnapshotHtmlElements(emptyList())

    override fun getElementsByClass(className: String): HtmlElements =
        IosSnapshotHtmlElements(emptyList())

    override fun select(css: String): HtmlElements = IosSnapshotHtmlElements(emptyList())

    override fun selectFirst(css: String): HtmlElement? = null

    override fun text(): String = ""

    override fun ownText(): String = ""

    override fun attr(name: String): String = ""

    override fun hasAttr(name: String): Boolean = false

    override fun id(): String = ""

    override fun tagName(): String = tag

    override fun className(): String = ""

    override fun classNames(): Set<String> = emptySet()

    override fun parent(): HtmlElement? = null

    override fun children(): HtmlElements = IosSnapshotHtmlElements(emptyList())

    override fun child(index: Int): HtmlElement = throw IndexOutOfBoundsException("Empty element")

    override fun childrenSize(): Int = 0

    override fun html(): String = ""

    override fun outerHtml(): String = ""

    override fun parents(): HtmlElements = IosSnapshotHtmlElements(emptyList())

    override fun siblingElements(): HtmlElements = IosSnapshotHtmlElements(emptyList())

    override fun nextElementSiblings(): HtmlElements = IosSnapshotHtmlElements(emptyList())

    override fun previousElementSiblings(): HtmlElements = IosSnapshotHtmlElements(emptyList())

    override fun firstElementSibling(): HtmlElement = this

    override fun lastElementSibling(): HtmlElement = this

    override fun elementSiblingIndex(): Int = 0

    override fun getElementsByAttribute(key: String): HtmlElements =
        IosSnapshotHtmlElements(emptyList())

    override fun getElementsByAttributeStarting(keyPrefix: String): HtmlElements =
        IosSnapshotHtmlElements(emptyList())

    override fun getElementsByAttributeValue(key: String, value: String): HtmlElements =
        IosSnapshotHtmlElements(emptyList())

    override fun getElementsByAttributeValueNot(key: String, value: String): HtmlElements =
        IosSnapshotHtmlElements(emptyList())

    override fun getElementsByAttributeValueStarting(
        key: String,
        valuePrefix: String
    ): HtmlElements = IosSnapshotHtmlElements(emptyList())

    override fun getElementsByAttributeValueEnding(key: String, valueSuffix: String): HtmlElements =
        IosSnapshotHtmlElements(emptyList())

    override fun getElementsByAttributeValueContaining(key: String, match: String): HtmlElements =
        IosSnapshotHtmlElements(emptyList())

    override fun getElementsContainingText(searchText: String): HtmlElements =
        IosSnapshotHtmlElements(emptyList())

    override fun getElementsContainingOwnText(searchText: String): HtmlElements =
        IosSnapshotHtmlElements(emptyList())

    override fun getElementsMatchingText(regex: String): HtmlElements =
        IosSnapshotHtmlElements(emptyList())

    override fun getElementsMatchingOwnText(regex: String): HtmlElements =
        IosSnapshotHtmlElements(emptyList())

    override fun getAllElements(): HtmlElements = IosSnapshotHtmlElements(listOf(this))

    override fun hasClass(className: String): Boolean = false

    override fun addClass(className: String): HtmlElement = this

    override fun removeClass(className: String): HtmlElement = this

    override fun toggleClass(className: String): HtmlElement = this

    override fun value(): String = ""

    override fun value(value: String): HtmlElement = this

    override fun wholeText(): String = ""

    override fun wholeOwnText(): String = ""

    override fun hasText(): Boolean = false

    override fun data(): String = ""

    override fun cssSelector(): String = ""

    override fun matches(cssQuery: String): Boolean = false

    override fun closest(cssQuery: String): HtmlElement? = null

    override fun selectXpath(xpath: String): HtmlElements = IosSnapshotHtmlElements(emptyList())
}