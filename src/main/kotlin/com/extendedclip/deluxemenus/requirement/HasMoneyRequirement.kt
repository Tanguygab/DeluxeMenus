package com.extendedclip.deluxemenus.requirement

import com.extendedclip.deluxemenus.DeluxeMenus
import com.extendedclip.deluxemenus.menu.MenuHolder

class HasMoneyRequirement(
    private val plugin: DeluxeMenus,
    private var amount: Double,
    private val invert: Boolean,
    private val placeholder: String?
) : Requirement() {
    override fun evaluate(holder: MenuHolder): Boolean {
        if (plugin.vault == null) return false

        if (placeholder != null) {
            try {
                val expected = holder.setPlaceholdersAndArguments(placeholder)
                amount = expected.toDouble()
            } catch (e: NumberFormatException) {
                plugin.printStacktrace("Invalid amount found for has money requirement: " + holder.setPlaceholdersAndArguments(placeholder), e)
            }
        }
        return invert != plugin.vault!!.hasEnough(holder.viewer, amount)
    }
}
