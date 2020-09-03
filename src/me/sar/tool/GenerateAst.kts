import java.io.PrintWriter

val outputDir = "../klox"

defineAst(outputDir, "Expr",
        "Assign = val name: Token, val value: Expr",
        "Binary = val left: Expr, val operator: Token, val right: Expr",
        "Call = val callee: Expr, val paren: Token, val arguments: List<Expr>",
        "Get = val objekt: Expr, val name: Token",
        "Grouping = val expression: Expr",
        "Literal = val value: Any",
        "Logical = val left: Expr, val operator: Token, val right: Expr",
        "Set = val objekt: Expr, val name: Token, val value: Expr",
        "Super = val keyword: Token, val method: Token",
        "This = val keyword: Token",
        "Unary = val operator: Token, val right: Expr",
        "Variable = val name: Token"
)

defineAst(outputDir, "Stmt",
        "Block = val statements: List<Stmt>",
        "Class = val name: Token, var superclass: Expr.Variable?, val methods: List<Stmt.Function>",
        "Expression = val expression: Expr",
        "If = val condition: Expr, val thenBranch: Stmt, val elseBranch: Stmt",
        "While = val condition: Expr, val body: Stmt",
        "Function = val name: Token, val params: List<Token>, val body: List<Stmt>",
        "Return = val keyword: Token, val value: Expr",
        "Var = val name: Token, val initializer: Expr"
)




fun defineAst(outputDir: String, baseName: String, vararg types: String) {
    val path = "$outputDir/$baseName.kt"
    val writer = PrintWriter(path, "UTF-8")
    writer.println("// This file was generated using GenerateAsts.kts")
    writer.println("package me.sar.klox")
    writer.println()
    writer.println("sealed class $baseName {")
    writer.println()
    defineVisitor(writer, baseName, *types)
    writer.println()
    writer.println("    abstract fun <R> accept(visitor: Visitor<R>): R")
    writer.println()
    types.forEach { type ->
        val split = type.split("=".toRegex())
        val className = split[0].trim()
        val fields = split[1].trim()
        defineType(writer, baseName, className, fields)
        writer.println()
    }
    defineEmpty(writer, baseName)
    writer.println("}")
    writer.close()
}

fun defineType(writer: PrintWriter, baseName: String, className: String, fieldList: String) {
    writer.println("    data class $className(")
    writer.println("            ${fieldList.replace(", ",",\n            ")}")
    writer.println("    ): $baseName() {")
    writer.println("        override fun <R> accept(visitor: Visitor<R>): R {")
    writer.println("            return visitor.visit(this)")
    writer.println("        }")
    writer.println("    }")
}

fun defineEmpty(writer: PrintWriter, baseName: String) {
    writer.println("    object Empty: $baseName() {")
    writer.println("        override fun <R> accept(visitor: Visitor<R>): R {")
    writer.println("            return visitor.visit(this)")
    writer.println("        }")
    writer.println("    }")
}

fun defineVisitor(writer: PrintWriter, baseName: String, vararg types: String) {
    writer.println("    interface Visitor<R> {")
    types
            .map { it.split("=".toRegex()).toTypedArray()[0].trim() }
            .forEach { writer.println("        fun visit(${baseName.toLowerCase()}: $it): R") }
    writer.println("        fun visit(${baseName.toLowerCase()}: Empty): R")
    writer.println("    }")
}
