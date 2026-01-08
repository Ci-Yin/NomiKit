@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    `multiplatform-lib-targets`
    id(libs.plugins.jetbrains.compose)
    id(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.ciyin.app.shared"
}

commonMainDependencies {

    implementation(compose.components.resources)

    implementation(libs.bundles.arrow)
    implementation(libs.bundles.filekit)
    implementation(libs.kotlinx.coroutines.core)

    api(projects.core.io)
    api(projects.core.lang)
    api(projects.core.platform)
    api(projects.core.system)
    api(projects.core.uiPreview)
    api(projects.core.uiFoundation)
    api(projects.core.serialization)
    api(projects.core.datastore)

    api(projects.component.koin)

    api(projects.feature.kotlinScript)

}

androidMainDependencies {
    implementation(compose.preview)
    implementation(libs.androidx.activity.compose)
}

desktopMainDependencies {
    api(compose.desktop.currentOs) {
        exclude(compose.material) // We use material3
    }
    implementation(libs.kotlinx.coroutines.swing)
    implementation(kotlin("script-runtime"))
    implementation(libs.bundles.kotlin.scripting)
}
