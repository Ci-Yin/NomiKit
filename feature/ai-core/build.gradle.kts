plugins {
    `multiplatform-lib-targets`
}

kotlin {

    sourceSets.commonMain.dependencies {
        api(libs.kotlinx.coroutines.core)
        api(libs.kotlinx.serialization.json)
    }
}
