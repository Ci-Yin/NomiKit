plugins {
    `multiplatform-lib-targets`
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {

    sourceSets.commonMain.dependencies {
        api(libs.koin.core)
        api(projects.core.serialization)
    }

}
