package com.ciyin.app.domain.project.di

import com.ciyin.app.data.project.ProjectRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module


/**
 *
 * kotlin文件作用描述
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2025/11/6 14:05
 */

val ProjectDomainModule = module {
    singleOf(::ProjectRepository)
}
