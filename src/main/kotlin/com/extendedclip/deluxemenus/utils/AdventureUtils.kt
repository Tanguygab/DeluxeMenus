package com.extendedclip.deluxemenus.utils

import com.extendedclip.deluxemenus.DeluxeMenus
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.bukkit.command.CommandSender

object AdventureUtils {
    private val gson = GsonComponentSerializer.gson()

    fun sendJson(plugin: DeluxeMenus, sender: CommandSender, json: String) {
        plugin.audiences().sender(sender).sendMessage(fromJson(json))
    }

    fun fromJson(json: String) = gson.deserialize(json)
}