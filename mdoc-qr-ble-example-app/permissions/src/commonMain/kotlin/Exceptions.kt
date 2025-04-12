package com.sphereon.mdoc.example.app.permissions

public expect open class DeniedException : Exception
public expect class DeniedAlwaysException : DeniedException
