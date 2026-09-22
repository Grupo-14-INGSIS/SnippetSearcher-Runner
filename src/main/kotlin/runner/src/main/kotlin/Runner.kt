package runner.src.main.kotlin

import ast.src.main.kotlin.ASTNode
import ast.src.main.kotlin.ASTNodeType
import formatter.src.main.kotlin.Formatter
import inputprovider.src.main.kotlin.ConsoleInputProvider
import inputprovider.src.main.kotlin.InputProvider
import interpreter.src.main.kotlin.Interpreter
import lexer.src.main.kotlin.Lexer
import linter.src.main.kotlin.Linter
import linter.src.main.kotlin.config.ConfigFactory
import linter.src.main.kotlin.config.ConfigLoader
import parser.src.main.kotlin.Parser
import java.io.File
import java.io.FileWriter

class Runner {

    fun executionCommand(
        args: List<String>,
        inputProvider: InputProvider = ConsoleInputProvider(),
        printer: (Any?) -> Unit = ::println
    ) {
        if (args.isEmpty()) {
            println("Must specify the source file.")
            return
        }
        val sourceFile = File(args[0])
        val version = if (args.size > 1) args[1] else "1.0"
        if (!sourceFile.exists()) {
            println("Error: The source file '${sourceFile.path}' does not exist.")
            return
        }

        val source = sourceFile.readText()
        val statements = Lexer.from(source, version).lexIntoStatements().toList()
        val interpreter = Interpreter(version, inputProvider, printer)

        for (statement in statements) {
            val parser = Parser(statement, version)
            val ast: ASTNode = parser.parse()
            if (ast.type == ASTNodeType.INVALID) {
                println("SYNTAX ERROR: ${ast.content}")
                return
            }
            interpreter.interpret(ast)
        }
    }

    fun analyzerCommand(args: List<String>) {
        if (args.size < 2) {
            println("Error: Must specify the source file and the analysis configuration file.")
            return
        }
        val sourceFile = File(args[0])
        val configFile = File(args[1])
        val version = if (args.size > 2) args[2] else "1.0"

        if (!sourceFile.exists()) {
            println("Error: The source file '${sourceFile.path}' does not exist.")
            return
        }
        if (!configFile.exists()) {
            println("Error: The configuration file '${configFile.path}' does not exist.")
            return
        }

        val source = sourceFile.readText()
        val statements = Lexer.from(source, version).lexIntoStatements().toList()
        val asts = mutableListOf<ASTNode>()
        for (statement in statements) {
            val parser = Parser(statement, version)
            val ast = parser.parse()
            if (ast.type == ASTNodeType.INVALID) {
                println("SYNTAX ERROR: ${ast.content}")
                return
            }
            asts.add(ast)
        }

        val loader = ConfigLoader()
        val yamlMap = loader.loadYaml(configFile.path)
        val lintRules = ConfigFactory().createRules(yamlMap)
        val linter = Linter(lintRules)
        val allErrors = linter.lint(asts)

        if (allErrors.isEmpty()) {
            println("SUCCESS: No issues were found")
        } else {
            println("ANALYSIS RESULTS: ${allErrors.size} issue(s) found:")
            allErrors.forEach { println("  - $it") }
        }
    }

    fun formatterCommand(args: List<String>) {
        if (args.size < 2) {
            println("Error: Must specify the source file and the format configuration file.")
            return
        }
        val sourceFile = File(args[0])
        val configFile = File(args[1])
        val version = if (args.size > 2) args[2] else "1.0"

        if (!sourceFile.exists()) {
            println("Error: The source file '${sourceFile.path}' does not exist.")
            return
        }
        if (!configFile.exists()) {
            println("Error: The configuration file '${configFile.path}' does not exist.")
            return
        }

        val source = sourceFile.readText()
        val statements = Lexer.from(source, version).lexIntoStatements().toList()
        val formatter = Formatter()
        val formattedStatements = formatter.execute(statements, configFile)

        FileWriter(sourceFile).use { writer ->
            formattedStatements.forEach { container ->
                container.container.forEach { token ->
                    writer.append(token.content)
                }
            }
        }
    }

    private fun findInvalidNode(node: ASTNode): ASTNode? {
        if (node.type == ASTNodeType.INVALID) return node
        for (child in node.children) {
            val invalid = findInvalidNode(child)
            if (invalid != null) return invalid
        }
        return null
    }

    fun validationCommand(args: List<String>) {
        if (args.isEmpty()) {
            println("Must specify the source file.")
            return
        }
        val sourceFile = File(args[0])
        val version = if (args.size > 1) args[1] else "1.0"
        if (!sourceFile.exists()) {
            println("Error: The source file '${sourceFile.path}' does not exist.")
            return
        }

        try {
            val source = sourceFile.readText()
            val statements = Lexer.from(source, version).lexIntoStatements().toList()
            for (statement in statements) {
                val parser = Parser(statement, version)
                val ast = parser.parse()
                val invalidNode = findInvalidNode(ast)
                if (invalidNode != null) {
                    val errorMessage = if (invalidNode.content.isNotBlank()) invalidNode.content else "Invalid AST for statement"
                    println("\nSYNTAX ERROR: $errorMessage")
                    println("\nERROR during validation: $errorMessage")
                    println("Location: Line ${invalidNode.position.line}, Column ${invalidNode.position.column}")
                    return
                }
            }
            println("\nSUCCESS: File is syntactically and semantically valid")
        } catch (e: Exception) {
            println("\nERROR during validation: ${e.message}")
        }
    }
}
