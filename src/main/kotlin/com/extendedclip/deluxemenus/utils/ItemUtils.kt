package com.extendedclip.deluxemenus.utils

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ArmorMeta
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionType

object ItemUtils {
    /**
     * Checks if the string starts with the substring "placeholder-". The check is case-insensitive.
     *
     * @param material The string to check
     * @return true if the string starts with "placeholder-", false otherwise
     */
    fun isPlaceholderOption(material: String): Boolean {
        return material.lowercase().startsWith(Constants.PLACEHOLDER_PREFIX)
    }

    /**
     * Checks if the string starts with the substring "stack-". The check is case-insensitive.
     *
     * @param material The string to check
     * @return true if the string starts with "stack-", false otherwise
     */
    fun isItemStackOption(material: String): Boolean {
        return material.lowercase().startsWith(Constants.STACK_PREFIX)
    }

    /**
     * Checks if the string is a player item. The check is case-sensitive.
     * Player items are: "main_hand", "off_hand", "armor_helmet", "armor_chestplate", "armor_leggings", "armor_boots"
     *
     * @param material The string to check
     * @return true if the string is a player item, false otherwise
     */
    fun isPlayerItem(material: String): Boolean {
        return Constants.INVENTORY_ITEM_ACCESSORS.containsKey(material)
    }

    /**
     * Checks if the material is a water bottle. The check is case-insensitive.
     *
     * @param material The material to check
     * @return true if the material is a water bottle, false otherwise
     */
    fun isWaterBottle(material: String): Boolean {
        return material.equals(Constants.WATER_BOTTLE, ignoreCase = true)
    }

    /**
     * Checks if the material is a banner.
     *
     * @param material The material to check
     * @return true if the material is a banner, false otherwise
     */
    fun isBanner(material: Material): Boolean {
        return material.name.endsWith("_BANNER")
    }

    /**
     * Checks if the material is a shield.
     *
     * @param material The material to check
     * @return true if the material is a shield, false otherwise
     */
    fun isShield(material: Material): Boolean {
        return material == Material.SHIELD
    }

    fun hasArmorMeta(itemStack: ItemStack) = itemStack.itemMeta is ArmorMeta

    /**
     * Checks if the ItemStack is a potion or can hold potion effects.
     *
     * @param itemStack The ItemStack to check
     * @return true if the ItemStack is a potion or can hold a potion effect, false otherwise
     */
    fun hasPotionMeta(itemStack: ItemStack) = itemStack.itemMeta is PotionMeta

    /**
     * Creates water bottles stack
     * @param amount the amount of water bottles to put in the stack
     * @return the water bottles stack
     */
    fun createWaterBottles(amount: Int): ItemStack {
        val itemStack = ItemStack(Material.POTION, amount)
        val itemMeta = itemStack.itemMeta

        if (itemMeta is PotionMeta) {
            itemMeta.basePotionType = PotionType.WATER
            itemStack.itemMeta = itemMeta
        }

        return itemStack
    }
}
