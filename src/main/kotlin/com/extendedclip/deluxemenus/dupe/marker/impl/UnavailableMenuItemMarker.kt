package com.extendedclip.deluxemenus.dupe.marker.impl

import com.extendedclip.deluxemenus.dupe.marker.ItemMarker
import org.bukkit.inventory.ItemStack

class UnavailableMenuItemMarker : ItemMarker {
    override fun mark(itemStack: ItemStack) = itemStack
    override fun unmark(itemStack: ItemStack) = itemStack
    override fun isMarked(itemStack: ItemStack) = false
}
