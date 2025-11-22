package com.ciyin.app.di.modules

import com.ciyin.app.data.project.di.ProjectDataModule
import com.ciyin.app.data.setting.di.SettingsDataModule
import com.ciyin.app.domain.project.di.ProjectDomainModule
import com.ciyin.app.domain.script.di.ScriptDomainModule
import com.ciyin.app.domain.timed.di.TimedDomainModule
import com.yy.myuko.component.koin.ciyin.koin.KoinBootInitializer


/**
 *
 * kotlin文件作用描述
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2025/11/6 18:37
 */

val ProjectModules: KoinBootInitializer = {
    modules(ProjectDomainModule, ProjectDataModule)
}

val ScriptModules: KoinBootInitializer = {
    modules(ScriptDomainModule)
}

val TimedModules: KoinBootInitializer = {
    modules(TimedDomainModule)
}

val SettingsModules: KoinBootInitializer = {
    modules(SettingsDataModule)
}