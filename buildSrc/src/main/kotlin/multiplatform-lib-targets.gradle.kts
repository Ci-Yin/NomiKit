@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import com.android.build.api.dsl.androidLibrary
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.ComposePlugin
import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
}

val composeExtension = extensions.findByType(ComposeExtension::class)

/**
 * 平台架构:
 * ```
 * common
 *   - jvm (可访问 JDK, 但不能使用 Android SDK 没有的 API)
 *     - android (可访问 Android SDK)
 *     - desktop (可访问 JDK)
 *   - native
 *     - apple
 *       - ios
 *         - iosArm64
 *         - iosSimulatorArm64 TODO
 * ```
 *
 * `native - apple - ios` 的架构是为了契合 Kotlin 官方推荐的默认架构. 以后如果万一要添加其他平台, 可方便添加.
 */
configure<KotlinMultiplatformExtension> {

    androidLibrary {

        minSdk = getIntProperty("android.min.sdk")
        compileSdk = getIntProperty("android.compile.sdk")

        androidResources {
            enable = true
        }

        packaging {
            resources {
                excludes += "/META-INF/{AL2.0,LGPL2.1}"
                excludes += "META-INF/DEPENDENCIES"
                excludes += "META-INF/INDEX.LIST"
                pickFirsts += "META-INF/io.netty.versions.properties"
                pickFirsts += "META-INF/some/other-duplicate.properties"
            }
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

    if (composeExtension != null && enableDesktop) {
        sourceSets.getByName("desktopTest").dependencies {
            val compose = ComposePlugin.Dependencies(project)
            implementation(compose.desktop.uiTestJUnit4)
        }
    }

    sourceSets.commonMain.dependencies {
        // 添加常用依赖
//        if (composeExtension != null) {
//            val compose = ComposePlugin.Dependencies(project)
//            // Compose
//            api(compose.foundation)
//            api(compose.runtime)
//            api(compose.ui)
//            api(compose.animation)
//            api(compose.material3)
//            api(compose.materialIconsExtended)
//            // workaround in CMP 1.8.0-alpha04. Remove in the future.
//            api("org.jetbrains.androidx.window:window-core") {
//                version {
//                    strictly("1.4.0-alpha03")
//                }
//            }
//        }

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

}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}