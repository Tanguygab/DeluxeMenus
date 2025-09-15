package com.extendedclip.deluxemenus.hooks

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

interface ItemHook {
    val prefix: String

    fun getItem(vararg arguments: String) = ItemStack(Material.STONE)

    fun getItem(holder: Player, vararg arguments: String) = getItem(*arguments)

    fun itemMatchesIdentifiers(item: ItemStack, vararg arguments: String): Boolean
}
