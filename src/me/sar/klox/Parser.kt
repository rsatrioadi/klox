package me.sar.klox

import me.sar.klox.Stmt.Expression
import me.sar.klox.TokenType.*
import kotlin.reflect.KFunction1

class Parser(private val tokens: List<Token>) {
    private var current = 0

    class ParseError : RuntimeException()

    fun parse(): List<Stmt> {
        val statements = mutableListOf<Stmt>()
        while (!isAtEnd()) {
            statements.add(declaration())
        }

        return statements.toList()
    }

    private fun declaration(): Stmt {
        // rule: declaration -> functionDeclaration | varDeclaration | statement ;
        try {

            // functionDeclaration
            if (match(FUN)) return function("function")

            // varDeclaration
            if (match(VAR)) return varDeclaration()

            // statement
            return statement()

        } catch (e: ParseError) {
            synchronize()
            return Stmt.Empty
        }
    }

    private fun function(kind: String): Stmt.Function {
        val name = consume(IDENTIFIER, "Expect $kind name.")
        consume(LEFT_PAREN, "Expect '(' after $kind name.")
        val parameters = mutableListOf<Token>()
        if (!check(RIGHT_PAREN)) {
            do {
                if (parameters.size >= 255) {
                    error(peek(), "Cannot have more than 255 parameters.")
                }
                parameters.add(consume(IDENTIFIER, "Expect parameter name."))
            } while(match(COMMA))
        }
        consume(RIGHT_PAREN, "Expect ')' after parameters.")

        consume(LEFT_BRACE, "Expect '{' before $kind body.")
        val body = block()
        return Stmt.Function(name, parameters, body)
    }

    private fun varDeclaration(): Stmt {
        // rule: varDeclaration -> "var" IDENTIFIER ( "=" expression )? ";" ;
        val name = consume(IDENTIFIER, "Expect variable name.")
        val initializer = if (match(EQUAL)) expression() else Expr.Empty
        consume(SEMICOLON, "Expect ';' after variable declaration.")
        return Stmt.Var(name, initializer)
    }

    private fun statement(): Stmt {
        // rule: statement -> expressionStatement
        //          | forStatement
        //          | ifStatement
        //          | printStatement
        //          | whileStatement
        //          | returnStatement
        //          | block ;
        if (match(FOR)) return forStatement()
        if (match(IF)) return ifStatement()
        if (match(WHILE)) return whileStatement()
        if (match(RETURN)) return returnStatement()
        if (match(LEFT_BRACE)) return Stmt.Block(block())
        return expressionStatement()
    }

    private fun forStatement(): Stmt {
        // rule: forStmt -> "for" "(" ( varDeclaration | expressionStatement | ";" )
        //                            expression? ";"
        //                            expression? ")" statement ;
        consume(LEFT_PAREN, "Expect '(' after 'for'.")

        val initializer: Stmt = when {
            match(SEMICOLON) -> Stmt.Empty
            match(VAR) -> varDeclaration()
            else -> expressionStatement()
        }

        val condition: Expr = when {
            !check(SEMICOLON) -> expression()
            else -> Expr.Literal(true)
        }
        consume(SEMICOLON, "Expect ';' after loop condition.")

        val increment: Expr = when {
            !check(RIGHT_PAREN) -> expression()
            else -> Expr.Empty
        }
        consume(RIGHT_PAREN, "Expect ')' after for clause.")

        val body: Stmt = bodyWithInitializer(
                initializer,
                Stmt.While(
                        condition,
                        bodyWithFinalizer(
                                statement(),
                                Stmt.Expression(increment)
                        )
                )
        )

        return body
    }

    private fun bodyWithInitializer(initializer: Stmt, body: Stmt): Stmt {
        return when {
            initializer != Stmt.Empty -> Stmt.Block(listOf(initializer, body))
            else -> body
        }
    }

    private fun bodyWithFinalizer(body: Stmt, finalizer: Stmt): Stmt {
        return when {
            finalizer != Stmt.Empty -> Stmt.Block(listOf(body, finalizer))
            else -> body
        }
    }

