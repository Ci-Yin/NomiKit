package ciyin.feature.kmpsoup

/**
 * iOS 平台的文档类型（DOCTYPE）实现。
 *
 * @property nameValue 文档类型名称。
 * @property publicIdValue 文档 PublicId。
 * @property systemIdValue 文档 SystemId。
 * @property pubSysKeyValue 文档 pubSysKey（PUBLIC 或 SYSTEM）。
 */
internal class IosDocumentType(
    private var nameValue: String,
    private var publicIdValue: String,
    private var systemIdValue: String,
    private var pubSysKeyValue: String? = null,
) : DocumentType {

    /**
     * 获取文档类型名称。
     *
     * @return 文档类型名称。
     */
    override fun name(): String = nameValue

    /**
     * 获取 PublicId。
     *
     * @return PublicId。
     */
    override fun publicId(): String = publicIdValue

    /**
     * 获取 SystemId。
     *
     * @return SystemId。
     */
    override fun systemId(): String = systemIdValue

    /**
     * 获取节点名称。
     *
     * @return 固定返回 `#doctype`。
     */
    override fun nodeName(): String = "#doctype"

    /**
     * 设置 pubSysKey。
     *
     * @param value 新的 pubSysKey。
     */
    override fun setPubSysKey(value: String?) {
        pubSysKeyValue = value
    }
}
