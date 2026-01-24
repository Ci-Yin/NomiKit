import com.android.build.api.dsl.androidLibrary

plugins {
    `multiplatform-lib-targets`
}

kotlin {

    androidLibrary {
        namespace = "ciyin.feature.sample"
    }

    sourceSets.commonMain.dependencies {

    }

    sourceSets.androidMain.dependencies {

    }

    sourceSets.desktopMain.dependencies {

    }

    sourceSets.iosMain.dependencies {

    }

    sourceSets.webMain.dependencies {

    }

}
