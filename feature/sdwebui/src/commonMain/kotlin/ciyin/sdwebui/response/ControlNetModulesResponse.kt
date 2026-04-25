package ciyin.sdwebui.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * ControlNet 预处理器模块列表及每个模块的滑块元数据。
 */
@Serializable
data class ControlNetModulesResponse(
    @SerialName("module_list") val modules: List<String>,
    @SerialName("module_detail") val details: Map<String, ModuleDetail>,
) {

    /**
     * 单个预处理器的参数约束（是否免模型、滑块范围等）。
     */
    @Serializable
    data class ModuleDetail(
        @SerialName("model_free") val modelFree: Boolean,
        @SerialName("sliders") val sliders: List<Slider?>,
    )

    /**
     * 预处理器 UI 上某一滑块的名称与取值范围。
     */
    @Serializable
    data class Slider(
        @SerialName("name") val name: String?,
        @SerialName("value") val value: Float?,
        @SerialName("min") val min: Float?,
        @SerialName("max") val max: Float?,
    )
}
