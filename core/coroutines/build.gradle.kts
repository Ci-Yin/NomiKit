plugins {
    `multiplatform-lib-targets`
}

kotlin {

    sourceSets.commonMain.dependencies {
        api(libs.kotlinx.coroutines.core)
    }

}

