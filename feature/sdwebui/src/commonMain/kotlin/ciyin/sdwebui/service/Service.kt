package ciyin.sdwebui.service

import ciyin.sdwebui.client.Client
import kotlinx.serialization.json.Json

/**
 * 各具体 Service 实现的公共基类，持有基址、[Client] 与 [Json]。
 */
abstract class Service {

    abstract val baseUrl: String

    abstract val client: Client

    abstract val json: Json
}
