package me.sar.klox

import me.sar.klox.Expression.*

class AstPrinter : Visitor<String> {
    fun print(expression: Expression): String {
        return expression.accept(this)
    }

    override fun visit(expr: Binary): String {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right)
    }

    override fun visit(expr: Grouping): String {
        return parenthesize("group", expr.expression)
    }

    override fun visit(expr: Literal): String {
        return expr.value?.toString() ?: "nil"
    }

    override fun visit(expr: Unary): String {
        return parenthesize(expr.operator.lexeme, expr.right)
    }

    private fun parenthesize(name: String, vararg expressions: Expression): String {
        val builder = StringBuilder()

        builder.append("(").append(name)
        for (expr in expressions) {
            builder.append(" ")
            builder.append(expr.accept(this))
        }
        builder.append(")")

        return builder.toString()
    }
}