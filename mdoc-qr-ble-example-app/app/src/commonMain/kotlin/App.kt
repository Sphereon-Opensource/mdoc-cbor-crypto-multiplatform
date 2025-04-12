package com.sphereon.mdoc.example.app

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import com.sphereon.mdoc.example.app.features.scan.ScanScreen

@Composable
fun App() {
    MaterialTheme {
        Navigator(ScanScreen())
    }
}
