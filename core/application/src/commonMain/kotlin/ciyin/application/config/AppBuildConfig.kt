package ciyin.application.config

/**
 * 应用在编译期注入、运行时消费的跨平台构建配置。
 */
interface AppBuildConfig {
    /**
     * 应用稳定 ID，例如 `com.ciyin.nomikit`。
     */
    val id: String

    /**
     * 应用展示版本名。
     */
    val versionName: String

    /**
     * 额外运行时配置，key 不包含 `app.config.` 前缀。
     */
    val properties: Map<String, String>

    /**
     * 当前平台实际注入的构建配置。
     */
    companion object : AppBuildConfig by currentAppBuildConfigImpl
}

/**
 * 当前平台实际生成的应用构建配置实现。
 */
internal expect val currentAppBuildConfigImpl: AppBuildConfig
