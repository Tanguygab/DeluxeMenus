package com.extendedclip.deluxemenus.hooks

import com.extendedclip.deluxemenus.DeluxeMenus
import com.extendedclip.deluxemenus.cache.SimpleCache
import com.extendedclip.deluxemenus.utils.DebugLevel
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import ua.valeriishymchuk.simpleitemgenerator.api.SimpleItemGenerator
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Level

class SimpleItemGeneratorHook(private val plugin: DeluxeMenus) : ItemHook, SimpleCache {
    override val prefix = "simpleitemgenerator-"

    override val cache = ConcurrentHashMap<String, ItemStack>()

    override fun getItem(holder: Player, vararg arguments: String): ItemStack {
        if (arguments.isEmpty()) return errorItem("Item arguments are absent.")

        val item = getFromCache(arguments[0]) {
            SimpleItemGenerator.get().bakeItem(it, holder).orElse(null)
        }
        return item?.clone() ?: errorItem("Item ${arguments[0]} wasn't found by SimpleItemGenerator.")
    }

    private fun errorItem(error: String): ItemStack {
        val item = ItemStack(Material.STONE)
        plugin.debug(DebugLevel.HIGHEST, Level.WARNING, error)
        return item
    }

    override fun itemMatchesIdentifiers(item: ItemStack, vararg arguments: String): Boolean {
        return arguments.isNotEmpty() && SimpleItemGenerator.get()
            .getCustomItemKey(item)
            .map { it == arguments[0] }
            .orElse(false)
    }
}
