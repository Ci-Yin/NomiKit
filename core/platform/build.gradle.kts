plugins {
    `multiplatform-lib-targets`
    id(libs.plugins.jetbrains.compose)
    id(libs.plugins.compose.compiler)
}

android {
    namespace = "ciyin.core.platform"
}

androidMainDependencies {

}

commonMainDependencies {
    api(compose.runtime)
    api(compose.ui)
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
