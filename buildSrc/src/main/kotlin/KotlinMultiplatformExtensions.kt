// buildSrc/src/main/kotlin/KotlinMultiplatformExtensions.kt
import com.android.build.api.dsl.ApkSigningConfig
import com.android.build.api.dsl.SigningConfig
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.PluginDependenciesSpecScope
import org.gradle.kotlin.dsl.getting
import org.gradle.plugin.use.PluginDependency
import org.gradle.plugin.use.PluginDependencySpec
import org.jetbrains.compose.ComposePlugin
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet


val NamedDomainObjectContainer<out ApkSigningConfig>.release get() = getByName("release")

/**
 * 创建 release 签名配置
 */
fun NamedDomainObjectContainer<out ApkSigningConfig>.release(
    configureAction: SigningConfig.() -> Unit
): ApkSigningConfig = create("release") { configureAction() }


/**
 * 在 KotlinDependencyHandler 中快速访问 Compose 依赖
 */
val KotlinDependencyHandler.compose: ComposePlugin.Dependencies
    get() = ComposePlugin.Dependencies(project)

val NamedDomainObjectContainer<KotlinSourceSet>.desktopMain: KotlinSourceSet
    get() {
//        val desktopMain = getByName("desktopMain")
        val desktopMain by getting
        return desktopMain
    }

val NamedDomainObjectContainer<KotlinSourceSet>.desktopTest: KotlinSourceSet
    get() {
        val desktopTest by getting
        return desktopTest
    }

val NamedDomainObjectContainer<KotlinSourceSet>.skikoMain: KotlinSourceSet
    get() {
        val skikoMain by getting
        return skikoMain
    }

val NamedDomainObjectContainer<KotlinSourceSet>.skikoTest: KotlinSourceSet
    get() {
        val skikoTest by getting
        return skikoTest
    }


val NamedDomainObjectContainer<KotlinSourceSet>.webMain: KotlinSourceSet
    get() {
        val webMain by getting
        return webMain
    }

val NamedDomainObjectContainer<KotlinSourceSet>.webTest: KotlinSourceSet
    get() {
        val webTest by getting
        return webTest
    }

/**
 * 配置 Kotlin Multiplatform Extension
 */
private fun Project.kotlin(configure: Action<KotlinMultiplatformExtension>) {
    extensions.configure(KotlinMultiplatformExtension::class.java, configure)
}

fun PluginDependenciesSpecScope.id(notation: Provider<PluginDependency>): PluginDependencySpec {
    return id(notation.get().pluginId)
}

// ==================== CommonMain ====================

/**
 * 配置 commonMain 依赖
 */
fun Project.commonMainDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.commonMain.dependencies(configure) }
}

// ==================== Android ====================

/**
 * 配置 androidMain 依赖
 */
fun Project.androidMainDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.androidMain.dependencies(configure) }
}

// ==================== Desktop ====================

/**
 * 配置 desktopMain 依赖
 */
fun Project.desktopMainDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.desktopMain.dependencies(configure) }
}

// ==================== iOS ====================

/**
 * 配置 iosMain 依赖
 */
fun Project.iosMainDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.iosMain.dependencies(configure) }
}

/**
 * 配置 iosArm64Main 依赖
 */
fun Project.iosArm64MainDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.iosArm64Main.dependencies(configure) }
}

/**
 * 配置 iosSimulatorArm64Main 依赖
 */
fun Project.iosSimulatorArm64MainDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.iosSimulatorArm64Main.dependencies(configure) }
}

/**
 * 配置 iosX64Main 依赖
 */
fun Project.iosX64MainDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.iosX64Main.dependencies(configure) }
}

// ==================== JVM ====================

/**
 * 配置 jvmMain 依赖
 */
fun Project.jvmMainDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.jvmMain.dependencies(configure) }
}

// ==================== JS ====================

/**
 * 配置 jsMain 依赖
 */
fun Project.jsMainDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.jsMain.dependencies(configure) }
}

// ==================== WASM ====================

/**
 * 配置 wasmJsMain 依赖
 */
fun Project.wasmJsMainDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.wasmJsMain.dependencies(configure) }
}

/**
 * 配置 wasmWasiMain 依赖
 */
fun Project.wasmWasiMainDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.wasmWasiMain.dependencies(configure) }
}

// ==================== Native ====================

/**
 * 配置 nativeMain 依赖
 */
fun Project.nativeMainDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.nativeMain.dependencies(configure) }
}

// ==================== Apple ====================

/**
 * 配置 appleMain 依赖
 */
fun Project.appleMainDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.appleMain.dependencies(configure) }
}

// ==================== macOS ====================

/**
 * 配置 macosMain 依赖
 */
fun Project.macosMainDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.macosMain.dependencies(configure) }
}

/**
 * 配置 macosArm64Main 依赖
 */
fun Project.macosArm64MainDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.macosArm64Main.dependencies(configure) }
}

/**
 * 配置 macosX64Main 依赖
 */
