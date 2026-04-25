package ciyin.sdwebui.process

import ciyin.sdwebui.payload.Image2ImagePayload
import ciyin.sdwebui.payload.script.ScriptPayload
import ciyin.sdwebui.response.GenerateProcessResponse
import ciyin.sdwebui.service.StableDiffusionService

/**
 * 图生图流程封装，对应 WebUI `sdapi/v1/img2img`。
 *
 * 典型用法：在 [ciyin.sdwebui.SdWebUi] 上通过 [Process] 伴生对象的 `runImage2Image { ... }` 执行，
 * 或自行取得 [Builder] 链式配置后 [build]，再调用 [run]。
 *
 * [Builder] 所收集字段与 [Image2ImagePayload] 及官方请求 JSON 键语义一致。
 */
class Image2Image private constructor(
    private val stableDiffusionService: StableDiffusionService,
    private val payload: Image2ImagePayload,
) : Process {

    /**
     * 发起图生图请求并返回生成结果（含 Base64 图像与 `info` 字符串）。
     */
    suspend fun run(): Result<GenerateProcessResponse> {
        return stableDiffusionService.image2Image(payload)
    }

    /**
     * 以流式 API 组装与 [Image2ImagePayload] 等价的图生图请求参数。
     *
     * 下列 `xxx(...)` 链式方法对应 WebUI `img2img` 请求体中的同名/同义字段。
     * [samplerName] 与 [samplerIndex] 在本构建器中**分别**赋值，不会自动互相同步，请与 WebUI 侧约定保持一致。
     * 常驻扩展脚本通过 [alwaysonScripts] 或 [addAlwaysonScript] 写入 `alwayson_scripts`。
     */
    class Builder internal constructor(
        private val stableDiffusionService: StableDiffusionService,
    ) : Process.Builder {

        private var initImages: List<String> = emptyList()
        private var prompt: String = ""
        private var negativePrompt: String = ""
        private var resizeMode: Int = 0
        private var denoisingStrength: Float = 0.75f
        private var mask: String? = null
        private var maskBlur: Int = 4
        private var inpaintingFill: Int = 0
        private var inpaintFullRes: Boolean = true
        private var inpaintFullResPadding: Int = 0
        private var inpaintingMaskInvert: Int = 0
        private var initialNoiseMultiplier: Int = 1
        private var styles: List<String> = emptyList()
        private var seed: Int = -1
        private var subseed: Int = -1
        private var subseedStrength: Int = 0
        private var seedResizeFromH: Int = 0
        private var seedResizeFromW: Int = 0
        private var batchSize: Int = 1
        private var nIter: Int = 1
        private var steps: Int = 20
        private var cfgScale: Float = 7.0f
        private var imageCfgScale: Float = 1.5f
        private var width: Int = 512
        private var height: Int = 512
        private var restoreFaces: Boolean = false
        private var tiling: Boolean = false
        private var doNotSaveSamples: Boolean = false
        private var eta: Float = 1.0f
        private var sChurn: Int = 0
        private var sTmax: Int = 0
        private var sTmin: Int = 0
        private var sNoise: Int = 1
        private var overrideSettings: Map<String, String> = emptyMap()
        private var overrideSettingsRestoreAfterwards: Boolean = true
        private var samplerName: String = ""
        private var samplerIndex: String = ""
        private var includeInitImages: Boolean = false
        private var scriptName: String? = null
        private var scriptArgs: List<String> = emptyList()
        private var sendImages: Boolean = true
        private var saveImages: Boolean = false
        private var alwaysonScripts: MutableMap<String, ScriptPayload> = mutableMapOf()

        /** 输入初始图列表，Base64 编码（`init_images`）。 */
        fun initImages(initImages: List<String>) = apply {
            this.initImages = initImages
        }

        /** 正提示词（`prompt`）。 */
        fun prompt(prompt: String) = apply {
            this.prompt = prompt
        }

        /** 负提示词（`negative_prompt`）。 */
        fun negativePrompt(negativePrompt: String) = apply {
            this.negativePrompt = negativePrompt
        }

        /** 缩放/裁剪模式枚举值（`resize_mode`，与 WebUI 约定一致）。 */
        fun resizeMode(resizeMode: Int) = apply {
            this.resizeMode = resizeMode
        }

        /** 重绘去噪强度（`denoising_strength`）。 */
        fun denoisingStrength(denoisingStrength: Float) = apply {
            this.denoisingStrength = denoisingStrength
        }

        /** 可选遮罩图 Base64（`mask`）；非局部重绘可为 `null`。 */
        fun mask(mask: String?) = apply {
            this.mask = mask
        }

        /** 遮罩边缘模糊像素（`mask_blur`）。 */
        fun maskBlur(maskBlur: Int) = apply {
            this.maskBlur = maskBlur
        }

        /** 局部重绘填充方式（`inpainting_fill`）。 */
        fun inpaintingFill(inpaintingFill: Int) = apply {
            this.inpaintingFill = inpaintingFill
        }

        /** 是否仅对遮罩区域以全分辨率重绘（`inpaint_full_res`）。 */
        fun inpaintFullRes(inpaintFullRes: Boolean) = apply {
            this.inpaintFullRes = inpaintFullRes
        }

        /** 全分辨率重绘时遮罩扩展边距（`inpaint_full_res_padding`）。 */
        fun inpaintFullResPadding(inpaintFullResPadding: Int) = apply {
            this.inpaintFullResPadding = inpaintFullResPadding
        }

        /** 遮罩反转方式（`inpainting_mask_invert`）。 */
        fun inpaintingMaskInvert(inpaintingMaskInvert: Int) = apply {
            this.inpaintingMaskInvert = inpaintingMaskInvert
        }

        /** 初始噪声倍率（`initial_noise_multiplier`）。 */
        fun initialNoiseMultiplier(initialNoiseMultiplier: Int) = apply {
            this.initialNoiseMultiplier = initialNoiseMultiplier
        }

        /** 提示词风格标签列表（`styles`）。 */
        fun styles(styles: List<String>) = apply {
            this.styles = styles
        }

        /** 随机种子（`seed`，`-1` 表示随机）。 */
        fun seed(seed: Int) = apply {
            this.seed = seed
        }

        /** 子种子（`subseed`）。 */
        fun subseed(subseed: Int) = apply {
            this.subseed = subseed
        }

        /** 子种子强度（`subseed_strength`）。 */
        fun subseedStrength(subseedStrength: Int) = apply {
            this.subseedStrength = subseedStrength
        }

        /** 种子缩放来源高度（`seed_resize_from_h`）。 */
        fun seedResizeFromH(seedResizeFromH: Int) = apply {
            this.seedResizeFromH = seedResizeFromH
        }

        /** 种子缩放来源宽度（`seed_resize_from_w`）。 */
        fun seedResizeFromW(seedResizeFromW: Int) = apply {
            this.seedResizeFromW = seedResizeFromW
        }

        /** 单批张数（`batch_size`）。 */
        fun batchSize(batchSize: Int) = apply {
            this.batchSize = batchSize
        }

        /** 批次数（`n_iter`）。 */
        fun nIter(nIter: Int) = apply {
            this.nIter = nIter
        }

        /** 采样步数（`steps`）。 */
        fun steps(steps: Int) = apply {
            this.steps = steps
        }

        /** 提示词 CFG（`cfg_scale`）。 */
        fun cfgScale(cfgScale: Float) = apply {
            this.cfgScale = cfgScale
        }

        /** 图生图专用图像 CFG（`image_cfg_scale`，如 InstructPix2Pix 等模式）。 */
        fun imageCfgScale(imageCfgScale: Float) = apply {
            this.imageCfgScale = imageCfgScale
        }

        /** 输出宽度像素（`width`）。 */
        fun width(width: Int) = apply {
            this.width = width
        }

        /** 输出高度像素（`height`）。 */
        fun height(height: Int) = apply {
            this.height = height
        }

        /** 是否面部修复（`restore_faces`）。 */
        fun restoreFaces(restoreFaces: Boolean) = apply {
            this.restoreFaces = restoreFaces
        }

        /** 是否平铺纹理（`tiling`）。 */
        fun tiling(tiling: Boolean) = apply {
            this.tiling = tiling
        }

        /** 为真时不将样本写入磁盘（`do_not_save_samples`）。 */
        fun doNotSaveSamples(doNotSaveSamples: Boolean) = apply {
            this.doNotSaveSamples = doNotSaveSamples
        }

        /** 采样 ETA 噪声系数（`eta`）。 */
        fun eta(eta: Float) = apply {
            this.eta = eta
        }

        /** 随机调度器 churn（`s_churn`）。 */
        fun sChurn(sChurn: Int) = apply {
            this.sChurn = sChurn
        }

        /** 随机调度器 t_max（`s_tmax`）。 */
        fun sTmax(sTmax: Int) = apply {
            this.sTmax = sTmax
        }

        /** 随机调度器 t_min（`s_tmin`）。 */
        fun sTmin(sTmin: Int) = apply {
            this.sTmin = sTmin
        }

        /** 随机调度器噪声（`s_noise`）。 */
        fun sNoise(sNoise: Int) = apply {
            this.sNoise = sNoise
        }

        /** 临时覆盖选项（`override_settings`）。 */
        fun overrideSettings(overrideSettings: Map<String, String>) = apply {
            this.overrideSettings = overrideSettings
        }

        /** 请求结束后是否恢复选项（`override_settings_restore_afterwards`）。 */
        fun overrideSettingsRestoreAfterwards(overrideSettingsRestoreAfterwards: Boolean) = apply {
            this.overrideSettingsRestoreAfterwards = overrideSettingsRestoreAfterwards
        }

        /** 采样器名称（`sampler_name`）。 */
        fun samplerName(samplerName: String) = apply {
            this.samplerName = samplerName
        }

        /** 采样器索引字符串（`sampler_index`）。 */
        fun samplerIndex(samplerIndex: String) = apply {
            this.samplerIndex = samplerIndex
        }

        /** 是否在响应中回传初始图（`include_init_images`）。 */
        fun includeInitImages(includeInitImages: Boolean) = apply {
            this.includeInitImages = includeInitImages
        }

        /** 主脚本名（`script_name`）。 */
        fun scriptName(scriptName: String) = apply {
            this.scriptName = scriptName
        }

        /** 主脚本参数（`script_args`）。 */
        fun scriptArgs(scriptArgs: List<String>) = apply {
            this.scriptArgs = scriptArgs
        }

        /** 响应中是否包含生成图 Base64（`send_images`）。 */
        fun sendImages(sendImages: Boolean) = apply {
            this.sendImages = sendImages
        }

        /** 是否保存到服务器磁盘（`save_images`）。 */
        fun saveImages(saveImages: Boolean) = apply {
            this.saveImages = saveImages
        }

        /** 整体替换 `alwayson_scripts` 映射。 */
        fun alwaysonScripts(alwaysonScripts: Map<String, ScriptPayload>) = apply {
            this.alwaysonScripts.clear()
            this.alwaysonScripts.putAll(alwaysonScripts)
        }

        /**
         * 向 `alwayson_scripts` 注册或覆盖一条扩展脚本；键名需与 WebUI 扩展约定一致（如 `ControlNet`）。
         */
        override fun addAlwaysonScript(key: String, payload: ScriptPayload) {
            this.alwaysonScripts[key] = payload
        }

        /**
         * 根据当前配置创建可执行的 [Image2Image]。
         */
        fun build(): Image2Image = Image2Image(
            stableDiffusionService = stableDiffusionService,
            payload = Image2ImagePayload(
                initImages = initImages,
                prompt = prompt,
                negativePrompt = negativePrompt,
                resizeMode = resizeMode,
                denoisingStrength = denoisingStrength,
                mask = mask,
                maskBlur = maskBlur,
                inpaintingFill = inpaintingFill,
                inpaintFullRes = inpaintFullRes,
                inpaintFullResPadding = inpaintFullResPadding,
                inpaintingMaskInvert = inpaintingMaskInvert,
                initialNoiseMultiplier = initialNoiseMultiplier,
                styles = styles,
                seed = seed,
                subseed = subseed,
                subseedStrength = subseedStrength,
                seedResizeFromH = seedResizeFromH,
                seedResizeFromW = seedResizeFromW,
                batchSize = batchSize,
                nIter = nIter,
                steps = steps,
                cfgScale = cfgScale,
                imageCfgScale = imageCfgScale,
                width = width,
                height = height,
                restoreFaces = restoreFaces,
                tiling = tiling,
                doNotSaveSamples = doNotSaveSamples,
                eta = eta,
                sChurn = sChurn,
                sTmax = sTmax,
                sTmin = sTmin,
                sNoise = sNoise,
                overrideSettings = overrideSettings,
                overrideSettingsRestoreAfterwards = overrideSettingsRestoreAfterwards,
                samplerName = samplerName,
                samplerIndex = samplerIndex,
                includeInitImages = includeInitImages,
                scriptName = scriptName,
                scriptArgs = scriptArgs,
                sendImages = sendImages,
                saveImages = saveImages,
                alwaysonScripts = alwaysonScripts,
            )
        )
    }
}
