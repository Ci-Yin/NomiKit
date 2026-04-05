import com.android.build.api.dsl.ApkSigningConfig
import com.android.build.api.dsl.SigningConfig
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.PluginDependenciesSpecScope
import org.gradle.kotlin.dsl.getting
import org.gradle.plugin.use.PluginDependency
import org.gradle.plugin.use.PluginDependencySpec
import org.jetbrains.compose.ComposePlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

/**
 * KMP / Android 构建脚本 DSL 扩展：签名配置、`KotlinSourceSet` 常用名称的惰性访问、Compose 依赖入口，以及
 * 基于 [Provider] 的插件 id 解析。
 *
 * 供各模块 `build.gradle.kts` 与 `buildSrc` 预编译脚本使用。
 */

/**
 * 按固定名称 `release` 从 APK 签名配置容器中取出现有 [ApkSigningConfig]。
 */
val NamedDomainObjectContainer<out ApkSigningConfig>.release: ApkSigningConfig? get() = getByName("release")

/**
 * 新建名为 `release` 的 APK 签名配置并立即用 [configureAction] 配置。
 *
 * @return 创建后的 [ApkSigningConfig]
 */
fun NamedDomainObjectContainer<out ApkSigningConfig>.release(
    configureAction: SigningConfig.() -> Unit
): ApkSigningConfig = create("release") { configureAction() }


/**
 * 在 [KotlinDependencyHandler]（如 `dependencies { }` 块）中取得当前工程的 [ComposePlugin.Dependencies]，用于声明 Compose 相关坐标。
 */
val KotlinDependencyHandler.compose: ComposePlugin.Dependencies
    get() = ComposePlugin.Dependencies(project)

/**
 * 在 `kotlin { sourceSets { } }` 中通过属性语法访问 `desktopMain` 源集（对应 `jvm("desktop")` 目标的主源集）。
 */
val NamedDomainObjectContainer<KotlinSourceSet>.desktopMain: KotlinSourceSet
    get() {
//        val desktopMain = getByName("desktopMain")
        val desktopMain by getting
        return desktopMain
    }

/**
 * 在 `kotlin { sourceSets { } }` 中通过属性语法访问 `desktopTest` 源集。
 */
val NamedDomainObjectContainer<KotlinSourceSet>.desktopTest: KotlinSourceSet
    get() {
        val desktopTest by getting
        return desktopTest
    }

/**
 * 在 `kotlin { sourceSets { } }` 中通过属性语法访问 `skikoMain` 源集（Skiko 相关分层中的主源集）。
 */
val NamedDomainObjectContainer<KotlinSourceSet>.skikoMain: KotlinSourceSet
    get() {
        val skikoMain by getting
        return skikoMain
    }

/**
 * 在 `kotlin { sourceSets { } }` 中通过属性语法访问 `skikoTest` 源集。
 */
val NamedDomainObjectContainer<KotlinSourceSet>.skikoTest: KotlinSourceSet
    get() {
        val skikoTest by getting
        return skikoTest
    }


/**
 * 在 `kotlin { sourceSets { } }` 中通过属性语法访问 `webMain` 源集（JS / Wasm 等 Web 相关分层中的主源集）。
 */
val NamedDomainObjectContainer<KotlinSourceSet>.webMain: KotlinSourceSet
    get() {
        val webMain by getting
        return webMain
    }

/**
 * 在 `kotlin { sourceSets { } }` 中通过属性语法访问 `webTest` 源集。
 */
val NamedDomainObjectContainer<KotlinSourceSet>.webTest: KotlinSourceSet
    get() {
        val webTest by getting
        return webTest
    }

/**
 * 从 Version Catalog 等提供的 [PluginDependency] [Provider] 解析插件 id，并委托给 [PluginDependenciesSpecScope.id]。
 *
 * 便于在 `plugins { }` 中书写 `id(libs.plugins.xxx)` 形式的非 `alias` 场景（具体是否适用取决于当前 Gradle/插件 DSL 约束）。
 *
 * @param notation 解析后取 [PluginDependency.pluginId] 作为插件标识
 */
fun PluginDependenciesSpecScope.id(notation: Provider<PluginDependency>): PluginDependencySpec {
    return id(notation.get().pluginId)
}
