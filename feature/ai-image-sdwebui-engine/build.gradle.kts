plugins {
    `multiplatform-lib-targets`
    alias(libs.plugins.kotlin.serialization)
}

kotlin {

    sourceSets.commonMain.dependencies {
        implementation(projects.core.serialization)
        api(projects.feature.aiCore)
        implementation(projects.feature.sdwebui)
        implementation(libs.kotlinx.coroutines.core)
    }

    sourceSets.commonTest.dependencies {
        implementation(libs.kotlin.test)
    }
}
