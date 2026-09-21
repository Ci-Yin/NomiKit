plugins {
    `multiplatform-lib-targets`
}

base {
    archivesName.set("nomikit-component-media-directory")
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(projects.component.koin)
        api(projects.feature.mediaDirectory)
    }

    sourceSets.desktopTest.dependencies {
        implementation(libs.kotlin.test)
        implementation(libs.koin.test)
    }
}
