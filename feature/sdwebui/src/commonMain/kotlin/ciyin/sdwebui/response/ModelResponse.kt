package ciyin.sdwebui.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * `sdapi/v1/sd-models` 中的单个 checkpoint 描述。
 *
 * @property title 模型显示标题。
 * @property name 模型名称。
 * @property hash 可空短哈希；服务端尚未计算或未提供时为 `null`。
 * @property sha256 可空完整哈希；服务端尚未计算或未提供时为 `null`。
 * @property filename 服务端模型文件路径。
 * @property config 可空模型配置文件路径。
 */
@Serializable
data class ModelResponse(
    @SerialName("title") val title: String,
    @SerialName("model_name") val name: String,
    @SerialName("hash") val hash: String? = null,
    @SerialName("sha256") val sha256: String? = null,
    @SerialName("filename") val filename: String,
    @SerialName("config") val config: String?,
)
