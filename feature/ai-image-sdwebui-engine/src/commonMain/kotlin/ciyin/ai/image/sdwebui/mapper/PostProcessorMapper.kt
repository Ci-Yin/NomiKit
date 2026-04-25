package ciyin.ai.image.sdwebui.mapper

import ciyin.ai.core.image.GeneratedImage
import ciyin.ai.core.image.ImagePostProcessor
import ciyin.ai.core.image.ImageResult
import ciyin.sdwebui.SdWebUi
import ciyin.sdwebui.extension.ADetailer.Companion.aDetailer
import ciyin.sdwebui.extension.ReActor.Companion.reActor
import ciyin.sdwebui.process.Process
import ciyin.sdwebui.process.Process.Companion.runExtraSingleImage
import ciyin.sdwebui.process.Process.Companion.runRemBG
import kotlin.math.roundToInt
import ciyin.sdwebui.extension.ADetailer.Companion.aDetailer as applyADetailer
import ciyin.sdwebui.extension.ReActor.Companion.reActor as applyReActor

/**
 * 把前置型后处理器（ADetailer / ReActor）映射到生成前脚本。
 */
internal fun Process.Builder.applyPreGenerationPostProcessors(
    postProcessors: List<ImagePostProcessor>,
) {
    postProcessors.forEach { processor ->
        when (processor) {
            is ImagePostProcessor.FaceDetailer -> applyADetailer(
                aDetailer {
                    model(processor.model)
                    confidence(processor.confidence)
                },
            )

            is ImagePostProcessor.FaceSwap -> applyReActor(
                reActor {
                    image(processor.sourceFace.toSdWebUiBase64())
                },
            )

            ImagePostProcessor.BackgroundRemoval,
            is ImagePostProcessor.Upscale,
                -> Unit
        }
    }
}

/**
 * 执行生成完成后的后处理流水线（当前支持 RemBG / Upscale）。
 */
internal suspend fun SdWebUi.applyPostGenerationPostProcessors(
    result: ImageResult,
    postProcessors: List<ImagePostProcessor>,
): Result<ImageResult> = runCatching {
    if (postProcessors.isEmpty()) return@runCatching result

    val transformed = result.images.map { image ->
        postProcessors.fold(image) { current, processor ->
            when (processor) {
                is ImagePostProcessor.BackgroundRemoval -> removeBackground(current)
                is ImagePostProcessor.Upscale -> upscale(current, processor)
                is ImagePostProcessor.FaceDetailer,
                is ImagePostProcessor.FaceSwap,
                    -> current
            }
        }
    }
    result.copy(images = transformed)
}

/**
 * 调用 RemBG 对单张图像去背景。
 */
private suspend fun SdWebUi.removeBackground(image: GeneratedImage): GeneratedImage =
    runRemBG {
        inputImage(image.bytes.toSdWebUiBase64())
    }.getOrThrow().toGeneratedImage()

/**
 * 调用 SD WebUI extras 对单张图像执行超分。
 */
private suspend fun SdWebUi.upscale(
    image: GeneratedImage,
    processor: ImagePostProcessor.Upscale,
): GeneratedImage = runExtraSingleImage {
    image(image.bytes.toSdWebUiBase64())
    upscalingResize(processor.factor.roundToInt().coerceAtLeast(1))
    processor.model?.let(::upscaler1)
}.getOrThrow().toGeneratedImage()
