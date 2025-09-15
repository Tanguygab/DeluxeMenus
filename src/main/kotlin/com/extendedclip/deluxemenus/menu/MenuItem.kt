package com.extendedclip.deluxemenus.menu

import com.extendedclip.deluxemenus.DeluxeMenus
import com.extendedclip.deluxemenus.menu.options.HeadType
import com.extendedclip.deluxemenus.menu.options.LoreAppendMode
import com.extendedclip.deluxemenus.menu.options.MenuItemOptions
import com.extendedclip.deluxemenus.nbt.NbtProvider
import com.extendedclip.deluxemenus.nbt.NbtProvider.isAvailable
import com.extendedclip.deluxemenus.utils.*
import com.google.common.collect.ImmutableMultimap
import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.bukkit.block.Banner
import org.bukkit.block.data.type.Light
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemRarity
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.*
import org.bukkit.inventory.meta.components.CustomModelDataComponent
import org.bukkit.inventory.meta.trim.ArmorTrim
import org.bukkit.util.io.BukkitObjectInputStream
import java.io.ByteArrayInputStream
import java.util.Base64
import java.util.logging.Level
import kotlin.math.max
import kotlin.math.min

class MenuItem(private val plugin: DeluxeMenus, val options: MenuItemOptions) {

    fun getItemStack(holder: MenuHolder): ItemStack? {
        val viewer = holder.viewer

        var itemStack: ItemStack? = null
        var amount = 1

        var stringMaterial = options.material
        var lowercaseStringMaterial = stringMaterial.lowercase()

        if (ItemUtils.isPlaceholderOption(lowercaseStringMaterial)) {
            stringMaterial = holder.setPlaceholdersAndArguments(stringMaterial.substring(Constants.PLACEHOLDER_PREFIX.length))
            lowercaseStringMaterial = stringMaterial.lowercase()
        }
        if (ItemUtils.isItemStackOption(lowercaseStringMaterial)) {
            stringMaterial = holder.setPlaceholdersAndArguments(stringMaterial.substring(Constants.STACK_PREFIX.length))
            val base64Item: ItemStack? = base64ToItemStack(stringMaterial)
            if (base64Item != null) {
                itemStack = base64Item
                amount = itemStack.amount
                lowercaseStringMaterial = itemStack.type.toString().lowercase()
            }
        }


        if (ItemUtils.isPlayerItem(lowercaseStringMaterial)) {
            val playerItem = Constants.INVENTORY_ITEM_ACCESSORS[lowercaseStringMaterial]!!(viewer.inventory)

            if (playerItem == null || playerItem.type == Material.AIR) {
                return ItemStack(Material.AIR)
            }

            itemStack = playerItem.clone()
            amount = playerItem.amount
        }

        val temporaryAmount = amount

        val finalMaterial = lowercaseStringMaterial
        val pluginHook = plugin.itemHooks.values.find { finalMaterial.startsWith(it.prefix) }

        if (pluginHook != null) {
            itemStack = pluginHook.getItem(
                viewer,
                holder.setPlaceholdersAndArguments(stringMaterial.substring(pluginHook.prefix.length))
            )
        }

        if (ItemUtils.isWaterBottle(stringMaterial)) {
            itemStack = ItemUtils.createWaterBottles(amount)
        }

        // The item is neither a water bottle nor plugin hook item
        if (itemStack == null) {
            val material = Material.getMaterial(stringMaterial.uppercase())
            if (material == null) {
                plugin.debug(
                    DebugLevel.HIGHEST,
                    Level.WARNING,
                    "Material: $stringMaterial is not valid! Setting to Stone."
                )
                itemStack = ItemStack(Material.STONE, temporaryAmount)
            } else {
                itemStack = ItemStack(material, temporaryAmount)
            }
        }

        if (ItemUtils.isBanner(itemStack.type)) {
            val meta = itemStack.itemMeta as BannerMeta?
            if (meta != null) {
                if (!options.bannerMeta.isEmpty()) {
                    meta.patterns = options.bannerMeta
                }
                itemStack.itemMeta = meta
            }
        }

        if (ItemUtils.isShield(itemStack.type)) {
            val blockStateMeta = itemStack.itemMeta as BlockStateMeta?

            if (blockStateMeta != null) {
                val banner = blockStateMeta.blockState as Banner
                if (options.baseColor != null) {
                    banner.baseColor = options.baseColor
                    banner.update()
                    blockStateMeta.blockState = banner
                }
                if (!options.bannerMeta.isEmpty()) {
                    banner.patterns = options.bannerMeta
                    banner.update()
                    blockStateMeta.blockState = banner
                }

                itemStack.itemMeta = blockStateMeta
            }
        }

        if (ItemUtils.hasPotionMeta(itemStack)) {
            val meta = itemStack.itemMeta as PotionMeta?

            if (meta != null) {
                if (options.rgb != null) {
                    val color = parseRGBColor(holder.setPlaceholdersAndArguments(options.rgb))
                    if (color != null) {
                        meta.color = color
                    }
                }

                if (!options.potionEffects.isEmpty()) {
                    for (effect in options.potionEffects) {
                        meta.addCustomEffect(effect, true)
                    }
                }

                itemStack.itemMeta = meta
            }
        }

        if (itemStack.type == Material.AIR) return itemStack

        if (options.damage != null) {
            val parsedDamage = holder.setPlaceholdersAndArguments(options.damage)
            try {
                val damage = parsedDamage.toInt()
                if (damage > 0) {
                    val meta = itemStack.itemMeta
                    if (meta is Damageable) {
                        meta.damage = damage
                        itemStack.itemMeta = meta
                    }
                }
            } catch (exception: NumberFormatException) {
                plugin.printStacktrace(
                    "Invalid damage found: $parsedDamage.",
                    exception
                )
            }
        }

        if (options.amount != -1) {
            amount = options.amount
        }

        if (options.dynamicAmount != null) {
            try {
                val dynamicAmount = holder.setPlaceholdersAndArguments(options.dynamicAmount).toDouble().toInt()
                amount = max(dynamicAmount, 1)
            } catch (_: NumberFormatException) {}
        }

        if (amount > 64) amount = 64

        itemStack.amount = amount

        val itemMeta = itemStack.itemMeta ?: return itemStack

        if (VersionHelper.IS_CUSTOM_MODEL_DATA && options.customModelData != null) {
            try {
                val modelData = holder.setPlaceholdersAndArguments(options.customModelData).toInt()
                @Suppress("DEPRECATION")
                itemMeta.setCustomModelData(modelData)
            } catch (_: Exception) {}
        }

        if (VersionHelper.IS_CUSTOM_MODEL_DATA_COMPONENT && options.customModelDataComponent != null) {
            @Suppress("UnstableApiUsage")
            itemMeta.setCustomModelDataComponent(
                parseCustomModelDataComponent(
                    options.customModelDataComponent, itemMeta.customModelDataComponent, holder
                )
            )
        }

        if (options.displayName != null) {
            val displayName = holder.setPlaceholdersAndArguments(options.displayName)
            itemMeta.setDisplayName(StringUtils.color(displayName))
        }

        val lore = mutableListOf<String>()
        // This checks if a lore should be kept from the hooked item, and then if a lore exists on the item
        // ItemMeta.getLore is nullable. In that case, we just create a new ArrayList so we don't add stuff to a null list.
        val itemLore = itemMeta.lore ?: mutableListOf<String>()
        // Ensures backwards compatibility with how hooked items are currently handled
        var mode: LoreAppendMode = options.loreAppendMode ?: LoreAppendMode.OVERRIDE
        if (!options.hasLore && options.loreAppendMode == null) mode = LoreAppendMode.IGNORE
        when (mode) {
            LoreAppendMode.IGNORE -> lore.addAll(itemLore)
            LoreAppendMode.TOP -> {
                lore.addAll(getMenuItemLore(holder, options.lore))
                lore.addAll(itemLore)
            }

            LoreAppendMode.BOTTOM -> {
                lore.addAll(itemLore)
                lore.addAll(getMenuItemLore(holder, options.lore))
            }

            LoreAppendMode.OVERRIDE -> lore.addAll(getMenuItemLore(holder, options.lore))
        }

        itemMeta.lore = lore

        if (options.unbreakable) itemMeta.isUnbreakable = true

        if (VersionHelper.HAS_DATA_COMPONENTS) {
            if (options.hideTooltip != null) {
                val hideTooltip = holder.setPlaceholdersAndArguments(options.hideTooltip)
                itemMeta.isHideTooltip = hideTooltip.toBoolean()
            }
            if (options.enchantmentGlintOverride != null) {
                val enchantmentGlintOverride = holder.setPlaceholdersAndArguments(options.enchantmentGlintOverride)
                itemMeta.setEnchantmentGlintOverride(enchantmentGlintOverride.toBoolean())
            }
            if (options.rarity != null) {
                val rarity = holder.setPlaceholdersAndArguments(options.rarity)
                try {
                    itemMeta.setRarity(ItemRarity.valueOf(rarity.uppercase()))
                } catch (_: IllegalArgumentException) {
                    plugin.debug(
                        DebugLevel.HIGHEST,
                        Level.WARNING,
                        "Rarity $rarity is not a valid!"
                    )
                }
            }
        }
        if (VersionHelper.HAS_TOOLTIP_STYLE) {
            if (options.tooltipStyle != null) {
                val tooltipStyle = NamespacedKey.fromString(holder.setPlaceholdersAndArguments(options.tooltipStyle))
                if (tooltipStyle != null) itemMeta.tooltipStyle = tooltipStyle
            }
            if (options.itemModel != null) {
                val itemModel = NamespacedKey.fromString(holder.setPlaceholdersAndArguments(options.itemModel))
                if (itemModel != null) itemMeta.itemModel = itemModel
            }
        }

        if (VersionHelper.HAS_ARMOR_TRIMS && ItemUtils.hasArmorMeta(itemStack)) {
            val trimMaterialName = options.trimMaterial
            val trimPatternName = options.trimPattern

            @Suppress("UnstableApiUsage")
            if (trimMaterialName != null && trimPatternName != null) {
                val trimMaterial = Registry.TRIM_MATERIAL.match(holder.setPlaceholdersAndArguments(trimMaterialName))
                val trimPattern = Registry.TRIM_PATTERN.match(holder.setPlaceholdersAndArguments(trimPatternName))

                if (trimMaterial != null && trimPattern != null) {
                    val armorMeta = itemMeta as ArmorMeta
                    armorMeta.trim = ArmorTrim(trimMaterial, trimPattern)
                    itemStack.itemMeta = armorMeta
                } else {
                    if (trimMaterial == null) {
                        plugin.debug(
                            DebugLevel.HIGHEST,
                            Level.WARNING,
                            "Trim material $trimMaterialName is not a valid!"
                        )
                    }

                    if (trimPattern == null) {
                        plugin.debug(
                            DebugLevel.HIGHEST,
                            Level.WARNING,
                            "Trim pattern $trimPatternName is not a valid!"
                        )
                    }
                }
            } else if (trimMaterialName != null) {
                plugin.debug(
                    DebugLevel.HIGHEST,
                    Level.WARNING,
                    "Trim pattern is not set for item with trim material $trimMaterialName"
                )
            } else if (trimPatternName != null) {
                plugin.debug(
                    DebugLevel.HIGHEST,
                    Level.WARNING,
                    "Trim material is not set for item with trim pattern $trimPatternName"
                )
            }
        }

        when (itemMeta) {
            is LeatherArmorMeta if options.rgb != null -> {
                val color = parseRGBColor(holder.setPlaceholdersAndArguments(options.rgb))
                if (color != null) {
                    itemMeta.setColor(color)
                } else {
                    plugin.debug(
                        DebugLevel.HIGHEST,
                        Level.WARNING,
                        "Invalid rgb colors found for leather armor: " + options.rgb
                    )
                }
            }

            is FireworkEffectMeta if options.rgb != null -> {
                val color = parseRGBColor(holder.setPlaceholdersAndArguments(options.rgb))
                if (color != null) {
                    itemMeta.effect = FireworkEffect.builder().withColor(color).build()
                } else {
                    plugin.debug(
                        DebugLevel.HIGHEST,
                        Level.WARNING,
                        "Invalid RGB color found for firework or firework star: " + options.rgb
                    )
                }
            }

            is EnchantmentStorageMeta if !options.enchantments.isEmpty() -> {
                for (entry in options.enchantments.entries) {
                    val result = itemMeta.addStoredEnchant(entry.key, entry.value, true)
                    if (!result) {
                        plugin.debug(
                            DebugLevel.HIGHEST,
                            Level.INFO,
                            "Failed to add enchantment " + entry.key.name + " to item " + itemStack.type
                        )
                    }
                }
            }
        }
        itemStack.itemMeta = itemMeta

        if (itemMeta !is EnchantmentStorageMeta && !options.enchantments.isEmpty()) {
            options.enchantments.forEach { itemMeta.addEnchant(it.key, it.value, true) }
        }

        if (options.lightLevel != null && itemMeta is BlockDataMeta) {
            val blockData = itemMeta.getBlockData(itemStack.type)
            if (blockData is Light) {
                val parsedLightLevel = holder.setPlaceholdersAndArguments(options.lightLevel)
                try {
                    val lightLevel = min(parsedLightLevel.toInt(), blockData.maximumLevel)
                    blockData.level = max(lightLevel, 0)
                    if (lightLevel < 0) {
                        plugin.debug(
                            DebugLevel.MEDIUM,
                            Level.WARNING,
                            "Invalid light level found for light block: $parsedLightLevel. Setting to 0."
                        )
                    }
                    if (lightLevel > blockData.maximumLevel) {
                        plugin.debug(
                            DebugLevel.MEDIUM,
                            Level.WARNING,
                            "Invalid light level found for light block: " + parsedLightLevel + ". Setting to " + blockData.maximumLevel + "."
                        )
                    }

                    itemMeta.setBlockData(blockData)
                } catch (exception: Exception) {
                    plugin.printStacktrace(
                        "Invalid light level found for light block: $parsedLightLevel",
                        exception
                    )
                }
            }
        }

        if (!options.itemFlags.isEmpty()) {
            for (flag in options.itemFlags) {
                itemMeta.addItemFlags(flag)

                if (flag == ItemFlag.HIDE_ATTRIBUTES && VersionHelper.HAS_DATA_COMPONENTS) {
                    itemMeta.attributeModifiers = ImmutableMultimap.of()
                }
            }
        }

        itemStack.itemMeta = itemMeta

        if (isAvailable) {
            if (options.nbtString != null) {
                val tag = holder.setPlaceholdersAndArguments(options.nbtString)
                if (tag.contains(":")) {
                    val parts = tag.split(":", limit = 2)
                    itemStack = NbtProvider.setString(itemStack, parts[0], parts[1])
                }
            }

            if (options.nbtByte != null) {
                val tag = holder.setPlaceholdersAndArguments(options.nbtByte)
                if (tag.contains(":")) {
                    val parts = tag.split(":")
                    itemStack = NbtProvider.setByte(itemStack, parts[0], parts[1].toByte())
                }
            }

            if (options.nbtShort != null) {
                val tag = holder.setPlaceholdersAndArguments(options.nbtShort)
                if (tag.contains(":")) {
                    val parts = tag.split(":".toRegex())
                    itemStack = NbtProvider.setShort(itemStack, parts[0], parts[1].toShort())
                }
            }

            if (options.nbtInt != null) {
                val tag = holder.setPlaceholdersAndArguments(options.nbtInt)
                if (tag.contains(":")) {
                    val parts = tag.split(":")
                    itemStack = NbtProvider.setInt(itemStack, parts[0], parts[1].toInt())
                }
            }

            for (nbtTag in options.nbtStrings) {
                val tag = holder.setPlaceholdersAndArguments(nbtTag)
                if (tag.contains(":")) {
                    val parts = tag.split(":", limit = 2).toTypedArray()
                    itemStack = NbtProvider.setString(itemStack, parts[0], parts[1])
                }
            }

            for (nbtTag in options.nbtBytes) {
                val tag = holder.setPlaceholdersAndArguments(nbtTag)
                if (tag.contains(":")) {
                    val parts = tag.split(":")
                    itemStack = NbtProvider.setByte(itemStack, parts[0], parts[1].toByte())
                }
            }

            for (nbtTag in options.nbtShorts) {
                val tag = holder.setPlaceholdersAndArguments(nbtTag)
                if (tag.contains(":")) {
                    val parts = tag.split(":")
                    itemStack = NbtProvider.setShort(itemStack, parts[0], parts[1].toShort())
                }
            }

            for (nbtTag in options.nbtInts) {
                val tag = holder.setPlaceholdersAndArguments(nbtTag)
                if (tag.contains(":")) {
                    val parts = tag.split(":")
                    itemStack = NbtProvider.setInt(itemStack, parts[0], parts[1].toInt())
                }
            }
        }

        return itemStack
    }

