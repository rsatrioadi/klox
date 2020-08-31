package me.sar.klox

import me.sar.klox.Expr.*

class AstPrinter : Visitor<String> {
    fun print(expr: Expr): String {
        return expr.accept(this)
    }

    override fun visit(expr: Assign): String {
        TODO("Not yet implemented")
    }

    override fun visit(expr: Binary): String {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right)
    }

    override fun visit(expr: Grouping): String {
        return parenthesize("group", expr.expression)
    }

    override fun visit(expr: Literal): String {
        return expr.value.toString()
    }

    override fun visit(expr: Unary): String {
        return parenthesize(expr.operator.lexeme, expr.right)
    }

    override fun visit(expr: Variable): String {
        TODO("Not yet implemented")
    }

    override fun visit(expr: Logical): String {
        TODO("Not yet implemented")
    }

    override fun visit(expr: Empty): String {
        return ""
    }

    private fun parenthesize(name: String, vararg expressions: Expr): String {
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