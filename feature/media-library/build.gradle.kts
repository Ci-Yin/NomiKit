plugins {
    `multiplatform-lib-targets`
}

base {
    archivesName.set("nomikit-feature-media-library")
}

kotlin {
    sourceSets.commonMain.dependencies {
        api(projects.core.io)
        api(projects.core.platform)
        implementation(libs.kotlinx.coroutines.core)
    }

    sourceSets.desktopMain.dependencies {
        implementation(libs.jna.platform)
    }

    sourceSets.commonTest.dependencies {
        implementation(libs.kotlin.test)
    }

    sourceSets.desktopTest.dependencies {
        implementation(libs.kotlinx.coroutines.test)
    }
}
