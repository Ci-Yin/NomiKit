plugins {
    `multiplatform-lib-targets`
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
    id(libs.plugins.room)
}

kotlin {

    sourceSets.commonMain.dependencies {
        implementation(projects.component.koin)
        api(libs.androidx.room.runtime)
        api(libs.sqlite.bundled)
    }

    sourceSets.desktopTest.dependencies {
        implementation(projects.core.system)
        implementation(libs.kotlinx.coroutines.test)
    }

}

room {
    schemaDirectory("${projectDir}/schemas")
}

dependencies {
    listOf(libs.androidx.room.compiler).forEach {
        add("kspDesktopTest", it)
    }
}

