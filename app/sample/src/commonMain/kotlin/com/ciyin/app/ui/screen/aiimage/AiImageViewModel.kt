package com.ciyin.app.ui.screen.aiimage

import ciyin.ai.core.error.AiEngineError
import ciyin.ai.core.image.ImageEvent
import ciyin.ui.foundation.viewmodel.StateMachineMviViewModel
import com.ciyin.app.ui.screen.aiimage.data.AiImageRepository
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
 * 基于 [StateMachineMviViewModel] 与 FlowRedux2：用户操作走 [AiImageAction]；
 * 生成过程中由内部协程将 [ciyin.ai.core.image.ImageEvent] 回灌为 [AiImageAction]，保证每个 `on<...>` 内至多一次
 * `mutate` / `override` / `noChange`。
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class AiImageViewModel(
    private val repository: AiImageRepository = AiImageRepository(),
) : StateMachineMviViewModel<AiImageUiState, AiImageAction, AiImageEffect>() {

    override fun FlowReduxStateMachineFactory<AiImageUiState, AiImageAction>.initialize() {
        initializeWith { AiImageUiState() }
    }

    override fun FlowReduxBuilder<AiImageUiState, AiImageAction>.spec() {
        inState<AiImageUiState> {

            // 进入页面时从 DataStore 读取偏好并派发 PrefsLoaded
            onEnterEffect {
                val prefs = repository.loadPreferences()
                dispatchAction(AiImageAction.PrefsLoaded(prefs))
            }

            // 合并 DataStore 中的主机与提示词
            on<AiImageAction.PrefsLoaded> { action ->
                mutate {
                    copy(
                        serverHost = action.prefs.serverHost,
                        prompt = action.prefs.prompt,
                    )
                }
            }

            // 返回上一页（副作用导航）
            onActionEffect<AiImageAction.BackClick> {
                poseEffect(AiImageEffect.NavigateBack)
            }

            // 更新 SD WebUI 服务地址并异步写回偏好
            on<AiImageAction.ServerHostChange> { action ->
                mutate {
                    copy(serverHost = action.value).apply { persistDemoInput(this) }
                }
            }

            // 更新文生图提示词并异步写回偏好
            on<AiImageAction.PromptChange> { action ->
                mutate {
                    copy(prompt = action.value).apply { persistDemoInput(this) }
                }
            }

            // 校验提示词后启动后台生成并进入加载态
            on<AiImageAction.GenerateClick> {
                val trimmed = snapshot.prompt.trim()
                when {
                    snapshot.isLoading -> noChange()
                    trimmed.isEmpty() -> mutate { copy(errorMessage = "请输入提示词") }
                    else -> {
                        launchDemoImageGeneration(prompt = trimmed)
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

            // 回灌生成进度与阶段说明
            on<AiImageAction.GenerationProgress> { action ->
                mutate {
                    copy(
                        progress = action.progress,
                        progressMessage = action.message,
                    )
                }
            }

            // 回灌中间预览图
            on<AiImageAction.GenerationPreview> { action ->
                mutate {
                    copy(
                        resultBytes = action.bytes.copyOf(),
                        resultMimeType = action.mimeType,
                    )
                }
            }

            // 生成成功：展示最终图像并清除加载态
            on<AiImageAction.GenerationCompleted> { action ->
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

            // 完成事件但无图像数据
            on<AiImageAction.GenerationCompletedEmpty> {
                mutate {
                    copy(
                        isLoading = false,
                        progress = null,
                        progressMessage = null,
                        errorMessage = "未返回图像数据",
                    )
                }
            }

            // 生成失败：展示错误信息
            on<AiImageAction.GenerationFailed> { action ->
                mutate {
                    copy(
                        isLoading = false,
                        progress = null,
                        progressMessage = null,
                        errorMessage = action.message,
                    )
                }
            }

            // 协程取消等：结束加载态不保留错误图
            on<AiImageAction.GenerationDismissed> {
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
     * 在后台收集 [ciyin.ai.integrat.image.AiImageIntegrat] 的 `generate` 事件流，并回灌为 [AiImageAction]。
     */
    private fun launchDemoImageGeneration(prompt: String) {
        backgroundScope.launch(Dispatchers.IO) {
            try {
                repository.generate(prompt).collect { event ->
                    logger.d { "ImageEvent: $event" }
                    dispatchImageEvent(event)
                }
            } catch (e: CancellationException) {
                dispatchAction(AiImageAction.GenerationDismissed)
                throw e
            } catch (e: Throwable) {
                dispatchAction(AiImageAction.GenerationFailed(e.message ?: "未知错误"))
            }
        }
    }

    /** 将单次 [ciyin.ai.core.image.ImageEvent] 映射为对应的回灌 [AiImageAction]。 */
    private fun dispatchImageEvent(event: ImageEvent) {
        when (event) {
            ImageEvent.Started -> Unit
            is ImageEvent.Progress -> {
                dispatchAction(
                    AiImageAction.GenerationProgress(
                        progress = event.progress,
                        message = event.message,
                    ),
                )
            }

            is ImageEvent.Preview -> {
                val img = event.image
                dispatchAction(
                    AiImageAction.GenerationPreview(
                        bytes = img.bytes.copyOf(),
                        mimeType = img.mimeType,
                    ),
                )
            }

            is ImageEvent.Completed -> {
                val first = event.result.images.firstOrNull()
                if (first != null) {
                    dispatchAction(
                        AiImageAction.GenerationCompleted(
                            bytes = first.bytes.copyOf(),
                            mimeType = first.mimeType,
                        ),
                    )
                } else {
                    dispatchAction(AiImageAction.GenerationCompletedEmpty)
                }
            }

            is ImageEvent.Failed -> {
                dispatchAction(AiImageAction.GenerationFailed(event.error.toReadableMessage()))
            }
        }
    }

    private fun persistDemoInput(state: AiImageUiState) {
        backgroundScope.launch {
            repository.persistServerHostAndPrompt(
                serverHost = state.serverHost,
                prompt = state.prompt,
            )
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
