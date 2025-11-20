package com.ciyin.app

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.ciyin.rpa.ui.app.RpaApp

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport {
        RpaApp()
    }
}
