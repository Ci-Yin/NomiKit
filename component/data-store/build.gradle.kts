plugins {
    `multiplatform-lib-targets`
    alias(libs.plugins.kotlin.serialization)
}

kotlin {

    sourceSets.commonMain.dependencies {
        implementation(projects.component.koin)
        implementation(projects.core.serialization)
        implementation(projects.core.coroutines)
        api(libs.datastore.core)
        api(libs.datastore.core.okio)
    }

    sourceSets.commonTest.dependencies {
        implementation(libs.kotlinx.coroutines.test)
    }

    sourceSets.desktopTest.dependencies {
        implementation(libs.okio.fakefilesystem)
    }

}


