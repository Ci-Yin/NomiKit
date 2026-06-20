plugins {
    `multiplatform-lib-targets`
    id(libs.plugins.jetbrains.compose)
    id(libs.plugins.compose.compiler)
}

kotlin {
    sourceSets.commonMain.dependencies {
        api(projects.core.uiFoundation)
    }
}
