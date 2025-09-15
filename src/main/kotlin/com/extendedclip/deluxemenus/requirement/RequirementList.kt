package com.extendedclip.deluxemenus.requirement

import com.extendedclip.deluxemenus.action.ClickHandler
import com.extendedclip.deluxemenus.menu.MenuHolder

class RequirementList(val requirements: MutableList<Requirement>) {
    var denyHandler: ClickHandler? = null
    var minimumRequirements: Int = 0
    var stopAtSuccess = false


    fun evaluate(holder: MenuHolder): Boolean {
        var successful = 0
        for (r in requirements) {
            if (r.evaluate(holder)) {
                ++successful
                r.successHandler?.onClick(holder)
                if (stopAtSuccess && successful >= minimumRequirements) {
                    break
                }
                continue
            }
            r.denyHandler?.onClick(holder)
            if (!r.isOptional) return false
        }
        return successful >= minimumRequirements
    }

}
