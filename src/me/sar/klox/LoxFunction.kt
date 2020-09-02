package me.sar.klox

class LoxFunction(private val declaration: Stmt.Function, private val closure: Environment): LoxCallable {
    override fun call(interpreter: Interpreter, vararg arguments: Any): Any {
        val environment = Environment(closure)
        declaration.params.zip(arguments).forEach { pair ->
            environment.define(pair.first.lexeme, pair.second)
        }
        try {
            interpreter.executeBlock(declaration.body, environment)
        } catch (returnValue: Return) {
            return returnValue.value
        }
        return Nil
    }

    override fun arity(): Int = declaration.params.size

    override fun toString(): String = "<fn ${declaration.name.lexeme}>"
}
