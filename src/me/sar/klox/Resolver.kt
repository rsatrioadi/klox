package me.sar.klox

import java.util.*
import kotlin.system.exitProcess

class Resolver(private val interpreter: Interpreter) : Expr.Visitor<Unit>, Stmt.Visitor<Unit> {
    private val scopes = Stack<MutableMap<String, Boolean>>()
    private var currentFunction: FunctionType = FunctionType.NONE

    enum class FunctionType {
        NONE, FUNCTION
    }

    override fun visit(expr: Expr.Assign) {
        resolve(expr.value)
        resolveLocal(expr, expr.name)
    }

    override fun visit(expr: Expr.Binary) {
        resolve(expr.left)
        resolve(expr.right)
    }

    override fun visit(expr: Expr.Call) {
        resolve(expr.callee)
        expr.arguments.forEach { resolve(it) }
    }

    override fun visit(expr: Expr.Grouping) {
        resolve(expr.expression)
    }

    override fun visit(expr: Expr.Literal) {}

    override fun visit(expr: Expr.Unary) {
        resolve(expr.right)
    }

    override fun visit(expr: Expr.Variable) {
        if (!scopes.empty() && scopes.peek()[expr.name.lexeme] == false) {
            Lox.error(expr.name, "Cannot read local variable in its own initializer.")
        }
        resolveLocal(expr, expr.name)
    }

    override fun visit(expr: Expr.Logical) {
        resolve(expr.left)
        resolve(expr.right)
    }

    override fun visit(expr: Expr.Empty) {}

    override fun visit(stmt: Stmt.Block) {
        beginScope()
        resolve(stmt.statements)
        endScope()
    }

    override fun visit(stmt: Stmt.Expression) {
        resolve(stmt.expression)
    }

    override fun visit(stmt: Stmt.Function) {
        declare(stmt.name)
        define(stmt.name)
        resolveFunction(stmt, FunctionType.FUNCTION)
    }

    override fun visit(stmt: Stmt.Return) {
        if (currentFunction == FunctionType.NONE) {
            val exitCode = stmt.value.accept(interpreter)
            if (exitCode is Number) exitProcess(exitCode.toInt())
            else Lox.error(stmt.keyword, "Return from top-level code only accepts Numbers.")
        }
        resolve(stmt.value)
    }

    override fun visit(stmt: Stmt.Var) {
        declare(stmt.name)
        resolve(stmt.initializer)
        define(stmt.name)
    }

    override fun visit(stmt: Stmt.If) {
        resolve(stmt.condition)
        resolve(stmt.thenBranch)
        resolve(stmt.elseBranch)
    }

    override fun visit(stmt: Stmt.While) {
        resolve(stmt.condition)
        resolve(stmt.body)
    }

    override fun visit(stmt: Stmt.Empty) {}

    private fun beginScope() {
        scopes.push(mutableMapOf())
    }

    private fun endScope() {
        scopes.pop()
    }

    fun resolve(statements: List<Stmt>) {
        statements.forEach { resolve(it) }
    }

    private fun resolve(stmt: Stmt) {
        stmt.accept(this)
    }

    private fun resolve(expr: Expr) {
        expr.accept(this)
    }

    private fun resolveLocal(expr: Expr, name: Token) {
        val i = scopes.indexOfLast { it.containsKey(name.lexeme) }
        if (i > -1) {
            interpreter.resolve(expr, scopes.size - 1 - i)
        }
    }

    private fun resolveFunction(function: Stmt.Function, type: FunctionType) {
        val enclosingFunction = currentFunction
        currentFunction = type
        beginScope()
        function.params.forEach { declare(it); define(it) }
        resolve(function.body)
        endScope()
        currentFunction = enclosingFunction
    }

    private fun declare(name: Token) {
        if (!scopes.empty()) {
            val scope = scopes.peek()
            if (scope.containsKey(name.lexeme)) {
                Lox.error(name, "Variable with this name already declared in this scope.")
            }
            scope[name.lexeme] = false
        }
    }

    private fun define(name: Token) {
        if (!scopes.empty()) {
            val scope = scopes.peek()
            scope[name.lexeme] = true
        }
    }
}