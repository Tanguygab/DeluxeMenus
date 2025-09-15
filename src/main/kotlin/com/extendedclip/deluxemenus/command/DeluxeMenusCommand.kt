package com.extendedclip.deluxemenus.command

import com.extendedclip.deluxemenus.DeluxeMenus
import com.extendedclip.deluxemenus.command.subcommand.*
import com.extendedclip.deluxemenus.utils.Messages
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor

class DeluxeMenusCommand(private val plugin: DeluxeMenus) : TabExecutor {
    private val subCommands = mutableMapOf<String, SubCommand>()

    fun register(): Boolean {
        val command = this.plugin.getCommand("deluxemenus") ?: return false

        command.setExecutor(this)
        registerSubCommands()
        return true
    }

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        arguments: Array<String>
    ): Boolean {

        if (arguments.isEmpty()) {
            plugin.sms(
                sender,
                Messages.PLUGIN_VERSION.message
                    .replaceText(VERSION_REPLACER_BUILDER.replacement(plugin.description.version).build())
                    .replaceText(
                        AUTHORS_REPLACER_BUILDER.replacement(
                            plugin.description.authors.stream().map {
                                Component.text(
                                    it,
                                    NamedTextColor.WHITE
                                )
                            }.collect(Component.toComponent(Component.text(", ", NamedTextColor.GRAY)))
                        ).build()
                    )
            )
            return true
        }

        val subCommand = subCommands[arguments[0].lowercase()]

        if (subCommand != null) {
            subCommand.execute(sender, arguments.slice(IntRange(1, arguments.size - 1)))
            return true
        }

        plugin.sms(sender, Messages.WRONG_USAGE)
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        arguments: Array<String>
    ): List<String> {

        return subCommands.values
            .mapNotNull { it.onTabComplete(sender, arguments.toList()) }
            .flatMap { it }
    }

    private fun registerSubCommands() {
        listOf(
            DumpCommand(plugin),
            ExecuteCommand(plugin),
            HelpCommand(plugin),
            ListCommand(plugin),
            MetaCommand(plugin),
            OpenCommand(plugin),
            RefreshCommand(plugin),
            ReloadCommand(plugin)
        ).forEach { subCommands[it.name] = it }
    }

    companion object {
        private val VERSION_REPLACER_BUILDER = TextReplacementConfig.builder().matchLiteral("<version>")
        private val AUTHORS_REPLACER_BUILDER = TextReplacementConfig.builder().matchLiteral("<authors>")
    }
}
