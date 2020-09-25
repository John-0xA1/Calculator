package calculator

import java.util.Scanner;
import java.math.BigInteger;
import kotlin.math.exp

var variables = mutableMapOf<String, BigInteger>()

fun main() {
    val scanner = Scanner(System.`in`);

    while (true) {
        var input = scanner.nextLine();
        if (input.isNotEmpty()) {
            if (input.first() == '/') {
                if (processCommand(input) == 1) {
                    break;
                }
            }
            else if (input.first() == '+' || input.first() == '-') {
                println(input.trim())
            }
            else if (input.contains('=')) {
                processAssignment(input);
            }
            else {
                processExpression(input);
            }
        }
    }

}

fun processCommand(input: String) : Int {
    var command = input.trim();
    if (command == "/help") {
        println("Calculator");
    }
    else if (command == "/exit") {
        println("Bye!");
        return 1;
    }
    else {
        println("Unknown command");
    }
    return 0;
}

fun processAssignment(input: String) {
    var assignment = input.trim();
    var equals = 0;
    var splitted : Array<String> = assignment.split("=").toTypedArray();
    for (i in assignment) {
        if (i == '=') {
            equals++;
        }
    }
    if (equals != 1 || splitted.size != 2) {
        println("Invalid assignment")
    }
    else {
        var variable = splitted[0].trim(); var value = splitted[1].trim();
        var validVar = true; var validVal = true;

        if (variable.contains(Regex("[0-9]"))) {
            validVar = false;
        }
        if (!validVar) {
            println("Invalid identifier")
        }
        else {
            if (value.contains(Regex("[0-9]")) && value.toBigIntegerOrNull() == null) {
                validVal = false;
            }

            if (!validVal) {
                println("Invalid assignment")
            }
            else {
                if (value.toBigIntegerOrNull() == null) {
                    if (!variables.contains(value)) {
                        println("Unknown variable")
                    }
                    else {
                        variables[variable] = variables.getValue(value);
                    }
                }
                else {
                    variables[variable] = value.toBigInteger();
                }
            }
        }
    }
}

fun processExpression(input: String) {
    var expression : String = input.trim();
    var validExpression = validateExpression(expression);

    var expressionList = mutableListOf<String>();

    if (validExpression) {
        expression = formatExpression(expression)
        var passed = IntArray(expression.length) {0}
        if (expression != "NaN") {
            for (i in 0..expression.length - 1) {
                if (passed[i] == 0) {
                    if (expression[i].toString().toIntOrNull() == null) {
                        expressionList.add(expression[i].toString());
                        passed[i] = 1
                    } else {
                        var num = expression[i].toString();
                        for (j in i + 1..expression.length - 1) {
                            if (expression[j].toString().toIntOrNull() != null) {
                                num += expression[j].toString();
                                passed[j] = 1
                            } else {
                                break;
                            }
                        }
                        expressionList.add(num)
                    }
                }
            }
            var postFix = getPostFixExpression(expressionList);
            println(calculatePostFix(postFix))
        }
        else {
            println("Unknown variable")
        }

    }
    else {
        println("Invalid expression")
    }
}

fun getPostFixExpression(expressionList: List<String>) : List<String> {
    var stack = mutableListOf<String>()
    var queue = mutableListOf<String>()

    expressionList.forEach {
        when {
            it == "(" -> stack.add(it)

            it == ")" -> {
                if (expressionList.contains("(")) {
                    for (i in stack.lastIndex downTo 0) {
                        if (stack[i] == "(") {
                            stack[i] = " "
                            break;
                        }
                        queue.add(stack[i])
                        stack[i] = " "
                    }
                    stack.removeIf {it == " "}
                }
            }

            Regex("[\\d]").containsMatchIn(it) -> queue.add(it)

            Regex("[+-]").containsMatchIn(it) -> {
                if (stack.isEmpty() || stack.last() == "(") stack.add(it)
                else if (stack.last().contains(Regex("[/*]"))) {
                    for (i in stack.lastIndex downTo 0) {
                        if (stack[i] == "(") {
                            stack[i] = " "
                            break;
                        }
                        queue.add(stack[i])
                        stack[i] = " "
                    }
                    stack.removeIf { it == " " }
                    stack.add(it)
                } else {
                    queue.add(stack.last())
                    stack[stack.lastIndex] = it

                }
            }

            Regex("[*/]").containsMatchIn(it) -> {
                if (stack.isNotEmpty() && (stack.last() == "*" || stack.last() == "/")) {
                    for (i in stack.lastIndex downTo 0) {
                        if (stack[i] == "(") {
                            stack[i] = " "
                            break;
                        }
                        queue.add(stack[i])
                        stack[i] = " "
                    }
                    stack.removeIf {it == " "}
                }
                stack.add(it)
            }
        }
    }

    if (stack.isNotEmpty()) {
        for (i in stack.lastIndex downTo 0) {
            if (stack[i] != "(") {
                queue.add(stack[i])
            }
        }
    }
    var postFix = "";
    queue.forEach {
        postFix += it;
        //println(it)
    }
    return queue;
}

