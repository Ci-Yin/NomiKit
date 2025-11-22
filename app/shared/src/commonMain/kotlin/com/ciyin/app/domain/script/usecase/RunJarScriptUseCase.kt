package com.ciyin.app.domain.script.usecase

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import ciyin.io.resolve
import ciyin.io.toFile
import ciyin.platform.Log
import ciyin.serialization.json.writeJson
import com.ciyin.app.data.project.ProjectArgs
import com.ciyin.app.data.project.ScriptArgs
import com.ciyin.app.data.project.datasource.DataStoreManager.settingLocalData2
import com.ciyin.app.data.project.model.GameProject
import com.ciyin.app.data.project.model.game
import com.ciyin.app.data.project.toKeyValueArgs
import com.ciyin.app.domain.project.Platform
import com.ciyin.app.domain.script.JarScriptManager
import com.ciyin.app.util.FilePath.RuntimeDataDir


/**
 *
 * kotlin类作用描述
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2025/11/4 15:40
 */
class RunJarScriptUseCase {
    suspend operator fun invoke(project: GameProject): Either<ScriptError, Unit> = either {

        // 停止Jar进程
        if (project.isRunning) {
            JarScriptManager.destroy(project.jarPath)
            raise(ScriptError.Stop)
        }

        Log.info("JarScript", "运行项目：$this")

        // 检查WindowsDriver文件是否存在
        ensure(settingLocalData2.data.windowsDriverPath.toFile().exists()) {
            ScriptError.WindowsDriverNotExist("WindowsDriver 文件不存在: ${settingLocalData2.data.windowsDriverPath}")
        }

        // 写入当前项目的游戏数据
        val resolve = RuntimeDataDir.resolve("${project.name}.json").apply {
            writeJson(project.game, true)
        }

        // 运行jar文件
        JarScriptManager.run(
            jarPath = project.jarPath,
            args = ScriptArgs(
                driverPath = settingLocalData2.data.windowsDriverPath,
                scriptProjectClass = project.scriptProjectClass,
                platform = Platform.Windows.ordinal,
                args = ProjectArgs(
                    runtimeDataFile = resolve,
                )
            ).toKeyValueArgs()
        )

        // 等待进程结束
        JarScriptManager.wait(project.jarPath, project.name)

        // 切换运行状态
//        project.isRunning = !project.isRunning
    }
}