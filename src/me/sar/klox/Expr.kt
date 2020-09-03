// This file was generated using GenerateAsts.kts
package me.sar.klox

sealed class Expr {

    interface Visitor<R> {
        fun visit(expr: Assign): R
        fun visit(expr: Binary): R
        fun visit(expr: Call): R
        fun visit(expr: Get): R
        fun visit(expr: Grouping): R
        fun visit(expr: Literal): R
        fun visit(expr: Logical): R
        fun visit(expr: Set): R
        fun visit(expr: Super): R
        fun visit(expr: This): R
        fun visit(expr: Unary): R
        fun visit(expr: Variable): R
        fun visit(expr: Empty): R
    }

    abstract fun <R> accept(visitor: Visitor<R>): R

    data class Assign(
            val name: Token,
            val value: Expr
    ): Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visit(this)
        }
    }

    data class Binary(
            val left: Expr,
            val operator: Token,
            val right: Expr
    ): Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visit(this)
        }
    }

    data class Call(
            val callee: Expr,
            val paren: Token,
            val arguments: List<Expr>
    ): Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visit(this)
        }
    }

    data class Get(
            val objekt: Expr,
            val name: Token
    ): Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visit(this)
        }
    }

    data class Grouping(
            val expression: Expr
    ): Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visit(this)
        }
    }

    data class Literal(
            val value: Any
    ): Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visit(this)
        }
    }

    data class Logical(
            val left: Expr,
            val operator: Token,
            val right: Expr
    ): Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visit(this)
        }
    }

    data class Set(
            val objekt: Expr,
            val name: Token,
            val value: Expr
    ): Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visit(this)
        }
    }

    data class Super(
            val keyword: Token,
            val method: Token
    ): Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visit(this)
        }
    }

    data class This(
            val keyword: Token
    ): Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visit(this)
        }
    }

    data class Unary(
            val operator: Token,
            val right: Expr
    ): Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visit(this)
        }
    }

    data class Variable(
            val name: Token
    ): Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visit(this)
        }
    }

    object Empty: Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visit(this)
        }
    }
}
