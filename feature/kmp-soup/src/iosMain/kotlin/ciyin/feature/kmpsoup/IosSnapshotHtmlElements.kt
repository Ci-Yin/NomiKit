package ciyin.feature.kmpsoup

/**
 * 基于内存快照的元素集合实现。
 *
 * @property values 快照元素列表。
 */
internal class IosSnapshotHtmlElements(
    internal val values: List<HtmlElement>,
) : HtmlElements, List<HtmlElement> by values {

    /**
     * 拼接所有元素文本。
     */
    override fun text(): String {
        return values
            .map { element ->
                safeString {
                    element.text()
                }
            }
            .filter { text -> text.isNotEmpty() }
            .joinToString(separator = " ")
    }

    /**
     * 获取第一个元素的属性值。
     */
    override fun attr(name: String): String {
        val first = values.firstOrNull() ?: return ""
        return safeString {
            first.attr(name)
        }
    }

    /**
     * 返回集合迭代器。
     */
    override fun iterator(): Iterator<HtmlElement> = values.iterator()

    override fun hasAttr(name: String): Boolean =
        values.any { safeBoolean { it.hasAttr(name) } }

    override fun eachAttr(name: String): List<String> =
        values.mapNotNull { element ->
            val value = safeString { element.attr(name) }
            value.takeIf { it.isNotEmpty() }
        }

    override fun attr(name: String, value: String): HtmlElements {
        values.forEach { element ->
            val bridge = (element as? IosHtmlElement)?.delegate ?: return@forEach
            safeUnit { bridge.setAttrName(name, value) }
        }
        return this
    }

    override fun removeAttr(name: String): HtmlElements {
        values.forEach { element ->
            val bridge = (element as? IosHtmlElement)?.delegate ?: return@forEach
            safeUnit { bridge.removeAttrName(name) }
        }
        return this
    }

    override fun addClass(className: String): HtmlElements {
        values.forEach { element ->
            safeUnit { element.addClass(className) }
        }
        return this
    }

    override fun removeClass(className: String): HtmlElements {
        values.forEach { element ->
            safeUnit { element.removeClass(className) }
        }
        return this
    }

    override fun toggleClass(className: String): HtmlElements {
        values.forEach { element ->
            safeUnit { element.toggleClass(className) }
        }
        return this
    }

    override fun hasClass(className: String): Boolean =
        values.any { element -> safeBoolean { element.hasClass(className) } }

    override fun value(): String {
        val first = values.firstOrNull() ?: return ""
        return safeString { first.value() }
    }

    override fun value(value: String): HtmlElements {
        values.forEach { element ->
            safeUnit { element.value(value) }
        }
        return this
    }

    override fun hasText(): Boolean =
        values.any { safeBoolean { it.hasText() } }

    override fun eachText(): List<String> =
        values.map { safeString { it.text() } }
            .filter { it.isNotEmpty() }

    override fun html(): String =
        values.joinToString(separator = "\n") { safeString { it.html() } }

    override fun tagName(tagName: String): HtmlElements {
        values.forEach { element ->
            val bridge = (element as? IosHtmlElement)?.delegate ?: return@forEach
            safeUnit { bridge.setTagName(tagName) }
        }
        return this
    }

    override fun html(html: String): HtmlElements {
        values.forEach { element ->
            val bridge = (element as? IosHtmlElement)?.delegate ?: return@forEach
            safeUnit { bridge.setHtml(html) }
        }
        return this
    }

    override fun prepend(html: String): HtmlElements {
        values.forEach { element ->
            val bridge = (element as? IosHtmlElement)?.delegate ?: return@forEach
            safeUnit { bridge.prependHtml(html) }
        }
        return this
    }

    override fun append(html: String): HtmlElements {
        values.forEach { element ->
            val bridge = (element as? IosHtmlElement)?.delegate ?: return@forEach
            safeUnit { bridge.appendHtml(html) }
        }
        return this
    }

    override fun before(html: String): HtmlElements {
        values.forEach { element ->
            val bridge = (element as? IosHtmlElement)?.delegate ?: return@forEach
            safeUnit { bridge.beforeHtml(html) }
        }
        return this
    }

    override fun after(html: String): HtmlElements {
        values.forEach { element ->
            val bridge = (element as? IosHtmlElement)?.delegate ?: return@forEach
            safeUnit { bridge.afterHtml(html) }
        }
        return this
    }

    override fun wrap(html: String): HtmlElements {
        values.forEach { element ->
            val bridge = (element as? IosHtmlElement)?.delegate ?: return@forEach
            safeUnit { bridge.wrapHtml(html) }
        }
        return this
    }

    override fun unwrap(): HtmlElements {
        values.forEach { element ->
            val bridge = (element as? IosHtmlElement)?.delegate ?: return@forEach
            safeUnit { bridge.unwrapNode() }
        }
        return this
    }

    override fun empty(): HtmlElements {
        values.forEach { element ->
            val bridge = (element as? IosHtmlElement)?.delegate ?: return@forEach
            safeUnit { bridge.emptyNode() }
        }
        return this
    }

    override fun remove(): HtmlElements {
        values.forEach { element ->
            val bridge = (element as? IosHtmlElement)?.delegate ?: return@forEach
            safeUnit { bridge.removeNode() }
        }
        return this
    }

    override fun select(css: String): HtmlElements {
        val collected = values.flatMap { element ->
            safeElements { (element as? IosHtmlElement)?.delegate?.select(css) }.toList()
        }
        return IosSnapshotHtmlElements(collected)
    }

    override fun selectFirst(css: String): HtmlElement? =
        values.firstNotNullOfOrNull { it.selectFirst(css) }

    override fun expectFirst(css: String): HtmlElement =
        selectFirst(css)
            ?: throw IllegalArgumentException("未找到匹配 CSS [$css] 的元素。")

    override fun not(css: String): HtmlElements =
        IosSnapshotHtmlElements(values.filterNot { it.matches(css) })

    override fun eq(index: Int): HtmlElements {
        if (index < 0 || index >= values.size) {
            return IosSnapshotHtmlElements(emptyList())
        }
        return IosSnapshotHtmlElements(listOf(values[index]))
    }

    override fun matches(css: String): Boolean =
        values.any { it.matches(css) }

    override fun next(): HtmlElements =
        IosSnapshotHtmlElements(values.mapNotNull { it.nextElementSiblings().firstOrNull() })

    override fun next(css: String): HtmlElements =
        IosSnapshotHtmlElements(values.mapNotNull { element ->
            element.nextElementSiblings().firstOrNull { it.matches(css) }
        })

    override fun nextAll(): HtmlElements =
        IosSnapshotHtmlElements(values.flatMap { it.nextElementSiblings() })

    override fun nextAll(css: String): HtmlElements =
        IosSnapshotHtmlElements(values.flatMap { element ->
            element.nextElementSiblings().filter { it.matches(css) }
        })

    override fun prev(): HtmlElements =
        IosSnapshotHtmlElements(values.mapNotNull { it.previousElementSiblings().lastOrNull() })

    override fun prev(css: String): HtmlElements =
        IosSnapshotHtmlElements(values.mapNotNull { element ->
            element.previousElementSiblings().lastOrNull { it.matches(css) }
        })

    override fun prevAll(): HtmlElements =
        IosSnapshotHtmlElements(values.flatMap { it.previousElementSiblings() })

    override fun prevAll(css: String): HtmlElements =
        IosSnapshotHtmlElements(values.flatMap { element ->
            element.previousElementSiblings().filter { it.matches(css) }
        })

    override fun parents(): HtmlElements =
        IosSnapshotHtmlElements(values.flatMap { it.parents() })

    override fun forms(): List<FormElement> =
        values.mapNotNull { element ->
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
