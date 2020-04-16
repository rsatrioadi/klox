package me.sar.klox

import me.sar.klox.Expression.*
import me.sar.klox.TokenType.*
import kotlin.reflect.KFunction1


class Parser(private val tokens: List<Token>) {
    private var current = 0

    class ParseError: RuntimeException()

    fun parse(): Expression? {
        return try {
            expression()
        } catch (e: ParseError) {
            null
        }
    }

    private fun expression(): Expression {
        // rule: expression -> equality ;

        // equality
        return equality()
    }

    private fun equality(): Expression {
        // rule: equality -> comparison ( ( "!=" | "==" ) comparison )* ;
        return leftAssociativeBinary(Parser::comparison,
                BANG_EQUAL, EQUAL_EQUAL)
    }

    private fun comparison(): Expression {
        // rule: comparison -> addition ( ( ">" | ">=" | "<" | "<=" ) addition )* ;
        return leftAssociativeBinary(Parser::addition,
                GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)
    }

    private fun addition(): Expression {
        // rule: addition -> multiplication ( ( "-" | "+" ) multiplication )* ;
        return leftAssociativeBinary(Parser::multiplication,
                MINUS, PLUS)
    }

    private fun multiplication(): Expression {
        // rule: multiplication -> unary ( ( "/" | "*" ) unary )* ;
        return leftAssociativeBinary(Parser::unary,
                SLASH, STAR)
    }

    private fun unary(): Expression {
        // rule: unary -> ( "!" | "-" ) unary | primary ;

        // ( "!" | "-" ) unary
        if (match(BANG, MINUS)) {
            val operator = previous()
            val right = unary()
            return Unary(operator, right)
        }

        // primary
        return primary()
    }

    private fun primary(): Expression {
        // rule: primary -> INTEGER | REAL | STRING | "false" | "true" | "nil" | "(" expression ")" ;

        // INTEGER | REAL | STRING
        if (match(INTEGER, REAL, STRING)) {
            return Literal(previous().literal)
        }

        // "false"
        if (match(FALSE)) return Literal(false)

        // "true"
        if (match(TRUE)) return Literal(true)

        // "nil"
        if (match(NIL)) return Literal(null)

        // "(" expression ")"
        if (match(LEFT_PAREN)) {
            val expr = expression()
            consume(RIGHT_PAREN, "Expect ')' after expression.")
            return Grouping(expr)
        }

        // error
        throw error(peek(), "Expect expression.")
    }

    private fun leftAssociativeBinary(operation: KFunction1<Parser, Expression>, vararg operators: TokenType): Expression {
        // "some clever Java 8" (as Nystrom called it) in Kotlin
        // rule: leftAssociativeBinary -> operation ( ( *operators ) operation )* ;
        // where *operators expands to operators[0] | operators[1] | ...

        // operation
        var expr = operation.invoke(this)

        // ( ( *operators ) operation )*
        while (match(*operators)) {
            val operator = previous()
            val right = operation.invoke(this)
            expr = Binary(expr, operator, right)
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