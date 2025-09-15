package com.extendedclip.deluxemenus.hooks

import com.extendedclip.deluxemenus.cache.SimpleCache
import dev.lone.itemsadder.api.CustomStack
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.util.concurrent.ConcurrentHashMap

class ItemsAdderHook : ItemHook, SimpleCache {
    override val prefix = "itemsadder-"

    override val cache = ConcurrentHashMap<String, ItemStack>()

    override fun getItem(vararg arguments: String): ItemStack {
        if (arguments.isEmpty()) return ItemStack(Material.STONE)

        val cached = cache[arguments[0]]
        if (cached != null) return cached.clone()

        val customStack = CustomStack.getInstance(arguments[0]) ?: return ItemStack(Material.STONE)

        val item = customStack.itemStack.clone()
        cache[arguments[0]] = item

        return item.clone()
    }

    override fun itemMatchesIdentifiers(item: ItemStack, vararg arguments: String): Boolean {
        if (arguments.isEmpty()) return false

        val stack = CustomStack.byItemStack(item)
        return stack != null && stack.id.equals(arguments[0], ignoreCase = true)
    }

}
