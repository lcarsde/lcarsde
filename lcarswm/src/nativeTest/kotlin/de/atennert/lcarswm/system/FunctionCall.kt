package de.atennert.lcarswm.system

class FunctionCall(val name: String, vararg parameterValues: Any?) {
    val parameters: List<Any?> = List(parameterValues.size) {parameterValues[it]}
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FunctionCall) return false

        if (name != other.name) return false
        return parameters == other.parameters
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + parameters.hashCode()
        return result
    }

    override fun toString(): String {
        return "FunctionCall(name='$name', parameters=$parameters)"
    }
}