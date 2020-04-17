package me.sar.klox

import me.sar.klox.Expression.*
import me.sar.klox.TokenType.*


class Interpreter : Visitor<Any> {

    fun interpret(expression: Expression) {
        try {
            val value = evaluate(expression)
            println(value.toString())
        } catch (error: RuntimeError) {
            Lox.runtimeError(error)
        }
    }

    override fun visit(expr: Binary): Any {
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
                return !isEqual(left,right)
            }
            EQUAL_EQUAL -> {
                return isEqual(left,right)
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
        }
        return Nil
    }

    override fun visit(expr: Grouping): Any {
        return evaluate(expr.expression)
    }

    override fun visit(expr: Literal): Any {
        return expr.value
    }

    override fun visit(expr: Unary): Any {
        val right = evaluate(expr.right)
        when (expr.operator.type) {
            BANG -> return !isTruthy(right)
            MINUS -> {
                checkOperandsType(expr.operator, Number::class.java, right)
                return if (right is Double) -right else -(right as Long)
            }
        }

        return Nil
    }

    private fun checkOperandsType(operator: Token, clazz: Class<*>, vararg operands: Any) {
        if (operands.all { operand -> clazz.isInstance(operand) }) return
        throw RuntimeError(operator, "Operand(s) must be ${clazz.simpleName}.")
    }

    override fun visit(expr: Empty): Any {
        return Nil
    }

    private fun isTruthy(obj: Any): Boolean {
        return when (obj) {
            Nil -> false
            is Boolean -> obj
            else -> true
        }
    }

    private fun isEqual(a: Any, b: Any): Boolean {
        // nil is only equal to nil.
//        if (a === Nil && b === Nil) return true
//        if (a === Nil || b === Nil) return false

        return a == b
    }

    private fun evaluate(expr: Expression): Any {
        return expr.accept(this)
    }
}

