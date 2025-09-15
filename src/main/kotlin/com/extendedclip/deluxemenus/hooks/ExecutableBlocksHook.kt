package com.extendedclip.deluxemenus.hooks

import com.extendedclip.deluxemenus.cache.SimpleCache
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.util.concurrent.ConcurrentHashMap

class ExecutableBlocksHook : ItemHook, SimpleCache {
    override val prefix = "executableblocks-"

    override val cache = ConcurrentHashMap<String, ItemStack>()

    @Suppress("DEPRECATION")
    override fun getItem(vararg arguments: String): ItemStack {
        if (arguments.isEmpty()) return ItemStack(Material.STONE)

        val item = getFromCache(arguments[0]) {
            com.ssomar.score.api.ExecutableBlocksAPI.getExecutableBlock(it)?.clone()
        }

        return item?.clone() ?: ItemStack(Material.STONE)
    }

    override fun itemMatchesIdentifiers(item: ItemStack, vararg arguments: String) = false
}
