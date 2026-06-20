import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * 应用构建配置生成插件的声明式扩展。
 */
abstract class AppBuildConfigExtension {
    /**
     * 生成源码的包名。
     */
    abstract val packageName: Property<String>

    /**
     * 运行时扩展配置的 Gradle 属性前缀。
     */
    abstract val configPrefix: Property<String>

    /**
     * 生成源码的根输出目录。
     */
    abstract val outputDir: DirectoryProperty
}

/**
 * 生成单个平台源集的 `XXXAppBuildConfig` 源码。
 */
@CacheableTask
abstract class GeneratePlatformAppBuildConfigTask : DefaultTask() {
    /**
     * 生成源码的包名。
     */
    @get:Input
    abstract val packageName: Property<String>

    /**
     * 平台名，例如 `desktop`、`android`、`ios`。
     */
    @get:Input
    abstract val platformName: Property<String>

    /**
     * 当前应用 ID。
     */
    @get:Input
    abstract val appId: Property<String>

    /**
     * 当前应用版本名。
     */
    @get:Input
    abstract val versionName: Property<String>

    /**
     * Android namespace；仅 Android 平台生成字段。
     */
    @get:Input
    abstract val androidNamespace: Property<String>

    /**
     * Android versionCode；仅 Android 平台生成字段。
     */
    @get:Input
    abstract val androidVersionCode: Property<Int>

    /**
     * 桌面端可选组织目录名；仅 Desktop 平台生成字段。
     */
    @get:Input
    abstract val desktopOrganization: Property<String>

    /**
     * 桌面端可选应用目录名；仅 Desktop 平台生成字段。
     */
    @get:Input
    abstract val desktopName: Property<String>

    /**
     * 运行时扩展配置。
     */
    @get:Input
    abstract val runtimeProperties: MapProperty<String, String>

    /**
     * 生成源码输出目录。
     */
    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    /**
     * 写入平台构建配置源码。
     */
    @TaskAction
    fun generate() {
        val outputDir = outputDirectory.get().asFile
        val packagePath = packageName.get().replace('.', File.separatorChar)
        val className = platformClassName()
        val outputFile = File(outputDir, "$packagePath/$className.kt")
        outputFile.parentFile.mkdirs()
        outputFile.writeText(buildFileContent(className))
    }

    /**
     * 当前平台生成对象的类名。
     */
    private fun platformClassName(): String =
        "${platformName.get().replaceFirstChar { it.uppercase() }}AppBuildConfig"

    /**
     * 构建完整 Kotlin 文件内容。
     */
    private fun buildFileContent(className: String): String {
        val extraFields = buildExtraFields()
        return """
            package ${packageName.get()}

            /**
             * ${platformKDocSummary()}
             */
            object $className : AppBuildConfig {
                /**
                 * 应用稳定 ID。
                 */
                override val id: String = "${appId.get().escapeKotlinString()}"

                /**
                 * 应用展示版本名。
                 */
                override val versionName: String = "${versionName.get().escapeKotlinString()}"

                /**
                 * 额外运行时配置。
                 */
                override val properties: Map<String, String> = ${runtimePropertiesExpression()}$extraFields
            }

            /**
             * 当前平台实际生成的应用构建配置实现。
             */
            internal actual val currentAppBuildConfigImpl: AppBuildConfig = $className
        """.trimIndent()
    }

    /**
     * 构建平台特有字段源码。
     */
    private fun buildExtraFields(): String =
        when (platformName.get()) {
            "desktop" -> buildDesktopFields()
            "android" -> buildAndroidFields()
            else -> ""
        }

    /**
     * 构建 Desktop 平台特有字段源码。
     */
    private fun buildDesktopFields(): String =
        """

                /**
                 * 可选组织目录名；和 [name] 必须同时配置。
                 */
                val organization: String? = ${desktopOrganization.get().nullableStringExpression()}

                /**
                 * 可选应用目录名；和 [organization] 必须同时配置。
                 */
                val name: String? = ${desktopName.get().nullableStringExpression()}
        """.trimEnd()

    /**
     * 构建 Android 平台特有字段源码。
     */
    private fun buildAndroidFields(): String =
        """

                /**
                 * Android namespace，用于资源和 R 类命名。
                 */
                val namespace: String = "${androidNamespace.get().escapeKotlinString()}"

                /**
                 * Android versionCode。
                 */
                val versionCode: Int = ${androidVersionCode.get()}
        """.trimEnd()

    /**
     * 构建运行时扩展配置 Map 表达式。
     */
    private fun runtimePropertiesExpression(): String {
        val properties = runtimeProperties.get()
        if (properties.isEmpty()) return "emptyMap()"
        val entries = properties.entries.joinToString(",\n") { (key, value) ->
            "        \"${key.escapeKotlinString()}\" to \"${value.escapeKotlinString()}\""
        }
        return "mapOf(\n$entries,\n    )"
    }

