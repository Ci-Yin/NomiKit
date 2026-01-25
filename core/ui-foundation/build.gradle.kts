import com.android.build.api.dsl.androidLibrary

plugins {
    `multiplatform-lib-targets`
    id(libs.plugins.jetbrains.compose)
    id(libs.plugins.compose.compiler)
}

kotlin {

    androidLibrary {
        namespace = "ciyin.core.ui.foundation"
    }

    sourceSets.commonMain.dependencies {
        api(libs.compose.runtime)
        api(libs.compose.foundation)
        api(libs.compose.material3)
        api(libs.compose.ui)
        api(libs.compose.ui.util)
        api(libs.compose.components.resources)
        api(libs.compose.ui.tooling.preview)

        api(libs.androidx.lifecycle.viewmodel)
        api(libs.androidx.lifecycle.viewmodel.compose)
        api(libs.androidx.lifecycle.runtime.compose)
        api(libs.bundles.compose)
        api(libs.bundles.material3)
        api(libs.bundles.coil)
        api(libs.freeletics.flowredux2)

        api(projects.core.uiPreview)
        api(projects.core.coroutines)
    }

    sourceSets.androidMain.dependencies {
        api(libs.androidx.window)
    }

}
