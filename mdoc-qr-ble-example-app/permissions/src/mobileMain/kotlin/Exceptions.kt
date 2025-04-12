package com.sphereon.mdoc.example.app.permissions

import dev.icerock.moko.permissions.DeniedAlwaysException as MokoDeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException as MokoDeniedException

public actual typealias DeniedException = MokoDeniedException
public actual typealias DeniedAlwaysException = MokoDeniedAlwaysException