    /**
     * 当前平台生成对象的 KDoc 首句。
     */
    private fun platformKDocSummary(): String =
        when (platformName.get()) {
            "android" -> "由 Gradle 生成的 Android 应用构建配置。"
            "desktop" -> "由 Gradle 生成的 Desktop 端应用构建配置。"
            "ios" -> "由 Gradle 生成的 iOS 应用构建配置。"
            else -> "由 Gradle 生成的 ${
                platformName.get().replaceFirstChar { it.uppercase() }
            } 应用构建配置。"
        }

    /**
     * 生成可空 Kotlin 字符串表达式。
     */
    private fun String.nullableStringExpression(): String =
        if (isBlank()) {
            "null"
        } else {
            "\"${escapeKotlinString()}\""
        }

    /**
     * 将属性值转义为 Kotlin 字符串字面量内容。
     */
    private fun String.escapeKotlinString(): String =
        replace("\\", "\\\\")
            .replace("\$", "\\\$")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
}

/**
 * `app-build-config` 预编译插件的实现。
 */
class AppBuildConfigConventionPlugin : Plugin<Project> {
    /**
     * 注册应用构建配置生成扩展与平台任务。
     */
    override fun apply(project: Project) {
        val extension = project.extensions.create(
            "appBuildConfig",
            AppBuildConfigExtension::class.java,
        )

        extension.packageName.convention("ciyin.application.config")
        extension.configPrefix.convention("app.config.")
        extension.outputDir.convention(project.layout.buildDirectory.dir("generated/app_build_config"))

        project.afterEvaluate {
            val appId = getRequiredTrimmedProperty("app.id")
            val versionName = getRequiredTrimmedProperty("app.version.name")
            val desktopOrganization = getPropertyOrNull("app.organization")?.trim().orEmpty()
            val desktopName = getPropertyOrNull("app.name")?.trim().orEmpty()

            require(
                (desktopOrganization.isEmpty() && desktopName.isEmpty()) ||
                        (desktopOrganization.isNotEmpty() && desktopName.isNotEmpty())
            ) {
                "app.organization 和 app.name 必须同时配置，或同时省略"
            }

            registerPlatform(
                extension = extension,
                platformName = "desktop",
                appId = appId,
                versionName = versionName,
                androidNamespace = "",
                androidVersionCode = 0,
                desktopOrganization = desktopOrganization,
                desktopName = desktopName,
            )
            registerPlatform(
                extension = extension,
                platformName = "android",
                appId = appId,
                versionName = versionName,
                androidNamespace = getRequiredTrimmedProperty("android.namespace"),
                androidVersionCode = getIntProperty("android.version.code"),
                desktopOrganization = "",
                desktopName = "",
            )
            registerPlatform(
                extension = extension,
                platformName = "ios",
                appId = appId,
                versionName = versionName,
                androidNamespace = "",
                androidVersionCode = 0,
                desktopOrganization = "",
                desktopName = "",
            )
        }
    }

    /**
     * 注册指定平台的生成任务与源码目录。
     */
    private fun Project.registerPlatform(
        extension: AppBuildConfigExtension,
        platformName: String,
        appId: String,
        versionName: String,
        androidNamespace: String,
        androidVersionCode: Int,
        desktopOrganization: String,
        desktopName: String,
    ) {
        if (!hasKotlinSourceSet("${platformName}Main")) return

        val capitalizedPlatformName = platformName.replaceFirstChar { it.uppercase() }
        val taskName = "generate${capitalizedPlatformName}AppBuildConfig"
        val platformOutputDir = extension.outputDir.dir(platformName)
        val generateTask = tasks.register<GeneratePlatformAppBuildConfigTask>(taskName) {
            group = "build"
            description = "Generates AppBuildConfig for $platformName platform"
            packageName.set(extension.packageName)
            this.platformName.set(platformName)
            this.appId.set(appId)
            this.versionName.set(versionName)
            this.androidNamespace.set(androidNamespace)
            this.androidVersionCode.set(androidVersionCode)
            this.desktopOrganization.set(desktopOrganization)
            this.desktopName.set(desktopName)
            runtimeProperties.set(getPropertiesByPrefix(extension.configPrefix.get()))
            outputDirectory.set(platformOutputDir)
        }

        configureGeneratedSourceSet(
            platformName = platformName,
            generateTask = generateTask,
        )
        configurePlatformTaskDependencies(platformName, generateTask.name)
    }

    /**
     * 将生成任务输出目录接入对应平台源集。
     */
    private fun Project.configureGeneratedSourceSet(
        platformName: String,
        generateTask: TaskProvider<GeneratePlatformAppBuildConfigTask>,
    ) {
        extensions.findByType(KotlinMultiplatformExtension::class.java)
            ?.sourceSets
            ?.findByName("${platformName}Main")
            ?.kotlin
            ?.srcDir(generateTask.flatMap { it.outputDirectory })
    }

    /**
     * 判断 KMP 源集是否存在。
     */
    private fun Project.hasKotlinSourceSet(name: String): Boolean =
        extensions.findByType(KotlinMultiplatformExtension::class.java)
            ?.sourceSets
            ?.findByName(name) != null

    /**
     * 读取并校验非空属性。
     */
    private fun Project.getRequiredTrimmedProperty(name: String): String {
        val value = getProperty(name).trim()
        require(value.isNotEmpty()) { "$name 不能为空" }
        return value
    }
}

apply<AppBuildConfigConventionPlugin>()
