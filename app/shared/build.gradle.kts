plugins {
    `multiplatform-lib-targets`
    `koin-boot-initializer`
    id(libs.plugins.jetbrains.compose)
    id(libs.plugins.compose.compiler)
    id(libs.plugins.room)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.ciyin.app.shared"
}

compose.resources {
    publicResClass = true
    packageOfResClass = getProperty("android.namespace") + ".shared"
}

val componentDependencies = listOf<Dependency>(
    projects.component.room,
//    projects.component.dataStore,
)

commonMainDependencies {
    // component.room 依赖了 Room 库，Room 可能不支持所有平台
    // 因此将 room 依赖移到特定平台源集，而不是 commonMain
    componentDependencies.forEach(::api)
    implementation(compose.components.resources)

    implementation(libs.bundles.arrow)
    implementation(libs.bundles.filekit)

    api(projects.core.io)
    api(projects.core.lang)
    api(projects.core.platform)
    api(projects.core.system)
    api(projects.core.uiPreview)
    api(projects.core.uiFoundation)
    api(projects.core.coroutines)
    api(projects.core.application)
    api(projects.core.serialization)
    api(projects.core.datastore)

    api(projects.component.koin)

    api(projects.feature.kotlinScript)

}

androidMainDependencies {
    implementation(compose.preview)
    implementation(libs.androidx.activity.compose)
}

desktopMainDependencies {
    api(compose.desktop.currentOs) {
        exclude(compose.material) // We use material3
    }
    implementation(libs.kotlinx.coroutines.swing)
    implementation(kotlin("script-runtime"))
    implementation(libs.bundles.kotlin.scripting)
}

room {
    schemaDirectory("${projectDir}/schemas")
}
//
kspPlatformMain(libs.androidx.room.compiler)

koinBootInitializer {
    includes(componentDependencies)
}
