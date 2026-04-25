package ciyin.sdwebui.process

import ciyin.sdwebui.payload.RemBGPayload
import ciyin.sdwebui.payload.script.ScriptPayload
import ciyin.sdwebui.response.RemBGResponse
import ciyin.sdwebui.service.StableDiffusionService

/**
 * RemBG 抠图流程：调用 `StableDiffusionService.remBG`。
 */
class RemBG private constructor(
    private val stableDiffusionService: StableDiffusionService,
    private val payload: RemBGPayload,
): Process {

    /**
     * 发起抠图请求并返回处理后的图像。
     */
    suspend fun run(): Result<RemBGResponse> {
        return stableDiffusionService.remBG(payload)
    }

    /**
     * 配置输入图、抠图模型与 alpha matting 等 RemBG 参数。
     */
    class Builder internal constructor(
        private val stableDiffusionService: StableDiffusionService,
    ) : Process.Builder {

        private var inputImage: String = ""
        private var model: String = "u2net"
        private var returnMask: Boolean = false
        private var alphaMatting: Boolean = false
        private var alphaMattingForegroundThreshold: Int = 240
        private var alphaMattingBackgroundThreshold: Int = 10
        private var alphaMattingErodeSize: Int = 10

        fun inputImage(inputImage: String) = apply {
            this.inputImage = inputImage
        }

        fun model(model: String) = apply {
            this.model = model
        }

        fun returnMask(returnMask: Boolean) = apply {
            this.returnMask = returnMask
        }

        fun alphaMatting(alphaMatting: Boolean) = apply {
            this.alphaMatting = alphaMatting
        }

        fun alphaMattingForegroundThreshold(alphaMattingForegroundThreshold: Int) = apply {
            this.alphaMattingForegroundThreshold = alphaMattingForegroundThreshold
        }

        fun alphaMattingBackgroundThreshold(alphaMattingBackgroundThreshold: Int) = apply {
            this.alphaMattingBackgroundThreshold = alphaMattingBackgroundThreshold
        }

        fun alphaMattingErodeSize(alphaMattingErodeSize: Int) = apply {
            this.alphaMattingErodeSize = alphaMattingErodeSize
        }

        override fun addAlwaysonScript(key: String, payload: ScriptPayload) {
            // Do nothing
        }

        /**
         * 根据当前配置创建可执行的 [RemBG]。
         */
        fun build(): RemBG = RemBG(
            stableDiffusionService = stableDiffusionService,
            payload = RemBGPayload(
                inputImage = inputImage,
                model = model,
                returnMask = returnMask,
                alphaMatting = alphaMatting,
                alphaMattingForegroundThreshold = alphaMattingForegroundThreshold,
                alphaMattingBackgroundThreshold = alphaMattingBackgroundThreshold,
                alphaMattingErodeSize = alphaMattingErodeSize
            )
        )
    }
}