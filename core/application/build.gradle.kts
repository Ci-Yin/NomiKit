plugins {
    `multiplatform-lib-targets`
    `app-build-config`
    id(libs.plugins.jetbrains.compose)
    id(libs.plugins.compose.compiler)
}

appBuildConfig {
    packageName.set("ciyin.application.config")
    configPrefix.set("app.config.")
}

kotlin {

    sourceSets.commonMain.dependencies {
        implementation(compose.runtime)
        implementation(projects.component.koin)
    }

}
