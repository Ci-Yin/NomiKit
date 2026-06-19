package ciyin.ai.integrate.image

/**
 * 聚合模块内置的引擎配置基线；可在模块内构造 [DefaultAiImageIntegrate] 时替换整表，
 * 或在 [AiImageIntegrate.engines] 中按 [ImageEngineConfig] 子类类型覆盖。
 */
internal object IntegrateImageDefaults {

    /**
     * 首版本机常见 SD WebUI；不含敏感信息，生产环境通常由 [AiImageIntegrate.engines] 覆盖连接参数。
     */
    fun sdWebUiLocalhost(): List<ImageEngineConfig> = listOf(
        ImageEngineConfig.SdWebUi(
            baseUrl = "http://127.0.0.1:7860",
            apiKey = "",
            defaultModel = null,
        ),
    )
}
