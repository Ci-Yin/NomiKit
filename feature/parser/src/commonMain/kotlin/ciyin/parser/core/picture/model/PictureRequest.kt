package ciyin.parser.core.picture.model

import ciyin.parser.core.picture.PictureParserType
import ciyin.parser.model.ParserRequest

/**
 * 图站解析请求模型。
 */
data class PictureRequest(
    override val type: PictureParserType,
    val id: String = "",
    val page: Int = 0,
    val tags: List<String> = emptyList(),
    val search: String = "",
    val date: String = "",
    val scale: String = "",
) : ParserRequest