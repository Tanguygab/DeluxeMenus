package com.extendedclip.deluxemenus.cache

import org.bukkit.inventory.ItemStack
import java.util.concurrent.ConcurrentHashMap

interface SimpleCache {

    val cache: ConcurrentHashMap<String, ItemStack>

    fun getFromCache(id: String, f: (String) -> ItemStack?) = cache.compute(id) { _, v -> v ?: f(id) }
    fun clearCache() = cache.clear()
}
