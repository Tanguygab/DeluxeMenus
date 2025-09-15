package com.extendedclip.deluxemenus.events

import com.extendedclip.deluxemenus.menu.MenuHolder
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent

class DeluxeMenusOpenMenuEvent(player: Player, val holder: MenuHolder) : PlayerEvent(player) {
    override fun getHandlers() = handlerList

    companion object {
        val handlerList = HandlerList()
    }
}
