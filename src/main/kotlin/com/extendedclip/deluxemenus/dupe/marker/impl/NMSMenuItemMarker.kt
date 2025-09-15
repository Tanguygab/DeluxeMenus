package com.extendedclip.deluxemenus.dupe.marker.impl

import com.extendedclip.deluxemenus.dupe.marker.ItemMarker
import com.extendedclip.deluxemenus.nbt.NbtProvider
import org.bukkit.inventory.ItemStack

class NMSMenuItemMarker(private val mark: String) : ItemMarker {
    override fun mark(itemStack: ItemStack) = NbtProvider.setBoolean(itemStack, mark, true)!!
    override fun unmark(itemStack: ItemStack) = NbtProvider.removeKey(itemStack, mark)!!
    override fun isMarked(itemStack: ItemStack) = NbtProvider.hasKey(itemStack, mark)
}
