package com.extendedclip.deluxemenus.requirement

import com.extendedclip.deluxemenus.menu.MenuHolder

class HasPermissionsRequirement(
    private val permissions: MutableList<String>,
    private val minimum: Int,
    private val invert: Boolean
) : Requirement() {
    override fun evaluate(holder: MenuHolder): Boolean {
        val count = permissions
            .map { holder.setPlaceholdersAndArguments(it) }
            .count { holder.viewer.hasPermission(it) }
        return if (invert)
            count + minimum <= permissions.size
        else count >= minimum
    }
}
