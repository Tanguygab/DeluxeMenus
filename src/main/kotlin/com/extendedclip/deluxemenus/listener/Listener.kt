package com.extendedclip.deluxemenus.listener

import com.extendedclip.deluxemenus.DeluxeMenus
import org.bukkit.event.Listener

abstract class Listener(protected val plugin: DeluxeMenus) : Listener {
    fun register() = plugin.server.pluginManager.registerEvents(this, plugin)
}
