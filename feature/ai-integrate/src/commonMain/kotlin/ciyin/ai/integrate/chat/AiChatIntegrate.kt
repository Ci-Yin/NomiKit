package ciyin.ai.integrate.chat

import ciyin.ai.core.chat.ChatEvent
import ciyin.ai.core.chat.ChatModelInfo
import ciyin.ai.core.chat.ChatRequest
import ciyin.ai.core.engine.ChatEngine
import ciyin.ai.core.engine.EngineId
import ciyin.ai.core.registry.ChatEngineSelector
import ciyin.ai.core.registry.DefaultChatEngineRegistry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * 聊天聚合入口：在 [engines] 注册的后端之上完成路由、默认模型合并与模型列表去重。
 *
 * 默认实现保持薄装配，不做自动重试或备用引擎降级；具体 HTTP 协议由聊天引擎模块承担。
 *
 * @param defaultEngineConfigs 构造时注入的默认聊天引擎配置。
 * @param buildChatEngine 将 [ChatEngineConfig] 装配为 [ChatEngine] 的函数。
 */
class AiChatIntegrate internal constructor(
    private val defaultEngineConfigs: List<ChatEngineConfig>,
    private val buildChatEngine: (ChatEngineConfig) -> ChatEngine,
) {

    /** 保护 [runtime] 的读写并保证调用时看到一致快照。 */
    private val mutex = Mutex()

    /** 当前已装配的聊天引擎选择器及「引擎 id 到配置」快照。 */
    private var runtime: Runtime = buildRuntime(defaultEngineConfigs)

    /**
     * 按业务侧最新意图重建聊天引擎实例。
     *
     * @param configs 覆盖项；与 [defaultEngineConfigs] 按 [EngineId] 合并，后出现的配置覆盖同 ID 配置。
     */
    suspend fun engines(configs: List<ChatEngineConfig>) = mutex.withLock {
        val unique = mergeChatEngineConfigsWithDefaults(
            defaults = defaultEngineConfigs,
            overrides = configs,
        )
        runtime = buildRuntime(unique)
    }

    /**
     * 发起一次聊天请求。
     *
     * @param request 通用聊天请求。
     * @param spec 引擎路由描述；默认使用注册顺序中的首个可用聊天引擎。
     * @return 符合 [ChatEvent] 契约的事件流。
     */
    fun stream(
        request: ChatRequest,
        spec: ChatEngineSpec = ChatEngineSpec.Default,
    ): Flow<ChatEvent> = flow {
        val snap = mutex.withLock { runtime }
        val primaryEngine = engineFromSpec(
            spec = spec,
            selector = snap.selector,
        )
        val requestWithDefaultModel = request.withDefaultModelFrom(
            engineId = primaryEngine.id,
            configsByEngineId = snap.configsByEngineId,
        )
        val requestForEngine = requestWithDefaultModel.withModel(
            model = modelFromSpec(
                spec = spec,
                request = requestWithDefaultModel,
            ),
        )
        primaryEngine.stream(requestForEngine).collect { event -> emit(event) }
    }

    /**
     * 列出当前已注册聊天引擎的可用模型，并按模型名小写去重。
     *
     * @param spec 引擎路由描述；默认枚举全部已注册聊天引擎。
     * @return 供 UI 展示与选择的 [ChatModelInfo] 列表。
     */
    suspend fun models(spec: ChatEngineSpec = ChatEngineSpec.Default): List<ChatModelInfo> {
        val snap = mutex.withLock { runtime }
        if (snap.selector.all().isEmpty()) return emptyList()
        val deduped = LinkedHashMap<String, ChatModelInfo>()
        enginesForModelListing(
            spec = spec,
            selector = snap.selector,
        ).forEach { engine ->
            engine.models().forEach { model ->
                deduped.getOrPut(model.model.lowercase()) { model }
            }
        }
        return deduped.values.toList()
    }

    /**
     * 由合并后的配置列表构建运行时快照。
     *
     * @param unique 已按 [EngineId] 去重后的聊天引擎配置。
     */
    private fun buildRuntime(unique: List<ChatEngineConfig>): Runtime {
        val chatEngines = unique.map(buildChatEngine)
        val registry = DefaultChatEngineRegistry(chatEngines)
        return Runtime(
            selector = ChatEngineSelector(registry = registry),
            configsByEngineId = unique.associateBy { it.engineId },
        )
    }

    /**
     * 根据 [spec] 与当前 [selector] 选择目标聊天引擎。
     */
    private fun engineFromSpec(
        spec: ChatEngineSpec,
        selector: ChatEngineSelector,
    ): ChatEngine = when (spec) {
        ChatEngineSpec.Default -> selector.select()
        is ChatEngineSpec.Explicit -> selector.select(preferredId = spec.engineId)
        is ChatEngineSpec.ByCapability -> selector.select(required = spec.required)
    }

    /**
     * 计算进入具体引擎时使用的模型名。
     *
     * 显式规格的模型表示调用方本次主动选择，优先于请求模型与配置默认模型。
     */
    private fun modelFromSpec(
        spec: ChatEngineSpec,
        request: ChatRequest,
    ): String? = when (spec) {
        ChatEngineSpec.Default -> request.model
        is ChatEngineSpec.Explicit -> spec.model ?: request.model
        is ChatEngineSpec.ByCapability -> request.model
    }

    /**
     * 根据 [spec] 限定参与模型枚举的引擎集合。
     */
    private fun enginesForModelListing(
        spec: ChatEngineSpec,
        selector: ChatEngineSelector,
    ): List<ChatEngine> = when (spec) {
        ChatEngineSpec.Default -> selector.all()
        is ChatEngineSpec.Explicit -> listOf(selector.select(preferredId = spec.engineId))
        is ChatEngineSpec.ByCapability -> {
            if (spec.required.isEmpty()) {
                selector.all()
            } else {
                selector.all().filter { engine ->
                    engine.capabilities.containsAll(spec.required)
                }
            }
        }
    }

    /**
     * 若 [model] 变化，则复制一份带新模型的请求。
     */
    private fun ChatRequest.withModel(model: String?): ChatRequest =
        if (this.model == model) this else copy(model = model)

    /**
     * 若请求未指定模型且配置含默认模型，则补全 [ChatRequest.model]。
     */
    private fun ChatRequest.withDefaultModelFrom(
        engineId: EngineId,
        configsByEngineId: Map<EngineId, ChatEngineConfig>,
    ): ChatRequest {
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
     * @property selector 与当前注册表绑定的聊天引擎选择器。
     * @property configsByEngineId 当前各 [EngineId] 对应的配置，供默认模型合并查询。
     */
    private data class Runtime(
        val selector: ChatEngineSelector,
        val configsByEngineId: Map<EngineId, ChatEngineConfig>,
    )
}

/**
 * 按 [EngineId] 合并聊天引擎配置：默认配置先写入，覆盖配置后写入。
 *
 * @return 合并后的稳定列表；同 ID 后出现的配置覆盖前者。
 */
internal fun mergeChatEngineConfigsWithDefaults(
    defaults: List<ChatEngineConfig>,
    overrides: List<ChatEngineConfig>,
): List<ChatEngineConfig> {
    val byId = LinkedHashMap<EngineId, ChatEngineConfig>()
    for (cfg in defaults) {
        byId[cfg.engineId] = cfg
    }
    for (cfg in overrides) {
        byId[cfg.engineId] = cfg
    }
    return byId.values.toList()
}
