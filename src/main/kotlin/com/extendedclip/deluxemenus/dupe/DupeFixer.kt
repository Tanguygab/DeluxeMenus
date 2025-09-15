package com.extendedclip.deluxemenus.dupe

import com.extendedclip.deluxemenus.DeluxeMenus
import com.extendedclip.deluxemenus.listener.Listener
import com.extendedclip.deluxemenus.utils.DebugLevel
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerJoinEvent
import java.util.logging.Level

/**
 * Prevents duplication of items created by DeluxeMenus. Items created by DeluxeMenus are marked and removed if found
 * outside the inventory they were created in.
 */
class DupeFixer(plugin: DeluxeMenus, private val marker: MenuItemMarker) : Listener(plugin) {
    @EventHandler
    private fun onPickup(event: EntityPickupItemEvent) {
        if (!marker.isMarked(event.item.itemStack)) {
            return
        }

        plugin.debug(
            DebugLevel.LOWEST,
            Level.INFO,
            "Someone picked up a DeluxeMenus item. Removing it."
        )
        event.item.remove()
    }

    @EventHandler
    private fun onDrop(event: PlayerDropItemEvent) {
        if (!marker.isMarked(event.itemDrop.itemStack)) {
            return
        }

        plugin.debug(
            DebugLevel.LOWEST,
            Level.INFO,
            "A DeluxeMenus item was dropped in the world. Removing it."
        )
        event.itemDrop.remove()
    }

    @EventHandler
    private fun onLogin(event: PlayerJoinEvent) {
        plugin.server.scheduler.runTaskLater(
            plugin,
            Runnable {
                for (itemStack in event.player.inventory.contents) {
                    if (itemStack == null) continue
                    if (!marker.isMarked(itemStack)) continue

                    plugin.debug(
                        DebugLevel.LOWEST,
                        Level.INFO,
                        "Player logged in with a DeluxeMenus item in their inventory. Removing it."
                    )
                    event.player.inventory.remove(itemStack)
                }
            },
            10L
        )
    }
}