package ciyin.ai.core.registry

import ciyin.ai.core.capability.ChatCapability
import ciyin.ai.core.engine.ChatEngine
import ciyin.ai.core.engine.EngineId

/**
 * [ChatEngineRegistry] 的默认实现：构造时一次性吃下全部引擎实例。
 *
 * 由调用方在装配点构造，例如：
 * ```
 * DefaultChatEngineRegistry(listOf(openAiEngine, ollamaEngine))
 * ```
 *
 * 装配方式不做规定（Koin `getAll<ChatEngine>()` / 手写 `listOf(...)` 都可），
 * 本模块不感知任何 DI 框架。
 *
 * 校验规则：
 * - **要求 `engines` 内 `id.value` 互不重复**；重复时直接抛 [IllegalArgumentException]，
 *   避免 [get] 返回的引擎"取决于注册顺序"这种隐式行为。
 *
 * @param engines 被注册的引擎列表，注册顺序即遍历顺序。
 */
class DefaultChatEngineRegistry(engines: List<ChatEngine>) : ChatEngineRegistry {

    private val ordered: List<ChatEngine> = engines.toList()
    private val byId: Map<String, ChatEngine>

    init {
        val duplicates = ordered.groupBy { it.id.value }.filter { it.value.size > 1 }.keys
        require(duplicates.isEmpty()) {
            "DefaultChatEngineRegistry 检测到重复 EngineId: $duplicates"
        }
        byId = ordered.associateBy { it.id.value }
    }

    override fun all(): List<ChatEngine> = ordered

    override fun get(id: EngineId): ChatEngine? = byId[id.value]

    override fun findByCapability(vararg required: ChatCapability): List<ChatEngine> {
        if (required.isEmpty()) return ordered
        val requiredSet = required.toSet()
        return ordered.filter { it.capabilities.containsAll(requiredSet) }
    }
}
