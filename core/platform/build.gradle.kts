plugins {
    `multiplatform-lib-targets`
}

android {
    namespace = "ciyin.core.platform"
}

androidMainDependencies {

}

commonMainDependencies {
    api(libs.kermit)
    api(libs.kotlinx.datetime)
    api(libs.kotlinx.coroutines.core)
    api(projects.core.lang)
    api(projects.core.io)
}

desktopMainDependencies {
    api(libs.jna)
    api(libs.jna.platform)
}
