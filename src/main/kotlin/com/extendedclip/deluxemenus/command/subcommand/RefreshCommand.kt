package com.extendedclip.deluxemenus.command.subcommand

import com.extendedclip.deluxemenus.DeluxeMenus
import com.extendedclip.deluxemenus.menu.Menu
import com.extendedclip.deluxemenus.utils.Messages
import org.bukkit.command.CommandSender

class RefreshCommand(plugin: DeluxeMenus) : SubCommand(plugin, "refresh") {

    override fun execute(sender: CommandSender, arguments: List<String>) {
        if (!sender.hasPermission(REFRESH_COMMAND)) {
            plugin.sms(sender, Messages.NO_PERMISSION)
            return
        }

        if (arguments.isEmpty()) {
            plugin.sms(sender, Messages.WRONG_USAGE_REFRESH_COMMAND)
            return
        }

        if (Menu.getAllMenus().isEmpty()) {
            plugin.sms(
                sender,
                Messages.MENUS_LOADED.message.replaceText(AMOUNT_REPLACER_BUILDER.replacement("There are no").build())
            )
            return
        }

        val menu: Menu? = Menu.getMenuByName(arguments[0])

        if (menu == null) {
            plugin.sms(
                sender,
                Messages.INVALID_MENU.message
                    .replaceText(MENU_REPLACER_BUILDER.replacement(arguments[0]).build())
            )
            return
        }

        menu.refreshForAll()

        if (arguments.size < 2 || !arguments[1].equals("-s", ignoreCase = true)) {
            plugin.sms(
                sender, Messages.MENU_REFRESHED.message
                    .replaceText(MENU_REPLACER_BUILDER.replacement(menu.options.name).build())
                    .replaceText(
                        AMOUNT_REPLACER_BUILDER.replacement(java.lang.String.valueOf(menu.getActiveViewers())).build()
                    )
            )
        }
    }

    override fun onTabComplete(sender: CommandSender, arguments: List<String>): List<String>? {
        if (!sender.hasPermission(REFRESH_COMMAND)) return null
        if (arguments.isEmpty()) return listOf(name)
        if (arguments.size > 4) return null

        if (arguments.size == 1) {
            if (arguments[0].isEmpty()) return listOf(name)

            if (name.startsWith(arguments[0], ignoreCase = true)) return listOf(name)

            return null
        }

        val firstArgument = arguments[0].lowercase()
        if (name != firstArgument) return null

        val menuNames = Menu.getAllMenuNames()
        if (menuNames.isEmpty()) return null

        if (arguments.size == 2) {
            val secondArgument = arguments[1].lowercase()
            if (secondArgument.isEmpty()) return menuNames.toList()

            return menuNames.filter { it.startsWith(secondArgument) }
        }

        return null
    }

    companion object {
        private const val REFRESH_COMMAND = "deluxemenus.refresh"
    }
}
