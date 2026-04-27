plugins {
    `multiplatform-lib-targets`
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.io)
            implementation(libs.arrow.core)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.logging)
        }
        commonTest.dependencies {
            implementation(libs.ktor.client.mock)
        }
    }
}

