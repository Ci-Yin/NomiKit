package ciyin.ai.integrate.image

import ciyin.ai.core.engine.ImageEngine
import ciyin.ai.image.sdwebui.SdWebUiImageEngine
import ciyin.ai.image.sdwebui.SdWebUiImageEngineConfig

/**
 * 面向调用方的默认装配入口：内置本机 SD WebUI 基线、[IntegrateEnginePreferences] 默认偏好，
 * 以及生产环境使用的 [defaultSdWebUiImageEngine]。
 *
 * 需要自定义默认引擎表、偏好或装配逻辑时，请在模块内使用 [DefaultAiImageIntegrate]。
 */
fun AiImageIntegrate(): AiImageIntegrate = DefaultAiImageIntegrate(
    defaultEngineConfigs = IntegrateImageDefaults.sdWebUiLocalhost(),
    preferences = IntegrateEnginePreferences(),
    buildImageEngine = ::defaultSdWebUiImageEngine,
)

/**
 * 将 [ImageEngineConfig.SdWebUi] 转为 [SdWebUiImageEngine]：
 * 使用 [parseHttpOrigin] 解析 [ImageEngineConfig.baseUrl]，[apiKey] 等字段保留给上层 HTTP 栈接线。
 */
private fun defaultSdWebUiImageEngine(config: ImageEngineConfig): ImageEngine = when (config) {
    is ImageEngineConfig.SdWebUi -> {
        val (host, port, useHttps) = parseHttpOrigin(config.baseUrl)
        SdWebUiImageEngine(
            config = SdWebUiImageEngineConfig(
                id = config.engineId,
                host = host,
                port = port,
                useHttps = useHttps,
            ),
        )
    }
}
