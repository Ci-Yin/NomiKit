package com.ciyin.app.application

import ciyin.platform.Context
import ciyin.platform.EmptyContext

class IosApplication(
    override val context: Context = EmptyContext
) : MultiplatformApplication {
    override fun onCreate() {

    }

    override fun onDestroy() {

    }
}
