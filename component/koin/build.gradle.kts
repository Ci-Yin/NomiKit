plugins {
    `multiplatform-lib-targets`
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

commonMainDependencies {
    api(libs.koin.core)
    api(libs.koin.annotations)
    api(projects.core.serialization)
}

android {
    namespace = "ciyin.component.koin"
}