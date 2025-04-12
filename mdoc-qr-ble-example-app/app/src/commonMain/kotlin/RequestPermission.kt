package com.sphereon.mdoc.example.app

import com.sphereon.mdoc.example.app.permissions.DeniedAlwaysException
import com.sphereon.mdoc.example.app.permissions.DeniedException
import com.sphereon.mdoc.example.app.permissions.Permission
import com.sphereon.mdoc.example.app.permissions.PermissionState
import com.sphereon.mdoc.example.app.permissions.PermissionsController

suspend fun PermissionsController.requestPermission(permission: Permission) = try {
    providePermission(permission)
    PermissionState.Granted
} catch (e: DeniedAlwaysException) {
    PermissionState.DeniedAlways
} catch (e: DeniedException) {
    PermissionState.Denied
} catch (e: Exception) {
    // RequestCanceledException
    null
}
