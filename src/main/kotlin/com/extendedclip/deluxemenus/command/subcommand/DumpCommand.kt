package com.extendedclip.deluxemenus.command.subcommand

import com.extendedclip.deluxemenus.DeluxeMenus
import com.extendedclip.deluxemenus.menu.Menu
import com.extendedclip.deluxemenus.utils.DumpUtils
import com.extendedclip.deluxemenus.utils.Messages
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import org.bukkit.command.CommandSender

class DumpCommand(plugin: DeluxeMenus) : SubCommand(plugin, "dump") {

    override fun execute(sender: CommandSender, arguments: List<String>) {
        if (!sender.hasPermission(ADMIN_PERMISSION)) {
            plugin.sms(sender, Messages.NO_PERMISSION)
            return
        }

        if (arguments.size != 1) {
            plugin.sms(sender, Messages.WRONG_USAGE_DUMP_COMMAND)
            return
        }

        var dump = ""
        try {
            dump = DumpUtils.createDump(plugin, arguments[0])
        } catch (_: RuntimeException) {}

        if (dump.isBlank()) {
            plugin.sms(sender, Messages.DUMP_FAILED)
            return
        }

        DumpUtils.postDump(dump).whenComplete { result: Any?, error: Throwable? ->
            if (error != null) {
                plugin.printStacktrace("Something went wrong while trying to create and post a dump!", error)
                plugin.sms(sender, Messages.DUMP_FAILED)
                return@whenComplete
            }
            val link = Component.text(DumpUtils.URL + result.toString()).clickEvent(ClickEvent.openUrl(DumpUtils.URL + result.toString()))
            plugin.sms(sender, Messages.DUMP_SUCCESS.message.append(link))
        }
    }

    override fun onTabComplete(sender: CommandSender, arguments: List<String>): List<String>? {
        if (!sender.hasPermission(ADMIN_PERMISSION)) return null
        if (arguments.isEmpty()) return listOf(name)
        if (arguments.size > 2) return null

        if (arguments.size == 1) {
            if (arguments[0].isEmpty()) return listOf(name)

            if (name.startsWith(arguments[0], ignoreCase = true)) return listOf(name)

            return null
        }

        val firstArgument = arguments[0].lowercase()
        if (name != firstArgument) return null

        val secondArgument = arguments[1]

        val completions = Menu.getAllMenuNames().toMutableList()
        completions.add("config")

        if (secondArgument.isEmpty()) return completions

        return completions.filter { it.startsWith(secondArgument, ignoreCase = true) }
    }

}