fun Project.macosX64MainDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.macosX64Main.dependencies(configure) }
}

// ==================== tvOS ====================

/**
 * 配置 tvosMain 依赖
 */
fun Project.tvosMainDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.tvosMain.dependencies(configure) }
}

/**
 * 配置 tvosArm64Main 依赖
 */
fun Project.tvosArm64MainDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.tvosArm64Main.dependencies(configure) }
}

/**
 * 配置 tvosSimulatorArm64Main 依赖
 */
fun Project.tvosSimulatorArm64MainDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.tvosSimulatorArm64Main.dependencies(configure) }
}

/**
 * 配置 tvosX64Main 依赖
 */
fun Project.tvosX64MainDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.tvosX64Main.dependencies(configure) }
}

// ==================== watchOS ====================

/**
 * 配置 watchosMain 依赖
 */
fun Project.watchosMainDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.watchosMain.dependencies(configure) }
}

/**
 * 配置 watchosArm32Main 依赖
 */
fun Project.watchosArm32MainDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.watchosArm32Main.dependencies(configure) }
}

/**
 * 配置 watchosArm64Main 依赖
 */
fun Project.watchosArm64MainDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.watchosArm64Main.dependencies(configure) }
}

/**
 * 配置 watchosDeviceArm64Main 依赖
 */
fun Project.watchosDeviceArm64MainDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.watchosDeviceArm64Main.dependencies(configure) }
}

/**
 * 配置 watchosSimulatorArm64Main 依赖
 */
fun Project.watchosSimulatorArm64MainDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.watchosSimulatorArm64Main.dependencies(configure) }
}

/**
 * 配置 watchosX64Main 依赖
 */
fun Project.watchosX64MainDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.watchosX64Main.dependencies(configure) }
}

// ==================== Linux ====================

/**
 * 配置 linuxMain 依赖
 */
fun Project.linuxMainDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.linuxMain.dependencies(configure) }
}

/**
 * 配置 linuxArm32HfpMain 依赖
 */
fun Project.linuxArm32HfpMainDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.linuxArm32HfpMain.dependencies(configure) }
}

/**
 * 配置 linuxArm64Main 依赖
 */
fun Project.linuxArm64MainDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.linuxArm64Main.dependencies(configure) }
}

/**
 * 配置 linuxX64Main 依赖
 */
fun Project.linuxX64MainDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.linuxX64Main.dependencies(configure) }
}

// ==================== Windows (MinGW) ====================

/**
 * 配置 mingwMain 依赖
 */
fun Project.mingwMainDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.mingwMain.dependencies(configure) }
}

/**
 * 配置 mingwX64Main 依赖
 */
fun Project.mingwX64MainDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.mingwX64Main.dependencies(configure) }
}

// ==================== Android Native ====================

/**
 * 配置 androidNativeMain 依赖
 */
fun Project.androidNativeMainDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.androidNativeMain.dependencies(configure) }
}

/**
 * 配置 androidNativeArm32Main 依赖
 */
fun Project.androidNativeArm32MainDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.androidNativeArm32Main.dependencies(configure) }
}

/**
 * 配置 androidNativeArm64Main 依赖
 */
fun Project.androidNativeArm64MainDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.androidNativeArm64Main.dependencies(configure) }
}

/**
 * 配置 androidNativeX64Main 依赖
 */
fun Project.androidNativeX64MainDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.androidNativeX64Main.dependencies(configure) }
}

/**
 * 配置 androidNativeX86Main 依赖
 */
fun Project.androidNativeX86MainDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.androidNativeX86Main.dependencies(configure) }
}

// ==================== skiko ====================

/**
 * 配置 skikoMain 依赖
 */
fun Project.skikoMainDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.skikoMain.dependencies(configure) }
}

// ==================== web ====================

/**
 * 配置 webMain 依赖
 */
fun Project.webMainDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.webMain.dependencies(configure) }
}


// ==================== Test SourceSets ====================

/**
 * 配置 commonTest 依赖
 */
fun Project.commonTestDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.commonTest.dependencies(configure) }
}

/**
 * 配置 androidUnitTest 依赖
 */
fun Project.androidUnitTestDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.androidUnitTest.dependencies(configure) }
}

/**
 * 配置 androidInstrumentedTest 依赖
 */
fun Project.androidInstrumentedTestDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.androidInstrumentedTest.dependencies(configure) }
}

/**
 * 配置 desktopTest 依赖
 */
fun Project.desktopTestDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.desktopTest.dependencies(configure) }
}

/**
 * 配置 iosTest 依赖
 */
fun Project.iosTestDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.iosTest.dependencies(configure) }
}

/**
 * 配置 iosArm64Test 依赖
 */
fun Project.iosArm64TestDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.iosArm64Test.dependencies(configure) }
}

/**
 * 配置 iosSimulatorArm64Test 依赖
 */
fun Project.iosSimulatorArm64TestDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.iosSimulatorArm64Test.dependencies(configure) }
}

/**
 * 配置 iosX64Test 依赖
 */