    /**
     * Checks if the string is a head item. The check is case-insensitive.
     * Head items are:
     *
     *  * "head-{player-name}" (a simple named player head, supports placeholders. eg. "head-%player_name% or head-extendedclip")
     *  * "texture-{texture-url}" (a head with a custom texture specified by a texture url. eg. "texture-93a728ad8d31486a7f9aad200edb373ea803d1fc5fd4321b2e2a971348234443")
     *  * "basehead-{base64-encoded-texture-url}" (a head with a custom texture specified by a base64 encoded texture url)
     *  * "hdb-{hdb-head-id}" (a head with a custom texture specified by a [HeadDatabase](https://www.spigotmc.org/resources/14280/) id)
     *
     *
     * @param material The string to check
     * @return true if the string is a head item, false otherwise
     */
    private fun isHeadItem(material: String): Boolean {
        val headType = HeadType.parseHeadType(material)
        if (headType != null) options.headType = headType
        return headType != null
    }

    private fun getItemFromHook(hookName: String?, vararg args: String) = plugin
        .getItemHook(hookName)
        ?.getItem(*args)

    fun getMenuItemLore(holder: MenuHolder, lore: List<String>) = lore
        .map { holder.setPlaceholdersAndArguments(it) }
        .map { StringUtils.color(it) }
        .map { it.split("\n") }
        .flatMap { it }
        .map { it.split("\\n") }
        .flatMap { it }

