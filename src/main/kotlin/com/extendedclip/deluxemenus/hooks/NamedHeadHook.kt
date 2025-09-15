package com.extendedclip.deluxemenus.hooks

import com.extendedclip.deluxemenus.DeluxeMenus
import com.extendedclip.deluxemenus.cache.SimpleCache
import com.extendedclip.deluxemenus.listener.Listener
import com.extendedclip.deluxemenus.utils.SkullUtils
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import java.util.concurrent.ConcurrentHashMap

class NamedHeadHook(plugin: DeluxeMenus) : Listener(plugin), ItemHook, SimpleCache {
    override val prefix = "head-"

    override val cache = ConcurrentHashMap<String, ItemStack>()

    override fun getItem(vararg arguments: String): ItemStack {
        if (arguments.isEmpty()) return plugin.head.clone()

        try {
            return cache.computeIfAbsent(arguments[0]) { SkullUtils.getSkullByName(plugin, it) }.clone()
        } catch (exception: Exception) {
            plugin.printStacktrace(
                "Something went wrong while trying to get a head by name" +
                        ": " + arguments[0],
                exception
            )
        }

        return plugin.head.clone()
    }

    override fun itemMatchesIdentifiers(item: ItemStack, vararg arguments: String): Boolean {
        return arguments.isNotEmpty() && arguments[0].equals(SkullUtils.getSkullOwner(item), ignoreCase = true)
    }

    @EventHandler(ignoreCancelled = true)
    fun onPlayerQuit(e: PlayerQuitEvent) {
        cache.remove(e.player.name)
    }
}
