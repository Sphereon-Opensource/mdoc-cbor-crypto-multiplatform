package com.sphereon.mdoc.example.app.bluetooth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
public actual fun rememberSystemControl(): SystemControl =
    remember { AppleSystemControl() }
