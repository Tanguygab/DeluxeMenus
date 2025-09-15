package com.extendedclip.deluxemenus.command.subcommand

import com.extendedclip.deluxemenus.DeluxeMenus
import com.extendedclip.deluxemenus.action.ActionType
import com.extendedclip.deluxemenus.action.ClickAction
import com.extendedclip.deluxemenus.action.ClickActionTask
import com.extendedclip.deluxemenus.config.DeluxeMenusConfig
import com.extendedclip.deluxemenus.menu.Menu
import com.extendedclip.deluxemenus.menu.MenuHolder
import com.extendedclip.deluxemenus.utils.Messages
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import java.util.regex.Pattern

class ExecuteCommand(plugin: DeluxeMenus) : SubCommand(plugin, "execute") {

    override fun execute(sender: CommandSender, arguments: List<String>) {
        if (!sender.isOp) {
            plugin.sms(sender, Messages.NO_PERMISSION)
            return
        }

        if (arguments.size < 2) {
            plugin.sms(sender, Messages.WRONG_USAGE_EXECUTE_COMMAND)
            return
        }

        val target = Bukkit.getPlayerExact(arguments[0])
        if (target == null) {
            plugin.sms(
                sender,
                Messages.PLAYER_IS_NOT_ONLINE.message
                    .replaceText(PLAYER_REPLACER_BUILDER.replacement(arguments[1]).build())
            )
            return
        }

        var executable = arguments.subList(1, arguments.size).joinToString(" ")

        val type = ActionType.getByStart(executable)

        if (type == null) {
            plugin.sms(sender, Messages.WRONG_ACTION_TYPE)
            return
        }

        executable = executable.replaceFirst(type.identifier, "")

        val action = ClickAction(type, executable)

        val d = DeluxeMenusConfig.DELAY_MATCHER.matcher(executable)

        if (d.find()) {
            action.delay = d.group(1)
            executable = executable.replaceFirst(d.group(), "")
        }

        val ch = DeluxeMenusConfig.CHANCE_MATCHER.matcher(executable)

        if (ch.find()) {
            action.chance = ch.group(1)
            executable = executable.replaceFirst(Pattern.quote(ch.group()).toRegex(), "")
        }

        action.executable = executable

        val holder: MenuHolder = Menu.getMenuHolder(target) ?: MenuHolder(plugin, target)

        if (!action.checkChance(holder)) {
            plugin.sms(sender, Messages.CHANCE_FAIL)
            return
        }

        val actionTask = ClickActionTask(
            plugin,
            target.uniqueId,
            action.type,
            action.executable,
            holder.typedArgs!!,
            parsePlaceholdersInArguments = true,
            parsePlaceholdersAfterArguments = true
        )

        if (action.hasDelay()) {
            actionTask.runTaskLater(plugin, action.getDelay(holder))

            plugin.sms(
                sender,
                Messages.ACTION_TO_BE_EXECUTED.message
                    .replaceText(AMOUNT_REPLACER_BUILDER.replacement(action.getDelay(holder).toString()).build())
            )
            return
        }

        actionTask.runTask(plugin)

        plugin.sms(
            sender,
            Messages.ACTION_EXECUTED_FOR.message
                .replaceText(PLAYER_REPLACER_BUILDER.replacement(target.name).build())
        )
    }

    override fun onTabComplete(sender: CommandSender, arguments: List<String>): List<String>? {
        if (!sender.isOp) return null
        if (arguments.isEmpty()) return listOf(name)
        if (arguments.size > 2) return null

        if (arguments.size == 1) {
            if (arguments[0].isEmpty()) return listOf(name)
            if (name.startsWith(arguments[0], ignoreCase = true)) return listOf(name)
            return null
        }

        if (name != arguments[0].lowercase()) {
            return null
        }

        return getPlayerNameCompletion(arguments[1])
    }
}
