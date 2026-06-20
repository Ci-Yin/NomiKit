import org.gradle.api.Project
import java.io.File
import java.util.Properties

/**
 * 按项目约定读取必填 Gradle 属性。
 */
fun Project.getProperty(name: String) =
    getPropertyOrNull(name) ?: error("Property $name not found")

/**
 * 按优先级读取可选 Gradle 属性。
 *
 * 优先级为：命令行 `-P`、JVM system property、环境变量、`local.properties`、
 * `gradle.properties`、extra properties。
 */
fun Project.getPropertyOrNull(name: String) =
    gradle.startParameter.projectProperties[name]?.toString()
        ?: System.getProperty(name)
        ?: System.getenv(name)
        ?: getLocalProperty(name)
        ?: getGradleProperty(name)
        ?: extensions.extraProperties.runCatching { get(name).toString() }.getOrNull()

/**
 * 根工程的 `local.properties` 文件。
 */
val Project.localPropertiesFile: File get() = project.rootProject.file("local.properties")

/**
 * 根工程的 `gradle.properties` 文件。
 */
val Project.gradlePropertiesFile: File get() = project.rootProject.file("gradle.properties")

/**
 * 从 `local.properties` 读取单个属性；文件不存在时创建空文件以保持既有行为。
 */
fun Project.getLocalProperty(key: String): String? {
    return if (localPropertiesFile.exists()) {
        val properties = Properties()
        localPropertiesFile.inputStream().buffered().use { input ->
            properties.load(input)
        }
        properties.getProperty(key)
    } else {
        localPropertiesFile.createNewFile()
        null
    }
}

/**
 * 从根工程 `gradle.properties` 读取单个属性。
 */
fun Project.getGradleProperty(key: String): String? =
    loadGradlePropertiesOrNull()?.getProperty(key)

/**
 * 按 [getPropertyOrNull] 对应的优先级合并指定前缀的配置项，并移除 key 前缀。
 */
fun Project.getPropertiesByPrefix(prefix: String): Map<String, String> {
    val mergedProperties = linkedMapOf<String, String>()
    mergedProperties.putPrefixedValues(
        prefix = prefix,
        values = extensions.extraProperties.properties,
    )
    loadGradlePropertiesOrNull()?.let { gradleProperties ->
        mergedProperties.putPrefixedValues(
            prefix = prefix,
            values = gradleProperties.stringPropertyNames()
                .associateWith(gradleProperties::getProperty),
        )
    }
    loadLocalPropertiesOrNull()?.let { localProperties ->
        mergedProperties.putPrefixedValues(
            prefix = prefix,
            values = localProperties.stringPropertyNames()
                .associateWith(localProperties::getProperty),
        )
    }
    mergedProperties.putPrefixedValues(
        prefix = prefix,
        values = System.getenv(),
    )
    mergedProperties.putPrefixedValues(
        prefix = prefix,
        values = System.getProperties().stringPropertyNames().associateWith(System::getProperty),
    )
    mergedProperties.putPrefixedValues(
        prefix = prefix,
        values = gradle.startParameter.projectProperties,
    )
    return mergedProperties.toSortedMap()
}

/**
 * 读取根工程 `gradle.properties`，不存在时返回空。
 */
private fun Project.loadGradlePropertiesOrNull(): Properties? {
    if (!gradlePropertiesFile.exists()) return null
    return Properties().apply {
        gradlePropertiesFile.inputStream().buffered().use { input ->
            load(input)
        }
    }
}

/**
 * 读取 `local.properties`，不存在时不创建文件。
 */
private fun Project.loadLocalPropertiesOrNull(): Properties? {
    if (!localPropertiesFile.exists()) return null
    return Properties().apply {
        localPropertiesFile.inputStream().buffered().use { input ->
            load(input)
        }
    }
}

/**
 * 将带指定前缀的配置项合并到当前 Map，并移除 key 前缀。
 */
private fun MutableMap<String, String>.putPrefixedValues(
    prefix: String,
    values: Map<*, *>,
) {
    values.forEach { (rawKey, rawValue) ->
        val key = rawKey?.toString() ?: return@forEach
        if (key.startsWith(prefix)) {
            this[key.removePrefix(prefix)] = rawValue?.toString().orEmpty()
        }
    }
}

/**
 * 按项目约定读取整型 Gradle 属性。
 */
fun Project.getIntProperty(name: String) = getProperty(name).toInt()

/**
 * 是否启用 Desktop KMP 目标。
 */
val Project.enableDesktop
    get() = getPropertyOrNull("multiplatform.enable.desktop")?.toBooleanStrict() ?: false

/**
 * 是否启用 iOS KMP 目标。
 */
val Project.enableIos
    get() = getPropertyOrNull("multiplatform.enable.ios")?.toBooleanStrict() ?: false

/**
 * 是否启用 JS KMP 目标。
 */
val Project.enableJs
    get() = getPropertyOrNull("multiplatform.enable.js")?.toBooleanStrict() ?: false

/**
 * 是否启用 Wasm JS KMP 目标。
 */
val Project.enableWasmJs
    get() = getPropertyOrNull("multiplatform.enable.wasmJs")?.toBooleanStrict() ?: false

/**
 * 是否启用 Web KMP 目标。
 */
val Project.enableWeb
    get() = getPropertyOrNull("multiplatform.enable.web")?.toBooleanStrict() ?: false
