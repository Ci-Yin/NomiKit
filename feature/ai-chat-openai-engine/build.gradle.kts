plugins {
    `multiplatform-lib-targets`
    alias(libs.plugins.kotlin.serialization)
}

kotlin {

    sourceSets.commonMain.dependencies {
        api(project(":feature:ai-core"))
        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.ktor.client.core)
        implementation(libs.ktor.client.content.negotiation)
        implementation(libs.ktor.client.serialization.kotlinx.json)
        implementation(libs.ktor.client.logging)
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

    sourceSets.commonTest.dependencies {
        implementation(libs.kotlin.test)
        implementation(libs.ktor.client.mock)
    }
}
