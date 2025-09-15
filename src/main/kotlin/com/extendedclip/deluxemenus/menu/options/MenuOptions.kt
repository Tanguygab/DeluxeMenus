package com.extendedclip.deluxemenus.menu.options

import com.extendedclip.deluxemenus.action.ClickHandler
import com.extendedclip.deluxemenus.requirement.RequirementList
import org.bukkit.event.inventory.InventoryType

data class MenuOptions(
    val name: String,
    val title: String,
    val type: InventoryType = InventoryType.CHEST,
    val size: Int = 9,
    val updateInterval: Int = 10,
    val refreshInterval: Int = 10,
    val refresh: Boolean = false,
    val parsePlaceholdersInArguments: Boolean = false,
    val parsePlaceholdersAfterArguments: Boolean = false,
    val enableBypassPerm: Boolean = false,

    val commands: List<String> = listOf(),
    val registerCommands: Boolean = false,
    val arguments: List<String> = listOf(),
    val argumentRequirements: List<RequirementList> = listOf(),
    val argumentsUsageMessage: String? = null,

    val openRequirements: RequirementList? = null,
    val openHandler: ClickHandler? = null,
    val closeHandler: ClickHandler? = null
)