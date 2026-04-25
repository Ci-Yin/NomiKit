package ciyin.sdwebui.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * `extra-single-image` 接口返回的 HTML 提示与结果图 Base64。
 */
@Serializable
data class ExtraSingleImageResponse(
    @SerialName("html_info") val htmlInfo: String,
    @SerialName("image") val image: String,
)
