package com.sphereon.mdoc.example.app.permissions

import androidx.compose.runtime.Composable

public fun interface PermissionsControllerFactory {
    public fun createPermissionsController(): PermissionsController
}

@Composable
public expect fun rememberPermissionsControllerFactory(): PermissionsControllerFactory
