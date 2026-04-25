package ciyin.ai.core.registry

import ciyin.ai.core.capability.ImageCapability
import ciyin.ai.core.engine.EngineId
import ciyin.ai.core.engine.ImageEngine

/**
 * [ImageEngineRegistry] 的默认实现：构造时一次性吃下全部引擎实例。
 *
 * 行为与 [DefaultChatEngineRegistry] 同构：注册顺序即遍历顺序，重复 ID 直接拒绝。
 *
 * @param engines 被注册的引擎列表，注册顺序即遍历顺序。
 */
class DefaultImageEngineRegistry(engines: List<ImageEngine>) : ImageEngineRegistry {

    private val ordered: List<ImageEngine> = engines.toList()
    private val byId: Map<String, ImageEngine>

    init {
        val duplicates = ordered.groupBy { it.id.value }.filter { it.value.size > 1 }.keys
        require(duplicates.isEmpty()) {
            "DefaultImageEngineRegistry 检测到重复 EngineId: $duplicates"
        }
        byId = ordered.associateBy { it.id.value }
    }

    override fun all(): List<ImageEngine> = ordered

    override fun get(id: EngineId): ImageEngine? = byId[id.value]

    override fun findByCapability(vararg required: ImageCapability): List<ImageEngine> {
        if (required.isEmpty()) return ordered
        val requiredSet = required.toSet()
        return ordered.filter { it.capabilities.containsAll(requiredSet) }
    }
}
