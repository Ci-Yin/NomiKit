import com.android.build.api.dsl.androidLibrary

plugins {
    `multiplatform-lib-targets`
    alias(libs.plugins.kotlin.serialization)
}
kotlin {

    androidLibrary {
        namespace = "ciyin.core.datastore"
    }

    sourceSets.commonMain.dependencies {
        api(projects.core.serialization)
    }

}

