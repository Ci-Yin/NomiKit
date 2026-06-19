package ciyin.ai.integrate.image

/**
 * 聚合层默认偏好：默认生图路由固定到 [IntegrateImageEngineIds.sdWebUi]。
 */
internal class IntegrateEnginePreferences {

    /**
     * 返回默认生图路由描述。
     */
    fun defaultImageSpec(): ImageEngineSpec =
        ImageEngineSpec.Explicit(engineId = IntegrateImageEngineIds.sdWebUi, model = null)

    /**
     * 返回默认生图降级策略。
     */
    fun imageFallback(): FallbackPolicy = FallbackPolicy()
}
