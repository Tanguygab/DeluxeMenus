package com.extendedclip.deluxemenus.hooks

import com.extendedclip.deluxemenus.cache.SimpleCache
import com.nexomc.nexo.api.NexoItems.idFromItem
import com.nexomc.nexo.api.NexoItems.itemFromId
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.util.concurrent.ConcurrentHashMap

class NexoHook : ItemHook, SimpleCache {
    override val prefix = "nexo-"

    override val cache = ConcurrentHashMap<String, ItemStack>()

    override fun getItem(vararg arguments: String): ItemStack {
        if (arguments.isEmpty()) return ItemStack(Material.STONE)

        val item = getFromCache(arguments[0]) { itemFromId(it)?.build()?.clone() }

        return item?.clone() ?: ItemStack(Material.STONE)
    }

    override fun itemMatchesIdentifiers(item: ItemStack, vararg arguments: String): Boolean {
        return arguments.isNotEmpty() && arguments[0].equals(idFromItem(item), ignoreCase = true)
    }

}
