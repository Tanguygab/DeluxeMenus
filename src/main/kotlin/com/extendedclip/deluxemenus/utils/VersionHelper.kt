package com.extendedclip.deluxemenus.utils

import org.bukkit.Bukkit
import org.bukkit.event.inventory.InventoryType
import java.util.regex.Pattern

/**
 * Class for detecting server version.
 *
 * @author Matt from triumph-gui
 */
object VersionHelper {
    private val PACKAGE_NAME: String = Bukkit.getServer().javaClass.`package`.name
    val NMS_VERSION: String = PACKAGE_NAME.substringAfterLast('.')

    // Custom Model Data Component
    private const val V1_21_4 = 1214

    // Tooltip Style & Item Model
    private const val V1_21_2 = 1212

    // Data components
    private const val V1_20_5 = 1205

    // ArmorTrims
    private const val V1_19_4 = 1194

    // PlayerProfile API
    private const val V1_18_1 = 1181

    // Mojang obfuscation changes
    private const val V1_17 = 1170

    // Material and components on items change
    private const val V1_13 = 1130

    // PDC and customModelData
    private const val V1_14 = 1140

    // Hex colors
    private const val V1_16 = 1160

    // Paper adventure changes
    private const val V1_16_5 = 1165

    // SkullMeta#setOwningPlayer was added
    private const val V1_12 = 1120

    val CURRENT_VERSION: Int = getCurrentVersion()

    private val IS_PAPER = checkPaper()

    /**
     * Checks if the current version includes the setTooltipStyle and setItemModel
     */
    val HAS_TOOLTIP_STYLE: Boolean = CURRENT_VERSION >= V1_21_2

    /**
     * Checks if the current version includes the [Data Components](https://minecraft.wiki/w/Data_component_format)
     */
    val HAS_DATA_COMPONENTS: Boolean = CURRENT_VERSION >= V1_20_5

    /**
     * Checks if the current version includes the ArmorTrims API
     */
    val HAS_ARMOR_TRIMS: Boolean = CURRENT_VERSION >= V1_19_4

    /**
     * Checks if current version includes the PlayerProfile API
     */
    val HAS_PLAYER_PROFILES: Boolean = CURRENT_VERSION >= V1_18_1

    /**
     * Checks if the current version was a version without versioned packages.
     */
    val HAS_OBFUSCATED_NAMES: Boolean = CURRENT_VERSION >= V1_17

    /**
     * Checks if the version supports Components or not
     * Paper versions above 1.16.5 would be true
     * Spigot always false
     */
    val IS_COMPONENT: Boolean = IS_PAPER && CURRENT_VERSION >= V1_16_5

    /**
     * Checks if the version is lower than 1.13 due to the item changes
     */
    val IS_ITEM_LEGACY: Boolean = CURRENT_VERSION < V1_13

    /**
     * Checks if the version supports [org.bukkit.persistence.PersistentDataContainer]
     */
    val IS_PDC_VERSION: Boolean = CURRENT_VERSION >= V1_14

    /**
     * Checks if the version doesn't have [org.bukkit.inventory.meta.SkullMeta.setOwningPlayer] and
     * [org.bukkit.inventory.meta.SkullMeta.setOwner] should be used instead
     */
    val IS_SKULL_OWNER_LEGACY: Boolean = CURRENT_VERSION <= V1_12

    /**
     * Checks if the version has [org.bukkit.inventory.meta.ItemMeta.setCustomModelData]
     */
    val IS_CUSTOM_MODEL_DATA: Boolean = CURRENT_VERSION >= V1_14

    val IS_CUSTOM_MODEL_DATA_COMPONENT: Boolean = CURRENT_VERSION >= V1_21_4

    val IS_HEX_VERSION: Boolean = CURRENT_VERSION >= V1_16

    private val CHEST_INVENTORY_TYPES = getChestInventoryTypes()

    val VALID_INVENTORY_TYPES = getValidInventoryTypes()

    private fun getChestInventoryTypes(): List<InventoryType> {
        val types = mutableListOf(
            InventoryType.CHEST,
            InventoryType.CRAFTING,
            InventoryType.CREATIVE,
            InventoryType.ENDER_CHEST,
            InventoryType.MERCHANT,
            InventoryType.SHULKER_BOX
        )
        if (CURRENT_VERSION >= V1_14) {
            types.add(InventoryType.BARREL)
            types.add(InventoryType.LECTERN)
        }
        return types
    }

    private fun getValidInventoryTypes(): List<InventoryType> {
        val types = InventoryType.entries.toMutableList()
        types.removeIf { it != InventoryType.CHEST && it in CHEST_INVENTORY_TYPES }
        return types
    }

    /**
     * Check if the server has access to the Paper API
     * Taken from [PaperLib](https://github.com/PaperMC/PaperLib)
     *
     * @return True if on Paper server (or forks), false anything else
     */
    private fun checkPaper(): Boolean {
        return try {
            Class.forName("com.destroystokyo.paper.PaperConfig")
            true
        } catch (_: ClassNotFoundException) { false }
    }
    /**
     * Gets the current server version
     *
     * @return A protocol like number representing the version, for example 1.16.5 - 1165
     */
    private fun getCurrentVersion(): Int {
        // No need to cache since will only run once
        val matcher = Pattern
            .compile("(?<version>\\d+\\.\\d+)(?<patch>\\.\\d+)?")
            .matcher(Bukkit.getBukkitVersion())

        val stringBuilder = StringBuilder()
        if (matcher.find()) {
            stringBuilder.append(matcher.group("version").replace(".", ""))
            val patch = matcher.group("patch")
            if (patch == null) stringBuilder.append("0")
            else stringBuilder.append(patch.replace(".", ""))
        }

        return stringBuilder.toString().toIntOrNull() // Should never fail
            ?: throw RuntimeException("Could not retrieve server version!")
    }

    fun getNmsVersion(): String {
        val version = Bukkit.getServer().javaClass.`package`.name
        return version.substring(version.lastIndexOf('.') + 1)
    }

    /**
     * Gets the NMS class from class name.
     *
     * @return The NMS class.
     */
    @Throws(ClassNotFoundException::class)
    fun getNMSClass(pkg: String?, className: String?): Class<*> {
        return if (HAS_OBFUSCATED_NAMES) Class.forName("net.minecraft.$pkg.$className")
        else Class.forName("net.minecraft.server.$NMS_VERSION.$className")
    }

    /**
     * Gets the craft class from class name.
     *
     * @return The craft class.
     */
    @Throws(ClassNotFoundException::class)
    fun getCraftClass(name: String): Class<*> = Class.forName("org.bukkit.craftbukkit.$NMS_VERSION.$name")
}
