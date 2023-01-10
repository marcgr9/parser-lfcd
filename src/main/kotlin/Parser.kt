import java.util.*

class Parser(
    val grammar: Grammar,
    private var firstSet: MutableMap<String, MutableSet<String>> = mutableMapOf(),
    private var followSet: MutableMap<String, MutableSet<String>> = mutableMapOf(),
    private val parseTable: MutableMap<Pair<String, String>, Pair<String, Int>> = mutableMapOf(),
    private var productionsRhs: MutableList<MutableList<String>> = mutableListOf(),
) {

    val epsilon = "ε"

    init {
        generateFirst()
        generateFollow()
        generateParseTable()
    }

    private fun generateFirst() {
        grammar.nonTerminals.forEach { nonterminal ->
            firstSet[nonterminal] = mutableSetOf()
            val productionForNonterminal = grammar.getProductionForNonterminal(nonterminal)
            productionForNonterminal.forEach { production ->
                if (grammar.terminals.contains(production[0]) || production[0] == epsilon) firstSet[nonterminal]!!.add(
                    production[0]
                )
            }
        }

        var isChanged = true
        while (isChanged) {
            isChanged = false

            val newColumn = mutableMapOf<String, Set<String>>()
            grammar.nonTerminals.forEach { nonterminal ->
                val productionForNonterminal = grammar.getProductionForNonterminal(nonterminal)
                val toAdd: MutableSet<String> =
                    mutableSetOf<String>().apply { addAll(firstSet[nonterminal] ?: listOf()) }
                productionForNonterminal.forEach { production ->
                    val rhsNonTerminals = mutableListOf<String>()

                    var rhsTerminal: String? = null
                    for (symbol in production) {
                        if (grammar.nonTerminals.contains(symbol)) rhsNonTerminals.add(symbol)
                        else {
                            rhsTerminal = symbol
                            break
                        }
                    }
                    toAdd.addAll(concatenationOfSizeOne(rhsNonTerminals, rhsTerminal))
                }
                if (toAdd != firstSet[nonterminal]) {
                    isChanged = true
                }
                newColumn[nonterminal] = toAdd
            }
            firstSet = newColumn as MutableMap<String, MutableSet<String>>
        }
    }

    private fun concatenationOfSizeOne(nonTerminals: List<String>, terminal: String?): Set<String> {
        if (nonTerminals.isEmpty()) return mutableSetOf()

        if (nonTerminals.size == 1) {
            return firstSet[nonTerminals.iterator().next()]!!
        }
        val concatenation = mutableSetOf<String>()
        var step = 0
        var allEpsilon = true
        nonTerminals.forEach { nonTerminal ->
            if (!firstSet[nonTerminal]!!.contains(epsilon)) {
                allEpsilon = false
            }
        }
        if (allEpsilon) {
            concatenation.add(terminal ?: epsilon)
        }
        while (step < nonTerminals.size) {
            var thereIsOneEpsilon = false
            for (s in firstSet[nonTerminals[step]]!!) {
                if (s == epsilon) thereIsOneEpsilon = true
                else concatenation.add(s)
            }
            if (thereIsOneEpsilon) step++
            else break
        }
        return concatenation
    }

    private fun generateFollow() {
        //initialization
        for (nonterminal in grammar.nonTerminals) {
            followSet[nonterminal] = mutableSetOf()
        }
        followSet[grammar.startingSymbol]!!.add(epsilon)

        //rest of iterations
        var isChanged = true
        while (isChanged) {
            isChanged = false
            val newColumn = HashMap<String, Set<String>>()
            grammar.nonTerminals.forEach { nonterminal ->
                newColumn[nonterminal] = HashSet()
                val productionsWithNonterminalInRhs = mutableMapOf<String, MutableSet<List<String>>>()
                val allProductions = grammar.productions
                allProductions.forEach { (k, v) ->
                    for (eachProduction in v) {
                        if (eachProduction.contains(nonterminal)) {
                            val key = k.iterator().next()
                            if (!productionsWithNonterminalInRhs.containsKey(key)) productionsWithNonterminalInRhs[key] =
                                mutableSetOf()

                            productionsWithNonterminalInRhs[key]!!.add(eachProduction)
                        }
                    }
                }

                val toAdd = mutableSetOf<String>().apply { addAll(followSet[nonterminal] ?: listOf()) }

                productionsWithNonterminalInRhs.forEach { (k, v) ->
                    v.forEach { production ->
                        production.indices.forEach { indexOfNonterminal ->
                            if (production[indexOfNonterminal] == nonterminal) {
                                if (indexOfNonterminal + 1 == production.size) {
                                    toAdd.addAll(followSet[k]!!)
                                } else {
                                    val followSymbol = production[indexOfNonterminal + 1]
                                    if (grammar.terminals.contains(followSymbol)) toAdd.add(followSymbol)
                                    else {
                                        for (symbol in firstSet[followSymbol]!!) {
                                            if (symbol == epsilon) toAdd.addAll(followSet[k]!!) else toAdd.addAll(
                                                firstSet[followSymbol]!!
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (toAdd != followSet[nonterminal]) {
                    isChanged = true
                }
                newColumn[nonterminal] = toAdd
            }
            followSet = newColumn as MutableMap<String, MutableSet<String>>
        }
    }

    private fun generateParseTable() {
        val rows: MutableList<String> = ArrayList()
        rows.addAll(grammar.nonTerminals)
        rows.addAll(grammar.terminals)
        rows.add("$")
        val columns: MutableList<String> = ArrayList()
        columns.addAll(grammar.terminals)
        columns.add("$")
        for (row in rows) for (col in columns) parseTable[Pair(row, col)] = Pair("error", -1)
        for (col in columns) parseTable[Pair(col, col)] = Pair("pop", -1)
        parseTable[Pair("$", "$")] = Pair("accept", -1)
        val productions = grammar.productions
        productionsRhs.clear()
        productions.forEach { (k, v) ->
            val nonterminal = k.iterator().next()
            for (prod in v) if (prod[0] != epsilon) productionsRhs.add(prod) else {
                productionsRhs.add(mutableListOf(epsilon, nonterminal))
            }
        }
        productions.forEach { (k, v) ->
            val key = k.iterator().next()
            for (production in v) {
                val firstSymbol = production[0]
                if (grammar.terminals.contains(firstSymbol)) {
                    if (parseTable[Pair(key, firstSymbol)]!!.first == "error") {
                        parseTable[Pair(key, firstSymbol)] =
                            Pair(java.lang.String.join(" ", production), productionsRhs.indexOf(production) + 1)
                    } else {
                        try {
                            throw IllegalAccessException("conflict in pair:  $key,$firstSymbol")
                        } catch (e: IllegalAccessException) {
                            e.printStackTrace()
                        }
                    }
                } else if (grammar.nonTerminals.contains(firstSymbol)) {
                    if (production.size == 1) {
                        for (symbol: String in firstSet.get(firstSymbol)!!) {
                            if (parseTable[Pair(key, symbol)]!!.first == "error") parseTable[Pair(key, symbol)] =
                                Pair(java.lang.String.join(" ", production), productionsRhs.indexOf(production) + 1)
                            else {
                                try {
                                    throw IllegalAccessException("conflict in pair:  $key,$symbol")
                                } catch (e: IllegalAccessException) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    } else {
                        var i = 1
                        var nextSymbol = production.get(1)
                        val firstSetForProduction: MutableSet<String>? = firstSet.get(firstSymbol)
                        while (i < production.size && grammar.nonTerminals.contains(nextSymbol)) {
                            val firstForNext: Set<String>? = firstSet.get(nextSymbol)
                            if (firstSetForProduction!!.contains(epsilon)) {
                                firstSetForProduction.remove(epsilon)
                                firstSetForProduction.addAll((firstForNext)!!)
                            }
                            i++
                            if (i < production.size) nextSymbol = production.get(i)
                        }
                        for (symb in firstSetForProduction!!) {
                            val symbol = if (symb == epsilon) "$" else symb
                            if (parseTable[Pair(key, symbol)]!!.first == "error") parseTable[Pair(key, symbol)] = Pair(
                                production.joinToString(separator = " "), productionsRhs.indexOf(production) + 1
                            )
                            else {
                                try {
                                    throw IllegalAccessException("conflict in pair:  $key,$symbol")
                                } catch (e: IllegalAccessException) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }
                } else {
                    val follow: Set<String>? = followSet[key]
                    for (symbol: String in follow!!) {
                        if ((symbol == epsilon)) {
                            if (parseTable[Pair(key, "$")]!!.first == "error") {
                                val prod = listOf(epsilon, key)
                                parseTable[Pair(key, "$")] = Pair(epsilon, productionsRhs.indexOf(prod) + 1)
                            } else {
                                try {
                                    throw IllegalAccessException("conflict in pair:  $key,$symbol")
                                } catch (e: IllegalAccessException) {
                                    e.printStackTrace()
                                }
                            }
                        } else if (parseTable[Pair(key, symbol)]!!.first == "error") {
                            val prod: ArrayList<String> = ArrayList(listOf(epsilon, key))
                            parseTable[Pair(key, symbol)] = Pair(epsilon, productionsRhs.indexOf(prod) + 1)
                        } else {
                            try {
                                throw IllegalAccessException("conflict in pair:  $key,$symbol")
                            } catch (e: IllegalAccessException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }
        }
    }

    fun printFirst(): String {
        val builder = StringBuilder()
        firstSet.forEach { (k: String?, v: Set<String?>?) ->
            builder.append(
                k
            ).append(": ").append(v).append("\n")
        }
        return builder.toString()
    }

    fun printFollow(): String {
        val builder = StringBuilder()
        followSet.forEach { (k: String?, v: Set<String?>?) ->
            builder.append(k).append(": ").append(v).append("\n")
        }
        return builder.toString()
    }

    fun printParseTable(): String {
        val builder = StringBuilder()
        parseTable.forEach { (k, v) ->
            builder.append(k).append(" -> ").append(v).append("\n")
        }
        return builder.toString()
    }

    fun getProductionByOrderNumber(order: Int): List<String> {
        val production: List<String> = productionsRhs[order - 1]
        return if (production.contains(epsilon)) listOf(epsilon) else production
    }

    fun parseSequence(sequence: List<String>): List<Int> {
        val alpha = Stack<String>()
        val beta = Stack<String>()
        var result: MutableList<Int> = ArrayList()

        //initialization
        alpha.push("$")
        for (i in sequence.indices.reversed()) alpha.push(sequence[i])
        beta.push("$")
        beta.push(grammar.startingSymbol)
        while (!(alpha.peek() == "$" && beta.peek() == "$")) {
            val alphaPeek = alpha.peek()
            val betaPeek = beta.peek()
            val key = Pair(betaPeek, alphaPeek)
            val value = parseTable[key]!!
            if (value.first != "error") {
                if (value.first == "pop") {
                    alpha.pop()
                    beta.pop()
                } else {
                    beta.pop()
                    if (value.first != epsilon) {
                        val value = value.first.split(" ")
                        for (i in value.indices.reversed()) beta.push(value[i])
                    }
                    result.add(value.second)
                }
            } else {
                println("Syntax error for key $key")
                println("Current alpha and beta for sequence parsing:")
                println(alpha)
                println(beta)
                result = ArrayList(listOf(-1))
                return result
            }
        }
        return result
    }


}