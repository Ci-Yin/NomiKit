package com.ciyin.app.application

import ciyin.platform.Context

class DesktopApplication(
    override val context: Context
) : CommonApplication() {
    override fun onCreate() {
        super.onCreate()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}