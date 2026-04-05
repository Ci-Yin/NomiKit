@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.ComposePlugin
import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
}

val composeExtension = extensions.findByType(ComposeExtension::class)

/**
 * 将 [KotlinMultiplatformExtension] 上的 `android { }` 桥接到 AGP 的
 * [KotlinMultiplatformAndroidLibraryTarget] 扩展（插件 id：`com.android.kotlin.multiplatform.library`），
 * 以便在 `kotlin { }` 内用与官方文档一致的 DSL 名配置 Android KMP 库目标。
 */
fun KotlinMultiplatformExtension.android(configure: Action<KotlinMultiplatformAndroidLibraryTarget>): Unit =
    (this as ExtensionAware).extensions.configure("android", configure)

/**
 * `multiplatform-lib-targets` 预编译约定脚本：为业务库模块统一注册 KMP 目标、Android KMP Library、
 * 默认 hierarchy、Compose/JUnit 相关测试依赖等。
 *
 * ### 与本脚本相关的 Gradle 插件
 * - `org.jetbrains.kotlin.multiplatform`
 * - `com.android.kotlin.multiplatform.library`（AGP 9+ 推荐的 KMP Android 库插件，与旧版 `com.android.library` + `androidTarget` 组合不同）
 *
 * ### 源码集与目标关系（示意）
 *
 * **始终存在**：`commonMain` / `commonTest`；在 `android { }` 中注册 Android 目标后，会出现 `androidMain` 等 Android 相关源集。
 *
 * ```
 * common
 * ├── jvm
 * │   ├── android
 * │   └── desktop
 * ├── native
 * │   └── apple
 * │       └── ios
 * │           ├── iosArm64
 * │           └── iosSimulatorArm64
 * ├── web
 * │   ├── js
 * │   └── wasmJs
 * ```
 *
 * `withJvm()` / `withNative()` 等分组与 Kotlin 默认 hierarchy 模板一致，便于日后在 `apple`、`ios` 等维度上扩展更多 Native 目标，
 * 而无需重写整套 `dependsOn` 关系。
 *
 * ### 功能开关（`local.properties` 或 `gradle.properties`，布尔，缺省为 false）
 *
 * | 属性 | 作用 |
 * |------|------|
 * | `multiplatform.enable.desktop` | 注册 `jvm("desktop")` 并应用默认 hierarchy（含 jvm 组：JVM + Android；skiko 组：JVM + Native，供 Compose 等） |
 * | `multiplatform.enable.ios` | 注册 `iosArm64`、`iosSimulatorArm64` 及测试资源拷贝任务 |
 * | `multiplatform.enable.js` | 注册 `js { browser(); binaries.executable() }`（可与 web 联用） |
 * | `multiplatform.enable.wasmJs` | 注册 `wasmJs { browser(); binaries.executable() }`（实验性 DSL 需 OptIn） |
 * | `multiplatform.enable.web` | 为同时启用 JS 与 Wasm 的 Web 场景提供统一开关（与 js / wasmJs 开关组合使用） |
 *
 * ### 其它约定
 * - Android KMP 目标的 `namespace` 默认由 [resolveAndroidKmpLibraryNamespace] 根据模块 `path` 推导；可通过 Project 属性 `android.kmp.namespace` 或在模块 `kotlin { android { namespace = ... } }` 中覆盖。
 * - 若工程已应用 Jetpack Compose（存在 [ComposeExtension]），会为 `commonTest` / `desktopTest` 等补充 UI 测试依赖。
 * - `compilerOptions` 中统一加入 `-Xexpect-actual-classes`，与项目 expect/actual 类风格一致。
 */
