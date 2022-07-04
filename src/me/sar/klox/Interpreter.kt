package me.sar.klox

import me.sar.klox.TokenType.*


class Interpreter: Expr.Visitor<Any>, Stmt.Visitor<Nil> {

    private var environment = Environment()

    fun interpret(statements: List<Stmt>) {
        try {
            statements.forEach { statement -> execute(statement) }
        } catch (error: RuntimeError) {
            Lox.runtimeError(error)
        }
    }

    private fun execute(stmt: Stmt) {
        stmt.accept(this)
    }

    private fun executeBlock(statements: List<Stmt>, environment: Environment) {
        val previous = this.environment
        try {
            this.environment = environment

            statements.forEach { statement -> execute(statement) }
        } finally {
            this.environment = previous
        }
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
                return left!=right
            }
            EQUAL_EQUAL -> {
                return left==right
            }
            MINUS -> {
                checkOperandsType(expr.operator, Number::class.java, left, right)
                if (left is Long && right is Long) return left-right
                return (left as Number).toDouble() - (right as Number).toDouble()
            }
            SLASH -> {
                checkOperandsType(expr.operator, Number::class.java, left, right)
                if (left is Long && right is Long) return left/right
                return (left as Number).toDouble() / (right as Number).toDouble()
            }
            STAR -> {
                checkOperandsType(expr.operator, Number::class.java, left, right)
                if (left is Long && right is Long) return left*right
                return (left as Number).toDouble() * (right as Number).toDouble()
            }
            PLUS -> {
                if (left is Long && right is Long) return left+right
                else if (left is Number && right is Number) return left.toDouble() + right.toDouble()
                else if (left is String || right is String) return "${left}${right}"
                throw RuntimeError(expr.operator, "Operands must be two numbers or at least one string.")
            }
            else -> {}
        }
        return Nil
    }

    override fun visit(expr: Expr.Grouping): Any {
        return evaluate(expr.expression)
    }

    override fun visit(expr: Expr.Literal): Any {
        return expr.value
    }

    override fun visit(expr: Expr.Unary): Any {
        val right = evaluate(expr.right)
        when (expr.operator.type) {
            BANG -> return !isTruthy(right)
            MINUS -> {
                checkOperandsType(expr.operator, Number::class.java, right)
                return if (right is Double) -right else -(right as Long)
            }
            else -> {}
        }

        return Nil
    }

    override fun visit(expr: Expr.Variable): Any {
        return environment.get(expr.name)
    }

    override fun visit(expr: Expr.Assign): Any {
        val value = evaluate(expr.value)
        environment.assign(expr.name, value)
        return value
    }

    override fun visit(expr: Expr.Empty): Any {
        return Nil
    }

    private fun checkOperandsType(operator: Token, clazz: Class<*>, vararg operands: Any) {
        if (operands.all { operand -> clazz.isInstance(operand) }) return
        throw RuntimeError(operator, "Operand(s) must be ${clazz.simpleName}.")
    }

    private fun isTruthy(obj: Any): Boolean {
        return when (obj) {
            Nil -> false
            is Boolean -> obj
            else -> true
        }
    }

    private fun evaluate(expr: Expr): Any {
        return expr.accept(this)
    }

    override fun visit(stmt: Stmt.Block): Nil {
        executeBlock(stmt.statements, Environment(environment))
        return Nil
    }

    override fun visit(stmt: Stmt.Expression): Nil {
        evaluate(stmt.expression)
        return Nil
    }

    override fun visit(stmt: Stmt.If): Nil {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch)
        } else if (stmt.elseBranch != Stmt.Empty) {
            execute(stmt.elseBranch)
        }
        return Nil
    }

    override fun visit(stmt: Stmt.Print): Nil {
        val value = evaluate(stmt.expression)
        println(value.toString())
        return Nil
    }

    override fun visit(stmt: Stmt.Var): Nil {
        var value: Any = Nil
        if (stmt.initializer!=Expr.Empty) {
            value = evaluate(stmt.initializer)
        }
        environment.define(stmt.name.lexeme, value)
        return Nil
    }

    override fun visit(stmt: Stmt.Empty): Nil {
        return Nil
    }
}

