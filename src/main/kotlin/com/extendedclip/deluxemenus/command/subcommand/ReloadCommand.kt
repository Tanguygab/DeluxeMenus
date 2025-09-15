package com.extendedclip.deluxemenus.command.subcommand

import com.extendedclip.deluxemenus.DeluxeMenus
import com.extendedclip.deluxemenus.menu.Menu
import com.extendedclip.deluxemenus.utils.Messages
import org.bukkit.command.CommandSender

class ReloadCommand(plugin: DeluxeMenus) : SubCommand(plugin, "reload") {

    override fun execute(sender: CommandSender, arguments: List<String>) {
        if (!sender.hasPermission(RELOAD_PERMISSION)) {
            plugin.sms(sender, Messages.NO_PERMISSION)
            return
        }

        if (plugin.configuration.checkConfig(null, "config.yml", false) == null) {
            plugin.sms(sender, Messages.RELOAD_FAIL)
            return
        }

        if (!arguments.isEmpty()) {
            if (Menu.getMenuByName(arguments[0]) == null) {
                plugin.sms(
                    sender,
                    Messages.INVALID_MENU.message
                        .replaceText(MENU_REPLACER_BUILDER.replacement(arguments[0]).build())
                )
                return
            }

            Menu.unload(plugin, arguments[0])

            if (plugin.configuration.loadGUIMenu(arguments[0])) {
                plugin.sms(
                    sender,
                    Messages.MENU_RELOADED.message
                        .replaceText(MENU_REPLACER_BUILDER.replacement(arguments[0]).build())
                )
                return
            }

            plugin.sms(
                sender,
                Messages.MENU_NOT_RELOADED.message
                    .replaceText(MENU_REPLACER_BUILDER.replacement(arguments[0]).build())
            )
            return
        }

        plugin.apply {
            clearCaches()
            reloadConfig()
            saveConfig()
            reload()
            Menu.unload(this)
            configuration.loadGUIMenus()
            sms(sender, Messages.RELOAD_SUCCESS)
        }

        val gLoaded = Menu.getLoadedMenuSize()

        plugin.sms(
            sender,
            (if (gLoaded == 1) Messages.MENU_LOADED else Messages.MENUS_LOADED)
                .message
                .replaceText(AMOUNT_REPLACER_BUILDER.replacement(gLoaded.toString()).build())
        )

    }

    override fun onTabComplete(sender: CommandSender, arguments: List<String>): List<String>? {
        if (!sender.hasPermission(RELOAD_PERMISSION)) return null
        if (arguments.isEmpty()) return listOf(name)
        if (arguments.size > 2) return null

        if (arguments.size == 1) {
            if (arguments[0].isEmpty()) return listOf(name)

            if (name.startsWith(arguments[0], ignoreCase = true)) return listOf(name)

            return null
        }

        val firstArgument = arguments[0].lowercase()

        if (name != firstArgument) {
            return null
        }

        val menuNames = Menu.getAllMenuNames()

        if (menuNames.isEmpty()) {
            return null
        }

        val secondArgument = arguments[1]

        if (secondArgument.isEmpty()) return menuNames.toList()

        return menuNames.filter { it.startsWith(secondArgument, ignoreCase = true) }
    }

    companion object {
        private const val RELOAD_PERMISSION = "deluxemenus.reload"
    }
}
