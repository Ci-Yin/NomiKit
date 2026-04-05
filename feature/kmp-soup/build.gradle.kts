import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    `multiplatform-lib-targets`
}

kotlin {

    sourceSets {
        commonMain.dependencies {
        }

        jvmMain.dependencies {
            implementation(libs.jsoup)
        }

        iosMain.dependencies {
        }

        webMain.dependencies {
        }
    }

    targets.withType<KotlinNativeTarget>().configureEach {
        if (name.startsWith("ios")) {
            compilations.getByName("main").cinterops.create("swiftsoupBridge") {
                defFile(project.file("src/nativeInterop/cinterop/swiftsoup_bridge.def"))
                includeDirs(project.file("src/nativeInterop/cinterop/include"))
            }
        }
    }
}
