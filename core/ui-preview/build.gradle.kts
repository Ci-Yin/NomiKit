plugins {
    `multiplatform-lib-targets`
    id(libs.plugins.jetbrains.compose)
    id(libs.plugins.compose.compiler)
}

kotlin {
    // 添加或修改这个 compilerOptions 块
    compilerOptions {
        freeCompilerArgs.add("-Xannotation-target-all")
    }
}

android {
    namespace = "ciyin.core.ui.preview"
}

commonMainDependencies {
    api(compose.animation)
}

androidMainDependencies {
    api(libs.androidx.compose.ui.tooling)
    api(libs.androidx.compose.ui.tooling.preview)
}

skikoMainDependencies {
    api(compose.components.uiToolingPreview)
}
