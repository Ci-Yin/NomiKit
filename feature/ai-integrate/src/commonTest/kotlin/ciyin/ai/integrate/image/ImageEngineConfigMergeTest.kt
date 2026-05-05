package ciyin.ai.integrate.image

import kotlin.test.Test
import kotlin.test.assertEquals

class ImageEngineConfigMergeTest {

    @Test
    fun overrides_replace_defaults_per_sealed_type() {
        val defaults = IntegrateImageDefaults.sdWebUiLocalhost()
        val overrides = listOf(
            ImageEngineConfig.SdWebUi(
                baseUrl = "http://remote:7860",
                apiKey = "k",
                defaultModel = "m",
            ),
        )
        val merged = mergeEngineConfigsWithDefaults(
            defaults = defaults,
            overrides = overrides,
        )
        assertEquals(1, merged.size)
        val sd = merged.single() as ImageEngineConfig.SdWebUi
        assertEquals("http://remote:7860", sd.baseUrl)
        assertEquals("k", sd.apiKey)
        assertEquals("m", sd.defaultModel)
    }

    @Test
    fun empty_overrides_keep_defaults() {
        val defaults = IntegrateImageDefaults.sdWebUiLocalhost()
        val merged = mergeEngineConfigsWithDefaults(
            defaults = defaults,
            overrides = emptyList(),
        )
        assertEquals(defaults, merged)
    }
}
