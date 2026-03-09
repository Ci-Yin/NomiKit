package ciyin.parser.site

import ciyin.parser.model.Tag
import ciyin.serialization.json.fromJson
import com.fleeksoft.ksoup.nodes.Element
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

internal interface HanimeBaseElement {

    fun List<Tag>.formatTags(): String {
        return joinToString(",").replace("_", "%20")
    }

    fun Element.context(cssQuery: String = "script[type=application/ld+json]"): HanimeContext {
        return selectFirst(cssQuery)
            ?.html()
            ?.fromJson<HanimeContext>()
            ?: HanimeContext()
    }


    /**
     * 提取最后一页的页码
     */
    fun Element.totalPages(cssQuery: String = "ul.pagination li.page-item a.page-link"): Int {
        return select(cssQuery)
            .mapNotNull { it.text().toIntOrNull() }
            .maxOrNull() ?: 1
    }

    /**
     * 提取视频标题
     */
    fun Element.title(cssQuery: String = "div.card-mobile-title"): String {
        return select(cssQuery).text().trim()
    }

    /**
     * 提取视频时长并转换为 Long 类型（单位：秒）
     */
    fun Element.duration(cssQuery: String = "div.card-mobile-duration"): Long {
        val durationText = select(cssQuery)
            .firstOrNull()?.text() ?: "00:00"
        val durationParts = durationText.split(":").map { it.toLongOrNull() ?: 0 }
        return if (durationParts.size == 2) {
            durationParts[0] * 60 + durationParts[1]
        } else {
            0L
        }
    }

    /**
     * 提取视频播放次数并转换为 Long 类型
     */
    fun Element.count(cssQuery: String = "div.card-mobile-duration"): Long {
        val viewsText = select(cssQuery).lastOrNull()?.text() ?: "0"
        return if (viewsText.contains("萬")) {
            viewsText.replace("萬次", "").toDoubleOrNull()?.times(10_000)?.toLong() ?: 0L
        } else {
            viewsText.replace("次", "").toLongOrNull() ?: 0L
        }

    }

}

@Serializable
internal data class HanimeTag(
    val artistList: ArrayList<String> = arrayListOf(),
    val characterList: ArrayList<String> = arrayListOf(),
    val copyrightList: ArrayList<String> = arrayListOf(),
    val generalList: ArrayList<String> = arrayListOf(),
    val languageList: ArrayList<String> = arrayListOf(),
    val categoriesList: ArrayList<String> = arrayListOf(),
    var count: Int = 1,
)

@Serializable
internal data class HanimeContext(
    @SerialName("@context")
    val context: String = "",
    @SerialName("description")
    val description: String = "",
    @SerialName("interactionStatistic")
    val interactionStatistic: InteractionStatistic = InteractionStatistic(),
    @SerialName("name")
    val name: String = "",
    @SerialName("thumbnailUrl")
    val thumbnailUrl: List<String> = emptyList(),
    @SerialName("contentUrl")
    val contentUrl: String = "",
    @SerialName("@type")
    val type: String = "",
    @SerialName("uploadDate")
    val uploadDate: String = "",
)

@Serializable
internal data class InteractionStatistic(
    @SerialName("interactionType")
    val interactionType: InteractionType = InteractionType(),
    @SerialName("@type")
    val type: String = "",
    @SerialName("userInteractionCount")
    val userInteractionCount: Long = 0L,
)

@Serializable
internal data class InteractionType(
    @SerialName("@type")
    val type: String = "",
)