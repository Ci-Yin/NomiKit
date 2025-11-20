import org.gradle.api.Project
import java.io.File
import java.util.Properties

fun Project.getProperty(name: String) =
    getPropertyOrNull(name) ?: error("Property $name not found")

fun Project.getPropertyOrNull(name: String) =
    getLocalProperty(name)
        ?: System.getProperty(name)
        ?: System.getenv(name)
        ?: findProperty(name)?.toString()
        ?: properties[name]?.toString()
        ?: extensions.extraProperties.runCatching { get(name).toString() }.getOrNull()


val Project.localPropertiesFile: File get() = project.rootProject.file("local.properties")

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


fun Project.getIntProperty(name: String) = getProperty(name).toInt()

val Project.enableDesktop
    get() = getPropertyOrNull("multiplatform.enable.desktop")?.toBooleanStrict() ?: false

val Project.enableIos
    get() = getPropertyOrNull("multiplatform.enable.ios")?.toBooleanStrict() ?: false

val Project.enableJs
    get() = getPropertyOrNull("multiplatform.enable.js")?.toBooleanStrict() ?: false

val Project.enableWasmJs
    get() = getPropertyOrNull("multiplatform.enable.wasmJs")?.toBooleanStrict() ?: false

val Project.enableWeb
    get() = getPropertyOrNull("multiplatform.enable.web")?.toBooleanStrict() ?: false
