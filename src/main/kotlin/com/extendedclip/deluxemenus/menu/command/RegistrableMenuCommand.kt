package com.extendedclip.deluxemenus.menu.command

import com.extendedclip.deluxemenus.DeluxeMenus
import com.extendedclip.deluxemenus.menu.Menu
import com.extendedclip.deluxemenus.utils.DebugLevel
import me.clip.placeholderapi.util.Msg
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandMap
import org.bukkit.command.CommandSender
import org.bukkit.command.SimpleCommandMap
import org.bukkit.entity.Player
import java.lang.reflect.Field
import java.util.logging.Level

class RegistrableMenuCommand(
    private val plugin: DeluxeMenus,
    var menu: Menu?
) : Command(if (menu!!.options.commands.isEmpty()) menu.options.name else menu.options.commands.first()) {
    private var registered = false
    private var unregistered = false

    init {
        if (menu!!.options.commands.size > 1) {
            setAliases(menu!!.options.commands.subList(1, menu!!.options.commands.size))
        }
    }

    override fun execute(sender: CommandSender, commandLabel: String, typedArgs: Array<String>): Boolean {
        check(!unregistered) { "This command was unregistered!" }

        if (sender !is Player) {
            Msg.msg(sender, "Menus can only be opened by players!")
            return true
        }
        val menu = menu!!
        var argMap: MutableMap<String, String>? = null

        if (!menu.options.arguments.isEmpty()) {
            plugin.debug(DebugLevel.LOWEST, Level.INFO, "has args")
            if (typedArgs.size < menu.options.arguments.size) {
                if (menu.options.argumentsUsageMessage != null) {
                    Msg.msg(sender, menu.options.argumentsUsageMessage!!)
                }
                return true
            }
            argMap = mutableMapOf()
            var index = 0
            for (arg in menu.options.arguments) {
                if (index + 1 == menu.options.arguments.size) {
                    val last = typedArgs.slice(IntRange(index, typedArgs.size)).joinToString(" ")
                    plugin.debug(DebugLevel.LOWEST, Level.INFO, "arg: $arg => $last")
                    argMap[arg] = last
                } else {
                    argMap[arg] = typedArgs[index]
                    plugin.debug(DebugLevel.LOWEST, Level.INFO, "arg: " + arg + " => " + typedArgs[index])
                }
                index++
            }
        }

        plugin.debug(DebugLevel.LOWEST, Level.INFO, "opening menu: " + menu.options.name)
        menu.openMenu(sender, argMap, null)
        return true
    }

    fun register() {
        check(!registered) { "This command was already registered!" }

        registered = true

        if (commandMap == null) {
            try {
                val f = plugin.server.javaClass.getDeclaredField("commandMap")
                f.setAccessible(true)
                commandMap = f.get(plugin.server) as CommandMap?
            } catch (e: Exception) {
                plugin.printStacktrace("Something went wrong while trying to register command: $name", e)
                return
            }
        }

        val registered: Boolean = commandMap!!.register(FALLBACK_PREFIX, this)
        if (registered) {
            plugin.debug(
                DebugLevel.LOW,
                Level.INFO,
                "Registered command: $name for menu: " + menu!!.options.name
            )
        }
    }

    fun unregister() {
        check(registered) { "This command was not registered!" }

        check(!unregistered) { "This command was already unregistered!" }

        unregistered = true

        if (commandMap == null) {
            menu = null
            return
        }

        val cMap: Field?
        val knownCommands: Field?
        try {
            cMap = Bukkit.getServer().javaClass.getDeclaredField("commandMap")
            cMap.setAccessible(true)
            knownCommands = SimpleCommandMap::class.java.getDeclaredField("knownCommands")
            knownCommands.setAccessible(true)

            val knownCommandsMap = knownCommands.get(cMap.get(Bukkit.getServer())) as MutableMap<*, *>

            // We need to remove every single alias because CommandMap#register() adds them all to the map.
            // If we do not remove them, then we will have dangling references to the command.
            knownCommandsMap.remove(name)
            knownCommandsMap.remove("$FALLBACK_PREFIX:$name")

            for (alias in aliases) {
                knownCommandsMap.remove(alias)
                knownCommandsMap.remove("$FALLBACK_PREFIX:$alias")
            }

            val unregistered = unregister((cMap.get(Bukkit.getServer()) as CommandMap?)!!)
            unregister(commandMap!!)
            if (unregistered) {
                plugin.debug(
                    DebugLevel.HIGH,
                    Level.INFO,
                    "Successfully unregistered command: $name"
                )
            } else {
                plugin.debug(
                    DebugLevel.HIGHEST,
                    Level.WARNING,
                    "Failed to unregister command: $name"
                )
            }
        } catch (exception: Exception) {
            plugin.printStacktrace(
                "Something went wrong while trying to unregister command: $name",
                exception
            )
        }

        menu = null
    }

    companion object {
        private val FALLBACK_PREFIX = "DeluxeMenus".lowercase()
        private var commandMap: CommandMap? = null
    }
}
