package me.sar.klox

object FnClock : LoxCallable {
    override fun call(interpreter: Interpreter, vararg arguments: Any): Any = System.currentTimeMillis().toDouble() / 1000.0
    override fun arity(): Int = 0
    override fun toString(): String = "<native fn clock>"
}

object FnPrint : LoxCallable {
    override fun call(interpreter: Interpreter, vararg arguments: Any): Any = println(arguments[0])
    override fun arity(): Int = 1
    override fun toString(): String = "<native fn print>"
}
