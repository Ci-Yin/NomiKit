import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    id(libs.plugins.kotlin.multiplatform)
    id(libs.plugins.jetbrains.compose)
    id(libs.plugins.compose.compiler)
}

kotlin {

    js {
        browser()
        binaries.executable()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        // webMain 作为中间层
        val webMain by creating {
            dependsOn(commonMain.get())
        }

        // JS 和 WASM 都依赖 webMain
        jsMain {
            dependsOn(webMain)
        }

        wasmJsMain {
            dependsOn(webMain)
        }

        commonMain.dependencies {
            implementation(compose.components.resources)
//            implementation(compose.runtime)
//            implementation(compose.foundation)
//            implementation(compose.material3)
//            implementation(compose.ui)
//            implementation(compose.components.uiToolingPreview)
//            implementation(libs.androidx.lifecycle.viewmodel)
//            implementation(libs.androidx.lifecycle.viewmodel.compose)
//            implementation(libs.androidx.lifecycle.runtime.compose)
            // 这些依赖不支持 JS/WASM 平台，已移除
            // 如果需要在 web 中使用，需要确保这些项目支持 JS/WASM 或创建替代实现
            // implementation(projects.core.uiFoundation)
            // implementation(projects.core.platform)
            // implementation(projects.app.shared)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
