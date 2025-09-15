package com.extendedclip.deluxemenus.requirement

import com.extendedclip.deluxemenus.menu.MenuHolder

class StringLengthRequirement(private val input: String, private val min: Int, private val max: Int?) : Requirement() {
    override fun evaluate(holder: MenuHolder): Boolean {
        val toCheck = holder.setPlaceholdersAndArguments(input)
        return if (max == null) toCheck.length >= min
        else toCheck.length in min..max
    }
}
