plugins {
    `multiplatform-lib-targets`
}

kotlin {

    sourceSets.commonMain.dependencies {
        api(kotlin("test-annotations-common"))
        api(libs.kotlinx.coroutines.test)
    }

    sourceSets.jvmMain.dependencies {
        api(kotlin("test-junit5"))
    }

}
