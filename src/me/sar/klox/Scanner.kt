package me.sar.klox

import me.sar.klox.TokenType.*

internal class Scanner(private val source: String) {
    private val tokens: MutableList<Token> = arrayListOf()
    private var start = 0
    private var current = 0
    private var line = 1

    companion object {
        val keywords = mapOf(
                "and" to AND,
                "class" to CLASS,
                "else" to ELSE,
                "false" to FALSE,
                "for" to FOR,
                "fun" to FUN,
                "if" to IF,
                "nil" to NIL,
                "or" to OR,
                "print" to PRINT,
                "return" to RETURN,
                "super" to SUPER,
                "this" to THIS,
                "true" to TRUE,
                "var" to VAR,
                "while" to WHILE
        )
    }

    fun tokens(): List<Token> {
        if (tokens.isEmpty()) {
            while (!isAtEnd()) {
                // We are at the beginning of the next lexeme.
                start = current
                scanToken()
            }
            tokens.add(Token(EOF, "", EOF, line))
        }
        return tokens.toList()
    }

    private fun isAtEnd(): Boolean {
        return current >= source.length
    }

    private fun scanToken() {
        when (val c = advance()) {
            '(' -> addToken(LEFT_PAREN)
            ')' -> addToken(RIGHT_PAREN)
            '{' -> addToken(LEFT_BRACE)
            '}' -> addToken(RIGHT_BRACE)
            ',' -> addToken(COMMA)
            '.' -> addToken(DOT)
            '-' -> addToken(MINUS)
            '+' -> addToken(PLUS)
            ';' -> addToken(SEMICOLON)
            '*' -> addToken(STAR)
            '!' -> addToken(if (match('=')) BANG_EQUAL else BANG)
            '=' -> addToken(if (match('=')) EQUAL_EQUAL else EQUAL)
            '<' -> addToken(if (match('=')) LESS_EQUAL else LESS)
            '>' -> addToken(if (match('=')) GREATER_EQUAL else GREATER)
            '/' -> {
                if (match('/')) {
                    while (peek() != '\n' && !isAtEnd()) {
                        advance()
                    }
                } else {
                    addToken(SLASH)
                }
            }
            ' ', '\r', '\t' -> {
                // ignore
            }
            '\n' -> {
                line++
            }
            '"' -> string()
            else -> {
                when {
                    c.isDigit() -> {
                        number()
                    }
                    c.isLetter() -> {
                        identifier()
                    }
                    else -> {
                        Lox.error(line, "Unexpected character.")
                    }
                }
            }
        }
    }

    private fun identifier() {
        while (peek().isValidIdentifier()) advance()

        // See if the identifier is a reserved word.
        val text = source.substring(start, current)

        addToken(keywords[text]?:IDENTIFIER)
    }

    private fun number() {
        while (peek().isDigit()) advance()

        // Look for a fractional part.
        if (peek() == '.' && peekNext().isDigit()) {
            // Consume the "."
            advance()

            while (peek().isDigit()) advance()
        }

        val text = source.substring(start, current)
        if (text.contains('.')) {
            addToken(REAL, text.toDouble())
        } else {
            addToken(INTEGER, text.toLong())
        }
    }

    private fun advance(): Char {
        current++
        return source[current - 1]
    }

    private fun addToken(type: TokenType, literal: Any) {
        val text = source.substring(start, current)
        tokens.add(Token(type, text, literal, line))
    }

    private fun addToken(type: TokenType) {
        addToken(type, type)
    }

    private fun match(expected: Char): Boolean {
        if (isAtEnd()) return false
        if (source[current] != expected) return false
        current++
        return true
    }

    private fun peek(): Char {
        return if (isAtEnd()) '\u0000' else source[current]
    }

    private fun peekNext(): Char {
        if (current + 1 >= source.length) return '\u0000'
        return source[current + 1]
    }

    private fun string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++
            advance()
        }

        // Unterminated string.
        if (isAtEnd()) {
            Lox.error(line, "Unterminated string.")
            return
        }

        // The closing ".
        advance()

        // Trim the surrounding quotes.
        val value = source
                .substring(start + 1, current - 1)
                .replace("""\t""", "\t", true)
                .replace("""\b""", "\b", true)
                .replace("""\n""", "\n", true)
                .replace("""\r""", "\r", true)
                .replace("""\'""", "\'", true)
                .replace("""\"""", "\"", true)
                .replace("""\\""", "\\", true)
        addToken(STRING, value)
    }

    private fun Char.isValidIdentifier(): Boolean {
        return this.isLetter() || this == '_'
    }
}
