package ciyin.ai.integrate.image

import ciyin.ai.core.capability.ImageCapability
import ciyin.ai.core.engine.EngineId
import ciyin.ai.core.engine.ImageEngine
import ciyin.ai.core.error.UnsupportedCapabilityException
import ciyin.ai.core.image.ImageEvent
import ciyin.ai.core.image.ImageModelInfo
import ciyin.ai.core.image.ImagePostProcessor
import ciyin.ai.core.image.ImageRequest
import ciyin.ai.core.image.ImageSource
import ciyin.ai.core.registry.DefaultImageEngineRegistry
import ciyin.ai.core.registry.ImageEngineSelector
import ciyin.ai.integrate.image.internal.EngineAttempt
import ciyin.ai.integrate.image.internal.InvocationIds
import ciyin.ai.integrate.image.internal.buildAttempts
import ciyin.ai.integrate.image.internal.collectWithFallback
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.reflect.KClass

/**
 * 生图聚合入口契约：在 [engines] 注册的后端之上完成路由、默认模型合并、重试与降级调度。
 */
interface AiImageIntegrate {

    /**
     * 按业务侧最新意图重建引擎实例。
     *
     * @param configs 覆盖项；未出现的 sealed 子类仍沿用实现构造时传入的内置基线。
     */
    suspend fun engines(configs: List<ImageEngineConfig>)

    /**
     * 生成图像。
     *
     * @param request 通用生图请求。
     * @param spec 引擎路由描述；[ImageEngineSpec.Default] 时与实现默认偏好解析路径一致。
     * @return 符合 [ImageEvent] 契约的事件流。
     */
    fun generate(
        request: ImageRequest,
        spec: ImageEngineSpec = ImageEngineSpec.Default,
    ): Flow<ImageEvent>

    /**
     * 列出当前已注册引擎的可用模型。
     *
     * @param spec 引擎路由描述；[ImageEngineSpec.Default] 时由实现决定默认枚举范围。
     * @return 供 UI 展示与选择的 [ImageModelInfo] 列表。
     */
    suspend fun models(spec: ImageEngineSpec = ImageEngineSpec.Default): List<ImageModelInfo>

    /**
     * 按具体引擎类型获取当前已注册的第一个匹配实例。
     *
     * @param type 目标 [ImageEngine] 实现类型。
     * @throws UnsupportedCapabilityException 当前运行时没有匹配类型的引擎。
     */
    suspend fun engine(type: KClass<out ImageEngine>): ImageEngine
}

/**
 * 按泛型类型获取当前已注册的具体生图引擎。
 *
 * @throws UnsupportedCapabilityException 当前运行时没有匹配类型的引擎。
 */
suspend inline fun <reified T : ImageEngine> AiImageIntegrate.engine(): T =
    engine(T::class) as? T ?: throw UnsupportedCapabilityException(emptySet())

/**
 * 默认生图聚合实现：在 [engines] 注册的后端之上完成路由、默认模型合并、重试与降级调度。
 *
 * 对外请优先使用同包中的无参工厂函数（定义见 `AiImageIntegrateFactory.kt`），以注入默认 SD WebUI 与本模块默认偏好；
 * [internal] 构造便于单测替换 [buildImageEngine] / [preferences]。
 *
 * @param defaultEngineConfigs 内置基线（常见本机地址等）；与每次 [engines] 传入列表按 sealed 子类合并，
 *   同类型时 [engines] 侧覆盖内置。若传 [emptyList] 则关闭内置基线，完全依赖后续 [engines]。
 * @param preferences 注入 [IntegrateEnginePreferences]，用于默认 [ImageEngineSpec] 与降级策略。
 * @param buildImageEngine 将 [ImageEngineConfig] 装配为 [ImageEngine]；生产环境为 SD WebUI 适配，单测可替换为 Stub。
 * @param listeners 生图调用观测监听器；当前默认工厂不注入监听器。
 */
