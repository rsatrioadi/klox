package me.sar.klox

class LoxClass(val name: String, private val methods: Map<String, LoxFunction>): LoxCallable {

    override fun call(interpreter: Interpreter, vararg arguments: Any): Any {
        val instance = LoxInstance(this)
        if (hasMethod("init")) {
            getMethod("init").bind(instance).call(interpreter, *arguments)
        }
        return instance
    }

    override fun arity(): Int = 0
    override fun toString(): String = name
    fun hasMethod(name: String): Boolean = methods.containsKey(name)
    fun getMethod(name: String): LoxFunction = methods[name] ?: error("Method not found.")
}
