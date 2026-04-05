plugins {
    `multiplatform-lib-targets`
}

kotlin {

    sourceSets.commonMain.dependencies {
        api(projects.core.lang)
        api(libs.kotlinx.coroutines.core)
        api(libs.okio)
    }

}

