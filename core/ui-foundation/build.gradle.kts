plugins {
    `multiplatform-lib-targets`
    id(libs.plugins.jetbrains.compose)
    id(libs.plugins.compose.compiler)
}

android {
    namespace = "ciyin.core.foundation"
}

androidMainDependencies {
    api(libs.androidx.window)
}

commonMainDependencies {
    api(compose.runtime)
    api(compose.foundation)
    api(compose.material3)
    api(compose.ui)
    api(compose.uiUtil)
    api(compose.components.resources)
    api(compose.components.uiToolingPreview)

    api(libs.androidx.lifecycle.viewmodel)
    api(libs.androidx.lifecycle.viewmodel.compose)
    api(libs.androidx.lifecycle.runtime.compose)
    api(libs.androidx.navigation.compose)
    api(libs.androidx.navigation.runtime)
    api(libs.freeletics.flowredux2)
    api(libs.bundles.androidx.compose.material3)
    api(libs.bundles.coil)

    api(projects.core.uiPreview)
}

desktopMainDependencies {

}