    @Suppress("UnstableApiUsage")
    private fun parseCustomModelDataComponent(
        unparsedComponent: com.extendedclip.deluxemenus.menu.options.CustomModelDataComponent,
        component: CustomModelDataComponent,
        holder: MenuHolder
    ): CustomModelDataComponent {
        if (!unparsedComponent.colors.isEmpty()) {
            component.colors = unparsedComponent.colors
                .map { holder.setPlaceholdersAndArguments(it) }
                .mapNotNull { parseRGBColor(it) }
        }

        if (!unparsedComponent.flags.isEmpty()) {
            component.flags = unparsedComponent.flags.map { holder.setPlaceholdersAndArguments(it).toBoolean() }
        }

        if (!unparsedComponent.floats.isEmpty()) {
            component.floats = unparsedComponent.floats.mapNotNull { holder.setPlaceholdersAndArguments(it).toFloatOrNull() }
        }

        if (!unparsedComponent.strings.isEmpty()) {
            component.strings = unparsedComponent.strings.map { holder.setPlaceholdersAndArguments(it) }
        }

        return component
    }

    private fun parseRGBColor(input: String): Color? {
        val color = StringUtils.parseRGBColor(input)
        if (color == null) {
            plugin.debug(
                DebugLevel.HIGHEST,
                Level.WARNING,
                "Invalid RGB color found: $input"
            )
        }
        return color
    }

    companion object {
        fun base64ToItemStack(data: String): ItemStack? {
            try {
                val bytes = Base64.getDecoder().decode(data)
                val inputStream = ByteArrayInputStream(bytes)
                val dataInput = BukkitObjectInputStream(inputStream)
                dataInput.close()
                val obj = dataInput.readObject()
                if (obj is ItemStack) return obj
            } catch (_: Exception) { }
            return null
        }
    }
}
