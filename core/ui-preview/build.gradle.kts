import com.android.build.api.dsl.androidLibrary

plugins {
    `multiplatform-lib-targets`
    id(libs.plugins.jetbrains.compose)
    id(libs.plugins.compose.compiler)
}

kotlin {

    androidLibrary {
        namespace = "ciyin.core.ui.preview"
    }

    // 添加或修改这个 compilerOptions 块
    compilerOptions {
        freeCompilerArgs.add("-Xannotation-target-all")
    }

    sourceSets.commonMain.dependencies {
        api(compose.animation)
    }

    sourceSets.androidMain.dependencies {
        api(libs.androidx.compose.ui.tooling)
        api(libs.androidx.compose.ui.tooling.preview)
    }

    sourceSets.skikoMain.dependencies {
        api(libs.compose.ui.tooling.preview)
    }

}
