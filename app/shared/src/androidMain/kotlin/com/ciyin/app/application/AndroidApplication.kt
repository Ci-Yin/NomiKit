package com.ciyin.app.application

import ciyin.platform.Context
import ciyin.platform.initApplicationContext

class AndroidApplication(override val context: Context) : MultiplatformApplication {
    override fun onCreate() {
        initApplicationContext(context)
    }

    override fun onDestroy() {

    }
}