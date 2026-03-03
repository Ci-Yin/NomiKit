package ciyin.feature.kmpsoup

/**
 * 跨平台文档类型（DOCTYPE）抽象。
 *
 * 对应 Jsoup `DocumentType`，仅保留与解析结果相关的必要字段。
 */
interface DocumentType {

    /**
     * 获取文档类型名称。
     *
     * 例如 HTML 文档通常为 `"html"`。
     *
     * @return 文档类型名称，未知时返回空字符串。
     */
    fun name(): String

    /**
     * 获取 PublicId。
     *
     * @return PublicId，未设置时返回空字符串。
     */
    fun publicId(): String

    /**
     * 获取 SystemId。
     *
     * @return SystemId，未设置时返回空字符串。
     */
    fun systemId(): String

    /**
     * 获取该 DOCTYPE 节点的内部名称。
     *
     * 对应 Jsoup `DocumentType.nodeName()`，HTML 中通常为 `#doctype`。
     *
     * @return 节点名称。
     */
    fun nodeName(): String

    /**
     * 设置内部的 `pubSysKey` 字段（PUBLIC 或 SYSTEM）。
     *
     * 对应 Jsoup `DocumentType.setPubSysKey(String)`。
     *
     * @param value 新的 pubSysKey 值；传入 `null` 时实现可选择忽略。
     */
    fun setPubSysKey(value: String?)
}

