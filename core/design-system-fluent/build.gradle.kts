plugins {
    `multiplatform-lib-targets`
    id(libs.plugins.jetbrains.compose)
    id(libs.plugins.compose.compiler)
}

kotlin {
    sourceSets.commonMain.dependencies {
        api(projects.core.designSystem)
        api(libs.compose.fluent)
        implementation(projects.core.platform)
    }

    sourceSets.commonTest.dependencies {
        implementation(libs.kotlin.test)
    }

    sourceSets.findByName("desktopTest")?.dependencies {
        implementation(projects.core.designSystemMaterial3)
        @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
        implementation(compose.uiTest)
        implementation(compose.desktop.currentOs)
        implementation(compose.desktop.uiTestJUnit4)
    }
}
