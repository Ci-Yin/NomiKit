plugins {
    `multiplatform-lib-targets`
}

base {
    archivesName.set("nomikit-component-media-library")
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(projects.component.koin)
        api(projects.feature.mediaLibrary)
    }

    sourceSets.desktopTest.dependencies {
        implementation(libs.kotlin.test)
    }
}
