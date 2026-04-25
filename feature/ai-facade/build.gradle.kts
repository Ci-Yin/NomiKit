plugins {
    `multiplatform-lib-targets`
}

kotlin {

    sourceSets.commonMain.dependencies {
        api(project(":feature:ai-core"))
        implementation(libs.kotlinx.coroutines.core)
    }

    sourceSets.commonTest.dependencies {
        implementation(libs.kotlin.test)
    }
}
