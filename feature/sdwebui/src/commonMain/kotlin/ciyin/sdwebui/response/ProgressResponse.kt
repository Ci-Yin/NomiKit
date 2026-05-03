package ciyin.sdwebui.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * `sdapi/v1/progress` 返回的当前采样进度、预览图与任务状态。
 */
@Serializable
data class ProgressResponse(
    @SerialName("progress") val progress: Float,
    @SerialName("eta_relative") val etaRelative: Float,
    @SerialName("state") val state: State,
    @SerialName("textinfo") val textInfo: String?,
    @SerialName("current_image") val currentImage: String?,
) {

    /**
     * 当前作业是否跳过、中断及采样步数等运行时状态。
     */
    @Serializable
    data class State(
        @SerialName("skipped") val skipped: Boolean,
        @SerialName("interrupted") val interrupted: Boolean,
        @SerialName("job") val job: String,
        @SerialName("job_count") val jobCount: Int,
        @SerialName("job_timestamp") val jobTimestamp: String,
        @SerialName("job_no") val jobNo: Int,
        @SerialName("sampling_step") val samplingStep: Int,
        @SerialName("sampling_steps") val samplingSteps: Int,
    )
}