fun Project.iosX64TestDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.iosX64Test.dependencies(configure) }
}

/**
 * 配置 jvmTest 依赖
 */
fun Project.jvmTestDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.jvmTest.dependencies(configure) }
}

/**
 * 配置 jsTest 依赖
 */
fun Project.jsTestDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.jsTest.dependencies(configure) }
}

/**
 * 配置 wasmJsTest 依赖
 */
fun Project.wasmJsTestDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.wasmJsTest.dependencies(configure) }
}

/**
 * 配置 wasmWasiTest 依赖
 */
fun Project.wasmWasiTestDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.wasmWasiTest.dependencies(configure) }
}

/**
 * 配置 nativeTest 依赖
 */
fun Project.nativeTestDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.nativeTest.dependencies(configure) }
}

/**
 * 配置 appleTest 依赖
 */
fun Project.appleTestDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.appleTest.dependencies(configure) }
}

/**
 * 配置 macosTest 依赖
 */
fun Project.macosTestDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.macosTest.dependencies(configure) }
}

/**
 * 配置 macosArm64Test 依赖
 */
fun Project.macosArm64TestDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.macosArm64Test.dependencies(configure) }
}

/**
 * 配置 macosX64Test 依赖
 */
fun Project.macosX64TestDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.macosX64Test.dependencies(configure) }
}

/**
 * 配置 tvosTest 依赖
 */
fun Project.tvosTestDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.tvosTest.dependencies(configure) }
}

/**
 * 配置 tvosArm64Test 依赖
 */
fun Project.tvosArm64TestDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.tvosArm64Test.dependencies(configure) }
}

/**
 * 配置 tvosSimulatorArm64Test 依赖
 */
fun Project.tvosSimulatorArm64TestDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.tvosSimulatorArm64Test.dependencies(configure) }
}

/**
 * 配置 tvosX64Test 依赖
 */
fun Project.tvosX64TestDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.tvosX64Test.dependencies(configure) }
}

/**
 * 配置 watchosTest 依赖
 */
fun Project.watchosTestDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.watchosTest.dependencies(configure) }
}

/**
 * 配置 watchosArm32Test 依赖
 */
fun Project.watchosArm32TestDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.watchosArm32Test.dependencies(configure) }
}

/**
 * 配置 watchosArm64Test 依赖
 */
fun Project.watchosArm64TestDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.watchosArm64Test.dependencies(configure) }
}

/**
 * 配置 watchosDeviceArm64Test 依赖
 */
fun Project.watchosDeviceArm64TestDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.watchosDeviceArm64Test.dependencies(configure) }
}

/**
 * 配置 watchosSimulatorArm64Test 依赖
 */
fun Project.watchosSimulatorArm64TestDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.watchosSimulatorArm64Test.dependencies(configure) }
}

/**
 * 配置 watchosX64Test 依赖
 */
fun Project.watchosX64TestDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.watchosX64Test.dependencies(configure) }
}

/**
 * 配置 linuxTest 依赖
 */
fun Project.linuxTestDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.linuxTest.dependencies(configure) }
}

/**
 * 配置 linuxArm32HfpTest 依赖
 */
fun Project.linuxArm32HfpTestDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.linuxArm32HfpTest.dependencies(configure) }
}

/**
 * 配置 linuxArm64Test 依赖
 */
fun Project.linuxArm64TestDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.linuxArm64Test.dependencies(configure) }
}

/**
 * 配置 linuxX64Test 依赖
 */
fun Project.linuxX64TestDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.linuxX64Test.dependencies(configure) }
}

/**
 * 配置 mingwTest 依赖
 */
fun Project.mingwTestDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.mingwTest.dependencies(configure) }
}

/**
 * 配置 mingwX64Test 依赖
 */
fun Project.mingwX64TestDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.mingwX64Test.dependencies(configure) }
}

/**
 * 配置 androidNativeTest 依赖
 */
fun Project.androidNativeTestDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.androidNativeTest.dependencies(configure) }
}

/**
 * 配置 androidNativeArm32Test 依赖
 */
fun Project.androidNativeArm32TestDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.androidNativeArm32Test.dependencies(configure) }
}

/**
 * 配置 androidNativeArm64Test 依赖
 */
fun Project.androidNativeArm64TestDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.androidNativeArm64Test.dependencies(configure) }
}

/**
 * 配置 androidNativeX64Test 依赖
 */
fun Project.androidNativeX64TestDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.androidNativeX64Test.dependencies(configure) }
}

/**
 * 配置 androidNativeX86Test 依赖
 */
fun Project.androidNativeX86TestDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.androidNativeX86Test.dependencies(configure) }
}

/**
 * 配置 skikoTest 依赖
 */
fun Project.skikoTestDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.skikoTest.dependencies(configure) }
}

/**
 * 配置 webTest 依赖
 */
fun Project.webTestDependencies(configure: KotlinDependencyHandler.() -> Unit) {
    kotlin { sourceSets.webTest.dependencies(configure) }
}