package me.sar.klox

import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess

object Lox {

    var hadError = false
    var hadRuntimeError = false

    private val interpreter = Interpreter()

    fun runFile(path: String) {
        val bytes = Files.readAllBytes(Paths.get(path))
        run(String(bytes, Charset.defaultCharset()))

        // Indicate an error in the exit code.
        if (hadError) exitProcess(65)
        if (hadRuntimeError) exitProcess(70)
    }

    fun runPrompt() {
        val input = InputStreamReader(System.`in`)
        val reader = BufferedReader(input)

        while (true) {
            print("klox> ")
            run(reader.readLine())
            hadError = false
        }
    }

    private fun run(source: String) {

        val tokens: List<Token> = Scanner(source).tokens()
        val expression = Parser(tokens).parse()

        // Stop if there was a syntax error.
        if (hadError) return

        interpreter.interpret(expression)
    }

    fun error(line: Int, message: String) {
        report(line, "", message)
    }

    fun error(token: Token, message: String) {
        if (token.type === TokenType.EOF) {
            report(token.line, " at end", message)
        } else {
            report(token.line, " at '" + token.lexeme + "'", message)
        }
    }

    fun runtimeError(error: RuntimeError) {
        System.err.println("${error.message}\n[line ${error.token.line}]")
        hadRuntimeError = true
    }

    private fun report(line: Int, where: String, message: String) {
        System.err.println("[line $line] Error$where: $message")
        hadError = true
    }
}

fun main(args: Array<String>) {
    when {
        args.size>1 -> {
            println("Usage: klox [script]")
            exitProcess(64)
        }
        args.size==1 -> {
            Lox.runFile(args[0])
        }
        else -> {
            Lox.runPrompt()
        }
    }
}