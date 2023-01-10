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
            val tokenAndPosition = Arrays.asList(*line.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray())
            if (tokenAndPosition[3] != "-1") {
                if (tokenAndPosition[0].contains("\"") || tokenAndPosition[0].contains("'") || !Pattern.matches(
                        "[a-zA-Z]+",
                        tokenAndPosition[0]
                    )
                ) tokens.add("constant") else tokens.add("identifier")
            } else tokens.add(tokenAndPosition[0].strip())
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
            val symbols = java.util.List.of(*line.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray())
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

//        System.out.println(grammar.printNonTerminals());
//        System.out.println(grammar.printTerminals());
//        System.out.println(grammar.printProductions());
//        System.out.println(grammar.printProductionsForNonTerminal("if_stmt"));
//        System.out.println(grammar.printProductionsForNonTerminal("declaration"));
//        System.out.println(grammar.getProductionForNonterminal("declaration"));
//        System.out.println(grammar.checkIfCFG());


//        System.out.println(grammar.printNonTerminals());
//        System.out.println(grammar.printTerminals());
//        System.out.println(grammar.printProductions());
//        System.out.println(grammar.printProductionsForNonTerminal("if_stmt"));
//        System.out.println(grammar.printProductionsForNonTerminal("declaration"));
//        System.out.println(grammar.getProductionForNonterminal("declaration"));
//        System.out.println(grammar.checkIfCFG());
    val parser = Parser(grammar)
    System.out.println(parser.printFirst())
    System.out.println(parser.printFollow())
    System.out.println(parser.printParseTable())
//        List<String> sequence = List.of("(","int",")","+","int");
    //        List<String> sequence = List.of("(","int",")","+","int");
//    val sequence: List<String> = readText("src/main/resources/seq.txt")
        val sequence = readPIF("src/main/resources/PIF.txt");
    //        List<String> sequence = readPIF("src/ubb/flcd/Resources/PIF.txt");
    System.out.println(parser.parseSequence(sequence))
//        System.out.println(parser.getProductionsRhs());

    //        System.out.println(parser.getProductionsRhs());
    val parserOutput = ParserOutput(parser, sequence, "src/main/resources/out1.txt")
    parserOutput.printTree()
}