    private fun ifStatement(): Stmt {
        // rule: ifStmt -> "if" "(" expression ")" statement ( "else" statement )? ;
        consume(LEFT_PAREN, "Expect '(' after 'if'.")
        val condition = expression()
        consume(RIGHT_PAREN, "Expect ')' after if condition.")
        val thenBranch = statement()
        val elseBranch = if (match(ELSE)) statement() else Stmt.Empty
        return Stmt.If(condition, thenBranch, elseBranch)
    }

    private fun whileStatement(): Stmt {
        // rule: whileStmt -> "while" "(" expression ")" statement ;
        consume(LEFT_PAREN, "Expect '(' after 'while'.")
        val condition = expression()
        consume(RIGHT_PAREN, "Expect ')' after while condition.")
        val body = statement()

        return Stmt.While(condition, body)
    }

    private fun returnStatement(): Stmt {
        val keyword = previous()
        val value: Expr = when {
            !check(SEMICOLON) -> expression()
            else -> Expr.Empty
        }
        consume(SEMICOLON, "Expect ';' after return value.")
        return Stmt.Return(keyword, value)
    }

    private fun block(): List<Stmt> {
        val statements = mutableListOf<Stmt>()

        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration())
        }

        consume(RIGHT_BRACE, "Expect '}' after block.")
        return statements.toList()
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
        // rule: assignment -> IDENTIFIER "=" assignment | logic_or ;

        // logic_or
        val expr = or()

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

    private fun or(): Expr {
        // rule: logic_or -> logic_and ( "or" logic_and )* ;
        return shortCircuitLogical(Parser::and,
                OR)
    }

    private fun and(): Expr {
        // rule: logic_and -> equality ( "and" equality )* ;
        return shortCircuitLogical(Parser::equality,
                AND)
    }

    private fun equality(): Expr {
        // rule: equality -> comparison ( ( "!=" | "==" ) comparison )* ;
        return leftAssociativeBinary(Parser::comparison,
                BANG_EQUAL, EQUAL_EQUAL)
    }

    private fun comparison(): Expr {
        // rule: comparison -> addition ( ( ">" | ">=" | "<" | "<=" ) addition )* ;
        return leftAssociativeBinary(Parser::addition,
                GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)
    }

    private fun addition(): Expr {
        // rule: addition -> multiplication ( ( "-" | "+" ) multiplication )* ;
        return leftAssociativeBinary(Parser::multiplication,
                MINUS, PLUS)
    }

    private fun multiplication(): Expr {
        // rule: multiplication -> unary ( ( "/" | "*" ) unary )* ;
        return leftAssociativeBinary(Parser::unary,
                SLASH, STAR)
    }

    private fun unary(): Expr {
        // rule: unary -> ( "!" | "-" ) unary | call ;

        // ( "!" | "-" ) unary
        if (match(BANG, MINUS)) {
            val operator = previous()
            val right = unary()
            return Expr.Unary(operator, right)
        }

        // call
        return call()
    }

    private fun call(): Expr {
        // rule: call -> primary ( "(" arguments? ")" )* ;

        var expr = primary()

        while(true) {
            if (match(LEFT_PAREN)) {
                expr = finishCall(expr)
            } else {
                break
            }
        }

        return expr
    }

    private fun finishCall(callee: Expr): Expr {
        val arguments = mutableListOf<Expr>()
        if (!check(RIGHT_PAREN)) {
            do {
                if (arguments.size >= 255) {
                    error(peek(), "Cannot have more than 255 arguments.")
                }
                arguments.add(expression())
            } while (match(COMMA))
        }

        val paren = consume(RIGHT_PAREN, "Expect ')' after arguments.")

        return Expr.Call(callee, paren, arguments)
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
        // where ( *operators ) expands to ( operators[0] | operators[1] | ... )

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

    private fun shortCircuitLogical(operation: KFunction1<Parser, Expr>, vararg operators: TokenType): Expr {
        // rule: logical -> operation ( ( *operators ) operation )* ;
        // where *operators expands to operators[0] | operators[1] | ...

        // operation
        var expr = operation.invoke(this)

        // ( ( *operators ) operation )*
        while (match(*operators)) {
            val operator = previous()
            val right = operation.invoke(this)
            expr = Expr.Logical(expr, operator, right)
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
                CLASS, FUN, VAR, FOR, IF, WHILE, RETURN -> return
                else -> {
                    // do nothing
                }
            }
            advance()
        }
    }
}