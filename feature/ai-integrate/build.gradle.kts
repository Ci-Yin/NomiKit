plugins {
    `multiplatform-lib-targets`
}

kotlin {

    sourceSets.commonMain.dependencies {
        api(projects.feature.aiCore)
        implementation(projects.feature.aiChatOpenaiEngine)
        implementation(projects.feature.aiImageSdwebuiEngine)
        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.koin.core)
    }

    sourceSets.commonTest.dependencies {
        implementation(libs.kotlin.test)
        implementation(libs.kotlinx.coroutines.test)
    }
}
