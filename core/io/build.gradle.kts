import com.android.build.api.dsl.androidLibrary

plugins {
    `multiplatform-lib-targets`
}

kotlin {

    androidLibrary {
        namespace = "ciyin.core.io"
    }

    sourceSets.commonMain.dependencies {
        api(projects.core.lang)
        api(libs.kotlinx.coroutines.core)
        api(libs.okio)
    }

}

kotlin.androidLibrary {
    namespace = "ciyin.core.io"
}

