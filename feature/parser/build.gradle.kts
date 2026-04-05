plugins {
    `multiplatform-lib-targets`
    alias(libs.plugins.kotlin.serialization)
}

kotlin {

    sourceSets.commonMain.dependencies {
        api(projects.core.serialization)
        api(libs.ksoup)
        api(libs.ktor.client.core)
//        api(libs.ktor.client.serialization.kotlinx.json)
    }

    sourceSets.jvmMain.dependencies {
        implementation(libs.ktor.client.okhttp)
    }

    sourceSets.iosMain.dependencies {
        implementation(libs.ktor.client.darwin)
    }

    sourceSets.webMain.dependencies {
        implementation(libs.ktor.client.cio)
    }
    sourceSets.androidDeviceTest.dependencies {
        api(kotlin("test"))
        api(libs.androidx.test.junit)
        api(libs.androidx.espresso.core)
        api(libs.kotlinx.coroutines.test)
    }
}
