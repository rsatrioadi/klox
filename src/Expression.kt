package me.sar.klox

sealed class Expression {
    interface Visitor<R> {
        fun visit(expr: Binary): R
        fun visit(expr: Grouping): R
        fun visit(expr: Literal): R
        fun visit(expr: Unary): R
    }
    abstract fun <R> accept(visitor: Visitor<R>): R


    data class Binary(
            val left: Expression,
            val operator: Token,
            val right: Expression
    ): Expression() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visit(this)
        }
    }

    data class Grouping(
            val expression: Expression
    ): Expression() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visit(this)
        }
    }

    data class Literal(
            val value: Any?
    ): Expression() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visit(this)
        }
    }

    data class Unary(
            val operator: Token,
            val right: Expression
    ): Expression() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visit(this)
        }
    }
}
