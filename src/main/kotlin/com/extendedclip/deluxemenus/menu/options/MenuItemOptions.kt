package com.extendedclip.deluxemenus.menu.options

import com.extendedclip.deluxemenus.action.ClickHandler
import com.extendedclip.deluxemenus.requirement.RequirementList
import org.bukkit.DyeColor
import org.bukkit.block.banner.Pattern
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.potion.PotionEffect

data class MenuItemOptions(
    val material: String,
    val damage: String?,
    val amount: Int,
    val customModelData: String?,
    val customModelDataComponent: CustomModelDataComponent?,
    val dynamicAmount: String?,
    val lightLevel: String?,
    val displayName: String?,
    val lore: List<String>,
    val baseColor: DyeColor?,
    var headType: HeadType?,
    val rgb: String?,

    val trimMaterial: String?,
    val trimPattern: String?,

    val hideTooltip: String?,
    val enchantmentGlintOverride: String?,
    val rarity: String?,
    val tooltipStyle: String?,
    val itemModel: String?,

    val enchantments: Map<Enchantment, Int>,
    val potionEffects: List<PotionEffect>,
    val bannerMeta: List<Pattern>,
    val itemFlags: Set<ItemFlag>,

    val unbreakable: Boolean,

    val displayNameHasPlaceholders: Boolean,
    val loreHasPlaceholders: Boolean,
    val hasLore: Boolean,
    val loreAppendMode: LoreAppendMode?,

    val nbtString: String?,
    val nbtByte: String?,
    val nbtShort: String?,
    val nbtInt: String?,
    val nbtStrings: List<String>,
    val nbtBytes: List<String>,
    val nbtShorts: List<String>,
    val nbtInts: List<String>,

    val slot: Int,
    val priority: Int,
    val updatePlaceholders: Boolean,

    val clickHandler: ClickHandler?,
    val leftClickHandler: ClickHandler?,
    val rightClickHandler: ClickHandler?,
    val shiftLeftClickHandler: ClickHandler?,
    val shiftRightClickHandler: ClickHandler?,
    val middleClickHandler: ClickHandler?,

    val viewRequirements: RequirementList?,
    val clickRequirements: RequirementList?,
    val leftClickRequirements: RequirementList?,
    val rightClickRequirements: RequirementList?,
    val shiftLeftClickRequirements: RequirementList?,
    val shiftRightClickRequirements: RequirementList?,
    val middleClickRequirements: RequirementList?
)