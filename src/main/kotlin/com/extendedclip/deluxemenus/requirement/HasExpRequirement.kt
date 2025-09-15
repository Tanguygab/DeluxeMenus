package com.extendedclip.deluxemenus.requirement

import com.extendedclip.deluxemenus.DeluxeMenus
import com.extendedclip.deluxemenus.menu.MenuHolder
import com.extendedclip.deluxemenus.utils.ExpUtils

class HasExpRequirement(
    private val plugin: DeluxeMenus,
    private val amt: String,
    private val invert: Boolean,
    private val level: Boolean
) : Requirement() {
    override fun evaluate(holder: MenuHolder): Boolean {
        val amount: Int
        val has = if (level) holder.viewer.level
        else ExpUtils.getTotalExperience(holder.viewer)

        try {
            amount = holder.setPlaceholdersAndArguments(amt).toInt()
        } catch (e: Exception) {
            plugin.printStacktrace("Invalid amount found for has exp requirement: " + holder.setPlaceholdersAndArguments(amt), e)
            return false
        }
        return if (has < amount) invert else !invert
    }
}
