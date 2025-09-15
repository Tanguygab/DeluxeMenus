package com.extendedclip.deluxemenus.requirement

import com.extendedclip.deluxemenus.menu.MenuHolder

class HasPermissionRequirement(private val perm: String, private val invert: Boolean) : Requirement() {
    override fun evaluate(holder: MenuHolder): Boolean {
        val check = holder.setPlaceholdersAndArguments(perm)
        return invert != holder.viewer.hasPermission(check)
    }
}
