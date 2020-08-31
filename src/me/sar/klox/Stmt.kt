// This file was generated using GenerateAsts.kts
package me.sar.klox

sealed class Stmt {

    interface Visitor<R> {
        fun visit(stmt: Block): R
        fun visit(stmt: Expression): R
        fun visit(stmt: Function): R
        fun visit(stmt: Return): R
        fun visit(stmt: Var): R
        fun visit(stmt: Empty): R
    }

    abstract fun <R> accept(visitor: Visitor<R>): R

    data class Block(
            val statements: List<Stmt>
    ): Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visit(this)
        }
    }

    data class Expression(
            val expression: Expr
    ): Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visit(this)
        }
    }

    data class Function(
            val name: Token,
            val params: List<Token>,
            val body: List<Stmt>
    ): Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visit(this)
        }
    }

    data class Return(
            val keyword: Token,
            val value: Expr
    ): Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visit(this)
        }
    }

    data class Var(
            val name: Token,
            val initializer: Expr
    ): Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visit(this)
        }
    }

    object Empty: Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visit(this)
        }
    }
}
