package com.extendedclip.deluxemenus.requirement

import com.extendedclip.deluxemenus.menu.MenuHolder

class InputResultRequirement(private val type: RequirementType, private val input: String, private val result: String) : Requirement() {
    override fun evaluate(holder: MenuHolder): Boolean {
        val parsedInput = holder.setPlaceholdersAndArguments(this.input)
        val parsedResult = holder.setPlaceholdersAndArguments(this.result)

        when (type) {
            RequirementType.STRING_CONTAINS -> return parsedInput.contains(parsedResult)
            RequirementType.STRING_EQUALS -> return parsedInput == parsedResult
            RequirementType.STRING_EQUALS_IGNORECASE -> return parsedInput.equals(parsedResult, ignoreCase = true)
            RequirementType.STRING_DOES_NOT_CONTAIN -> return !parsedInput.contains(parsedResult)
            RequirementType.STRING_DOES_NOT_EQUAL -> return parsedInput != parsedResult
            RequirementType.STRING_DOES_NOT_EQUAL_IGNORECASE -> return !parsedInput.equals(parsedResult, ignoreCase = true)
            else -> {}
        }

        val input: Double
        try {
            input = parsedInput.toDouble()
        } catch (e: NumberFormatException) {
            holder.plugin.printStacktrace("Input for comparison requirement is an invalid number: $parsedInput", e)
            return false
        }

        val result: Double
        try {
            result = parsedResult.toDouble()
        } catch (e: NumberFormatException) {
            holder.plugin.printStacktrace("Output for comparison requirement is an invalid number: $parsedResult", e)
            return false
        }

        return when (type) {
            RequirementType.GREATER_THAN -> input > result
            RequirementType.GREATER_THAN_EQUAL_TO -> input >= result
            RequirementType.EQUAL_TO -> input == result
            RequirementType.NOT_EQUAL_TO -> input != result
            RequirementType.LESS_THAN_EQUAL_TO -> input <= result
            RequirementType.LESS_THAN -> input < result
            else -> false
        }
    }
}
