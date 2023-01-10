import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.util.*

class ParserOutput(private val parser: Parser, sequence: List<String>, outputFile: String?) {

    private var productions: MutableList<Int>
    private var nodeNumber: Int = 1
    private var hasErrors: Boolean = false

    private val nodeList: MutableList<Node> = mutableListOf()
    private var outputFile: String
    private var root: Node = Node()

    init {
        productions = parser.parseSequence(sequence).toMutableList()
        hasErrors = productions.contains(-1)
        this.outputFile = outputFile!!
        generateTree()
    }

    fun generateTree() {
        if (hasErrors) return
        val nodeStack = Stack<Node>()
        var productionsIndex = 0
        //root
        val node = Node()
        node.parent = 0
        node.sibling = 0
        node.hasRight = false
        node.index = nodeNumber
        nodeNumber++
        node.value = parser.grammar.startingSymbol
        nodeStack.push(node)
        nodeList.add(node)
        root = node
        while (productionsIndex < productions.size && !nodeStack.isEmpty()) {
            val currentNode = nodeStack.peek() //father
            if (parser.grammar.terminals.contains(currentNode.value) || currentNode.value.contains("epsilon")) {
                while (nodeStack.size > 0 && !nodeStack.peek().hasRight) {
                    nodeStack.pop()
                }
                if (nodeStack.size > 0) nodeStack.pop() else break
                continue
            }

            //children
            val production = parser.getProductionByOrderNumber(productions[productionsIndex])
            nodeNumber += production.size - 1
            for (i in production.size - 1 downTo 0) {
                val child = Node()
                child.parent = currentNode.index
                child.value = production[i]
                child.index = nodeNumber
                if (i == 0) child.sibling = 0
                else child.sibling = nodeNumber - 1

                child.hasRight = i != production.size - 1
                nodeNumber--
                nodeStack.push(child)
                nodeList.add(child)
            }
            nodeNumber += production.size + 1
            productionsIndex++
        }
    }

    fun printTree() {
        try {
            nodeList.sortBy {
                it.index
            }
            val file = File(outputFile)
            val fileWriter = FileWriter(file, false)
            val bufferedWriter = BufferedWriter(fileWriter)
            bufferedWriter.write(
                """
                Index | Value | Parent | Sibling
                
                """.trimIndent()
            )
            for (node in nodeList) {
                bufferedWriter.write((((node.index.toString() + " | " + node.value) + " | " + node.parent) + " | " + node.sibling) + "\n")
            }
            bufferedWriter.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}