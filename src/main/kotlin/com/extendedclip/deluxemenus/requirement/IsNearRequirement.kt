package com.extendedclip.deluxemenus.requirement

import com.extendedclip.deluxemenus.menu.MenuHolder
import org.bukkit.Location

class IsNearRequirement(private val location: Location, private val distance: Int, private val invert: Boolean) : Requirement() {
    override fun evaluate(holder: MenuHolder): Boolean {
        var withinRange = false
        if (holder.viewer.world.name == location.world!!.name) {
            withinRange = holder.viewer.location.distance(location) < distance
        }
        return invert != withinRange
    }
}
