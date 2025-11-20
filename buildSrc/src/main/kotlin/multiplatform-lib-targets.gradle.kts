@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.ComposePlugin
import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.library")
}

val android = extensions.findByType(LibraryExtension::class)
val composeExtension = extensions.findByType(ComposeExtension::class)

configure<KotlinMultiplatformExtension> {
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
    if (project.enableIos) {
        iosArm64()
        iosSimulatorArm64() // to run tests
        // no x86
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

    if (android != null) {
        androidTarget {
            @OptIn(ExperimentalKotlinGradlePluginApi::class)
            instrumentedTestVariant.sourceSetTree.set(KotlinSourceSetTree.test)
            unitTestVariant.sourceSetTree.set(KotlinSourceSetTree.unitTest)
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

        // This won't work (KT 2.1.0)
//        sourceSets {
//            val commonAndroidTest = create("commonAndroidTest") {
//                dependsOn(getByName("jvmTest"))
//            }
//            getByName("androidInstrumentedTest").dependsOn(commonAndroidTest)
//            getByName("androidUnitTest").dependsOn(commonAndroidTest)
//        }
    } else if (enableDesktop) {
        jvm()

        applyDefaultHierarchyTemplate()
    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
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
    sourceSets.commonMain.dependencies {
        // 添加常用依赖
        if (composeExtension != null) {
            val compose = ComposePlugin.Dependencies(project)
            // Compose
            api(compose.foundation)
            api(compose.runtime)
            api(compose.ui)
            api(compose.animation)
            api(compose.material3)
            api(compose.materialIconsExtended)
            // workaround in CMP 1.8.0-alpha04. Remove in the future.
            api("org.jetbrains.androidx.window:window-core") {
                version {
                    strictly("1.4.0-alpha03")
                }
            }
        }

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

    if (composeExtension != null && enableDesktop) {
        sourceSets.getByName("desktopTest").dependencies {
            val compose = ComposePlugin.Dependencies(project)
            implementation(compose.desktop.uiTestJUnit4)
        }
    }

    if (android != null && composeExtension != null) {
        val composeVersion =
            versionCatalogs.named("libs").findVersion("compose-multiplatform").get()
        dependencies {
            //"debugImplementation"("androidx.compose.ui:ui-test-manifest:${composeVersion}")
        }
    }


    if (android != null) {
        val androidMainSourceSetDir = projectDir.resolve("androidMain")
        val androidExtension = extensions.findByType(CommonExtension::class)
        if (androidExtension != null) {
            androidExtension.sourceSets["main"].aidl.srcDirs(androidMainSourceSetDir.resolve("aidl"))
            androidExtension.sourceSets["main"].java.srcDirs(androidMainSourceSetDir.resolve("java"))
        }
    }
}

android?.apply {

    sourceSets.getByName("main") {
        manifest.srcFile("src/androidMain/AndroidManifest.xml")
        res.srcDirs("src/androidMain/res")
        //resources.srcDirs("src/commonMain/resources")
    }

    defaultConfig {
        minSdk = getIntProperty("android.min.sdk")
        compileSdk = getIntProperty("android.compile.sdk")
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

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        if (composeExtension != null) {
            compose = true
        }
    }

}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}