package ciyin.feature.kmpsoup

import ciyin.feature.kmpsoup.bridge.KmpSoupBridgeDocument
import ciyin.feature.kmpsoup.bridge.KmpSoupBridgeElement
import ciyin.feature.kmpsoup.bridge.KmpSoupBridgeElements

/**
 * 将桥接文档包装为统一文档模型。
 *
 * @return KMP 层文档对象。
 */
internal fun KmpSoupBridgeDocument.toHtmlDocument(): HtmlDocument = IosHtmlDocument(this)

/**
 * 将桥接元素包装为统一元素模型。
 *
 * @return KMP 层元素对象。
 */
internal fun KmpSoupBridgeElement.toHtmlElement(): HtmlElement = IosHtmlElement(this)

/**
 * 将桥接元素包装为表单元素模型。
 *
 * @return KMP 层表单元素对象。
 */
internal fun KmpSoupBridgeElement.toFormElement(): FormElement = IosFormElement(this)

/**
 * 将桥接集合包装为统一集合模型。
 *
 * @return KMP 层元素集合对象。
 */
internal fun KmpSoupBridgeElements?.toHtmlElements(): HtmlElements = IosHtmlElements(this)

/**
 * 安全执行桥接集合查询。
 *
 * @param block 查询逻辑。
 * @return 统一集合包装，失败时为空集合。
 */
internal inline fun safeElements(block: () -> KmpSoupBridgeElements?): HtmlElements {
    return try {
        IosHtmlElements(block())
    } catch (_: Throwable) {
        IosHtmlElements(null)
    }
}

/**
 * 安全执行桥接单元素查询。
 *
 * @param block 查询逻辑。
 * @return 统一元素包装，失败时返回 `null`。
 */
internal inline fun safeElement(block: () -> KmpSoupBridgeElement?): HtmlElement? {
    return try {
        block()?.toHtmlElement()
    } catch (_: Throwable) {
        null
    }
}

/**
 * 安全执行桥接表单元素查询。
 *
 * @param block 查询逻辑。
 * @return 表单元素包装，失败时返回 `null`。
 */
internal inline fun safeFormElement(block: () -> KmpSoupBridgeElement?): FormElement? {
    return try {
        block()?.toFormElement()
    } catch (_: Throwable) {
        null
    }
}

/**
 * 安全获取字符串结果。
 *
 * @param block 字符串计算逻辑。
 * @return 成功返回结果，失败返回空字符串。
 */
internal inline fun safeString(block: () -> String?): String {
    return try {
        block().orEmpty()
    } catch (_: Throwable) {
        ""
    }
}

/**
 * 安全获取整数结果。
 *
 * @param block 整数计算逻辑。
 * @return 成功返回结果，失败返回 `0`。
 */
internal inline fun safeInt(block: () -> Int): Int {
    return try {
        block()
    } catch (_: Throwable) {
        0
    }
}

/**
 * 安全获取布尔结果。
 *
 * @param block 布尔计算逻辑。
 * @return 成功返回结果，失败返回 `false`。
 */
internal inline fun safeBoolean(block: () -> Boolean): Boolean {
    return try {
        block()
    } catch (_: Throwable) {
        false
    }
}

/**
 * 安全执行无返回值的桥接调用。
 *
 * @param block 调用逻辑。
 */
internal inline fun safeUnit(block: () -> Unit) {
    try {
        block()
    } catch (_: Throwable) {
    }
}

/**
 * 将 class 字符串标准化为去重集合。
 *
 * @param raw 原始 class 字符串。
 * @return 去重后的 class 名集合。
 */
internal fun normalizeClassNames(raw: String): Set<String> {
    if (raw.isBlank()) {
        return emptySet()
    }
    return raw
        .split(' ', '\t', '\n', '\r')
        .map { value -> value.trim() }
        .filter { value -> value.isNotEmpty() }
        .toSet()
}

/**
 * 从文档 HTML 文本中解析 doctype。
 *
 * @param html 文档外层 HTML。
 * @return 解析得到的 doctype，未匹配时返回 `null`。
 */
internal fun parseDocumentTypeFromHtml(html: String): DocumentType? {
    val declaration = Regex("<!DOCTYPE\\s+([^>]+)>", RegexOption.IGNORE_CASE)
        .find(html)
        ?.groupValues
        ?.getOrNull(1)
        ?.trim()
        ?: return null

    if (declaration.isBlank()) {
        return null
    }

    val name = declaration
        .split(Regex("\\s+"), limit = 2)
        .firstOrNull()
        .orEmpty()
        .trim('"', '\'')

    if (name.isBlank()) {
        return null
    }

    val quotedValues = Regex("\"([^\"]*)\"|'([^']*)'")
        .findAll(declaration)
        .map { match ->
            match.groups[1]?.value ?: match.groups[2]?.value.orEmpty()
        }
        .toList()

    val upper = declaration.uppercase()
    val (publicId, systemId, pubSysKey) = when {
        "PUBLIC" in upper -> Triple(
            quotedValues.getOrElse(0) { "" },
            quotedValues.getOrElse(1) { "" },
            "PUBLIC",
        )

        "SYSTEM" in upper -> Triple(
            "",
            quotedValues.getOrElse(0) { "" },
            "SYSTEM",
        )

        else -> Triple("", "", null)
    }

    return IosDocumentType(
        nameValue = name,
        publicIdValue = publicId,
        systemIdValue = systemId,
        pubSysKeyValue = pubSysKey,
    )
}
