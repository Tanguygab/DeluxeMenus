package com.extendedclip.deluxemenus.placeholder

import com.extendedclip.deluxemenus.DeluxeMenus
import com.extendedclip.deluxemenus.menu.Menu
import com.extendedclip.deluxemenus.persistentmeta.DataType
import com.extendedclip.deluxemenus.utils.VersionHelper
import me.clip.placeholderapi.PlaceholderAPI
import me.clip.placeholderapi.PlaceholderAPIPlugin
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.OfflinePlayer

class Expansion(private val plugin: DeluxeMenus) : PlaceholderExpansion() {
    override fun persist() = true
    override fun getIdentifier() = plugin.name.lowercase()
    override fun getAuthor() = plugin.description.authors.toString()
    override fun getVersion() = plugin.description.version

    override fun getPlaceholders() = listOf(
        "%deluxemenus_is_in_menu%",
        "%deluxemenus_opened_menu%",
        "%deluxemenus_last_menu%",
        "%deluxemenus_meta_has_value_<key>_[type]%",
        "%deluxemenus_meta_<key>_<type>_[default-value]%"
    )

    override fun onRequest(offlinePlayer: OfflinePlayer?, input: String): String? {
        if (offlinePlayer == null || !offlinePlayer.isOnline) return null
        val onlinePlayer = offlinePlayer.player ?: return null

        val parsedInput = PlaceholderAPI.setBracketPlaceholders(onlinePlayer, input)
        val parsedInputLower = parsedInput.lowercase()

        when (parsedInputLower) {
            "is_in_menu" -> return boolean(Menu.getMenuHolder(onlinePlayer) != null)
            "opened_menu" -> return Menu.getOpenMenu(onlinePlayer)?.options?.name ?: ""
            "last_menu" -> return Menu.getLastMenu(onlinePlayer)?.options?.name ?: ""
        }

        if (!parsedInputLower.startsWith("meta_")) return null
        if (!VersionHelper.IS_PDC_VERSION || plugin.persistentMetaHandler == null) return null
        val pmh = plugin.persistentMetaHandler!!

        // %deluxemenus_meta_has_value_<key>_[type]%
        if (parsedInputLower.startsWith("meta_has_value_")) {
            val hasValueInput = parsedInput.substring(15)
            val hasValueParts = hasValueInput.split("_", limit = 2)

            if (hasValueParts.size !in 1..2) return null

            val key = pmh.getKey(hasValueParts[0]) ?: return boolean(false)

            if (hasValueParts.size == 1) {
                return boolean(pmh.hasMetaValue(onlinePlayer, key))
            }

            val type = DataType.getSupportedTypeByName(hasValueParts[1]) ?: return boolean(false)

            return boolean(pmh.hasMetaValue(onlinePlayer, key, type))
        }

        // %deluxemenus_meta_<key>_<type>_[default-value]%
        val getValueInput = parsedInput.substring(5)

        if (!getValueInput.contains("_")) return null

        val parts = getValueInput.split("_", limit = 3)
        if (parts.size < 2) return null

        val key = pmh.getKey(parts[0]) ?: return boolean(false)

        val type = DataType.getSupportedTypeByName(parts[1]) ?: return boolean(false)

        val result = pmh.getMetaValue(onlinePlayer, key, type)
        if (result != null) return result.toString()

        // return the default value
        return if (parts.size > 2) parts[2] else ""
    }

    private fun boolean(value: Boolean) = if (value) PlaceholderAPIPlugin.booleanTrue() else PlaceholderAPIPlugin.booleanFalse()
}
