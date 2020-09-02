package me.sar.klox

class LoxFunction(
        private val declaration: Stmt.Function,
        private val closure: Environment,
        private val isInitializer: Boolean
): LoxCallable {

    override fun call(interpreter: Interpreter, vararg arguments: Any): Any {
        val environment = Environment(closure)
        declaration.params.zip(arguments).forEach { environment.define(it.first.lexeme, it.second) }
        try {
            interpreter.executeBlock(declaration.body, environment)
        } catch (returnValue: Return) {
            return when {
                isInitializer -> closure.getAt(0, "this")
                else -> returnValue.value
            }
        }
        return when {
            isInitializer -> closure.getAt(0, "this")
            else -> Nil
        }
    }

    override fun arity(): Int = declaration.params.size

    override fun toString(): String = "<fn ${declaration.name.lexeme}>"

    fun bind(instance: LoxInstance): LoxFunction {
        val environment = Environment(closure)
        environment.define("this", instance)
        return LoxFunction(declaration, environment, isInitializer)
    }
}
