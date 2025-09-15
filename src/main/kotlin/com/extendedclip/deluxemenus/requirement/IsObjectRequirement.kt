package com.extendedclip.deluxemenus.requirement

import com.extendedclip.deluxemenus.menu.MenuHolder
import com.extendedclip.deluxemenus.utils.DebugLevel
import org.bukkit.Bukkit
import java.util.UUID
import java.util.logging.Level

class IsObjectRequirement(private val input: String, private val obj: String) : Requirement() {
    override fun evaluate(holder: MenuHolder): Boolean {
        val toCheck = holder.setPlaceholdersAndArguments(input)

        return when (obj) {
            "int" -> toCheck.toIntOrNull() != null
            "double" -> toCheck.toDoubleOrNull() != null
            "player" -> {
                try {
                    val id = UUID.fromString(toCheck)
                    Bukkit.getPlayer(id) != null
                } catch (_: IllegalArgumentException) {
                    Bukkit.getPlayerExact(toCheck) != null
                }
            }
            "uuid" -> {
                try {
                    UUID.fromString(toCheck)
                    true
                } catch (_: IllegalArgumentException) {
                    false
                }
            }
            else -> {
                holder.plugin.debug(DebugLevel.HIGHEST, Level.INFO, "Invalid object: $obj in \"is object\" check.")
                return false
            }
        }
    }
}
