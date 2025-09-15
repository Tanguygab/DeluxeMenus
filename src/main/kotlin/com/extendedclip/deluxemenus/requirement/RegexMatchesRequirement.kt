package com.extendedclip.deluxemenus.requirement

import com.extendedclip.deluxemenus.menu.MenuHolder
import java.util.regex.Pattern

class RegexMatchesRequirement(private val pattern: Pattern, private val input: String, private val invert: Boolean) : Requirement() {
    override fun evaluate(holder: MenuHolder): Boolean {
        val toCheck = holder.setPlaceholdersAndArguments(input)
        return invert != pattern.matcher(holder.setPlaceholdersAndArguments(toCheck)).find()
    }
}
