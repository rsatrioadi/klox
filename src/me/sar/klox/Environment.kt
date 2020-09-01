package me.sar.klox

open class Environment(private val enclosing: Environment?) {

    constructor(): this(null)

    private val values: MutableMap<String, Any> = mutableMapOf()

    open fun define(name: String, value: Any) {
        values[name] = value
    }

    open fun get(name: Token): Any {
        if (values.containsKey(name.lexeme)) return values[name.lexeme]!!
        if (enclosing != null) return enclosing.get(name)
        throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
    }

    open fun assign(name: Token, value: Any) {
        if (values.containsKey(name.lexeme)) {
            values[name.lexeme] = value
            return
        }
        if (enclosing != null) {
            enclosing.assign(name, value)
            return
        }
        throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
    }

    fun getAt(distance: Int, name: String): Any {
        return ancestor(distance).values[name] ?: Nil
    }

    private fun ancestor(distance: Int): Environment {
        var environment: Environment? = this
        repeat(distance) { environment = environment?.enclosing }
        if (environment == null) throw Error("Too deep!")
        return environment!!
    }

    fun assignAt(distance: Int, name: Token, value: Any) {
        ancestor(distance).values[name.lexeme] = value
    }
}