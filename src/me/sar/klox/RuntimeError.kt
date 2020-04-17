package me.sar.klox

class RuntimeError(
        val token: Token,
        message: String
) : RuntimeException (message)