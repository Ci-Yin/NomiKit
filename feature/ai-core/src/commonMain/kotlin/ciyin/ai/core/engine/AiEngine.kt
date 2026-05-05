package ciyin.ai.core.engine

import ciyin.ai.core.capability.AiCapability

/**
 * 所有 AI 引擎的共同父接口。
 *
 * 该接口只承载"治理性"信息：[id]、[provider]、[runtime] 与 [capabilities]，
 * **不**暴露任何"调用入口"。具体调用入口由子接口（[ChatEngine] / [ImageEngine] 等）提供。
 *
 * 之所以保留共同父接口，是为了让 `Registry` / [ciyin.ai.core.registry.ChatEngineSelector] / [ciyin.ai.core.registry.ImageEngineSelector] 这类治理组件能以
 * 统一形态遍历"所有已注册引擎"，做能力筛选、ID 查找等操作。
 */
interface AiEngine {

    /** 引擎实例的全局唯一 ID。 */
    val id: EngineId

    /**
     * 引擎所属厂商或协议的简短名称（例如 `"openai"`、`"sdwebui"`、`"ollama"`）。
     * 仅作展示与日志归类用途，不参与查找。
     */
    val provider: String

    /** 引擎运行时环境，参见 [EngineRuntime]。 */
    val runtime: EngineRuntime

    /**
     * 引擎已声明支持的能力集合。
     *
     * 这是**静态声明**：调用方可以据此快速判断"这个引擎是否能做某事"，
     * 但具体能否成功仍取决于运行时（如远程模型版本、本地资源），
     * 因此 [ChatEngine.validate] / [ImageEngine.validate] 提供运行时校验入口。
     */
    val capabilities: Set<AiCapability>
}
