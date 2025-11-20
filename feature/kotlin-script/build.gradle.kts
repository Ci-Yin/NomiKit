plugins {
    `multiplatform-lib-targets`
}

android {
    namespace = "ciyin.feature.kotlinscript"
}

androidMainDependencies {

}

commonMainDependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(projects.core.platform)
}

desktopMainDependencies {
    implementation(libs.ivy)
    implementation(kotlin("script-runtime"))
    implementation(libs.bundles.kotlin.scripting)
}
