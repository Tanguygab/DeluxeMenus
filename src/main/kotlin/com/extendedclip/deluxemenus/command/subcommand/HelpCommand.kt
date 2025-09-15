package com.extendedclip.deluxemenus.command.subcommand

import com.extendedclip.deluxemenus.DeluxeMenus
import com.extendedclip.deluxemenus.utils.Messages
import org.bukkit.command.CommandSender

class HelpCommand(plugin: DeluxeMenus) : SubCommand(plugin, "help") {

    override fun execute(sender: CommandSender, arguments: List<String>) {
        if (sender.isOp) {
            plugin.sms(sender, Messages.HELP_OP)
            return
        }

        if (sender.hasPermission(ADMIN_PERMISSION)) {
            plugin.sms(sender, Messages.HELP)
            return
        }

        plugin.sms(sender, Messages.NO_PERMISSION)
    }

    override fun onTabComplete(sender: CommandSender, arguments: List<String>): List<String>? {
        if (!sender.hasPermission(ADMIN_PERMISSION)) return null
        if (arguments.size > 1) return null

        if (arguments.isEmpty() || arguments[0].isEmpty()) return listOf(name)

        if (!name.startsWith(arguments[0], ignoreCase = true)) return null

        return listOf(name)
    }
}
