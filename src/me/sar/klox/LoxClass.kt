package me.sar.klox

class LoxClass(
        val name: String,
        val superclass: LoxClass?,
        private val methods: Map<String, LoxFunction>
): LoxCallable {

    override fun call(interpreter: Interpreter, vararg arguments: Any): Any {
        val instance = LoxInstance(this)
        findMethod("init")?.bind(instance)?.call(interpreter, *arguments)
        return instance
    }

    override fun arity(): Int = 0
    override fun toString(): String = name

    fun findMethod(name: String): LoxFunction? = when {
        methods.containsKey(name) -> methods[name]
        else -> superclass?.findMethod(name)
    }
}
