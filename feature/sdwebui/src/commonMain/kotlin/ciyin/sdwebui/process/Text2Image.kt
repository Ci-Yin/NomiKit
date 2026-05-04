package ciyin.sdwebui.process

import ciyin.sdwebui.payload.script.ScriptPayload
import ciyin.sdwebui.payload.Text2ImagePayload
import ciyin.sdwebui.response.GenerateProcessResponse
import ciyin.sdwebui.service.StableDiffusionService

/**
 * 文生图流程封装，对应 WebUI `sdapi/v1/txt2img`。
 *
 * 典型用法：在 [ciyin.sdwebui.SdWebUi] 上通过 [Process] 伴生对象的 `runText2Image { ... }` 执行，
 * 或自行取得 [Builder] 链式配置后 [build]，再调用 [run]。
 *
 * [Builder] 所收集字段与 [Text2ImagePayload] 及官方请求 JSON 键语义一致。
 */
class Text2Image private constructor(
    private val stableDiffusionService: StableDiffusionService,
    private val payload: Text2ImagePayload,
) : Process {

    /**
     * 发起文生图请求并返回生成结果（含 Base64 图像与 `info` 字符串）。
     */
    suspend fun run(): Result<GenerateProcessResponse> {
        return stableDiffusionService.text2Image(payload)
    }

    /**
     * 以流式 API 组装与 [Text2ImagePayload] 等价的文生图请求参数。
     *
     * 下列 `xxx(...)` 链式方法对应 WebUI `txt2img` 请求体中的同名/同义字段；
     * [samplerName] 会同时同步内部 `sampler_index` 所用取值，与 WebUI 行为对齐。
     * 常驻扩展脚本通过 [addAlwaysonScript] 或整表 [alwaysonScripts] 写入 `alwayson_scripts`。
     */
    class Builder internal constructor(
        private val stableDiffusionService: StableDiffusionService,
    ) : Process.Builder {

        private var prompt: String = ""
        private var negativePrompt: String = ""
        private var styles: List<String> = emptyList()
        private var seed: Int = -1
        private var subseed: Int = -1
        private var subseedStrength: Int = 0
        private var seedResizeFromH: Int = 0
        private var seedResizeFromW: Int = 0
        private var samplerName: String = ""
        private var batchSize: Int = 1
        private var nIter: Int = 1
        private var steps: Int = 25
        private var cfgScale: Float = 7.0f
        private var width: Int = 512
        private var height: Int = 512
        private var restoreFaces: Boolean = false
        private var tiling: Boolean = false
        private var doNotSaveSamples: Boolean = false
        private var doNotSaveGrid: Boolean = false
        private var eta: Float = 1.0f
        private var denoisingStrength: Float = 0.7f
        private var sChurn: Int = 0
        private var sTmax: Int = 0
        private var sTmin: Int = 0
        private var sNoise: Int = 1
        private var overrideSettings: Map<String, String> = emptyMap()
        private var overrideSettingsRestoreAfterwards: Boolean = true
        private var comments: Map<String, String> = emptyMap()
        private var enableHr: Boolean = false
        private var firstphaseWidth: Int = 0
        private var firstphaseHeight: Int = 0
        private var hrScale: Float = 2f
        private var hrUpscaler: String = "Latent"
        private var hrSecondPassSteps: Int = 0
        private var hrResizeX: Int = 0
        private var hrResizeY: Int = 0
        private var samplerIndex: String = ""
        private var scriptName: String? = null
        private var scriptArgs: List<String> = emptyList()
        private var sendImages: Boolean = true
        private var saveImages: Boolean = false
        private var alwaysonScripts: MutableMap<String, ScriptPayload> = mutableMapOf()

        /** 正提示词（`prompt`）。 */
        fun prompt(prompt: String) = apply {
            this.prompt = prompt
        }

        /** 负提示词（`negative_prompt`）。 */
        fun negativePrompt(negativePrompt: String) = apply {
            this.negativePrompt = negativePrompt
        }

        /** 提示词风格标签列表（`styles`）。 */
        fun styles(styles: List<String>) = apply {
            this.styles = styles
        }

        /** 随机种子（`seed`，`-1` 表示随机）。 */
        fun seed(seed: Int) = apply {
            this.seed = seed
        }

        /** 子种子 Variation（`subseed`）。 */
        fun subseed(subseed: Int) = apply {
            this.subseed = subseed
        }

        /** 子种子强度（`subseed_strength`）。 */
        fun subseedStrength(subseedStrength: Int) = apply {
            this.subseedStrength = subseedStrength
        }

        /** 从指定高度缩放种子影响区域（`seed_resize_from_h`）。 */
        fun seedResizeFromH(seedResizeFromH: Int) = apply {
            this.seedResizeFromH = seedResizeFromH
        }

        /** 从指定宽度缩放种子影响区域（`seed_resize_from_w`）。 */
        fun seedResizeFromW(seedResizeFromW: Int) = apply {
            this.seedResizeFromW = seedResizeFromW
        }

        /**
         * 采样器名称（`sampler_name`），并同步写入 [samplerIndex] 所用内部字段以匹配 `sampler_index`。
         */
        fun samplerName(samplerName: String) = apply {
            this.samplerName = samplerName
            this.samplerIndex = samplerName
        }

        /** 单批生成张数（`batch_size`）。 */
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

        /** 提示词相关性（`cfg_scale`）。 */
        fun cfgScale(cfgScale: Float) = apply {
            this.cfgScale = cfgScale
        }

        /** 图像宽度像素（`width`）。 */
        fun width(width: Int) = apply {
            this.width = width
        }

        /** 图像高度像素（`height`）。 */
        fun height(height: Int) = apply {
            this.height = height
        }

        /** 是否启用面部修复（`restore_faces`）。 */
        fun restoreFaces(restoreFaces: Boolean) = apply {
            this.restoreFaces = restoreFaces
        }

        /** 是否平铺无缝纹理（`tiling`）。 */
        fun tiling(tiling: Boolean) = apply {
            this.tiling = tiling
        }

        /** 为真时不将样本写入磁盘（`do_not_save_samples`）。 */
        fun doNotSaveSamples(doNotSaveSamples: Boolean) = apply {
            this.doNotSaveSamples = doNotSaveSamples
        }

        /** 为真时不保存预览网格图（`do_not_save_grid`）。 */
        fun doNotSaveGrid(doNotSaveGrid: Boolean) = apply {
            this.doNotSaveGrid = doNotSaveGrid
        }

        /** 采样 ETA 噪声系数（`eta`）。 */
        fun eta(eta: Float) = apply {
            this.eta = eta
        }

        /** 重绘/高分辨率相关去噪强度（`denoising_strength`）。 */
        fun denoisingStrength(denoisingStrength: Float) = apply {
            this.denoisingStrength = denoisingStrength
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

        /** 临时覆盖 WebUI 选项键值（`override_settings`）。 */
        fun overrideSettings(overrideSettings: Map<String, String>) = apply {
            this.overrideSettings = overrideSettings
        }

        /** 请求结束后是否恢复被覆盖的选项（`override_settings_restore_afterwards`）。 */
        fun overrideSettingsRestoreAfterwards(overrideSettingsRestoreAfterwards: Boolean) = apply {
            this.overrideSettingsRestoreAfterwards = overrideSettingsRestoreAfterwards
        }

        /** 附加注释键值（`comments`）。 */
        fun comments(comments: Map<String, String>) = apply {
            this.comments = comments
        }

        /** 是否启用高分辨率修复（`enable_hr`）。 */
        fun enableHr(enableHr: Boolean) = apply {
            this.enableHr = enableHr
        }

        /** HR 一阶段宽度（`firstphase_width`）。 */
        fun firstphaseWidth(firstphaseWidth: Int) = apply {
            this.firstphaseWidth = firstphaseWidth
        }

        /** HR 一阶段高度（`firstphase_height`）。 */
        fun firstphaseHeight(firstphaseHeight: Int) = apply {
            this.firstphaseHeight = firstphaseHeight
        }

        /** HR 放大倍数（`hr_scale`）。 */
        fun hrScale(hrScale: Float) = apply {
            this.hrScale = hrScale
        }

        /** HR 所用放大器名称（`hr_upscaler`）。 */
        fun hrUpscaler(hrUpscaler: String) = apply {
            this.hrUpscaler = hrUpscaler
        }

        /** HR 第二阶段额外步数（`hr_second_pass_steps`）。 */
        fun hrSecondPassSteps(hrSecondPassSteps: Int) = apply {
            this.hrSecondPassSteps = hrSecondPassSteps
        }

        /** HR 目标宽度像素（`hr_resize_x`）。 */
        fun hrResizeX(hrResizeX: Int) = apply {
            this.hrResizeX = hrResizeX
        }

        /** HR 目标高度像素（`hr_resize_y`）。 */
        fun hrResizeY(hrResizeY: Int) = apply {
            this.hrResizeY = hrResizeY
        }

        /** 采样器索引字符串（`sampler_index`）；通常由 [samplerName] 一并设置。 */
        fun samplerIndex(samplerIndex: String) = apply {
            this.samplerIndex = samplerIndex
        }

        /** 主脚本名称（`script_name`）。 */
        fun scriptName(scriptName: String) = apply {
            this.scriptName = scriptName
        }

        /** 主脚本参数列表（`script_args`）。 */
        fun scriptArgs(scriptArgs: List<String>) = apply {
            this.scriptArgs = scriptArgs
        }

        /** 是否在响应中返回图像 Base64（`send_images`）。 */
        fun sendImages(sendImages: Boolean) = apply {
            this.sendImages = sendImages
        }

        /** 是否将生成结果保存到服务器磁盘（`save_images`）。 */
        fun saveImages(saveImages: Boolean) = apply {
            this.saveImages = saveImages
        }

        /** 整体替换 `alwayson_scripts` 映射表。 */
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
         * 根据当前配置创建可执行的 [Text2Image]。
         */
        fun build(): Text2Image = Text2Image(
            stableDiffusionService = stableDiffusionService,
            payload = Text2ImagePayload(
                prompt = prompt,
                negativePrompt = negativePrompt,
                styles = styles,
                seed = seed,
                subseed = subseed,
                subseedStrength = subseedStrength,
                seedResizeFromH = seedResizeFromH,
                seedResizeFromW = seedResizeFromW,
                samplerName = samplerName,
                batchSize = batchSize,
                nIter = nIter,
                steps = steps,
                cfgScale = cfgScale,
                width = width,
                height = height,
                restoreFaces = restoreFaces,
                tiling = tiling,
                doNotSaveSamples = doNotSaveSamples,
                doNotSaveGrid = doNotSaveGrid,
                eta = eta,
                denoisingStrength = denoisingStrength,
                sChurn = sChurn,
                sTmax = sTmax,
                sTmin = sTmin,
                sNoise = sNoise,
                overrideSettings = overrideSettings,
                overrideSettingsRestoreAfterwards = overrideSettingsRestoreAfterwards,
                comments = comments,
                enableHr = enableHr,
                firstphaseWidth = firstphaseWidth,
                firstphaseHeight = firstphaseHeight,
                hrScale = hrScale,
                hrUpscaler = hrUpscaler,
                hrSecondPassSteps = hrSecondPassSteps,
                hrResizeX = hrResizeX,
                hrResizeY = hrResizeY,
                samplerIndex = samplerIndex,
                scriptName = scriptName,
                scriptArgs = scriptArgs,
                sendImages = sendImages,
                saveImages = saveImages,
                alwaysonScripts = alwaysonScripts,
            )
        )
    }
}