internal class DefaultAiImageIntegrate(
    private val defaultEngineConfigs: List<ImageEngineConfig>,
    private val preferences: IntegrateEnginePreferences,
    private val buildImageEngine: (ImageEngineConfig) -> ImageEngine,
    private val listeners: List<AiInvocationListener> = emptyList(),
) : AiImageIntegrate {

    /** 保护 [runtime] 的读写及与 [generate]/[models] 收集路径的并发安全。 */
    private val mutex = Mutex()

    /**
     * 当前已装配的引擎选择器及「引擎 id → 配置」快照（用于 [withDefaultModelFrom]）。
     * 构造时用 [defaultEngineConfigs] 初始化；之后每次 [engines] 整体替换。
     */
    private var runtime: Runtime = buildRuntime(defaultEngineConfigs)

    /**
     * 按业务侧最新意图重建引擎实例：将 [defaultEngineConfigs] 与 [configs] 做 [mergeEngineConfigsWithDefaults] 后替换 [runtime]。
     *
     * @param configs 覆盖项；未出现的 sealed 子类仍沿用构造时传入的内置基线。传 [emptyList] 表示仅保留内置基线（若 [defaultEngineConfigs] 非空）。
     */
    override suspend fun engines(configs: List<ImageEngineConfig>) = mutex.withLock {
        val unique = mergeEngineConfigsWithDefaults(
            defaults = defaultEngineConfigs,
            overrides = configs,
        )
        runtime = buildRuntime(unique)
    }

    /**
     * 生成图像。
     *
     * 会基于当前 [runtime] 与 [spec] 解析目标引擎，并在请求未带 [ImageRequest.model] 时用配置 [ImageEngineConfig.defaultModel] 补全（见 [withDefaultModelFrom]）。
     *
     * @param request 通用生图请求。
     * @param spec 引擎路由描述；[ImageEngineSpec.Default] 时与 [preferences] 解析路径一致。
     * @return 符合 [ImageEvent] 契约的事件流。
     */
    override fun generate(
        request: ImageRequest,
        spec: ImageEngineSpec,
    ): Flow<ImageEvent> = flow {
        val snap = mutex.withLock { runtime }
        val resolvedSpec = resolveRequestedSpec(spec)
        val primaryEngine = engineFromResolvedSpec(resolvedSpec, snap.selector)
        val requestForAttempts = request.withDefaultModelFrom(
            engineId = primaryEngine.id,
            configsByEngineId = snap.configsByEngineId,
        )
        val fallbackPolicy = preferences.imageFallback()
        val primaryAttempt = primaryEngine.toAttempt(
            model = modelFromResolvedSpec(
                resolved = resolvedSpec,
                request = requestForAttempts,
            ),
            request = requestForAttempts,
        )
        val attempts = buildAttempts(
            primary = primaryAttempt,
            primaryId = primaryEngine.id,
            backupIds = fallbackPolicy.backupEngines,
            resolve = { backupId ->
                resolveBackupAttempt(
                    engineId = backupId,
                    request = requestForAttempts,
                    snap = snap,
                )
            },
        )
        collectWithFallback(
            attempts = attempts,
            policy = fallbackPolicy,
            invocationId = InvocationIds.next(),
            capability = request.primaryCapability(),
            listeners = listeners,
            engineIdOf = { it.id },
            errorOf = { event -> (event as? ImageEvent.Failed)?.error },
            isCompleted = { event -> event is ImageEvent.Completed },
            uncaughtFailureEvent = { error -> ImageEvent.Failed(error) },
        )
    }

    /**
     * 列出当前已注册引擎的可用模型：按 [spec] 限定引擎后拉取并跨引擎按模型名去重。
     *
     * @param spec 引擎路由描述；[ImageEngineSpec.Default] 时与 [generate] 使用同一套默认偏好解析。
     * @return 供 UI 展示与选择的 [ImageModelInfo] 列表。
     */
    override suspend fun models(spec: ImageEngineSpec): List<ImageModelInfo> {
        val snap = mutex.withLock { runtime }
        if (snap.selector.all().isEmpty()) return emptyList()
        val resolved = resolveRequestedSpec(spec)
        val deduped = LinkedHashMap<String, ImageModelInfo>()
        enginesForModelListing(
            resolved = resolved,
            selector = snap.selector,
        ).forEach { engine ->
            engine.models().forEach { model ->
                deduped.getOrPut(model.model.lowercase()) { model }
            }
        }
        return deduped.values.toList()
    }

    /**
     * 按具体引擎类型从当前运行时快照中获取第一个匹配实例。
     *
     * @param type 目标 [ImageEngine] 实现类型。
     * @throws UnsupportedCapabilityException 当前运行时没有匹配类型的引擎。
     */
    override suspend fun engine(type: KClass<out ImageEngine>): ImageEngine {
        val snap = mutex.withLock { runtime }
        return snap.selector.all().firstOrNull { engine -> type.isInstance(engine) }
            ?: throw UnsupportedCapabilityException(emptySet())
    }

    /**
     * 由合并后的配置列表构建 [Runtime]：创建各 [ImageEngine]、注册表与 [ImageEngineSelector]。
     *
     * @param unique 已为「默认 ∪ 覆盖」合并后的配置（同 [ImageEngineConfig] 子类以最后一次为准）。
     */
    private fun buildRuntime(unique: List<ImageEngineConfig>): Runtime {
        val imageEngines = unique.map(buildImageEngine)
        val registry = DefaultImageEngineRegistry(imageEngines)
        val selector = ImageEngineSelector(registry = registry)
        return Runtime(
            selector = selector,
            configsByEngineId = unique.associateBy { it.engineId },
        )
    }

    /**
     * 将传入的 [spec] 解析为聚合层实际使用的「请求侧」规格：
     * [ImageEngineSpec.Default] 时读取 [preferences.defaultImageSpec]，若仍为 Default 则退化为空能力集合的 [ImageEngineSpec.ByCapability]。
     */
    private fun resolveRequestedSpec(spec: ImageEngineSpec): ImageEngineSpec = when (spec) {
        ImageEngineSpec.Default -> when (val preferred = preferences.defaultImageSpec()) {
            ImageEngineSpec.Default -> ImageEngineSpec.ByCapability(emptySet())
            else -> preferred
        }

        else -> spec
    }

    /**
     * 根据已解析的 [resolved] 与当前 [selector]，得到本次生成请求的主引擎。
     */
    private fun engineFromResolvedSpec(
        resolved: ImageEngineSpec,
        selector: ImageEngineSelector,
    ): ImageEngine = when (resolved) {
        ImageEngineSpec.Default -> selector.select()
        is ImageEngineSpec.Explicit -> selector.select(preferredId = resolved.engineId)
        is ImageEngineSpec.ByCapability -> selector.select(required = resolved.required)
    }

    /**
     * 计算 [resolved] 进入具体引擎时使用的模型名。
     *
     * 显式规格的模型表示调用方本次主动选择，优先于请求模型与配置默认模型。
     */
    private fun modelFromResolvedSpec(
        resolved: ImageEngineSpec,
        request: ImageRequest,
    ): String? = when (resolved) {
        ImageEngineSpec.Default -> request.model
        is ImageEngineSpec.Explicit -> resolved.model ?: request.model
        is ImageEngineSpec.ByCapability -> request.model
    }

    /**
     * 解析一个备用引擎尝试。
     *
     * 备用引擎只保留请求上已有的模型名，不复用 [ImageEngineSpec.Explicit.model]。
     */
    private fun resolveBackupAttempt(
        engineId: EngineId,
        request: ImageRequest,
        snap: Runtime,
    ): EngineAttempt<ImageEngine, ImageEvent>? {
        val engine = snap.selector.all().firstOrNull { it.id == engineId } ?: return null
        return engine.toAttempt(model = request.model, request = request)
    }

    /**
     * 将当前 [ImageEngine] 与 [request] 包装为一次可调度尝试。
     */
    private fun ImageEngine.toAttempt(
        model: String?,
        request: ImageRequest,
    ): EngineAttempt<ImageEngine, ImageEvent> = EngineAttempt(
        engine = this,
        model = model,
        stream = { generate(request.withModel(model)) },
    )

    /**
     * 若模型名变化，则复制一份带新模型的请求。
     */
    private fun ImageRequest.withModel(model: String?): ImageRequest =
        if (this.model == model) this else copy(model = model)

    /**
     * 根据当前请求内容推断最主要的生图能力，用于观测元信息。
     */
    private fun ImageRequest.primaryCapability(): ImageCapability =
        when (source) {
            ImageSource.TextToImage -> {
                when {
                    postProcessors.any { it is ImagePostProcessor.BackgroundRemoval } ->
                        ImageCapability.BackgroundRemoval

                    postProcessors.any { it is ImagePostProcessor.Upscale } ->
                        ImageCapability.Upscale

                    postProcessors.any { it is ImagePostProcessor.FaceSwap } ->
                        ImageCapability.FaceSwap

                    postProcessors.any { it is ImagePostProcessor.FaceDetailer } ->
                        ImageCapability.FaceDetailer

                    controls.isNotEmpty() -> ImageCapability.ControlNet
                    else -> ImageCapability.TextToImage
                }
            }

            is ImageSource.ImageToImage -> ImageCapability.ImageToImage
            is ImageSource.Inpainting -> ImageCapability.Inpainting
        }

    /**
     * 根据 [resolved] 限定参与模型枚举的引擎集合。
     */
    private fun enginesForModelListing(
        resolved: ImageEngineSpec,
        selector: ImageEngineSelector,
    ): List<ImageEngine> = when (resolved) {
        ImageEngineSpec.Default -> selector.all()
        is ImageEngineSpec.Explicit -> {
            listOf(
                selector.select(
                    preferredId = resolved.engineId,
                ),
            )
        }

        is ImageEngineSpec.ByCapability -> {
            if (resolved.required.isEmpty()) {
                selector.all()
            } else {
                selector.all().filter { engine ->
                    engine.capabilities.containsAll(resolved.required)
                }
            }
        }
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
     * @property selector 与当前注册表绑定的引擎选择器，用于解析 [ImageEngineSpec]。
     * @property configsByEngineId 当前各 [EngineId] 对应的聚合层配置，供默认模型合并查询。
     */
    private data class Runtime(
        val selector: ImageEngineSelector,
        val configsByEngineId: Map<EngineId, ImageEngineConfig>,
    )

}

/**
 * 按 [ImageEngineConfig] 的 sealed 子类类型合并：键为 [KClass]，先写入 [defaults]（列表中同类后者覆盖前者），
 * 再写入 [overrides] 覆盖同类槽位；仅出现在 [defaults] 的类型得以保留。
 *
 * @return 合并结果的稳定迭代顺序：先按 [defaults] 首次出现的类型顺序，再纳入仅在 [overrides] 中出现的类型（替换时值更新）。
 */
internal fun mergeEngineConfigsWithDefaults(
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
