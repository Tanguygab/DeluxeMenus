package com.extendedclip.deluxemenus.listener

import com.extendedclip.deluxemenus.DeluxeMenus
import com.extendedclip.deluxemenus.action.ClickHandler
import com.extendedclip.deluxemenus.menu.Menu
import com.extendedclip.deluxemenus.menu.MenuHolder
import com.extendedclip.deluxemenus.requirement.RequirementList
import com.google.common.cache.CacheBuilder
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.UUID
import java.util.concurrent.TimeUnit

class PlayerListener(plugin: DeluxeMenus) : Listener(plugin) {
    private val cache = CacheBuilder.newBuilder().expireAfterWrite(75, TimeUnit.MILLISECONDS).build<UUID, Long>()

    // This is so dumb. Mojang fix your shit.
    private val shiftCache = CacheBuilder.newBuilder().expireAfterWrite(200, TimeUnit.MILLISECONDS).build<UUID, Long>()

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    fun onCommandExecute(event: PlayerCommandPreprocessEvent) {
        val cmd = event.message.substring(1)
        val menu = Menu.getMenuByCommand(cmd.lowercase()) ?: return

        if (menu.options.registerCommands) return

        menu.openMenu(event.player)
        event.isCancelled = true
    }

    @EventHandler
    fun onLeave(event: PlayerQuitEvent) {
        val player = event.player

        if (Menu.isInMenu(player)) {
            Menu.closeMenu(plugin, player, false)
        }
    }

    @EventHandler
    fun onOpen(event: InventoryOpenEvent) {
        if (event.player !is Player) return
        val player = event.player as Player

        if (player.isSleeping) event.isCancelled = true

        if (Menu.isInMenu(player)) {
            Menu.closeMenu(plugin, player, true)
        }
    }

    @Suppress("UnstableApiUsage")
    @EventHandler
    fun onClose(event: InventoryCloseEvent) {
        if (event.player !is Player) return
        val player = event.player as Player

        if (Menu.isInMenu(player)) {
            Menu.closeMenu(plugin, player, false)
            plugin.server.scheduler.runTaskLater(plugin, Runnable {
                Menu.cleanInventory(plugin, player)
                player.updateInventory()
            }, 3L)
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    fun onClick(event: InventoryClickEvent) {
        if (event.whoClicked !is Player) return
        val player = event.whoClicked as Player

        val holder = Menu.getMenuHolder(player) ?: return

        if (holder.getMenu() == null) {
            Menu.closeMenu(plugin, player, true)
        }

        if (holder.isUpdating) {
            event.isCancelled = true
            return
        }

        event.isCancelled = true

        val slot = event.rawSlot
        val item = holder.getItem(slot) ?: return

        if (cache.getIfPresent(player.uniqueId) != null) return
        if (shiftCache.getIfPresent(player.uniqueId) != null) return
        if (event.click == ClickType.DOUBLE_CLICK) return

        if (event.click == ClickType.SHIFT_LEFT) {
            shiftCache.put(player.uniqueId, System.currentTimeMillis())
        }

        if (handleClick(player, holder, item.options.clickHandler, item.options.clickRequirements)) {
            return
        }

        var pair = when {
            event.isShiftClick && event.isLeftClick -> item.options.shiftLeftClickHandler to item.options.shiftLeftClickRequirements
            event.isShiftClick && event.isRightClick -> item.options.shiftRightClickHandler to item.options.shiftRightClickRequirements
            else -> null
        }
        if (pair != null) handleClick(player, holder, pair.first, pair.second)

        pair = when (event.click) {
            ClickType.LEFT -> item.options.leftClickHandler to item.options.leftClickRequirements
            ClickType.RIGHT -> item.options.rightClickHandler to item.options.rightClickRequirements
            ClickType.MIDDLE -> item.options.middleClickHandler to item.options.middleClickRequirements
            else -> null
        }
        if (pair != null) handleClick(player, holder, pair.first, pair.second)
    }

    /**
     * Handles menu click by player
     *
     * @param player       player who clicked
     * @param holder       menu holder
     * @param handler      click handler
     * @param requirements click requirements
     * @return true if click was handled successfully. will ever return false if no click handler was found
     */
    private fun handleClick(
        player: Player,
        holder: MenuHolder,
        handler: ClickHandler?,
        requirements: RequirementList?
    ): Boolean {
        if (handler == null) return false

        if (requirements != null) {
            val denyHandler = requirements.denyHandler

            if (!requirements.evaluate(holder)) {
                if (denyHandler == null) return true

                denyHandler.onClick(holder)
                return true
            }
        }

        cache.put(player.uniqueId, System.currentTimeMillis())
        handler.onClick(holder)
        return true
    }
}
