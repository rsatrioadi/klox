package me.sar.klox

class LoxInstance(val klass: LoxClass) {
    private val fields = mutableMapOf<String,Any>()

    override fun toString(): String = "${klass.name} instance"

    operator fun get(name: Token): Any = when {
        fields.containsKey(name.lexeme) -> fields[name.lexeme]!!
        klass.findMethod(name.lexeme) != null -> klass.findMethod(name.lexeme)!!.bind(this)
        else -> throw RuntimeError(name, "Undefined property '${name.lexeme}'.")
    }

    operator fun set(name: Token, value: Any) {
        fields[name.lexeme] = value
    }
}
