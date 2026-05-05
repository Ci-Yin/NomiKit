package ciyin.ai.integrate.image

import ciyin.ai.core.engine.EngineId

/**
 * 聚合模块内使用的固定生图 [EngineId]；新增后端时在此追加常量。
 *
 * 连接信息（主机、端口）**不得**编入 id，以便在变更 [ImageEngineConfig.baseUrl] 时保持 id 稳定。
 */
object IntegrateImageEngineIds {

    /** 首版 SD WebUI 槽位对应的稳定引擎标识。 */
    val sdWebUi: EngineId = EngineId("sdwebui:primary")
}
