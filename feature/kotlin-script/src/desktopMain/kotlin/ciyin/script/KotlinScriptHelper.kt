package ciyin.script

import ciyin.platform.Log
import ciyin.platform.log
import java.io.File
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.ScriptDiagnostic.Severity.DEBUG
import kotlin.script.experimental.api.ScriptDiagnostic.Severity.ERROR
import kotlin.script.experimental.api.ScriptDiagnostic.Severity.FATAL
import kotlin.script.experimental.api.ScriptDiagnostic.Severity.INFO
import kotlin.script.experimental.api.ScriptDiagnostic.Severity.WARNING
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost

object KotlinScriptHelper {

    fun runKotlinScript(filePath: String, vararg arg: String) {

        log("🔄 加载脚本: $filePath\n")

        val scriptFile = File(filePath)

        if (!scriptFile.exists()) {
            log("❌ 脚本文件不存在: $filePath\n")
            return
        }

        // 定义变量（可以是任意类型）
        val bindings: Map<String, Any> = mapOf(
            //"windowBot" to windowBot
        )
//
//    val configuration = ScriptCompilationConfiguration {
//
//        //importScripts(File(scriptFile.parent + "\\base1.gradle.kts").toScriptSource())
//        jvm {
//            dependenciesFromClassContext(Log::class, wholeClasspath = true)
//            dependenciesFromClassContext(Aibote::class, wholeClasspath = true)
//            dependenciesFromClassContext(ScriptManager::class, wholeClasspath = true)
//        }
//        refineConfiguration {
//            beforeCompiling { context ->
//                ResultWithDiagnostics.Success(
//                    ScriptCompilationConfiguration(context.compilationConfiguration) {
//                        providedProperties("windowBot" to WindowBot::class)
//                    }
//                )
//            }
//        }
//    }
//
//    val evalConfig = ScriptEvaluationConfiguration {
//        scriptsInstancesSharing(true)
//        providedProperties(bindings)
//        jvm {
//            baseClassLoader(ScriptManager::class.java.classLoader)
//        }
//    }

        val configuration = SimpleMainKtsScriptDefinition()
        val evalConfig = MainKtsEvaluationConfiguration

        val result =
            BasicJvmScriptingHost().eval(scriptFile.toScriptSource(), configuration, evalConfig)
        when (result) {
            is ResultWithDiagnostics.Success -> log("✅ 脚本执行完成\n")
            is ResultWithDiagnostics.Failure -> {
                log("❌ 脚本执行失败: \n")
                result.reports.forEach {
                    when (it.severity) {
                        ERROR -> Log.error("脚本输出", "❌ $it ${it.sourcePath}")
                        WARNING -> Log.warn("脚本输出", "⚠️ $it")
                        INFO -> Log.info("脚本输出", "ℹ️ $it")
                        DEBUG -> Log.debug("脚本输出", "🐞 $it")
                        FATAL -> Log.error("脚本输出", "❌ $it")
                    }
                }
            }
        }

    }

}