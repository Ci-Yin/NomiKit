plugins {
    `multiplatform-lib-targets`
    alias(libs.plugins.kotlin.serialization)
}

kotlin {

    sourceSets.commonMain.dependencies {
        api(projects.core.io)
        api(libs.kotlinx.serialization.core)
        api(libs.yamlkt)
    }
}
