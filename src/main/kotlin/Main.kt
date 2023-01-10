import java.io.BufferedReader
import java.io.FileReader
import java.util.*
import java.util.regex.Pattern

fun readPIF(filename: String): MutableList<String> {
    return try {
        val tokens: MutableList<String> = mutableListOf()
        val reader = BufferedReader(FileReader(filename))
        var line = reader.readLine()
        while (line != null) {
            val tokenAndPosition =
                listOf(*line.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
            if (tokenAndPosition[3] != "-1") {
                if (tokenAndPosition[0].contains("\"") || tokenAndPosition[0].contains("'") || !Pattern.matches(
                        "[a-zA-Z]+", tokenAndPosition[0]
                    )
                ) tokens.add("constant") else tokens.add("identifier")
            } else tokens.add(tokenAndPosition[0].trim())
            line = reader.readLine()
        }
        reader.close()
        tokens
    } catch (e: Exception) {
        ArrayList()
    }
}

fun readText(filename: String): List<String> {
    val sequence: MutableList<String> = ArrayList()
    try {
        val reader = BufferedReader(FileReader(filename))
        var line = reader.readLine()
        while (line != null) {
            val symbols = java.util.List.of(*line.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
            sequence.addAll(symbols)
            line = reader.readLine()
        }
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }
    return sequence
}

fun main() {
    val grammar = Grammar("src/main/resources/g2.txt")
    val parser = Parser(grammar)

    val scanner = Scanner(System.`in`)

    println(menu())
    while (true) {
        when (scanner.nextInt()) {
            0 -> println(menu())
            1 -> println(grammar.nonTerminals)
            2 -> println(grammar.terminals)
            3 -> println(grammar.productions)
            4 -> println(grammar.checkIfCFG())
            5 -> {
                print("Nonterminal: ")
                println(grammar.printProductionsForNonTerminal(scanner.next()))
            }
            6 -> println(parser.printFirst())
            7 -> println(parser.printFollow())
            8 -> {
                val sequence= readText("src/main/resources/seq.txt")
                val parserOutput = ParserOutput(parser, sequence, "src/main/resources/out1.txt")
                println(parser.parseSequence(sequence))
                parserOutput.printTree()
            }
            9 -> {
                val sequence = readPIF("src/main/resources/PIF.txt")
                val parserOutput = ParserOutput(parser, sequence, "src/main/resources/out2.txt")
                println(parser.parseSequence(sequence))
                parserOutput.printTree()
            }
        }
    }
}

fun menu(): String = """
        0. Menu
        1. Non terminals
        2. Terminals
        3. Productions
        4. Check if CFG
        5. Production for terminal
        6. First
        7. Follow
        8. From sequence (must use g1)
        9. From PIF (must use g2)
    """.trimIndent()

