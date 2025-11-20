package ciyin.platform.win32

import ciyin.io.resolve
import ciyin.io.toFile
import ciyin.io.writeText
import ciyin.platform.model.TaskSchedule
import com.sun.jna.platform.win32.Kernel32
import com.sun.jna.platform.win32.Psapi
import com.sun.jna.platform.win32.WinNT
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.nio.file.Paths
import java.time.DayOfWeek
import java.time.OffsetDateTime
import java.time.format.TextStyle
import java.util.Locale
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

fun getCurrentExePath(): String {
    val pid = Kernel32.INSTANCE.GetCurrentProcessId()
    val hProcess = Kernel32.INSTANCE.OpenProcess(
        WinNT.PROCESS_QUERY_INFORMATION or WinNT.PROCESS_VM_READ,
        false,
        pid
    )
    val exePathBuffer = CharArray(1024)
    Psapi.INSTANCE.GetModuleFileNameExW(hProcess, null, exePathBuffer, exePathBuffer.size)
    Kernel32.INSTANCE.CloseHandle(hProcess)
    return Paths.get(String(exePathBuffer).trim { it <= ' ' }).toString()
}

fun createScheduledTasksInFolder(
    taskFolder: String,
    taskNamePrefix: String,
    timings: List<TaskSchedule>,
    execPath: String,
    programArgs: List<String> = emptyList()
) {
    val xmlFile =
        System.getProperty("java.io.tmpdir").toFile().resolve("rpa_scheduled_task_temp_file.xml")
    TaskXmlBuilder(execPath, timings, programArgs).build().apply {
//        xmlFile.writeText(this, Charsets.UTF_16)
        xmlFile.writeText(this)
    }
    importScheduledTaskXml(taskNamePrefix, taskFolder, xmlFile.absolutePath)
}

fun deleteScheduledTask(taskFolder: String, taskNamePrefix: String) {
    val command = listOf(
        "schtasks",
        "/Delete",
        "/TN",
        if (taskFolder.isNotEmpty()) "\\$taskFolder\\$taskNamePrefix" else taskNamePrefix,
        "/F"
    )
    val process = ProcessBuilder(command)
        .redirectErrorStream(true)
        .start()

    val output = process.inputStream.bufferedReader().use { it.readText() }

    val exitCode = process.waitFor()
    if (exitCode != 0) {
        error("删除任务失败，exit code: $exitCode output: ${output.trim()}")
    }
}

fun createScheduledTasksInFolderForCmd(
    taskFolder: String,
    taskNamePrefix: String,
    timings: List<TaskSchedule>,
    execPath: String,
    programArgs: List<String> = emptyList()
) = timings.forEachIndexed { index, timing ->

    // 判断是不是每天
    val isEveryDay = timing.week.containsAll((1..7).toSet())

    // 组装 /D 参数（周几缩写，只有每周才用）
    val dayStr = timing.week.joinToString(",") {
        DayOfWeek.of(it).getDisplayName(TextStyle.SHORT, Locale.ENGLISH).uppercase()
    }

    val timeStr = "%02d:%02d".format(timing.hour, timing.minute)

    // 任务名带文件夹格式: \FolderName\TaskName-Index
    val taskName = "\\$taskFolder\\$taskNamePrefix-$index"

    val command = mutableListOf(
        "schtasks",
        "/Create",
        "/TN", taskName,
        "/TR", execPath,
        "/ST", timeStr,
        "/F"
    )

    command += if (isEveryDay) {
        listOf("/SC", "DAILY")
    } else {
        listOf("/SC", "WEEKLY", "/D", dayStr)
    }

    println("执行命令: ${command.joinToString(" ")}")
    val process = ProcessBuilder(command)
        .redirectErrorStream(true)
        .start()

    process.inputStream.bufferedReader(Charsets.UTF_8).use { reader ->
        reader.lineSequence().forEach { println(it) }
    }

    val exitCode = process.waitFor()
    if (exitCode != 0) {
        error("任务创建失败，exit code: $exitCode")
    }

}


