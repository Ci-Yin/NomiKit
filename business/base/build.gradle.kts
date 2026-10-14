plugins {
    `multiplatform-lib-targets`
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.io)
            implementation(projects.core.platform)
            implementation(libs.arrow.core)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.logging)
        }
        commonTest.dependencies {
            implementation(libs.ktor.client.mock)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.serialization.kotlinx.json)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

