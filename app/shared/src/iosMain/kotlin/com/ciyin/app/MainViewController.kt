package com.ciyin.app

import ciyin.application.runApplication
import com.ciyin.app.application.IosApplication
import com.ciyin.app.ui.app.App


fun MainViewController() = runApplication(::IosApplication) {
    App()
}