fun calculatePostFix (postFix : List<String>) : BigInteger {
    var stack = mutableListOf<BigInteger>()

    for (item in postFix) {
        when {
            Regex("[\\d]").containsMatchIn(item) -> {
                stack.add(item.toBigInteger())
            }

            item == "+" -> {
                if (stack.isNotEmpty()) {
                    stack[stack.lastIndex - 1] = stack[stack.lastIndex - 1] + stack.last()
                    stack.removeAt(stack.lastIndex)
                }
            }

            item == "*" -> {
                stack[stack.lastIndex - 1] = stack[stack.lastIndex - 1] * stack.last()
                stack.removeAt(stack.lastIndex)
            }

            item == "/" -> {
                stack[stack.lastIndex - 1] = stack[stack.lastIndex - 1] / stack.last()
                stack.removeAt(stack.lastIndex)
            }

            item == "-" -> {
                stack[stack.lastIndex - 1] = stack[stack.lastIndex - 1] - stack.last()
                stack.removeAt(stack.lastIndex)
            }
        }
    }
    return stack.first();
}

fun validateExpression(expression: String) : Boolean {
    var expression = expression.trim()
    expression = expression.replace(" ", "")
    var valid = true
    var balance = 0;
    for (i in expression) {
        if (i == '(') {
            balance++
        }
        else if (i == ')') {
            balance--
        }
    }

    if (balance != 0) {
        valid = false
    }

    for (i in 0 until expression.length - 1) {
        if ((expression[i] == '*' && expression[i + 1] == '*') || (expression[i] == '/' && expression[i + 1] == '/') || (expression[i] == '^' && expression[i + 1] == '^')) {
            valid = false;
            break;
        }

    }
    return valid;
}

fun formatExpression(expression: String) : String {
    var formattedExpression = ""
    var toFormat = expression.trim().replace(" ", "")
    var passed = IntArray(toFormat.length) {0}

    for (i in 0 until toFormat.length) {
        if (passed[i] == 0) {
            if (toFormat[i].isLetter()) {
                var startVar = toFormat[i].toString()
                passed[i] = 1
                for (j in i + 1 .. toFormat.length - 1) {
                    if (toFormat[j].isLetter()) {
                        startVar += toFormat[i].toString()
                        passed[j] = 1
                    }
                    else {
                        break
                    }
                }
                if (variables.contains(startVar)) {
                    formattedExpression += variables.getValue(startVar).toString()
                }
                else {
                    return "NaN"
                }
            }
            else if (toFormat[i] == '+') {
                passed[i] = 1
                for (j in i + 1 until toFormat.length - 1) {
                    if (toFormat[j] == '+') {
                        passed[j] = 1
                    }
                    else {
                        break
                    }
                }
                formattedExpression += "+"
            }
            else if (toFormat[i] == '-') {
                passed[i] = 1
                var counter = 1
                for (j in i + 1 until toFormat.length - 1) {
                    if (toFormat[j] == '-') {
                        counter++
                        passed[j] = 1
                    }
                    else {
                        break
                    }
                }
                if (counter % 2 == 0) {
                    formattedExpression += "+"
                }
                else {
                    formattedExpression += "-"
                }
            }
            else {
                formattedExpression += toFormat[i].toString()
            }
        }
    }

    return formattedExpression
}

