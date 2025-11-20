package com.ciyin.app

import androidx.compose.ui.window.ComposeUIViewController
import com.ciyin.app.application.CommonApplication
import com.ciyin.app.application.IosApplication
import com.ciyin.app.ui.app.App
import platform.UIKit.UIViewController


fun MainViewController(): UIViewController = ComposeUIViewController {
    val commonApplication = CommonApplication()
    val iosApplication = IosApplication()
    iosApplication.onCreate()
    commonApplication.onCreate()
    App()
}