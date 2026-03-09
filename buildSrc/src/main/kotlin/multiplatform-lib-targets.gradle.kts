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