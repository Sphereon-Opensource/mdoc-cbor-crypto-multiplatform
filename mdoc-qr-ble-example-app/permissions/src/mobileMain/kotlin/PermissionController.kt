package com.sphereon.mdoc.example.app.permissions

import dev.icerock.moko.permissions.PermissionsController as MokoPermissionsController

internal expect fun fromMoko(controller: MokoPermissionsController): PermissionsController
internal expect fun PermissionsController.toMoko(): MokoPermissionsController
