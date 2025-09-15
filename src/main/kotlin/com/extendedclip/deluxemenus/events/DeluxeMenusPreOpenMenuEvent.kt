package com.extendedclip.deluxemenus.events

import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent

class DeluxeMenusPreOpenMenuEvent(player: Player) : PlayerEvent(player), Cancellable {
    private var cancelled = false

    override fun getHandlers() = handlerList
    override fun isCancelled() = cancelled

    override fun setCancelled(cancelled: Boolean) {
        this.cancelled = cancelled
    }

    companion object {
        val handlerList = HandlerList()
    }
}

