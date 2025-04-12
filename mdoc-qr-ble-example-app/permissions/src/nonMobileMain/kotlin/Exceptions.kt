package com.sphereon.mdoc.example.app.permissions

public actual open class DeniedException : Exception()
public actual class DeniedAlwaysException : DeniedException()
