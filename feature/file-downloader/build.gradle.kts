plugins {
    `multiplatform-lib-targets`
}

kotlin {
    sourceSets.commonMain.dependencies {
        api(projects.core.io)
        api(libs.kotlinx.coroutines.core)
        implementation(projects.core.platform)
        implementation(libs.ktor.client.core)
        implementation(libs.ktor.client.logging)
    }

    sourceSets.jvmMain.dependencies {
        implementation(libs.ktor.client.okhttp)
    }

    sourceSets.iosMain.dependencies {
        implementation(libs.ktor.client.darwin)
    }

    sourceSets.commonTest.dependencies {
        implementation(libs.kotlin.test)
        implementation(libs.kotlinx.coroutines.test)
        implementation(libs.ktor.client.mock)
        implementation(libs.okio.fakefilesystem)
    }
}
