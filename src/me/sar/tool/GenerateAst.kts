import java.io.PrintWriter

val outputDir = "../klox"

defineAst(outputDir,
    "Expr",
    "Assign = val name: Token, val value: Expr",
    "Binary = val left: Expr, val operator: Token, val right: Expr",
    "Grouping = val expression: Expr",
    "Literal = val value: Any",
    "Unary = val operator: Token, val right: Expr",
    "Variable = val name: Token"
)

defineAst(outputDir,
    "Stmt",
    "Block = val statements: List<Stmt>",
    "Expression = val expression: Expr",
    "If = val condition: Expr, val thenBranch: Stmt, val elseBranch: Stmt",
    "Print = val expression: Expr",
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
    for (type in types) {
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
    for (type in types) {
        val typeName = type.split("=".toRegex()).toTypedArray()[0].trim()
        writer.println("        fun visit(${baseName.lowercase()}: $typeName): R")
    }
    writer.println("        fun visit(${baseName.lowercase()}: Empty): R")
    writer.println("    }")
}
