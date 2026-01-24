import com.android.build.api.dsl.androidLibrary

plugins {
    `multiplatform-lib-targets`
    id(libs.plugins.jetbrains.compose)
    id(libs.plugins.compose.compiler)
}

kotlin {

    androidLibrary {
        namespace = "ciyin.core.application"
    }

    sourceSets.commonMain.dependencies {
        implementation(projects.component.koin)
    }

}
