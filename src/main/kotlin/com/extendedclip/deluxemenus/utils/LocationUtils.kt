package com.extendedclip.deluxemenus.utils

import org.bukkit.Bukkit
import org.bukkit.Location

object LocationUtils {
    /**
     * Serialize a location into a string
     *
     * @param loc Location to serialize
     * @return Serialized location string
     */
    fun serializeLocation(loc: Location): String {
        val str = "${loc.x},${loc.y},${loc.z}"
        return if (loc.world != null) loc.world!!.name + ",$str" else str
    }

    /**
     * Deserialize a location from a string
     *
     * @param loc Serialized location string
     * @return Location
     * @throws NumberFormatException If the coordinates are not valid doubles
     */
    @Throws(NumberFormatException::class)
    fun deserializeLocation(loc: String): Location? {
        if (!loc.contains(",")) return null

        val data = loc.split(",", limit = 4)

        if (data.size !in 3..4) return null

        if (data.size == 3) return Location(
            null,
            data[0].toDouble(),
            data[1].toDouble(),
            data[2].toDouble()
        )

        return Location(
            Bukkit.getServer().getWorld(data[0]),
            data[1].toDouble(),
            data[2].toDouble(),
            data[3].toDouble()
        )
    }
}
