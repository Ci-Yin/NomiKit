package ciyin.feature.kmpsoup

import org.jsoup.nodes.DocumentType as JsoupDocumentType

/**
 * 基于 Jsoup 的文档类型（DOCTYPE）包装实现。
 *
 * @property delegate Jsoup 文档类型委托对象。
 */
internal class JsoupDocumentType(
    internal val delegate: JsoupDocumentType,
) : DocumentType {

    /**
     * 获取文档类型名称。
     */
    override fun name(): String = delegate.name()

    /**
     * 获取 PublicId。
     */
    override fun publicId(): String = delegate.publicId()

    /**
     * 获取 SystemId。
     */
    override fun systemId(): String = delegate.systemId()

    /**
     * 获取节点名称。
     */
    override fun nodeName(): String = delegate.nodeName()

    /**
     * 设置 pubSysKey（PUBLIC 或 SYSTEM）。
     */
    override fun setPubSysKey(value: String?) {
        delegate.setPubSysKey(value)
    }
}

