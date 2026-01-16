package ciyin.application

import android.app.Application

abstract class BaseAndroidApplication() : Application() {

    protected abstract val application: MultiplatformApplication

    override fun onCreate() {
        super.onCreate()
        application.onCreate()
    }

    override fun onTerminate() {
        super.onTerminate()
        application.onDestroy()
    }

}