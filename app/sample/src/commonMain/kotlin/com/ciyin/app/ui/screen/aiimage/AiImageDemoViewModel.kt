package com.ciyin.app.ui.screen.aiimage

import ciyin.ai.core.error.AiEngineError
import ciyin.ai.core.image.ImageEvent
import ciyin.ai.core.image.ImageRequest
import ciyin.ai.core.image.ImageSize
import ciyin.ai.core.image.ImageSource
import ciyin.ai.image.sdwebui.model.buildSdWebUiText2ImageExtras
import ciyin.ui.foundation.viewmodel.StateMachineMviViewModel
import com.freeletics.flowredux2.FlowReduxBuilder
import com.freeletics.flowredux2.FlowReduxStateMachineFactory
import com.freeletics.flowredux2.initializeWith
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

/**
 * 文生图演示页面的 ViewModel。
 *
 * 基于 [StateMachineMviViewModel] 与 FlowRedux2：用户操作走 [AiImageDemoAction]；
 * 生成过程中由内部协程将 [ciyin.ai.core.image.ImageEvent] 回灌为 [AiImageDemoAction]，保证每个 `on<...>` 内至多一次
 * `mutate` / `override` / `noChange`。
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class AiImageDemoViewModel :
    StateMachineMviViewModel<AiImageDemoUiState, AiImageDemoAction, AiImageDemoEffect>() {

    override fun FlowReduxStateMachineFactory<AiImageDemoUiState, AiImageDemoAction>.initialize() {
        initializeWith { AiImageDemoUiState() }
    }

    override fun FlowReduxBuilder<AiImageDemoUiState, AiImageDemoAction>.spec() {
        inState<AiImageDemoUiState> {
            onActionEffect<AiImageDemoAction.BackClick> {
                poseEffect(AiImageDemoEffect.NavigateBack)
            }
            on<AiImageDemoAction.ServerHostChange> { action ->
                mutate { copy(serverHost = action.value) }
            }
            on<AiImageDemoAction.PromptChange> { action ->
                mutate { copy(prompt = action.value) }
            }
            on<AiImageDemoAction.GenerateClick> {
                val trimmed = snapshot.prompt.trim()
                when {
                    snapshot.isLoading -> noChange()
                    trimmed.isEmpty() -> mutate { copy(errorMessage = "请输入提示词") }
                    else -> {
                        launchDemoImageGeneration(host = snapshot.serverHost, prompt = trimmed)
                        mutate {
                            copy(
                                isLoading = true,
                                errorMessage = null,
                                progress = 0f,
                                progressMessage = null,
                            )
                        }
                    }
                }
            }
            on<AiImageDemoAction.GenerationProgress> { action ->
                mutate {
                    copy(
                        progress = action.progress,
                        progressMessage = action.message,
                    )
                }
            }
            on<AiImageDemoAction.GenerationPreview> { action ->
                mutate {
                    copy(
                        resultBytes = action.bytes.copyOf(),
                        resultMimeType = action.mimeType,
                    )
                }
            }
            on<AiImageDemoAction.GenerationCompleted> { action ->
                mutate {
                    copy(
                        isLoading = false,
                        progress = null,
                        progressMessage = null,
                        resultBytes = action.bytes.copyOf(),
                        resultMimeType = action.mimeType,
                        errorMessage = null,
                    )
                }
            }
            on<AiImageDemoAction.GenerationCompletedEmpty> {
                mutate {
                    copy(
                        isLoading = false,
                        progress = null,
                        progressMessage = null,
                        errorMessage = "未返回图像数据",
                    )
                }
            }
            on<AiImageDemoAction.GenerationFailed> { action ->
                mutate {
                    copy(
                        isLoading = false,
                        progress = null,
                        progressMessage = null,
                        errorMessage = action.message,
                    )
                }
            }
            on<AiImageDemoAction.GenerationDismissed> {
                mutate {
                    copy(
                        isLoading = false,
                        progress = null,
                        progressMessage = null,
                    )
                }
            }
        }
    }

    /**
     * 在后台收集 [ciyin.ai.facade.AiImage] 的 `generate` 事件流，并回灌为 [AiImageDemoAction]。
     */
    private fun launchDemoImageGeneration(host: String, prompt: String) {
        backgroundScope.launch(Dispatchers.IO) {
            try {
                val request = ImageRequest(
                    prompt = prompt,
                    source = ImageSource.TextToImage,
                    size = ImageSize(600, 1000),
                    negativePrompt = "mosaic,fellatio,lowres,(bad),missing,worst quality,low quality,watermark,oldest,chromatic aberration,extra digits,artistic error,username,[abstract],",
                    steps = 34,
//                    seed = 146333388,
                    vendorOptions = mapOf(
                        buildSdWebUiText2ImageExtras {
                            copy(
                                samplerName = "Euler a",
//                                hiresFix = SdWebUiText2ImageHiresFix(
//                                    enable = true,
//                                    scale = 2,
//                                    upscaler = "R-ESRGAN 4x+ Anime6B",
//                                    denoisingStrength = 0.4f,
//                                )
                            )
                        }
                    )
                )
                AiImageDemoGraph.aiImage(host).generate(request).collect { event ->
                    logger.d { "ImageEvent: $event" }
                    dispatchImageEvent(event)
                }
            } catch (e: CancellationException) {
                dispatchAction(AiImageDemoAction.GenerationDismissed)
                throw e
            } catch (e: Throwable) {
                dispatchAction(AiImageDemoAction.GenerationFailed(e.message ?: "未知错误"))
            }
        }
    }

    /** 将单次 [ciyin.ai.core.image.ImageEvent] 映射为对应的回灌 [AiImageDemoAction]。 */
    private fun dispatchImageEvent(event: ImageEvent) {
        when (event) {
            ImageEvent.Started -> Unit
            is ImageEvent.Progress -> {
                dispatchAction(
                    AiImageDemoAction.GenerationProgress(
                        progress = event.progress,
                        message = event.message,
                    ),
                )
            }

            is ImageEvent.Preview -> {
                val img = event.image
                dispatchAction(
                    AiImageDemoAction.GenerationPreview(
                        bytes = img.bytes.copyOf(),
                        mimeType = img.mimeType,
                    ),
                )
            }

            is ImageEvent.Completed -> {
                val first = event.result.images.firstOrNull()
                if (first != null) {
                    dispatchAction(
                        AiImageDemoAction.GenerationCompleted(
                            bytes = first.bytes.copyOf(),
                            mimeType = first.mimeType,
                        ),
                    )
                } else {
                    dispatchAction(AiImageDemoAction.GenerationCompletedEmpty)
                }
            }

            is ImageEvent.Failed -> {
                dispatchAction(AiImageDemoAction.GenerationFailed(event.error.toReadableMessage()))
            }
        }
    }

    private fun AiEngineError.toReadableMessage(): String = when (this) {
        is AiEngineError.Network -> message ?: "网络错误"
        is AiEngineError.Unauthorized -> providerMessage ?: "未授权"
        is AiEngineError.RateLimited -> providerMessage ?: "请求过于频繁"
        is AiEngineError.Protocol -> message
        is AiEngineError.Refused -> reason
        is AiEngineError.Unsupported -> message
        AiEngineError.Cancelled -> "已取消"
        is AiEngineError.Unknown -> message ?: cause?.message ?: "未知错误"
    }
}
