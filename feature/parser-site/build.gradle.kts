plugins {
    `multiplatform-lib-targets`
    alias(libs.plugins.kotlin.serialization)
}

kotlin {

    sourceSets.commonMain.dependencies {
        implementation(projects.feature.parser)
    }
    sourceSets.androidDeviceTest.dependencies {
        api(kotlin("test"))
        api(libs.androidx.test.junit)
        api(libs.androidx.espresso.core)
        api(libs.kotlinx.coroutines.test)
    }
}
