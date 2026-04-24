package ciyin.sdwebui.process

import ciyin.sdwebui.SdWebUi
import ciyin.sdwebui.payload.script.ScriptPayload
import ciyin.sdwebui.response.ExtraBatchImagesResponse
import ciyin.sdwebui.response.ExtraSingleImageResponse
import ciyin.sdwebui.response.GenerateProcessResponse
import ciyin.sdwebui.response.RemBGResponse

interface Process {

    interface Builder {

        fun addAlwaysonScript(key: String, payload: ScriptPayload)
    }

    companion object {

        suspend fun SdWebUi.runText2Image(
            init: Text2Image.Builder.() -> Unit,
        ): Result<GenerateProcessResponse> {
            val builder = Text2Image.Builder(stableDiffusion)
            builder.init()
            return builder.build().run()
        }

        suspend fun SdWebUi.runImage2Image(
            init: Image2Image.Builder.() -> Unit,
        ): Result<GenerateProcessResponse> {
            val builder = Image2Image.Builder(stableDiffusion)
            builder.init()
            return builder.build().run()
        }

        suspend fun SdWebUi.runExtraSingleImage(
            init: ExtraSingleImage.Builder.() -> Unit,
        ): Result<ExtraSingleImageResponse> {
            val builder = ExtraSingleImage.Builder(stableDiffusion)
            builder.init()
            return builder.build().run()
        }

        suspend fun SdWebUi.runExtraBatchImages(
            init: ExtraBatchImages.Builder.() -> Unit,
        ): Result<ExtraBatchImagesResponse> {
            val builder = ExtraBatchImages.Builder(stableDiffusion)
            builder.init()
            return builder.build().run()
        }

        suspend fun SdWebUi.runRemBG(
            init: RemBG.Builder.() -> Unit
        ): Result<RemBGResponse> {
            val builder = RemBG.Builder(stableDiffusion)
            builder.init()
            return builder.build().run()
        }

        fun SdWebUi.text2Image() = Text2Image.Builder(stableDiffusion)

        fun SdWebUi.image2Image() = Image2Image.Builder(stableDiffusion)

        fun SdWebUi.extraSingleImage() = ExtraSingleImage.Builder(stableDiffusion)

        fun SdWebUi.extraBatchImages() = ExtraBatchImages.Builder(stableDiffusion)
    }
}
