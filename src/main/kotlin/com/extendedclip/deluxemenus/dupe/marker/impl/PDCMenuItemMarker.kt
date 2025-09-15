package com.extendedclip.deluxemenus.dupe.marker.impl

import com.extendedclip.deluxemenus.DeluxeMenus
import com.extendedclip.deluxemenus.dupe.marker.ItemMarker
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class PDCMenuItemMarker(plugin: DeluxeMenus, mark: String) : ItemMarker {
    private val mark: NamespacedKey

    init {
        this.mark = NamespacedKey(plugin, mark)
    }

    override fun mark(itemStack: ItemStack): ItemStack {
        val itemMeta = itemStack.getItemMeta()
        if (itemMeta == null) {
            return itemStack
        }

        val container = itemMeta.getPersistentDataContainer()
        container.set<Byte?, Byte?>(
            mark,
            PersistentDataType.BYTE,
            1.toByte()
        )

        itemStack.setItemMeta(itemMeta)
        return itemStack
    }

    override fun unmark(itemStack: ItemStack): ItemStack {
        val itemMeta = itemStack.getItemMeta()
        if (itemMeta == null) {
            return itemStack
        }

        val container = itemMeta.getPersistentDataContainer()
        container.remove(mark)

        itemStack.setItemMeta(itemMeta)
        return itemStack
    }

    override fun isMarked(itemStack: ItemStack): Boolean {
        val itemMeta = itemStack.getItemMeta()
        if (itemMeta == null) {
            return false
        }

        val container = itemMeta.getPersistentDataContainer()
        return container.has<Byte?, Byte?>(mark, PersistentDataType.BYTE)
    }
}
