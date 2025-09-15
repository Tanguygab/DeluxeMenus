package com.extendedclip.deluxemenus.command.subcommand

import com.extendedclip.deluxemenus.DeluxeMenus
import net.kyori.adventure.text.TextReplacementConfig
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

abstract class SubCommand(protected val plugin: DeluxeMenus, val name: String) {

    abstract fun execute(sender: CommandSender, arguments: List<String>)

    abstract fun onTabComplete(sender: CommandSender, arguments: List<String>): List<String>?

    protected fun getPlayerNameCompletion(argument: String?): List<String>? {
        val onlinePlayerNames = Bukkit.getOnlinePlayers().map { it.name }

        if (onlinePlayerNames.isEmpty()) return null
        if (argument.isNullOrEmpty()) return onlinePlayerNames

        return onlinePlayerNames.filter { it.startsWith(argument, ignoreCase = true) }
    }

    companion object {
        protected const val ADMIN_PERMISSION = "deluxemenus.admin"
        internal val PLAYER_REPLACER_BUILDER = TextReplacementConfig.builder().matchLiteral("<player>")
        internal val AMOUNT_REPLACER_BUILDER = TextReplacementConfig.builder().matchLiteral("<amount>")
        internal val MENU_REPLACER_BUILDER = TextReplacementConfig.builder().matchLiteral("<menu>")
        internal val KEY_REPLACER_BUILDER = TextReplacementConfig.builder().matchLiteral("<key>")
        internal val VALUE_REPLACER_BUILDER = TextReplacementConfig.builder().matchLiteral("<value>")
        internal val NEW_VALUE_REPLACER_BUILDER = TextReplacementConfig.builder().matchLiteral("<new-value>")
        internal val TYPE_REPLACER_BUILDER = TextReplacementConfig.builder().matchLiteral("<type>")
    }
}
