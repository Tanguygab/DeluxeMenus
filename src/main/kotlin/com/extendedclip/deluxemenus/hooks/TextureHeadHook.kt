package com.extendedclip.deluxemenus.hooks

import com.extendedclip.deluxemenus.DeluxeMenus
import com.extendedclip.deluxemenus.cache.SimpleCache
import com.extendedclip.deluxemenus.utils.SkullUtils
import org.bukkit.inventory.ItemStack
import java.util.concurrent.ConcurrentHashMap

class TextureHeadHook(private val plugin: DeluxeMenus) : ItemHook, SimpleCache {
    override val prefix = "texture-"

    override val cache = ConcurrentHashMap<String, ItemStack>()

    override fun getItem(vararg arguments: String): ItemStack {
        if (arguments.isEmpty()) return plugin.head.clone()

        return try {
            cache.computeIfAbsent(arguments[0]) {
                SkullUtils.getSkullByBase64EncodedTextureUrl(plugin, SkullUtils.getEncoded(it))
            }.clone()
        } catch (e: Exception) {
            plugin.printStacktrace("Something went wrong while trying to get texture head: " + arguments[0], e)
            plugin.head.clone()
        }
    }

    override fun itemMatchesIdentifiers(item: ItemStack, vararg arguments: String): Boolean {
        return arguments.isNotEmpty() && arguments[0] == SkullUtils.getTextureFromSkull(plugin, item)
    }
}
