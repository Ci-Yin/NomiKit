package ciyin.ai.core.engine

/**
 * 引擎所处的运行时环境。
 *
 * 业务层在 UI 上做"本地 / 云端"分组、限速策略、计费策略时使用本枚举。
 * 该枚举与具体厂商无关，单纯描述"这个引擎跑在哪儿"。
 */
enum class EngineRuntime {

    /** 远程云服务（OpenAI / Anthropic / OpenRouter 等）。 */
    RemoteCloud,

    /** 远程自托管服务（本机或局域网内的 SD WebUI / Ollama / vLLM 等）。 */
    RemoteSelfHosted,

    /** 进程内本地推理（llama.cpp / onnxruntime / Core ML 等）。 */
    LocalEmbedded,
}
