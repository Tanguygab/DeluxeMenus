package com.extendedclip.deluxemenus.requirement

import com.extendedclip.deluxemenus.DeluxeMenus
import com.extendedclip.deluxemenus.hooks.ItemHook
import com.extendedclip.deluxemenus.menu.MenuHolder
import com.extendedclip.deluxemenus.requirement.wrappers.ItemWrapper
import com.extendedclip.deluxemenus.utils.StringUtils
import com.extendedclip.deluxemenus.utils.VersionHelper
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.components.CustomModelDataComponent

class HasItemRequirement(
    private val plugin: DeluxeMenus,
    private val wrapper: ItemWrapper,
    private val invert: Boolean
) : Requirement() {
    override fun evaluate(holder: MenuHolder): Boolean {
        val materialName = holder.setPlaceholdersAndArguments(wrapper.material!!)
        val material = DeluxeMenus.MATERIALS[materialName.uppercase()]
        var pluginHook: ItemHook? = null
        if (material == null) {
            pluginHook = plugin.itemHooks.values.find { materialName.lowercase().startsWith(it.prefix) }
            //if (pluginHook == null) return invert
        }

        if (material == Material.AIR) return invert == (holder.viewer.inventory.firstEmpty() == -1)

        val armor = if (wrapper.checkArmor) holder.viewer.inventory.armorContents else null
        val offHand = if (wrapper.checkOffHand) holder.viewer.inventory.extraContents else null
        val inventory = holder.viewer.inventory.storageContents

        var total = 0
        for (listToCheck in listOf(inventory, offHand, armor)) {
            if (listToCheck == null) continue
            for (itemToCheck in listToCheck) {
                if (itemToCheck == null) continue
                if (!isRequiredItem(itemToCheck, holder, material, pluginHook)) continue
                total += itemToCheck.amount
            }
        }

        return invert == (total < wrapper.amount)
    }

    @Suppress("UnstableApiUsage", "DEPRECATION")
    private fun isRequiredItem(
        itemToCheck: ItemStack,
        holder: MenuHolder,
        material: Material?,
        pluginHook: ItemHook?
    ): Boolean {
        if (itemToCheck.type == Material.AIR) return false

        if (pluginHook != null) {
            if (!pluginHook.itemMatchesIdentifiers(
                    itemToCheck,
                    holder.setPlaceholdersAndArguments(wrapper.material!!.substring(pluginHook.prefix.length))
            )) return false
        } else if (wrapper.material != null && itemToCheck.type != material) return false
        if (wrapper.hasData && itemToCheck.getDurability() != wrapper.data) return false

        val metaToCheck = itemToCheck.itemMeta
        if (wrapper.isStrict) {
            if (metaToCheck != null) {
                if (VersionHelper.IS_CUSTOM_MODEL_DATA && metaToCheck.hasCustomModelData()) return false
                if (VersionHelper.IS_CUSTOM_MODEL_DATA_COMPONENT && !isEmptyModelData(metaToCheck.customModelDataComponent)) return false
                if (metaToCheck.hasLore()) return false
                return !metaToCheck.hasDisplayName()
            }
        } else {
            if (metaToCheck == null) {
                if (wrapper.customData != 0 || wrapper.name != null || wrapper.lore != null || wrapper.loreList != null) {
                    return false
                }

                if (VersionHelper.IS_CUSTOM_MODEL_DATA_COMPONENT) {
                    if (!isEmptyModelData(wrapper.customModelDataComponent)) {
                        return false
                    }
                }
            }

            if (wrapper.customData != 0) {
                if (VersionHelper.IS_CUSTOM_MODEL_DATA) {
                    if (!metaToCheck!!.hasCustomModelData()) return false
                    if (metaToCheck.customModelData != wrapper.customData) return false
                }
            }

            if (VersionHelper.IS_CUSTOM_MODEL_DATA_COMPONENT && !isEmptyModelData(wrapper.customModelDataComponent) && !itemModelComponentContains(
                    holder,
                    metaToCheck!!.customModelDataComponent,
                    wrapper.customModelDataComponent
                )
            ) {
                return false
            }

            if (wrapper.name != null) {
                if (!metaToCheck!!.hasDisplayName()) return false

                val name = StringUtils.color(holder.setPlaceholdersAndArguments(wrapper.name!!))
                val nameToCheck = StringUtils.color(holder.setPlaceholdersAndArguments(metaToCheck.displayName))

                if (wrapper.checkNameContains) if (!nameToCheck.contains(name, ignoreCase = wrapper.checkNameIgnoreCase)) return false
                else if (wrapper.checkNameIgnoreCase) if (!nameToCheck.equals(name, ignoreCase = true)) return false
                else if (nameToCheck != name) return false
            }

            if (wrapper.loreList != null) {
                val loreX = metaToCheck!!.lore ?: return false

                val lore = wrapper.loreList!!
                    .map { holder.setPlaceholdersAndArguments(it) }
                    .joinToString("&&") { StringUtils.color(it) }
                val loreToCheck = loreX
                    .map { holder.setPlaceholdersAndArguments(it) }
                    .joinToString("&&") { StringUtils.color(it) }


                if (wrapper.checkLoreContains) if (!loreToCheck.contains(lore, ignoreCase = wrapper.checkLoreIgnoreCase)) return false
                else if (wrapper.checkLoreIgnoreCase) if (!loreToCheck.equals(lore, ignoreCase = true)) return false
                else if (loreToCheck != lore) return false
            }

            if (wrapper.lore != null) {
                val loreX = metaToCheck!!.lore ?: return false

                val lore = StringUtils.color(holder.setPlaceholdersAndArguments(wrapper.lore!!))
                val loreToCheck = loreX
                    .map { holder.setPlaceholdersAndArguments(it) }
                    .joinToString("&&") { StringUtils.color(it) }

                return if (wrapper.checkLoreContains) loreToCheck.contains(lore, ignoreCase = wrapper.checkLoreIgnoreCase)
                else if (wrapper.checkLoreIgnoreCase) loreToCheck.equals(lore, ignoreCase = true)
                else loreToCheck == lore
            }
        }
        return true
    }

    @Suppress("UnstableApiUsage")
    private fun isEmptyModelData(modelData: CustomModelDataComponent): Boolean {
        return modelData.colors.isEmpty()
                && modelData.flags.isEmpty()
                && modelData.floats.isEmpty()
                && modelData.strings.isEmpty()
    }

    private fun isEmptyModelData(modelData: com.extendedclip.deluxemenus.menu.options.CustomModelDataComponent): Boolean {
        return modelData.colors.isEmpty()
                && modelData.flags.isEmpty()
                && modelData.floats.isEmpty()
                && modelData.strings.isEmpty()
    }

    @Suppress("UnstableApiUsage")
    private fun itemModelComponentContains(
        holder: MenuHolder,
        modelData: CustomModelDataComponent,
        wrapper: com.extendedclip.deluxemenus.menu.options.CustomModelDataComponent
    ): Boolean {
        if (!wrapper.colors.isEmpty()) {
            val colors = wrapper.colors
                .map { holder.setPlaceholdersAndArguments(it) }
                .mapNotNull { StringUtils.parseRGBColor(it) }

            if (colors.any { !modelData.colors.contains(it) }) return false
        }

        if (!wrapper.flags.isEmpty()) {
            val flags = wrapper.flags.map { holder.setPlaceholdersAndArguments(it).toBoolean() }

            if (flags.any { !modelData.flags.contains(it) }) return false
        }

        if (!wrapper.floats.isEmpty()) {
            val floats = wrapper.floats.map { holder.setPlaceholdersAndArguments(it).toFloat() }

            if (floats.any { !modelData.floats.contains(it) }) return false
        }

        if (!wrapper.strings.isEmpty()) {
            val strings = wrapper.strings.map { holder.setPlaceholdersAndArguments(it) }

            if (strings.any { !modelData.strings.contains(it) }) return false
        }

        return true
    }
}
