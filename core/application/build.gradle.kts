plugins {
    `multiplatform-lib-targets`
    id(libs.plugins.jetbrains.compose)
    id(libs.plugins.compose.compiler)
}

android {
    namespace = "ciyin.core.application"
}

commonMainDependencies {
    implementation(projects.component.koin)
}

androidMainDependencies {

}

desktopMainDependencies {

}

iosMainDependencies {

}
