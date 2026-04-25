plugins {
    `multiplatform-lib-targets`
}

kotlin {

    sourceSets.commonMain.dependencies {
        api(project(":feature:ai-core"))
        implementation(project(":feature:sdwebui"))
        implementation(libs.kotlinx.coroutines.core)
    }

    sourceSets.commonTest.dependencies {
        implementation(libs.kotlin.test)
    }
}