fun importScheduledTaskXml(taskName: String, folderName: String, xmlFilePath: String) {
    val psScript = """
        if (-not (Get-ScheduledTask -TaskPath "\$folderName" -ErrorAction SilentlyContinue)) {
            New-ScheduledTaskFolder -Name \$folderName
        }
        Register-ScheduledTask -TaskName "$taskName" -Xml (Get-Content -Path "$xmlFilePath" -Encoding Unicode | Out-String) -TaskPath "\$folderName" -Force
    """.trimIndent()

    val command = listOf("powershell.exe", "-NoProfile", "-NonInteractive", "-Command", psScript)

    val process = ProcessBuilder(command)
        .redirectErrorStream(true)
        .start()

    val output = process.inputStream.bufferedReader().use { it.readText() }
    val exitCode = process.waitFor()

    if (exitCode != 0) {
        error("任务创建失败，exit code: $exitCode output: ${output.trim()}")
    }

}


class TaskXmlBuilder(
    val execPath: String,
    val timings: List<TaskSchedule>,
    val programArgs: List<String> = emptyList()
) {

    @OptIn(ExperimentalTime::class)
    fun build(): String {
        val nowDate = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date.toString()

        val triggersXml = timings.joinToString("\n") { timing ->
            buildTriggerXml(timing, nowDate)
        }

        val argsXml = if (programArgs.isNotEmpty()) {
            // 转义 & < > 等 XML 特殊字符
            "<Arguments>${programArgs.joinToString(" ") { escapeXml(it) }}</Arguments>"
        } else {
            ""
        }

        return """<?xml version="1.0" encoding="UTF-16"?>
            <Task version="1.4" xmlns="http://schemas.microsoft.com/windows/2004/02/mit/task">
              <RegistrationInfo>
                <Date>${OffsetDateTime.now()}</Date>
                <Author>YourAppName</Author>
                <Description>自动生成的定时任务</Description>
              </RegistrationInfo>
              <Triggers>
              $triggersXml
              </Triggers>
              <Principals>
                <Principal id="Author">
                  <RunLevel>LeastPrivilege</RunLevel>
                </Principal>
              </Principals>
              <Settings>
                <MultipleInstancesPolicy>IgnoreNew</MultipleInstancesPolicy>
                <DisallowStartIfOnBatteries>false</DisallowStartIfOnBatteries>
                <StopIfGoingOnBatteries>false</StopIfGoingOnBatteries>
                <AllowHardTerminate>true</AllowHardTerminate>
                <StartWhenAvailable>true</StartWhenAvailable>
                <RunOnlyIfNetworkAvailable>false</RunOnlyIfNetworkAvailable>
                <IdleSettings>
                  <StopOnIdleEnd>true</StopOnIdleEnd>
                  <RestartOnIdle>false</RestartOnIdle>
                </IdleSettings>
                <AllowStartOnDemand>true</AllowStartOnDemand>
                <Enabled>true</Enabled>
                <Hidden>false</Hidden>
                <RunOnlyIfIdle>false</RunOnlyIfIdle>
                <WakeToRun>true</WakeToRun>
                <ExecutionTimeLimit>PT0S</ExecutionTimeLimit>
                <Priority>7</Priority>
              </Settings>
              <Actions Context="Author">
                <Exec>
                  <Command>$execPath</Command>
                  $argsXml
                </Exec>
              </Actions>
            </Task>
        """.trimIndent()
    }

    private fun buildTriggerXml(timing: TaskSchedule, nowDate: String): String {
        val timeStr = "%02d:%02d:00".format(timing.hour, timing.minute)
        val isEveryDay = timing.week.containsAll((1..7).toSet())

        return if (isEveryDay) {
            """
            <CalendarTrigger>
              <StartBoundary>${nowDate}T$timeStr</StartBoundary>
              <Enabled>true</Enabled>
              <ScheduleByDay>
                <DaysInterval>1</DaysInterval>
              </ScheduleByDay>
            </CalendarTrigger>
            """.trimIndent()
        } else {
            val daysXml = timing.week.sorted().joinToString("\n") { dayNum ->
                val dayName = DayOfWeek.of(dayNum).name
                "    <$dayName/>"
            }
            """
            <WeeklyTrigger>
              <StartBoundary>$nowDate""T$timeStr</StartBoundary>
              <Enabled>true</Enabled>
              <DaysOfWeek>
            $daysXml
              </DaysOfWeek>
              <WeeksInterval>1</WeeksInterval>
            </WeeklyTrigger>
            """.trimIndent()
        }
    }

    private fun escapeXml(s: String): String {
        return s.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

}