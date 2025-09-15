package com.extendedclip.deluxemenus.utils

import me.clip.placeholderapi.PlaceholderAPI
import net.md_5.bungee.api.ChatColor
import org.bukkit.Color
import org.bukkit.entity.Player
import java.util.regex.Pattern

object StringUtils {
    private val HEX_PATTERN = Pattern.compile("&(#[a-f0-9]{6})", Pattern.CASE_INSENSITIVE)

    /**
     * Translates the ampersand color codes like '&7' to their section symbol counterparts like '§7'.
     * <br></br>
     * It also translates hex colors like '&#aaFF00' to their section symbol counterparts like '§x§a§a§F§F§0§0'.
     *
     * @param input The string in which to translate the color codes.
     * @return The string with the translated colors.
     */
    fun color(input: String): String {
        // Hex Support for 1.16.1+
        var input = input
        val m = HEX_PATTERN.matcher(input)
        if (VersionHelper.IS_HEX_VERSION) {
            while (m.find()) {
                input = input.replace(m.group(), ChatColor.of(m.group(1)).toString())
            }
        }

        return ChatColor.translateAlternateColorCodes('&', input)
    }

    fun replacePlaceholdersAndArguments(
        input: String, arguments: Map<String, String>?,
        player: Player?,
        parsePlaceholdersInsideArguments: Boolean,
        parsePlaceholdersAfterArguments: Boolean
    ): String {
        if (player == null) return replaceArguments(input, arguments, null, parsePlaceholdersInsideArguments)

        return if (parsePlaceholdersAfterArguments)
            replacePlaceholders(replaceArguments(input, arguments, player, parsePlaceholdersInsideArguments), player)
        else replaceArguments(replacePlaceholders(input, player), arguments, player, parsePlaceholdersInsideArguments)
    }

    fun replacePlaceholders(input: String, player: Player) = PlaceholderAPI.setPlaceholders(player, input)

    fun replaceArguments(
        input: String, arguments: Map<String, String>?,
        player: Player?, parsePlaceholdersInsideArguments: Boolean
    ): String {
        var input = input
        if (arguments == null || arguments.isEmpty()) {
            return input
        }

        for (entry in arguments.entries) {
            val value = if (player != null && parsePlaceholdersInsideArguments)
                replacePlaceholders(entry.value, player)
            else entry.value
            input = input.replace("{" + entry.key + "}", value)
        }

        return input
    }

    fun parseRGBColor(input: String): Color? {
        val parts = input.split(",")
        return try { Color.fromRGB(
            parts[0].toInt(),
            parts[1].toInt(),
            parts[2].toInt()
        ) } catch (_: Exception) { null }
    }
}
