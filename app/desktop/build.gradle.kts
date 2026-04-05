import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    id(libs.plugins.kotlin.jvm)
    id(libs.plugins.jetbrains.compose)
    id(libs.plugins.compose.compiler)
}


//if (getLocalProperty("myuko.compose.hot.reload")?.toBooleanStrict() != false) {
//    apply(plugin = libs.plugins.compose.hot.reload.get().pluginId)
//}

dependencies {
    implementation(libs.compose.components.resources)
    implementation(projects.core.platform)
    implementation(projects.app.shared)
}

// workaround for compose limitation
tasks.named("processResources") {
    dependsOn(":app:shared:desktopProcessResources")
//    dependsOn(":app:shared:ui-foundation:desktopProcessResources")
}

sourceSets {
    main {
        resources.srcDirs(
            project(projects.app.shared.path).layout.buildDirectory.file("processedResources/desktop/main"),
        )
    }
}

compose.desktop {

    application {
        mainClass = "com.ciyin.app.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Exe, TargetFormat.Msi, TargetFormat.Deb)
            packageName = getProperty("android.applicationid")
            packageVersion = getProperty("android.version.name")
        }
    }

}

tasks.register("buildInstaller") {
    group = "distribution"
    description = "使用 Advanced Installer 打包 app 模块"

    // 声明依赖于资源处理后的 .aip 文件
    dependsOn(":app:createDistributable")

    doLast {
        val runnerExe = "D:\\APP\\Advanced Installer 22.4\\bin\\x86\\AdvancedInstaller.com"
        // .aip 文件路径
        val aipFile = "D:\\Studio\\AdvancedInstallerProjects\\Rpa\\rpa.aip"
        // 输出目录
        val outputDir = layout.buildDirectory.file("compose/binaries/main/exe")

        // 构建命令
        val command = listOf(
            runnerExe,
            "/build",
            aipFile
        )

        println("执行命令: ${command.joinToString(" ")}")

        // Gradle 9 起已移除 Project#exec；在 doLast 执行阶段使用进程 API。
        val exitCode = ProcessBuilder(command).inheritIO().start().waitFor()
        require(exitCode == 0) { "Advanced Installer 退出码: $exitCode" }
    }
}
