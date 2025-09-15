package com.extendedclip.deluxemenus.dupe.marker

import org.bukkit.inventory.ItemStack

interface ItemMarker {
    fun mark(itemStack: ItemStack): ItemStack
    fun unmark(itemStack: ItemStack): ItemStack
    fun isMarked(itemStack: ItemStack): Boolean
}
