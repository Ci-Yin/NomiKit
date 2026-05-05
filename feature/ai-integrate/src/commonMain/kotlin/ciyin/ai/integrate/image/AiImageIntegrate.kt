package ciyin.ai.integrate.image

import ciyin.ai.core.engine.EngineId
import ciyin.ai.core.engine.ImageEngine
import ciyin.ai.core.image.ImageEvent
import ciyin.ai.core.image.ImageModelInfo
import ciyin.ai.core.image.ImageRequest
import ciyin.ai.core.registry.DefaultImageEngineRegistry
import ciyin.ai.core.registry.ImageEngineSelector
import ciyin.ai.facade.DefaultAiImage
import ciyin.ai.facade.selection.EnginePreferences
import ciyin.ai.facade.selection.ImageEngineSpec
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.reflect.KClass

/**
 * 生图聚合入口：在 [engines] 注册的后端之上，对内委托 [DefaultAiImage] 完成路由、降级与观测。
 *
 * 对外请优先使用同包中的无参工厂函数（定义见 `AiImageIntegrateFactory.kt`），以注入默认 SD WebUI 与本模块偏好；
 * [internal] 构造便于单测替换 [buildImageEngine] / [preferences]。
 *
 * @param defaultEngineConfigs 内置基线（常见本机地址等）；与每次 [engines] 传入列表按 sealed 子类合并，
 *   同类型时 [engines] 侧覆盖内置。若传 [emptyList] 则关闭内置基线，完全依赖后续 [engines]。
 * @param preferences 注入 [EnginePreferences]（通常为 [IntegrateEnginePreferences]），用于默认 [ImageEngineSpec]、
 *   降级策略及与 [DefaultAiImage] 一致的 spec 解析。
 * @param buildImageEngine 将 [ImageEngineConfig] 装配为 [ImageEngine]；生产环境为 SD WebUI 适配，单测可替换为 Stub。
 */
