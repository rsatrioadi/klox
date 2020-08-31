package me.sar.klox

object FnClock: LoxCallable {
    override fun call(interpreter: Interpreter, vararg arguments: Any): Any {
        return System.currentTimeMillis().toDouble() / 1000.0
    }

    override fun arity(): Int {
        return 0
    }

    override fun toString(): String {
        return "<native fn clock>"
    }
}

object FnPrint: LoxCallable {
    override fun call(interpreter: Interpreter, vararg arguments: Any): Any {
        println(arguments[0])
        return Nil
    }

    override fun arity(): Int {
        return 1
    }

    override fun toString(): String {
        return "<native fn print>"
    }
}
