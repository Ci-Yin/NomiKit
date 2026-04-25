package ciyin.sdwebui.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * `sdapi/v1/extensions` 返回的已安装扩展元数据。
 */
@Serializable
data class ExtensionResponse(
    @SerialName("name") val name: String,
    @SerialName("remote") val remote: String?,
    @SerialName("branch") val branch: String?,
    @SerialName("commit_hash") val commitHash: String?,
    @SerialName("version") val version: String?,
    @SerialName("commit_date") val commitDate: String?,
    @SerialName("enabled") val enabled: Boolean,
)
