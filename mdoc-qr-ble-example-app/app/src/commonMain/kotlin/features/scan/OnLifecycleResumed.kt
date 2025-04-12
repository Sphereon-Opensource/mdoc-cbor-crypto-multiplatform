package com.sphereon.mdoc.example.app.features.scan

import androidx.compose.runtime.Composable

@Composable
expect fun onLifecycleResumed(onResumed: () -> Unit)
