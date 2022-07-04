package me.sar.klox

import me.sar.klox.Stmt.Expression
import me.sar.klox.TokenType.*
import kotlin.reflect.KFunction1

class Parser(private val tokens: List<Token>) {
    private var current = 0

    class ParseError: RuntimeException()

    fun parse(): List<Stmt> {
        val statements = mutableListOf<Stmt>()
        while (!isAtEnd()) {
            statements.add(declaration())
        }

        return statements.toList()
    }

    private fun declaration(): Stmt {
        // rule: declaration -> varDeclaration | statement ;
        try {

            // varDeclaration
            if (match(VAR)) return varDeclaration()

            // statement
            return statement()

        } catch (e: ParseError) {
            synchronize()
            return Stmt.Empty
        }
    }

    private fun varDeclaration(): Stmt {
        // rule: varDeclaration -> "var" IDENTIFIER ( "=" expression )? ";" ;
        val name = consume(IDENTIFIER, "Expect variable name.")
        var initializer: Expr = Expr.Empty
        if (match(EQUAL)) {
            initializer = expression()
        }
        consume(SEMICOLON, "Expect ';' after variable declaration.")
        return Stmt.Var(name, initializer)
    }

    private fun statement(): Stmt {
        // rule: statement -> ifStatement | printStatement | expressionStatement | block ;
        if (match(IF)) return ifStatement()
        if (match(PRINT)) return printStatement()
        if (match(LEFT_BRACE)) return Stmt.Block(block())
        return expressionStatement()
    }

    private fun ifStatement(): Stmt {
        // rule: ifStatement -> "if" "(" expression ")" statement ( "else" statement )? ;
        consume(LEFT_PAREN, "Expect '(' after 'if'.")
        val condition = expression()
        consume(RIGHT_PAREN, "Expect ')' after if condition.")
        val thenBranch = statement()
        val elseBranch = if (match(ELSE)) statement() else Stmt.Empty
        return Stmt.If(condition, thenBranch, elseBranch)
    }

    private fun block(): List<Stmt> {
        val statements = mutableListOf<Stmt>()

        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration())
        }

        consume(RIGHT_BRACE, "Expect '}' after block.")
        return statements.toList()
    }

    private fun printStatement(): Stmt {
        // rule: printStmt -> "print" expression ";"
        val value = expression()
        consume(SEMICOLON, "Expect ';' after value.")
        return Stmt.Print(value)
    }

    private fun expressionStatement(): Stmt {
        val expr = expression()
        consume(SEMICOLON, "Expect ';' after expression.")
        return Expression(expr)
    }

    private fun expression(): Expr {
        // rule: expression -> assignment ;
        return assignment()
    }

    private fun assignment(): Expr {
        // rule: assignment -> IDENTIFIER "=" assignment | equality ;

        // equality
        val expr = equality()

        // IDENTIFIER "=" assignment
        if (match(EQUAL)) {
            val equals = previous()
            val value = assignment() // recursive call since assignment() is right-associative

            if (expr is Expr.Variable) {
                val name = expr.name
                return Expr.Assign(name, value)
            }
            error(equals, "Invalid assignment target.")
        }
        return expr
    }

    private fun equality(): Expr {
        // rule: equality -> comparison ( ( "!=" | "==" ) comparison )* ;
        return leftAssociativeBinary(Parser::comparison,
                BANG_EQUAL, EQUAL_EQUAL)
    }

    private fun comparison(): Expr {
        // rule: comparison -> term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
        return leftAssociativeBinary(Parser::term,
                GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)
    }

    private fun term(): Expr {
        // rule: addition -> factor ( ( "-" | "+" ) factor )* ;
        return leftAssociativeBinary(Parser::factor,
                MINUS, PLUS)
    }

    private fun factor(): Expr {
        // rule: multiplication -> unary ( ( "/" | "*" ) unary )* ;
        return leftAssociativeBinary(Parser::unary,
                SLASH, STAR)
    }

    private fun unary(): Expr {
        // rule: unary -> ( "!" | "-" ) unary | primary ;

        // ( "!" | "-" ) unary
        if (match(BANG, MINUS)) {
            val operator = previous()
            val right = unary()
            return Expr.Unary(operator, right)
        }

        // primary
        return primary()
    }

    private fun primary(): Expr {
        // rule: primary -> INTEGER | REAL | STRING | IDENTIFIER | "false" | "true" | "nil" | "(" expression ")" ;

        // INTEGER | REAL | STRING
        if (match(INTEGER, REAL, STRING)) {
            return Expr.Literal(previous().literal)
        }

        if (match(IDENTIFIER)) {
            return Expr.Variable(previous())
        }

        // "false"
        if (match(FALSE)) return Expr.Literal(false)

        // "true"
        if (match(TRUE)) return Expr.Literal(true)

        // "nil"
        if (match(NIL)) return Expr.Literal(Nil)

        // "(" expression ")"
        if (match(LEFT_PAREN)) {
            val expr = expression()
            consume(RIGHT_PAREN, "Expect ')' after expression.")
            return Expr.Grouping(expr)
        }

        // error
        throw error(peek(), "Expect expression.")
    }

    private fun leftAssociativeBinary(operation: KFunction1<Parser, Expr>, vararg operators: TokenType): Expr {
        // "some clever Java 8" (as Nystrom called it) but in Kotlin
        // rule: leftAssociativeBinary -> operation ( ( *operators ) operation )* ;
        // where *operators expands to operators[0] | operators[1] | ...

        // operation
        var expr = operation.invoke(this)

        // ( ( *operators ) operation )*
        while (match(*operators)) {
            val operator = previous()
            val right = operation.invoke(this)
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun match(vararg types: TokenType): Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }
        return false
    }

    private fun consume(type: TokenType, message: String): Token {
        if (check(type)) return advance()
        throw error(peek(), message)
    }

    private fun check(type: TokenType): Boolean {
        if (isAtEnd()) return false
        return peek().type === type
    }

    private fun advance(): Token {
        if (!isAtEnd()) current++
        return previous()
    }

    private fun isAtEnd(): Boolean {
        return peek().type === EOF
    }

    private fun peek(): Token {
        return tokens[current]
    }

    private fun previous(): Token {
        return tokens[current - 1]
    }

    private fun error(token: Token, message: String): ParseError {
        Lox.error(token, message)
        return ParseError()
    }

    private fun synchronize() {
        advance()
        while (!isAtEnd()) {
            if (previous().type === SEMICOLON) return
            when (peek().type) {
                CLASS, FUN, VAR, FOR, IF, WHILE, PRINT, RETURN -> return
                else -> {
                    // do nothing
                }
            }
            advance()
        }
    }
}