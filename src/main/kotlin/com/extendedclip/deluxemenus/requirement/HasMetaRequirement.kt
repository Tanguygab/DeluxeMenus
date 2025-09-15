package com.extendedclip.deluxemenus.requirement

import com.extendedclip.deluxemenus.DeluxeMenus
import com.extendedclip.deluxemenus.menu.MenuHolder
import com.extendedclip.deluxemenus.persistentmeta.DataType

class HasMetaRequirement(
    private val plugin: DeluxeMenus,
    private val key: String,
    typeName: String,
    private val value: String,
    private val invert: Boolean
) : Requirement() {
    private val typeName: String = typeName.uppercase()

    override fun evaluate(holder: MenuHolder): Boolean {
        val player = holder.viewer

        val parsedKey = holder.setPlaceholdersAndArguments(key)
        val namespacedKey = plugin.persistentMetaHandler!!.getKey(parsedKey) ?: return invert

        val type = DataType.getSupportedTypeByName(typeName) ?: return invert

        val metaValue = plugin.persistentMetaHandler!!.getMetaValue(player, namespacedKey, type) ?: return invert

        val expectedValue = holder.setPlaceholdersAndArguments(value)
        // TODO: Is there any reason to parse placeholders in the stored value when reading them?
        //  Placeholders are parsed before value are stored. This means there will (or should) be no placeholders when reading.
        val actualValue = holder.setPlaceholdersAndArguments(metaValue.toString())

        if (type == DataType.STRING || type == DataType.BOOLEAN) {
            return invert != actualValue.equals(expectedValue, ignoreCase = true)
        }

        if (type == DataType.LONG || type == DataType.INTEGER) {
            try {
                val metaNum = actualValue.toLong()
                val toCheck = expectedValue.toLong()
                val pass = metaNum >= toCheck
                return invert != pass
            } catch (_: Exception) {
                return invert
            }
        }

        if (type == DataType.DOUBLE) {
            try {
                val metaNum = actualValue.toDouble()
                val toCheck = expectedValue.toDouble()
                val pass = metaNum >= toCheck
                return invert != pass
            } catch (_: Exception) {
                return invert
            }
        }

        return invert
    }
}
