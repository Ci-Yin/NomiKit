package ciyin.parser.core

/**
 * 站点身份枚举。
 */
interface ParserId {
    val site: String
}

internal data object EmptyParserId : ParserId {
    override val site: String = "empty"
}