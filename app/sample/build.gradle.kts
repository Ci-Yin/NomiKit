plugins {
    `multiplatform-lib-targets`
    id(libs.plugins.jetbrains.compose)
    id(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(libs.bundles.navigation)
        implementation(libs.bundles.compose)
        implementation(libs.bundles.material3)
        implementation(libs.compose.components.resources)
        implementation(projects.core.application)
        implementation(projects.core.material)
        implementation(projects.core.platform)
        implementation(projects.core.uiFoundation)
        implementation(projects.feature.fileDownloader)
        implementation(projects.feature.aiIntegrate)
        implementation(projects.feature.permissions)
        implementation(projects.feature.videoPlayer)
        implementation(projects.component.dataStore)
        implementation(projects.component.koin)
        implementation(projects.component.mediaLibrary)
    }

    sourceSets.androidMain.dependencies {
        implementation(libs.androidx.activity.compose)
        implementation(libs.compose.ui.tooling.preview)
        implementation(libs.xxpermissions.get().toString()) {
            exclude(group = "com.android.support")
        }
    }

    sourceSets.desktopMain.dependencies {
        implementation(libs.kotlinx.coroutines.swing)
    }

    sourceSets.commonTest.dependencies {
        @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
        implementation(compose.uiTest)
    }

    sourceSets.desktopTest.dependencies {
        implementation(compose.desktop.currentOs)
        implementation(compose.desktop.uiTestJUnit4)
    }
}

compose.resources {
    publicResClass = false
    packageOfResClass = getProperty("android.namespace") + ".sample"
}
