package com.extendedclip.deluxemenus.command.subcommand

import com.extendedclip.deluxemenus.DeluxeMenus
import com.extendedclip.deluxemenus.menu.Menu
import com.extendedclip.deluxemenus.utils.Messages
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class OpenCommand(plugin: DeluxeMenus) : SubCommand(plugin, "open") {

    override fun execute(sender: CommandSender, arguments: List<String>) {
        if (!sender.hasPermission(OPEN_COMMAND)) {
            plugin.sms(sender, Messages.NO_PERMISSION)
            return
        }

        val player = sender is Player

        if (arguments.isEmpty()) {
            plugin.sms(sender, Messages.WRONG_USAGE_OPEN_COMMAND)
            return
        }

        if (Menu.getAllMenus().isEmpty()) {
            plugin.sms(
                sender,
                Messages.MENUS_LOADED.message.replaceText(AMOUNT_REPLACER_BUILDER.replacement("There are no").build())
            )
            return
        }

        val viewer: Player?
        var placeholderPlayer: String? = null

        if (arguments.size == 2 && arguments[1].startsWith("-p:")) {
            if (!sender.hasPermission("deluxemenus.placeholdersfor")) {
                plugin.sms(sender, Messages.NO_PERMISSION_PLAYER_ARGUMENT)
                return
            }

            placeholderPlayer = arguments[1].replace("-p:", "")
        } else if (arguments.size >= 3 && arguments[2].startsWith("-p:")) {
            if (!sender.hasPermission("deluxemenus.placeholdersfor")) {
                plugin.sms(sender, Messages.NO_PERMISSION_PLAYER_ARGUMENT)
                return
            }

            placeholderPlayer = arguments[2].replace("-p:", "")
        }

        if (arguments.size >= 2) {
            if (placeholderPlayer == null) {
                if (player && !sender.hasPermission("deluxemenus.open.others")) {
                    plugin.sms(sender, Messages.NO_PERMISSION)
                    return
                }

                viewer = Bukkit.getPlayerExact(arguments[1])
            } else {
                if (arguments.size >= 3) {
                    if (!sender.hasPermission("deluxemenus.open.others")) {
                        plugin.sms(sender, Messages.NO_PERMISSION)
                        return
                    }

                    viewer = Bukkit.getPlayerExact(arguments[1])
                } else {
                    if (!player) {
                        plugin.sms(sender, Messages.MUST_SPECIFY_PLAYER)
                        return
                    }

                    viewer = sender
                }
            }
        } else {
            if (!player) {
                plugin.sms(sender, Messages.MUST_SPECIFY_PLAYER)
                return
            }

            viewer = sender
        }

        if (viewer == null) {
            plugin.sms(
                sender,
                Messages.PLAYER_IS_NOT_ONLINE.message
                    .replaceText(PLAYER_REPLACER_BUILDER.replacement(arguments[1]).build())
            )
            return
        }

        var placeholder: Player? = null

        if (placeholderPlayer != null) {
            placeholder = Bukkit.getPlayerExact(placeholderPlayer)

            if (placeholder == null) {
                plugin.sms(
                    sender,
                    Messages.PLAYER_IS_NOT_ONLINE.message
                        .replaceText(PLAYER_REPLACER_BUILDER.replacement(placeholderPlayer).build())
                )
                return
            } else {
                if (placeholder.hasPermission("deluxemenus.placeholdersfor.exempt")) {
                    plugin.sms(
                        sender,
                        Messages.PLAYER_IS_EXEMPT.message
                            .replaceText(PLAYER_REPLACER_BUILDER.replacement(placeholderPlayer).build())
                    )

                    return
                }
            }
        }

        val menu = Menu.getMenuByName(arguments[0])

        if (menu == null) {
            plugin.sms(
                sender,
                Messages.INVALID_MENU.message
                    .replaceText(MENU_REPLACER_BUILDER.replacement(arguments[0]).build())
            )
            return
        }

        menu.openMenu(viewer, null, placeholder)
    }

    override fun onTabComplete(sender: CommandSender, arguments: List<String>): List<String>? {
        if (!sender.hasPermission(OPEN_COMMAND)) return null
        if (arguments.isEmpty()) return listOf(name)
        if (arguments.size > 4) return null

        if (arguments.size == 1) {
            if (arguments[0].isEmpty()) return listOf(name)

            if (name.startsWith(arguments[0], ignoreCase = true)) return listOf(name)

            return null
        }

        if (name != arguments[0]) return null

        val menuNames = Menu.getAllMenuNames()
        if (menuNames.isEmpty()) return null

        if (arguments.size == 2) {
            if (arguments[1].isEmpty()) return menuNames.toList()

            return menuNames.filter { it.startsWith(arguments[1], ignoreCase = true) }
        }

        val onlinePlayerNames = plugin.server.onlinePlayers.map { it.name }.toMutableList()

        if (arguments.size == 3) {
            val thirdArgument = arguments[2]

            if (thirdArgument.isEmpty()) {
                onlinePlayerNames.add("-p:")
                return onlinePlayerNames
            }

            if (!thirdArgument.startsWith("-")) {
                return onlinePlayerNames.filter { it.startsWith(thirdArgument, ignoreCase = true) }
            }

            return onlinePlayerNames.map { "-p:$it" }.filter { it.startsWith(thirdArgument, ignoreCase = true) }
        }

        if (arguments.size == 4) {
            if (!arguments[2].startsWith("-p:", ignoreCase = true)) return null

            val fourthArgument = arguments[3]
            if (fourthArgument.isEmpty()) return onlinePlayerNames

            return onlinePlayerNames.filter { it.startsWith(fourthArgument, ignoreCase = true) }
        }

        return null
    }

    companion object {
        private const val OPEN_COMMAND = "deluxemenus.open"
    }
}
