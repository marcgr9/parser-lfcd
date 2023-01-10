import java.io.BufferedReader
import java.io.FileReader
import java.util.*

class Grammar(filename: String) {
    var n: Set<String> = HashSet()
        private set
    var e: Set<String> = HashSet()
        private set
    val p = HashMap<Set<String>, MutableSet<MutableList<String>>>()
    var s = ""
        private set

    init {
        readFromFile(filename)
    }

    private fun readFromFile(filename: String) {
        try {
            val reader = BufferedReader(FileReader(filename))
            var input = reader.readLine()
            val NlineSplit = input.split("=".toRegex(), input.indexOf("=").coerceAtLeast(0)).toTypedArray()
            var Nline = StringBuilder()
            for (i in 1 until NlineSplit.size) Nline.append(NlineSplit[i])
            var builder = StringBuilder(Nline.toString())
            builder.deleteCharAt(1).deleteCharAt(Nline.length - 2)
            Nline = StringBuilder(builder.toString())
            n = HashSet(Arrays.asList(*Nline.toString().strip().split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()))
            input = reader.readLine()
            val ElineSplit = input.split("=".toRegex(), input.indexOf("=").coerceAtLeast(0)).toTypedArray()
            var Eline = StringBuilder()
            for (i in 1 until ElineSplit.size) Eline.append(ElineSplit[i])
            builder = StringBuilder(Eline.toString())
            builder.deleteCharAt(1).deleteCharAt(Eline.length - 2)
            Eline = StringBuilder(builder.toString())
            e = HashSet(Arrays.asList(*Eline.toString().strip().split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()))
            s = reader.readLine().split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1].strip()

            // first and last lines for productions will not contain any relevant information, we only need to check starting from the second until the second-last
            reader.readLine()
            var line = reader.readLine()
            while (line != null) {
                if (line != "}") {
                    val tokens = line.split("->".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                    val lhsTokens = tokens[0].split(",".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                    val rhsTokens = tokens[1].split("\\|".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                    val lhs: MutableSet<String> = HashSet()
                    for (l in lhsTokens) lhs.add(l.strip())
                    if (!p.containsKey(lhs)) p[lhs] = HashSet()
                    for (rhsT in rhsTokens) {
                        val productionElements = ArrayList<String>()
                        val rhsTokenElement = rhsT.strip().split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray()
                        for (r in rhsTokenElement) productionElements.add(r.strip())
                        p[lhs]!!.add(productionElements)
                    }
                }
                line = reader.readLine()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun printNonTerminals(): String {
        val sb = StringBuilder("N = { ")
        for (n in n) sb.append(n).append(" ")
        sb.append("}")
        return sb.toString()
    }

    fun printTerminals(): String {
        val sb = StringBuilder("E = { ")
        for (e in e) sb.append(e).append(" ")
        sb.append("}")
        return sb.toString()
    }

    fun printProductions(): String {
        val sb = StringBuilder("P = { \n")
        p.forEach { (lhs: Set<String>, rhs: Set<List<String>>) ->
            sb.append("\t")
            var count = 0
            for (lh in lhs) {
                sb.append(lh)
                count++
                if (count < lhs.size) sb.append(", ")
            }
            sb.append(" -> ")
            count = 0
            for (rh in rhs) {
                for (r in rh) {
                    sb.append(r).append(" ")
                }
                count++
                if (count < rhs.size) sb.append("| ")
            }
            sb.append("\n")
        }
        sb.append("}")
        return sb.toString()
    }

    fun printProductionsForNonTerminal(nonTerminal: String): String {
        val sb = StringBuilder()
        for (lhs in p.keys) {
            if (lhs.contains(nonTerminal)) {
                sb.append(nonTerminal).append(" -> ")
                val rhs: Set<List<String>> = p[lhs]!!
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
        for (lhs in p.keys) if (lhs.contains(s)) {
            checkStartingSymbol = true
            break
        }
        if (!checkStartingSymbol) return false
        for (lhs in p.keys) {
            if (lhs.size > 1) return false else if (!n.contains(lhs.iterator().next())) return false
            val rhs: Set<List<String>> = p[lhs]!!
            for (rh in rhs) {
                for (r in rh) {
                    if (!(n.contains(r) || e.contains(r) || r == "epsilon")) return false
                }
            }
        }
        return true
    }

    fun getProductionForNonterminal(nonTerminal: String): Set<List<String>> {
        for (lhs in p.keys) {
            if (lhs.contains(nonTerminal)) {
                return p[lhs]!!
            }
        }
        return HashSet()
    }
}
