package com.sphereon.cbor.js

import io.kotest.core.config.AbstractProjectConfig

class ProjectConfig : AbstractProjectConfig() {
    override suspend fun beforeProject() {
        console.log("HELLO!")
    }

    override suspend fun afterProject() {
        console.log("GOODBYE!")
    }
}
