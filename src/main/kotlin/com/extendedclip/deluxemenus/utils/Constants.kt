package com.extendedclip.deluxemenus.utils

import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory

object Constants {
    const val MAIN_HAND = "main_hand"
    const val OFF_HAND = "off_hand"
    const val HELMET = "armor_helmet"
    const val CHESTPLATE = "armor_chestplate"
    const val LEGGINGS = "armor_leggings"
    const val BOOTS = "armor_boots"

    val PLAYER_ITEMS = listOf(MAIN_HAND, OFF_HAND, HELMET, CHESTPLATE, LEGGINGS, BOOTS)

    const val NAMED_HEAD_PREFIX = "head-"
    const val TEXTURE_HEAD_PREFIX = "texture-"
    const val BASE64_HEAD_PREFIX = "basehead-"
    const val HDB_HEAD_PREFIX = "hdb-"

    const val NAMED_HEAD_TYPE = "namedhead"
    const val TEXTURE_HEAD_TYPE = "texture"
    const val BASE64_HEAD_TYPE = "basehead"
    const val HDB_HEAD_TYPE = "hdb"

    const val STACK_PREFIX = "stack-"
    const val PLACEHOLDER_PREFIX = "placeholder-"
    const val WATER_BOTTLE = "water_bottle"

    /**
     * A map between a slot name and the method used to get that item from a player's inventory
     */
    val INVENTORY_ITEM_ACCESSORS = mapOf<String, (PlayerInventory) -> ItemStack?>(
        MAIN_HAND to { it.itemInMainHand },
        OFF_HAND to { it.itemInOffHand },
        HELMET to { it.helmet },
        CHESTPLATE to { it.chestplate },
        LEGGINGS to { it.leggings },
        BOOTS to { it.boots }
    )
}
