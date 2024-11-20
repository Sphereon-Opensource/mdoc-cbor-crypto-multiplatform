package com.sphereon.crypto.kms.utils

actual fun getEnv(key: String): String {
    return System.getenv(key)
}
