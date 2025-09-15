package com.extendedclip.deluxemenus.hooks

import com.extendedclip.deluxemenus.DeluxeMenus
import com.extendedclip.deluxemenus.cache.SimpleCache
import com.extendedclip.deluxemenus.utils.DebugLevel
import net.Indyuce.mmoitems.MMOItems
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Level

class MMOItemsHook(private val plugin: DeluxeMenus) : ItemHook, SimpleCache {
    override val prefix = "mmoitems-"

    override val cache = ConcurrentHashMap<String, ItemStack>()

    override fun getItem(vararg arguments: String): ItemStack {
        if (arguments.isEmpty()) return ItemStack(Material.STONE)

        val cached = cache.get(arguments[0])
        if (cached != null) return cached.clone()

        val splitArgs = arguments[0].split(":", limit = 2)
        if (splitArgs.size != 2) return ItemStack(Material.STONE)

        val itemType = MMOItems.plugin.types.get(splitArgs[0]) ?: return ItemStack(Material.STONE)

        var mmoItem: ItemStack? = null
        try {
            mmoItem = Bukkit.getScheduler().callSyncMethod(plugin) {
                val item = MMOItems.plugin.getItem(itemType, splitArgs[1]) ?: return@callSyncMethod ItemStack(Material.STONE)
                cache[arguments[0]] = item
                item
            }.get()
        } catch (_: Exception) {
            plugin.debug(DebugLevel.HIGHEST, Level.SEVERE, "Error getting MMOItem synchronously.")
        }

        return mmoItem ?: ItemStack(Material.STONE)
    }

    override fun itemMatchesIdentifiers(item: ItemStack, vararg arguments: String): Boolean {
        if (arguments.isEmpty()) return false

        val splitArgs = arguments[0].split(":", limit = 2)
        if (splitArgs.size != 2) return false
        return splitArgs[0].equals(MMOItems.getTypeName(item), ignoreCase = true)
                && splitArgs[1].equals(MMOItems.getID(item), ignoreCase = true)
    }

}
