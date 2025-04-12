package com.sphereon.mdoc.example.app

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    configureLogging()
    return ComposeUIViewController {
        App()
    }
}
