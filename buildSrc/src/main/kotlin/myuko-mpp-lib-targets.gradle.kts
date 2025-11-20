@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.ComposePlugin
import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree
import org.jetbrains.kotlin.gradle.plugin.cocoapods.CocoapodsExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

/*
 * 配置 JVM + Android 的 compose 项目. 默认不会配置 resources.
 *
 * 该插件必须在 kotlin, compose, android 之后引入.
 *
 * 如果开了 android, 就会配置 desktop + android, 否则只配置 jvm.
 */

val android = extensions.findByType(LibraryExtension::class)
val composeExtension = extensions.findByType(ComposeExtension::class)
//val composeCompilerExtension =
//    extensions.findByType(org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension::class)
val enableHotReload = getLocalProperty("myuko.compose.hot.reload")?.toBooleanStrict() != false

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
    if (android != null) {
        jvm("desktop")
        androidTarget {
            @OptIn(ExperimentalKotlinGradlePluginApi::class)
            instrumentedTestVariant.sourceSetTree.set(KotlinSourceSetTree.test)
            unitTestVariant.sourceSetTree.set(KotlinSourceSetTree.unitTest)
        }

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

        // This won't work (KT 2.1.0)
//        sourceSets {
//            val commonAndroidTest = create("commonAndroidTest") {
//                dependsOn(getByName("jvmTest"))
//            }
//            getByName("androidInstrumentedTest").dependsOn(commonAndroidTest)
//            getByName("androidUnitTest").dependsOn(commonAndroidTest)
//        }
    } else {
        jvm()

        applyDefaultHierarchyTemplate()
    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

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

        if (project.path != ":core:platform") {
            implementation(project(":core:platform"))
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

    if (composeExtension != null) {
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

if (android != null) {
    configure<KotlinMultiplatformExtension> {
        sourceSets {
            // Workaround for MPP compose bug, don't change
            removeIf { it.name == "androidAndroidTestRelease" }
            removeIf { it.name == "androidTestFixtures" }
            removeIf { it.name == "androidTestFixturesDebug" }
            removeIf { it.name == "androidTestFixturesRelease" }
        }
    }
    if (composeExtension != null) {
        tasks.named("generateComposeResClass") {
            mustRunAfter("generateResourceAccessorsForAndroidUnitTest")
        }
        tasks.withType(KotlinCompilationTask::class) {
            mustRunAfter(tasks.matching { it.name == "generateComposeResClass" })
            mustRunAfter(tasks.matching { it.name == "generateResourceAccessorsForAndroidRelease" })
            mustRunAfter(tasks.matching { it.name == "generateResourceAccessorsForAndroidUnitTest" })
            mustRunAfter(tasks.matching { it.name == "generateResourceAccessorsForAndroidUnitTestRelease" })
            mustRunAfter(tasks.matching { it.name == "generateResourceAccessorsForAndroidUnitTestDebug" })
            mustRunAfter(tasks.matching { it.name == "generateResourceAccessorsForAndroidDebug" })
        }
    }

    android.apply {
        compileSdk = getIntProperty("android.compile.sdk")
        defaultConfig {
            minSdk = getIntProperty("android.min.sdk")
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            testInstrumentationRunnerArguments["runnerBuilder"] =
                "de.mannodermaus.junit5.AndroidJUnit5Builder"
        }
//        packaging {
//            resources {
//                pickFirsts.add("META-INF/LICENSE.md")
//                pickFirsts.add("META-INF/LICENSE-notice.md")
//            }
//        }
//        flavorDimensions.add("api")
//        productFlavors {
//            create("minApi30") {
//                dimension = "api"
//                minSdk = 30
//                isDefault = false
//            }
//            create("default") {
//                dimension = "api"
//                isDefault = true
//            }
//        }
        buildTypes.getByName("release") {
            isMinifyEnabled = false // shared 不能 minify, 否则构建 app 会失败
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                *sharedAndroidProguardRules(),
            )
        }
        buildFeatures {
            if (composeExtension != null) {
                compose = true
            }
        }
    }
}

if (android != null) {
    apply(plugin = "de.mannodermaus.android-junit5")
}

if (enableIos) {
    if (getOs() == Os.MacOS) {
        apply(plugin = "org.jetbrains.kotlin.native.cocoapods")

        configure<KotlinMultiplatformExtension> {
            this.configure<CocoapodsExtension> {
                version = project.version.toString()
                summary = project.name
                name = project.name
                license = "MIT"
                homepage = "https://github.com/myuko/mpp"
                ios.deploymentTarget = "13.0"
                // Maps custom Xcode configuration to NativeBuildType
                xcodeConfigurationToNativeBuildType["CUSTOM_DEBUG"] = NativeBuildType.DEBUG
                xcodeConfigurationToNativeBuildType["CUSTOM_RELEASE"] = NativeBuildType.RELEASE
            }
        }
    }
}
