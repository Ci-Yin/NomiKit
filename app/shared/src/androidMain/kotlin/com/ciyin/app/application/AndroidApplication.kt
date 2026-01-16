package com.ciyin.app.application

import ciyin.application.BaseAndroidApplication
import ciyin.application.MultiplatformApplication
import ciyin.platform.Context

class AndroidApplication() : BaseAndroidApplication() {

    override val application: MultiplatformApplication = InternalApplication(this)

    override fun onCreate() {
        super.onCreate()
    }

    override fun onTerminate() {
        super.onTerminate()
    }

    private class InternalApplication(override val context: Context) : CommonApplication()

}

