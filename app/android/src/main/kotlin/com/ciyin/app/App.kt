package com.ciyin.app

import android.app.Application
import com.ciyin.app.application.AndroidApplication
import com.ciyin.app.application.CommonApplication

class App : Application() {

    private val androidApplication = AndroidApplication(this)
    private val commonApplication = CommonApplication(this)

    override fun onCreate() {
        super.onCreate()
        androidApplication.onCreate()
        commonApplication.onCreate()
    }

    override fun onTerminate() {
        androidApplication.onDestroy()
        commonApplication.onDestroy()
        super.onTerminate()
    }
}