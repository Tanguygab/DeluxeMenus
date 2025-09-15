package com.extendedclip.deluxemenus.hooks

import com.extendedclip.deluxemenus.cache.SimpleCache
import io.th0rgal.oraxen.api.OraxenItems
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.util.concurrent.ConcurrentHashMap

class OraxenHook : ItemHook, SimpleCache {
    override val prefix = "oraxen-"

    override val cache = ConcurrentHashMap<String, ItemStack>()

    override fun getItem(vararg arguments: String): ItemStack {
        if (arguments.isEmpty()) return ItemStack(Material.STONE)

        val item = getFromCache(arguments[0]) { OraxenItems.getItemById(it)?.build()?.clone() }

        return item?.clone() ?: ItemStack(Material.STONE)
    }

    override fun itemMatchesIdentifiers(item: ItemStack, vararg arguments: String): Boolean {
        return arguments.isNotEmpty() && arguments[0].equals(OraxenItems.getIdByItem(item), ignoreCase = true)
    }
}
