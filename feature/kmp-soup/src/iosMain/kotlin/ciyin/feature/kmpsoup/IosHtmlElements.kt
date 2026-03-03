package ciyin.feature.kmpsoup

import ciyin.feature.kmpsoup.bridge.KmpSoupBridgeElement
import ciyin.feature.kmpsoup.bridge.KmpSoupBridgeElements

/**
 * 基于 SwiftSoup 桥接集合的包装实现。
 *
 * @property delegate iOS 桥接集合对象。
 */
internal class IosHtmlElements(
    private val delegate: KmpSoupBridgeElements?,
) : HtmlElements {

    /**
     * 当前集合元素数量。
     */
    override val size: Int
        get() = safeInt {
            delegate?.size()?.toInt() ?: 0
        }

    /**
     * 判断集合是否为空。
     */
    override fun isEmpty(): Boolean = size == 0

    /**
     * 按索引获取元素。
     */
    override fun get(index: Int): HtmlElement {
        if (index !in 0..<size) {
            throw IndexOutOfBoundsException("index=$index, size=$size")
        }
        return safeElement {
            delegate?.elementAt(index.toLong())
        } ?: throw IndexOutOfBoundsException("index=$index, size=$size")
    }

    /**
     * 拼接所有元素文本。
     */
    override fun text(): String = safeString {
        delegate?.text()
    }

    /**
     * 获取第一个元素的属性值。
     */
    override fun attr(name: String): String = safeString {
        delegate?.attr(name)
    }

    override fun hasAttr(name: String): Boolean = asSnapshot().hasAttr(name)

    override fun eachAttr(name: String): List<String> = asSnapshot().eachAttr(name)

    override fun attr(name: String, value: String): HtmlElements {
        forEachBridgeElement { element ->
            element.setAttrName(name, value)
        }
        return this
    }

    override fun removeAttr(name: String): HtmlElements {
        forEachBridgeElement { element ->
            element.removeAttrName(name)
        }
        return this
    }

    override fun addClass(className: String): HtmlElements {
        forEachBridgeElement { element ->
            element.addClassName(className)
        }
        return this
    }

    override fun removeClass(className: String): HtmlElements {
        forEachBridgeElement { element ->
            element.removeClassName(className)
        }
        return this
    }

    override fun toggleClass(className: String): HtmlElements {
        forEachBridgeElement { element ->
            element.toggleClassName(className)
        }
        return this
    }

    override fun hasClass(className: String): Boolean = asSnapshot().hasClass(className)

    override fun value(): String {
        val first = firstOrNull() as? IosHtmlElement ?: return ""
        return safeString { first.delegate.value() }
    }

    override fun value(value: String): HtmlElements {
        forEachBridgeElement { element ->
            element.setValue(value)
        }
        return this
    }

    override fun hasText(): Boolean = asSnapshot().hasText()

    override fun eachText(): List<String> = asSnapshot().eachText()

    override fun html(): String = asSnapshot().html()

    override fun tagName(tagName: String): HtmlElements {
        forEachBridgeElement { element ->
            element.setTagName(tagName)
        }
        return this
    }

    override fun html(html: String): HtmlElements {
        forEachBridgeElement { element ->
            element.setHtml(html)
        }
        return this
    }

    override fun prepend(html: String): HtmlElements {
        forEachBridgeElement { element ->
            element.prependHtml(html)
        }
        return this
    }

    override fun append(html: String): HtmlElements {
        forEachBridgeElement { element ->
            element.appendHtml(html)
        }
        return this
    }

    override fun before(html: String): HtmlElements {
        forEachBridgeElement { element ->
            element.beforeHtml(html)
        }
        return this
    }

    override fun after(html: String): HtmlElements {
        forEachBridgeElement { element ->
            element.afterHtml(html)
        }
        return this
    }

    override fun wrap(html: String): HtmlElements {
        forEachBridgeElement { element ->
            element.wrapHtml(html)
        }
        return this
    }

    override fun unwrap(): HtmlElements {
        forEachBridgeElement { element ->
            element.unwrapNode()
        }
        return this
    }

    override fun empty(): HtmlElements {
        forEachBridgeElement { element ->
            element.emptyNode()
        }
        return this
    }

    override fun remove(): HtmlElements {
        forEachBridgeElement { element ->
            element.removeNode()
        }
        return this
    }

    override fun select(css: String): HtmlElements = asSnapshot().select(css)

    override fun selectFirst(css: String): HtmlElement? = asSnapshot().selectFirst(css)

    override fun expectFirst(css: String): HtmlElement = asSnapshot().expectFirst(css)

    override fun not(css: String): HtmlElements = asSnapshot().not(css)

    override fun eq(index: Int): HtmlElements = asSnapshot().eq(index)

    override fun matches(css: String): Boolean = asSnapshot().matches(css)

    override fun next(): HtmlElements = asSnapshot().next()

    override fun next(css: String): HtmlElements = asSnapshot().next(css)

    override fun nextAll(): HtmlElements = asSnapshot().nextAll()

    override fun nextAll(css: String): HtmlElements = asSnapshot().nextAll(css)

    override fun prev(): HtmlElements = asSnapshot().prev()

    override fun prev(css: String): HtmlElements = asSnapshot().prev(css)

    override fun prevAll(): HtmlElements = asSnapshot().prevAll()

    override fun prevAll(css: String): HtmlElements = asSnapshot().prevAll(css)

    override fun parents(): HtmlElements = asSnapshot().parents()

    override fun forms(): List<FormElement> {
        return snapshotList().mapNotNull { element ->
            when (element) {
                is IosFormElement -> element
                is IosHtmlElement ->
                    if (safeString { element.tagName() }.equals("form", ignoreCase = true)) {
                        IosFormElement(element.delegate)
                    } else {
                        null
                    }

                else -> null
            }
        }
    }

    override fun iterator(): Iterator<HtmlElement> = snapshotList().iterator()

    override fun contains(element: HtmlElement): Boolean = snapshotList().contains(element)

    override fun containsAll(elements: Collection<HtmlElement>): Boolean =
        snapshotList().containsAll(elements)

    override fun indexOf(element: HtmlElement): Int = snapshotList().indexOf(element)

    override fun lastIndexOf(element: HtmlElement): Int = snapshotList().lastIndexOf(element)

    override fun listIterator(): ListIterator<HtmlElement> = snapshotList().listIterator()

    override fun listIterator(index: Int): ListIterator<HtmlElement> =
        snapshotList().listIterator(index)

    override fun subList(fromIndex: Int, toIndex: Int): List<HtmlElement> =
        snapshotList().subList(fromIndex, toIndex)

    private fun snapshotList(): List<HtmlElement> {
        if (delegate == null) {
            return emptyList()
        }
        val values = ArrayList<HtmlElement>(size)
        var currentIndex = 0
        while (currentIndex < size) {
            val element = safeElement {
                delegate.elementAt(currentIndex.toLong())
            }
            if (element != null) {
                values += element
            }
            currentIndex += 1
        }
        return values
    }

    private fun forEachBridgeElement(action: (KmpSoupBridgeElement) -> Unit) {
        snapshotList().forEach { element ->
            val bridgeElement = (element as? IosHtmlElement)?.delegate ?: return@forEach
            safeUnit {
                action(bridgeElement)
            }
        }
    }

    private fun asSnapshot(): IosSnapshotHtmlElements = IosSnapshotHtmlElements(snapshotList())
}
