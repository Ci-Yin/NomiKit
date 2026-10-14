plugins {
    `multiplatform-lib-targets`
}

kotlin {

    sourceSets.commonMain.dependencies {
        api(libs.kotlinx.coroutines.core)
        implementation(projects.core.platform)
    }

}

