package com.extendedclip.deluxemenus.hooks

import com.extendedclip.deluxemenus.DeluxeMenus
import com.extendedclip.deluxemenus.cache.SimpleCache
import com.extendedclip.deluxemenus.utils.SkullUtils
import org.bukkit.inventory.ItemStack
import java.util.concurrent.ConcurrentHashMap

class BaseHeadHook(private val plugin: DeluxeMenus) : ItemHook, SimpleCache {
    override val prefix = "basehead-"

    override val cache = ConcurrentHashMap<String, ItemStack>()

    override fun getItem(vararg arguments: String): ItemStack {
        if (arguments.isEmpty()) return plugin.head.clone()

        return try {
            cache.computeIfAbsent(arguments[0]) { SkullUtils.getSkullByBase64EncodedTextureUrl(plugin, it) }.clone()
        } catch (e: Exception) {
            plugin.printStacktrace("Something went wrong while trying to get base64 head: " + arguments[0], e)
            plugin.head.clone()
        }
    }

    override fun itemMatchesIdentifiers(item: ItemStack, vararg arguments: String): Boolean {
        if (arguments.isEmpty()) return false

        val itemTexture = SkullUtils.getTextureFromSkull(plugin, item)
        var texture = SkullUtils.decodeSkinUrl(arguments[0])
        if (itemTexture == null || texture == null) return false

        texture = texture.substring("https://textures.minecraft.net/texture/".length - 1)
        return texture == itemTexture
    }
}
