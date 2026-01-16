plugins {
    `multiplatform-lib-targets`
}

android {
    namespace = "ciyin.core.system"
}

androidMainDependencies {

}

commonMainDependencies {
    api(libs.kotlinx.datetime)
    api(libs.directories)
    api(projects.core.platform)
    api(projects.core.io)
}

desktopMainDependencies {
    api(libs.jna)
    api(libs.jna.platform)
}
