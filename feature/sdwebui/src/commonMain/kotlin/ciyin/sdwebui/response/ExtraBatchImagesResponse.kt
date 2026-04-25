package ciyin.sdwebui.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * `extra-batch-images` 接口返回的 HTML 提示与多张结果图。
 */
@Serializable
data class ExtraBatchImagesResponse(
    @SerialName("html_info") val htmlInfo: String,
    @SerialName("images") val images: List<String>,
)
