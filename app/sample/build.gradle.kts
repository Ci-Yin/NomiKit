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
        implementation(projects.core.application)
        implementation(projects.core.platform)
        implementation(projects.core.uiFoundation)
        implementation(projects.feature.aiIntegrate)
        implementation(projects.component.dataStore)
        implementation(projects.component.koin)
    }

    sourceSets.androidMain.dependencies {
        implementation(libs.compose.ui.tooling.preview)
    }

    sourceSets.desktopMain.dependencies {
        implementation(libs.kotlinx.coroutines.swing)
    }
}
