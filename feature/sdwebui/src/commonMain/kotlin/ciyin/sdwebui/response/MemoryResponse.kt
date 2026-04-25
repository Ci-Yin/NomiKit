package ciyin.sdwebui.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * `sdapi/v1/memory` 返回的系统 RAM 与 CUDA 显存统计。
 */
@Serializable
data class MemoryResponse(
   @SerialName("ram") val ram: Ram,
   @SerialName("cuda") val cuda: Cuda,
) {

    /**
     * 主机内存占用（单位与 WebUI 返回一致）。
     */
    @Serializable
    data class Ram(
        @SerialName("free") val free: Double?,
        @SerialName("used") val used: Double?,
        @SerialName("total") val total: Double?,
        @SerialName("error") val error: String?,
    )

    /**
     * GPU 显存占用（若不可用则 [error] 有说明）。
     */
    @Serializable
    data class Cuda(
        @SerialName("free") val free: Double?,
        @SerialName("used") val used: Double?,
        @SerialName("total") val total: Double?,
        @SerialName("error") val error: String?,
    )
}