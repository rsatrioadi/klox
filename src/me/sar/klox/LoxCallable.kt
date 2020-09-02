package me.sar.klox

interface LoxCallable {
    fun call(interpreter: Interpreter, vararg arguments: Any): Any
    fun arity(): Int
}