fun preview() {}
configure<KotlinMultiplatformExtension> {

    android {

        namespace = project.resolveAndroidKmpLibraryNamespace()

        minSdk = getIntProperty("android.min.sdk")
        compileSdk = getIntProperty("android.compile.sdk")

        androidResources {
            enable = true
        }

        // 选择加入以启用和配置主机端（单元）测试
        withHostTest {
            isIncludeAndroidResources = true
        }

        // 选择加入以启用和配置设备端（带仪器）测试
        withDeviceTest {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            execution = "HOST"
        }

        packaging {
            resources {
                excludes += "/META-INF/{AL2.0,LGPL2.1}"
                excludes += "META-INF/DEPENDENCIES"
                excludes += "META-INF/INDEX.LIST"
                excludes += "META-INF/LICENSE.md"
                excludes += "META-INF/LICENSE-notice.md"
                pickFirsts += "META-INF/io.netty.versions.properties"
                pickFirsts += "META-INF/some/other-duplicate.properties"
            }
        }

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }

    }

    sourceSets {
        androidMain {
            dependsOn(jvmMain.get())
        }
    }

    if (enableDesktop) {
        jvm("desktop")
        applyDefaultHierarchyTemplate {
            common {
                group("jvm") {
                    withJvm()
                    withAndroidTarget()
                }
                group("skiko") {
                    withJvm()
                    withNative()
                }
            }
        }
    }

//    if (enableDesktop) {
//        jvm()
//        applyDefaultHierarchyTemplate()
//    }

    if (project.enableIos) {
        listOf(
            iosArm64(),
            iosSimulatorArm64()
        ).forEach { iosTarget ->
            iosTarget.binaries.framework {
                baseName = "ComposeApp"
                isStatic = true
            }
        }
    }

    if (project.enableJs || project.enableWeb) {
        js {
            browser()
            binaries.executable()
        }
    }

    if (project.enableWasmJs || project.enableWeb) {
        @OptIn(ExperimentalWasmDsl::class)
        wasmJs {
            browser()
            binaries.executable()
        }
    }

//    if (enableWeb || enableWasmJs || enableJs) {
//        sourceSets {
//
//            // webMain 作为中间层
//            val webMain by creating {
//                dependsOn(commonMain.get())
//            }
//
//            if (enableWeb || enableJs) {
//                // JS 和 WASM 都依赖 webMain
//                jsMain {
//                    dependsOn(webMain)
//                }
//            }
//
//            if (enableWeb || enableWasmJs) {
//                wasmJsMain {
//                    dependsOn(webMain)
//                }
//            }
//
//        }
//    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    sourceSets.commonMain.dependencies {
        if (project.path != ":core:lang" && project.path != ":core:platform" && project.path != ":core:system" && project.path != ":core:io") {
            implementation(project(":core:system"))
        }
    }

    sourceSets.commonTest.dependencies {
        // https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-test.html#writing-and-running-tests-with-compose-multiplatform
        if (composeExtension != null) {
            val compose = ComposePlugin.Dependencies(project)
            @OptIn(ExperimentalComposeLibrary::class)
            implementation(compose.uiTest)
        }
        if (project.path != ":core:testing") {
            implementation(project(":core:testing"))
        }
    }

    sourceSets.getByName("androidDeviceTest").dependencies {
        if (project.path != ":core:testing") {
            implementation(project(":core:testing")) {
                exclude(group = "org.jetbrains.kotlin", module = "kotlin-test-junit5")
            }
        }
    }

    if (composeExtension != null && enableDesktop) {
        sourceSets.getByName("desktopTest").dependencies {
            val compose = ComposePlugin.Dependencies(project)
            implementation(compose.desktop.uiTestJUnit4)
        }
    }

}

if (enableIos) {
    // ios testing workaround
    // https://developer.squareup.com/blog/kotlin-multiplatform-shared-test-resources/
    tasks.register<Copy>("copyiOSTestResources") {
        from("src/commonTest/resources")
        into("build/bin/iosSimulatorArm64/debugTest/resources")
    }
    tasks.named("iosSimulatorArm64Test") {
        dependsOn("copyiOSTestResources")
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}