plugins {
    `multiplatform-lib-targets`
    id(libs.plugins.jetbrains.compose)
    id(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(libs.bundles.navigation)
        implementation(libs.bundles.compose)
        implementation(libs.bundles.material3)
        implementation(projects.core.uiFoundation)
        implementation(projects.feature.aiFacade)
        implementation(projects.feature.aiImageSdwebuiEngine)
    }

    sourceSets.desktopMain.dependencies {
        implementation(libs.kotlinx.coroutines.swing)
    }
}
