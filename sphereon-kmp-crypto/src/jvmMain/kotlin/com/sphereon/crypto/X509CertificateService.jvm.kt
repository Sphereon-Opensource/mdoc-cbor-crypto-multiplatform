package com.sphereon.crypto

/**
 * We use the common code object for JVM
 */
actual fun x509Service(): X509CallbackService = X509ServiceObject
