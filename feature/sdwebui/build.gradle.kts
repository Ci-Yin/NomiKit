plugins {
    `multiplatform-lib-targets`
    alias(libs.plugins.kotlin.serialization)
}

kotlin {

    sourceSets.commonMain.dependencies {
        api(libs.ktor.client.core)
        api(libs.ktor.client.content.negotiation)
        api(libs.ktor.client.serialization.kotlinx.json)
        api(libs.kotlinx.serialization.json)
    }

    sourceSets.androidMain.dependencies {
        implementation(libs.ktor.client.okhttp)
    }

    sourceSets.desktopMain.dependencies {
        implementation(libs.ktor.client.cio)
    }

    sourceSets.iosMain.dependencies {
        implementation(libs.ktor.client.darwin)
    }
}
