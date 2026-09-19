plugins {
    `multiplatform-lib-targets`
    id(libs.plugins.jetbrains.compose)
    id(libs.plugins.compose.compiler)
}

base {
    archivesName.set("nomikit-feature-file-picker")
}

kotlin {
    sourceSets.commonMain.dependencies {
        api(projects.core.io)
        implementation(compose.runtime)
        implementation(libs.filekit.dialogs)
        implementation(libs.filekit.dialogs.compose)
    }
}
