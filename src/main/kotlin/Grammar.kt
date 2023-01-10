import java.io.BufferedReader
import java.io.FileReader
import java.util.*

class Grammar(filename: String) {
    var nonTerminals: Set<String> = HashSet()
        private set
    var terminals: Set<String> = HashSet()
        private set
    val productions = HashMap<Set<String>, MutableSet<MutableList<String>>>()
    var startingSymbol = ""
        private set

    init {
        readFromFile(filename)
    }

    private fun readFromFile(filename: String) {
        try {
            val reader = BufferedReader(FileReader(filename))
            var input = reader.readLine()
            val nonTerminalsLineSplit = input.split("=".toRegex(), input.indexOf("=").coerceAtLeast(0)).toTypedArray()
            var nonTerminals = StringBuilder()
            for (i in 1 until nonTerminalsLineSplit.size) nonTerminals.append(nonTerminalsLineSplit[i])
            var builder = StringBuilder(nonTerminals.toString())
            builder.deleteCharAt(1).deleteCharAt(nonTerminals.length - 2)
            nonTerminals = StringBuilder(builder.toString())
            this.nonTerminals = HashSet(listOf(*nonTerminals.toString().trim().split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()))
            input = reader.readLine()
            val terminalsLineSplit = input.split("=".toRegex(), input.indexOf("=").coerceAtLeast(0)).toTypedArray()
            var terminals = StringBuilder()
            for (i in 1 until terminalsLineSplit.size) terminals.append(terminalsLineSplit[i])
            builder = StringBuilder(terminals.toString())
            builder.deleteCharAt(1).deleteCharAt(terminals.length - 2)
            terminals = StringBuilder(builder.toString())
            this.terminals = HashSet(listOf(*terminals.toString().trim().split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()))
            startingSymbol = reader.readLine().split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1].trim()

            // first and last lines for productions will not contain any relevant information, we only need to check starting from the second until the second-last
            reader.readLine()
            var line = reader.readLine()
            while (line != null) {
                if (line != "}") {
                    val tokens = line.split("->".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    val lhsTokens = tokens[0].split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    val rhsTokens = tokens[1].split("\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    val lhs: MutableSet<String> = HashSet()
                    for (l in lhsTokens) lhs.add(l.trim())
                    if (!productions.containsKey(lhs)) productions[lhs] = HashSet()
                    for (rhsT in rhsTokens) {
                        val productionElements = ArrayList<String>()
                        val rhsTokenElement =
                            rhsT.trim().split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                        for (r in rhsTokenElement) productionElements.add(r.trim())
                        productions[lhs]!!.add(productionElements)
                    }
                }
                line = reader.readLine()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun printProductionsForNonTerminal(nonTerminal: String): String {
        val sb = StringBuilder()
        for (lhs in productions.keys) {
            if (lhs.contains(nonTerminal)) {
                sb.append(nonTerminal).append(" -> ")
                val rhs: Set<List<String>> = productions[lhs]!!
                var count = 0
                for (rh in rhs) {
                    for (r in rh) {
                        sb.append(r).append(" ")
                    }
                    count++
                    if (count < rhs.size) sb.append("| ")
                }
            }
        }
        return sb.toString()
    }

    fun checkIfCFG(): Boolean {
        var checkStartingSymbol = false
        for (lhs in productions.keys) if (lhs.contains(startingSymbol)) {
            checkStartingSymbol = true
            break
        }
        if (!checkStartingSymbol) return false
        for (lhs in productions.keys) {
            if (lhs.size > 1) return false else if (!nonTerminals.contains(lhs.iterator().next())) return false
            val rhs: Set<List<String>> = productions[lhs]!!
            for (rh in rhs) {
                for (r in rh) {
                    if (!(nonTerminals.contains(r) || terminals.contains(r) || r == Parser.epsilon)) return false
                }
            }
        }
        return true
    }

    fun getProductionForNonterminal(nonTerminal: String): Set<List<String>> {
        for (lhs in productions.keys) {
            if (lhs.contains(nonTerminal)) {
                return productions[lhs]!!
            }
        }
        return HashSet()
    }
}
