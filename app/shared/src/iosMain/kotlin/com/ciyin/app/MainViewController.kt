package com.ciyin.app

import ciyin.application.runApplication
import com.ciyin.app.application.IosApplication
import com.ciyin.app.presentation.app.App


fun MainViewController() = runApplication(::IosApplication) {
    App()
}