class AiImageIntegrate internal constructor(
    private val defaultEngineConfigs: List<ImageEngineConfig>,
    private val preferences: EnginePreferences,
    private val buildImageEngine: (ImageEngineConfig) -> ImageEngine,
) {

    /** 保护 [runtime] 的读写及与 [generate]/[models] 收集路径的并发安全。 */
    private val mutex = Mutex()

    /**
     * 当前已装配的 Facade 委托、引擎选择器及「引擎 id → 配置」快照（用于 [withDefaultModelFrom]）。
     * 构造时用 [defaultEngineConfigs] 初始化；之后每次 [engines] 整体替换。
     */
    private var runtime: Runtime = buildRuntime(defaultEngineConfigs)

    /**
     * 按业务侧最新意图重建引擎实例：将 [defaultEngineConfigs] 与 [configs] 做 [mergeEngineConfigsWithDefaults] 后替换 [runtime]。
     *
     * @param configs 覆盖项；未出现的 sealed 子类仍沿用构造时传入的内置基线。传 [emptyList] 表示仅保留内置基线（若 [defaultEngineConfigs] 非空）。
     */
    suspend fun engines(configs: List<ImageEngineConfig>) = mutex.withLock {
        val unique = mergeEngineConfigsWithDefaults(
            defaults = defaultEngineConfigs,
            overrides = configs,
        )
        runtime = buildRuntime(unique)
    }

    /**
     * 生成图像；路由、降级与观测语义与 [DefaultAiImage.generate] 一致。
     *
     * 委托前会基于当前 [runtime] 与 [spec] 解析目标引擎，并在请求未带 [ImageRequest.model] 时用配置 [ImageEngineConfig.defaultModel] 补全（见 [withDefaultModelFrom]）。
     *
     * @param request 通用生图请求。
     * @param spec 引擎路由描述；[ImageEngineSpec.Default] 时与 [preferences] 解析路径一致。
     * @return 与 Facade 约定一致的 [ImageEvent] 流。
     */
    fun generate(
        request: ImageRequest,
        spec: ImageEngineSpec = ImageEngineSpec.Default,
    ): Flow<ImageEvent> = flow {
        val snap = mutex.withLock { runtime }
        val resolvedSpec = resolveRequestedSpec(spec)
        val targetEngineId = engineIdFromResolvedSpec(resolvedSpec, snap.selector)
        val merged = request.withDefaultModelFrom(
            engineId = targetEngineId,
            configsByEngineId = snap.configsByEngineId,
        )
        emitAll(snap.delegate.generate(merged, spec))
    }

    /**
     * 列出当前已注册引擎的可用模型（与 [DefaultAiImage.models] 一致：跨引擎、按模型名去重）。
     *
     * @return 供 UI 展示与选择的 [ImageModelInfo] 列表。
     */
    suspend fun models(): List<ImageModelInfo> {
        return mutex.withLock { runtime }.delegate.models()
    }

    /**
     * 由合并后的配置列表构建 [Runtime]：创建各 [ImageEngine]、注册表、[ImageEngineSelector] 与 [DefaultAiImage]。
     *
     * @param unique 已为「默认 ∪ 覆盖」合并后的配置（同 [ImageEngineConfig] 子类以最后一次为准）。
     */
    private fun buildRuntime(unique: List<ImageEngineConfig>): Runtime {
        val imageEngines = unique.map(buildImageEngine)
        val registry = DefaultImageEngineRegistry(imageEngines)
        val selector = ImageEngineSelector(registry = registry)
        val delegate = DefaultAiImage(
            selector = selector,
            preferences = preferences,
            listeners = emptyList(),
        )
        return Runtime(
            delegate = delegate,
            selector = selector,
            configsByEngineId = unique.associateBy { it.engineId },
        )
    }

    /**
     * 按 [ImageEngineConfig] 的 sealed 子类类型合并：键为 [KClass]，先写入 [defaults]（列表中同类后者覆盖前者），
     * 再写入 [overrides] 覆盖同类槽位；仅出现在 [defaults] 的类型得以保留。
     *
     * @return 合并结果的稳定迭代顺序：先按 [defaults] 首次出现的类型顺序，再纳入仅在 [overrides] 中出现的类型（替换时值更新）。
     */
    private fun mergeEngineConfigsWithDefaults(
        defaults: List<ImageEngineConfig>,
        overrides: List<ImageEngineConfig>,
    ): List<ImageEngineConfig> {
        val byKind = LinkedHashMap<KClass<out ImageEngineConfig>, ImageEngineConfig>()
        for (cfg in defaults) {
            byKind[cfg::class] = cfg
        }
        for (cfg in overrides) {
            byKind[cfg::class] = cfg
        }
        return byKind.values.toList()
    }

    /**
     * 将传入的 [spec] 解析为与 [DefaultAiImage.generate] 第一步一致的「请求侧」规格：
     * [ImageEngineSpec.Default] 时读取 [preferences.defaultImageSpec]，若仍为 Default 则退化为空能力集合的 [ImageEngineSpec.ByCapability]。
     */
    private suspend fun resolveRequestedSpec(spec: ImageEngineSpec): ImageEngineSpec = when (spec) {
        ImageEngineSpec.Default -> when (val preferred = preferences.defaultImageSpec()) {
            ImageEngineSpec.Default -> ImageEngineSpec.ByCapability(emptySet())
            else -> preferred
        }

        else -> spec
    }

    /**
     * 根据已解析的 [resolved] 与当前 [selector]，得到本次生成请求用于合并默认模型的目标 [EngineId]
     *（与即将进入 [DefaultAiImage.generate] 的主引擎选择一致）。
     */
    private fun engineIdFromResolvedSpec(
        resolved: ImageEngineSpec,
        selector: ImageEngineSelector,
    ): EngineId = when (resolved) {
        ImageEngineSpec.Default -> selector.select().id
        is ImageEngineSpec.Explicit -> selector.select(preferredId = resolved.engineId).id
        is ImageEngineSpec.ByCapability -> selector.select(required = resolved.required).id
    }

    /**
     * 若 [model] 为 `null` 且对应 [ImageEngineConfig.defaultModel] 非空，则补全 [ImageRequest.model]；
     * 否则返回原请求（显式模型优先于配置默认）。
     */
    private fun ImageRequest.withDefaultModelFrom(
        engineId: EngineId,
        configsByEngineId: Map<EngineId, ImageEngineConfig>,
    ): ImageRequest {
        val cfg = configsByEngineId[engineId] ?: return this
        return if (model == null && cfg.defaultModel != null) {
            copy(model = cfg.defaultModel)
        } else {
            this
        }
    }

    /**
     * 一次 [engines] 装配结果的快照。
     *
     * @property delegate 对内唯一使用的 [DefaultAiImage]，承载降级与观测。
     * @property selector 与当前注册表绑定的引擎选择器，用于解析 [ImageEngineSpec]。
     * @property configsByEngineId 当前各 [EngineId] 对应的聚合层配置，供默认模型合并查询。
     */
    private data class Runtime(
        val delegate: DefaultAiImage,
        val selector: ImageEngineSelector,
        val configsByEngineId: Map<EngineId, ImageEngineConfig>,
    )

}
