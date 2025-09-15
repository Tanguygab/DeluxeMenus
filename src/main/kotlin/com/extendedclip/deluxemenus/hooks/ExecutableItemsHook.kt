package com.extendedclip.deluxemenus.hooks

import com.extendedclip.deluxemenus.cache.SimpleCache
import com.ssomar.score.api.executableitems.ExecutableItemsAPI
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.util.Optional.empty
import java.util.concurrent.ConcurrentHashMap

class ExecutableItemsHook : ItemHook, SimpleCache {
    override val prefix = "executableitems-"

    override val cache = ConcurrentHashMap<String, ItemStack>()

    override fun getItem(vararg arguments: String): ItemStack {
        if (arguments.isEmpty()) ItemStack(Material.STONE)

        val item = getFromCache(arguments[0]) { id ->
            ExecutableItemsAPI
                .getExecutableItemsManager()
                .getExecutableItem(id)
                .map { it.buildItem(1, empty()) }
                .orElse(null)
                ?.clone()
        }

        return item?.clone() ?: ItemStack(Material.STONE)
    }

    override fun itemMatchesIdentifiers(item: ItemStack, vararg arguments: String): Boolean {
        if (arguments.isEmpty()) return false
        val fromId = ExecutableItemsAPI.getExecutableItemsManager().getExecutableItem(arguments[0]).orElse(null)
        val fromItem = ExecutableItemsAPI.getExecutableItemsManager().getExecutableItem(item).orElse(null)
        return fromItem != null && fromItem == fromId
    }
}
