package me.sar.klox

import me.sar.klox.TokenType.*


class Interpreter : Expr.Visitor<Any>, Stmt.Visitor<Nil> {

    private val globals = Environment()
    private var environment = globals
    private val locals = mutableMapOf<Expr, Int>()

    init {
        with(globals) {
            define("clock", FnClock)
            define("print", FnPrint)
        }
    }

    fun interpret(statements: List<Stmt>) = try {
        statements.forEach { execute(it) }
    } catch (error: RuntimeError) {
        Lox.runtimeError(error)
    }

    private fun execute(stmt: Stmt) {
        stmt.accept(this)
    }

    fun executeBlock(statements: List<Stmt>, environment: Environment) {
        val previous = this.environment
        try {
            this.environment = environment
            statements.forEach { execute(it) }
        } finally {
            this.environment = previous
        }
    }

    fun resolve(expr: Expr, depth: Int) {
        locals[expr] = depth
    }

    override fun visit(expr: Expr.Binary): Any {
        val left = evaluate(expr.left)
        val right = evaluate(expr.right)
        when (expr.operator.type) {
            GREATER -> {
                checkOperandsType(expr.operator, Number::class.java, left, right)
                return (left as Number).toDouble() > (right as Number).toDouble()
            }
            GREATER_EQUAL -> {
                checkOperandsType(expr.operator, Number::class.java, left, right)
                return (left as Number).toDouble() >= (right as Number).toDouble()
            }
            LESS -> {
                checkOperandsType(expr.operator, Number::class.java, left, right)
                return (left as Number).toDouble() < (right as Number).toDouble()
            }
            LESS_EQUAL -> {
                checkOperandsType(expr.operator, Number::class.java, left, right)
                return (left as Number).toDouble() <= (right as Number).toDouble()
            }
            BANG_EQUAL -> {
                return left != right
            }
            EQUAL_EQUAL -> {
                return left == right
            }
            MINUS -> {
                checkOperandsType(expr.operator, Number::class.java, left, right)
                return when {
                    left is Long && right is Long -> left - right
                    else -> (left as Number).toDouble() - (right as Number).toDouble()
                }
            }
            SLASH -> {
                checkOperandsType(expr.operator, Number::class.java, left, right)
                return when {
                    left is Long && right is Long -> left / right
                    else -> (left as Number).toDouble() / (right as Number).toDouble()
                }
            }
            STAR -> {
                checkOperandsType(expr.operator, Number::class.java, left, right)
                return when {
                    left is Long && right is Long -> left * right
                    else -> (left as Number).toDouble() * (right as Number).toDouble()
                }
            }
            PLUS -> {
                return when {
                    left is Long && right is Long -> left + right
                    left is Number && right is Number -> left.toDouble() + right.toDouble()
                    left is String || right is String -> "${left}${right}"
                    else -> throw RuntimeError(expr.operator, "Operands must be two numbers or at least one string.")
                }
            }
        }
        return Nil
    }

    override fun visit(expr: Expr.Call): Any {
        val callee = evaluate(expr.callee)
        val arguments = expr.arguments.map { evaluate(it) }.toTypedArray()
        when {
            callee !is LoxCallable -> throw RuntimeError(expr.paren, "Can only call functions and classes.")
            arguments.size != callee.arity() -> throw RuntimeError(expr.paren, "Expected ${callee.arity()} arguments but got ${arguments.size}.")
            else -> return callee.call(this, *arguments)
        }
    }

    override fun visit(expr: Expr.Get): Any {
        val objekt = evaluate(expr.objekt)
        if (objekt is LoxInstance) {
            return objekt[expr.name]
        }
        throw RuntimeError(expr.name, "Only instances have properties.")
    }

    override fun visit(expr: Expr.Grouping): Any = evaluate(expr.expression)

    override fun visit(expr: Expr.Literal): Any = expr.value

    override fun visit(expr: Expr.Logical): Any {
        val left = evaluate(expr.left)

        when (expr.operator.type) {
            OR -> if (isTruthy(left)) return left
            AND -> if (!isTruthy(left)) return left
        }

        return evaluate(expr.right)
    }

    override fun visit(expr: Expr.Set): Any {
        val objekt = evaluate(expr.objekt)
        if (objekt !is LoxInstance) throw RuntimeError(expr.name, "Only instances have fields.")
        val value = evaluate(expr.value)
        objekt[expr.name] = value
        return value
    }

    override fun visit(expr: Expr.This): Any = lookupVariable(expr.keyword, expr)

    override fun visit(expr: Expr.Unary): Any {
        val right = evaluate(expr.right)
        when (expr.operator.type) {
            BANG -> return !isTruthy(right)
            MINUS -> {
                checkOperandsType(expr.operator, Number::class.java, right)
                return when (right) {
                    is Double -> -right
                    else -> -(right as Long)
                }
            }
        }

        return Nil
    }

    override fun visit(expr: Expr.Variable): Any = lookupVariable(expr.name, expr)

    private fun lookupVariable(name: Token, expr: Expr): Any {
        val distance = locals[expr]
        return when {
            distance != null -> environment.getAt(distance, name.lexeme)
            else -> globals.get(name)
        }
    }

    override fun visit(expr: Expr.Assign): Any {
        val value = evaluate(expr.value)
        val distance = locals[expr]
        when {
            distance != null -> environment.assignAt(distance, expr.name, value)
            else -> globals.assign(expr.name, value)
        }
        return value
    }

    override fun visit(expr: Expr.Empty): Any = Nil

    private fun checkOperandsType(operator: Token, clazz: Class<*>, vararg operands: Any) {
        when {
            operands.all { clazz.isInstance(it) } -> return
            else -> throw RuntimeError(operator, "Operand(s) must be ${clazz.simpleName}.")
        }
    }

    private fun isTruthy(obj: Any): Boolean = when (obj) {
        Nil -> false
        is Boolean -> obj
        else -> true
    }

    private fun evaluate(expr: Expr): Any = expr.accept(this)

    override fun visit(stmt: Stmt.Block): Nil {
        executeBlock(stmt.statements, Environment(environment))
        return Nil
    }

    override fun visit(stmt: Stmt.Expression): Nil {
        evaluate(stmt.expression)
        return Nil
    }

    override fun visit(stmt: Stmt.If): Nil {
        when {
            isTruthy(evaluate(stmt.condition)) -> execute(stmt.thenBranch)
            else -> execute(stmt.elseBranch)
        }
        return Nil
    }

    override fun visit(stmt: Stmt.Var): Nil {
        var value = evaluate(stmt.initializer)
        environment.define(stmt.name.lexeme, value)
        return Nil
    }

    override fun visit(stmt: Stmt.While): Nil {
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body)
        }
        return Nil
    }

    override fun visit(stmt: Stmt.Class): Nil {
        environment.define(stmt.name.lexeme, Nil)
        val methods = stmt.methods.map { it.name.lexeme }
                .zip(stmt.methods.map { LoxFunction(it, environment, it.name.lexeme == "init") })
                .toMap()
        val klass = LoxClass(stmt.name.lexeme, methods)
        environment.assign(stmt.name, klass)
        return Nil
    }

    override fun visit(stmt: Stmt.Function): Nil {
        val function = LoxFunction(stmt, environment, false)
        environment.define(stmt.name.lexeme, function)
        return Nil
    }

    override fun visit(stmt: Stmt.Return): Nil = throw Return(evaluate(stmt.value))

    override fun visit(stmt: Stmt.Empty): Nil = Nil
}
