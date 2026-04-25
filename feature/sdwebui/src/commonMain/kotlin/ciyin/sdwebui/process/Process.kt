package ciyin.sdwebui.process

import ciyin.sdwebui.SdWebUi
import ciyin.sdwebui.payload.script.ScriptPayload
import ciyin.sdwebui.response.ExtraBatchImagesResponse
import ciyin.sdwebui.response.ExtraSingleImageResponse
import ciyin.sdwebui.response.GenerateProcessResponse
import ciyin.sdwebui.response.RemBGResponse

/**
 * 标记基于 [ciyin.sdwebui.service.StableDiffusionService] 的可组合生成流程，
 * 并在伴生对象上对 [SdWebUi] 提供一键执行 DSL 的扩展方法。
 */
interface Process {

    /**
     * 允许向 WebUI `alwayson_scripts` 注入扩展脚本负载（如 ControlNet、ADetailer）。
     */
    interface Builder {

        /**
         * 注册一条常驻脚本条目，键名需与 WebUI 扩展约定一致。
         */
        fun addAlwaysonScript(key: String, payload: ScriptPayload)
    }

    companion object {

        /**
         * 构建并执行 [Text2Image] 流程。
         */
        suspend fun SdWebUi.runText2Image(
            init: Text2Image.Builder.() -> Unit,
        ): Result<GenerateProcessResponse> {
            val builder = Text2Image.Builder(stableDiffusion)
            builder.init()
            return builder.build().run()
        }

        /**
         * 构建并执行 [Image2Image] 流程。
         */
        suspend fun SdWebUi.runImage2Image(
            init: Image2Image.Builder.() -> Unit,
        ): Result<GenerateProcessResponse> {
            val builder = Image2Image.Builder(stableDiffusion)
            builder.init()
            return builder.build().run()
        }

        /**
         * 构建并执行 [ExtraSingleImage] 后期处理流程。
         */
        suspend fun SdWebUi.runExtraSingleImage(
            init: ExtraSingleImage.Builder.() -> Unit,
        ): Result<ExtraSingleImageResponse> {
            val builder = ExtraSingleImage.Builder(stableDiffusion)
            builder.init()
            return builder.build().run()
        }

        /**
         * 构建并执行 [ExtraBatchImages] 批量后期处理流程。
         */
        suspend fun SdWebUi.runExtraBatchImages(
            init: ExtraBatchImages.Builder.() -> Unit,
        ): Result<ExtraBatchImagesResponse> {
            val builder = ExtraBatchImages.Builder(stableDiffusion)
            builder.init()
            return builder.build().run()
        }

        /**
         * 构建并执行 [RemBG] 抠图流程。
         */
        suspend fun SdWebUi.runRemBG(
            init: RemBG.Builder.() -> Unit
        ): Result<RemBGResponse> {
            val builder = RemBG.Builder(stableDiffusion)
            builder.init()
            return builder.build().run()
        }

        /**
         * 返回文生图构建器，便于链式配置后自行 [Text2Image.run]。
         */
        fun SdWebUi.text2Image() = Text2Image.Builder(stableDiffusion)

        /**
         * 返回图生图构建器。
         */
        fun SdWebUi.image2Image() = Image2Image.Builder(stableDiffusion)

        /**
         * 返回单张后期处理构建器。
         */
        fun SdWebUi.extraSingleImage() = ExtraSingleImage.Builder(stableDiffusion)

        /**
         * 返回批量后期处理构建器。
         */
        fun SdWebUi.extraBatchImages() = ExtraBatchImages.Builder(stableDiffusion)
    }
}
