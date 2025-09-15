package com.extendedclip.deluxemenus.action

import com.extendedclip.deluxemenus.DeluxeMenus
import com.extendedclip.deluxemenus.menu.Menu
import com.extendedclip.deluxemenus.persistentmeta.PersistentMetaHandler
import com.extendedclip.deluxemenus.utils.*
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.scheduler.BukkitRunnable
import java.util.UUID
import java.util.logging.Level

class ClickActionTask(
    private val plugin: DeluxeMenus,
    private val uuid: UUID,
    private val actionType: ActionType,
    private val exec: String,
// Ugly hack to get around the fact that arguments are not available at task execution time
    private val arguments: MutableMap<String, String>?,
    private val parsePlaceholdersInArguments: Boolean,
    private val parsePlaceholdersAfterArguments: Boolean
) : BukkitRunnable() {
    override fun run() {
        val player = Bukkit.getPlayer(this.uuid) ?: return

        val holder = Menu.getMenuHolder(player)
        val target = holder?.placeholderPlayer ?: player

        val executable = StringUtils.replacePlaceholdersAndArguments(
            exec,
            arguments,
            target,
            parsePlaceholdersInArguments,
            parsePlaceholdersAfterArguments
        )

        when (actionType) {
            ActionType.META -> {
                if (!VersionHelper.IS_PDC_VERSION || plugin.persistentMetaHandler == null) {
                    plugin.debug(DebugLevel.HIGHEST, Level.INFO, "Meta action not supported on this server version.")
                    return
                }
                val result = plugin.persistentMetaHandler!!.parseAndExecuteMetaActionFromString(player, executable)
                when (result) {
                    PersistentMetaHandler.OperationResult.INVALID_SYNTAX -> plugin.debug(
                        DebugLevel.HIGHEST,
                        Level.INFO,
                        "Invalid meta action! Make sure you have the right syntax."
                    )

                    PersistentMetaHandler.OperationResult.NEW_VALUE_IS_DIFFERENT_TYPE -> plugin.debug(
                        DebugLevel.HIGHEST,
                        Level.INFO,
                        "Invalid meta action! New value is a different type than the old value!"
                    )

                    PersistentMetaHandler.OperationResult.INVALID_TYPE -> plugin.debug(
                        DebugLevel.HIGHEST,
                        Level.INFO,
                        "Invalid meta action! The specified type is not supported for the specified action!"
                    )

                    PersistentMetaHandler.OperationResult.EXISTENT_VALUE_IS_DIFFERENT_TYPE -> plugin.debug(
                        DebugLevel.HIGHEST,
                        Level.INFO,
                        "Invalid meta action! Existent value is a different type than the new value!"
                    )

                    PersistentMetaHandler.OperationResult.VALUE_NOT_FOUND, PersistentMetaHandler.OperationResult.SUCCESS -> {}
                }
            }

            ActionType.PLAYER, ActionType.PLAYER_COMMAND_EVENT -> player.chat("/$executable")
            ActionType.PLACEHOLDER -> holder?.setPlaceholders(executable)
            ActionType.CHAT -> player.chat(executable)
            ActionType.CONSOLE -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), executable)
            ActionType.MINI_MESSAGE -> plugin
                .audiences()
                .player(player)
                .sendMessage(MiniMessage.miniMessage().deserialize(executable))

            ActionType.MINI_BROADCAST -> plugin
                .audiences()
                .all()
                .sendMessage(MiniMessage.miniMessage().deserialize(executable))

            ActionType.MESSAGE -> player.sendMessage(StringUtils.color(executable))
            ActionType.LOG -> {
                val logParts = executable.split(" ", limit = 2)

                if (logParts.isEmpty() || logParts[0].isBlank()) {
                    plugin.debug(DebugLevel.HIGHEST, Level.WARNING, "LOG command requires at least a message")
                    return
                }

                val logLevel: Level
                val message: String

                if (logParts.size == 1) {
                    logLevel = Level.INFO
                    message = logParts[0]
                } else {
                    message = logParts[1]

                    logLevel = try {
                        Level.parse(logParts[0].uppercase())
                    } catch (_: IllegalArgumentException) {
                        plugin.debug(
                            DebugLevel.HIGHEST,
                            Level.WARNING,
                            "Log level " + logParts[0] + " is not a valid log level! Using INFO instead."
                        )
                        Level.INFO
                    }
                }

                plugin.logger.log(logLevel, "[${holder?.menuName ?: "Unknown Menu"}]: $message")
            }

            ActionType.BROADCAST -> Bukkit.broadcastMessage(StringUtils.color(executable))
            ActionType.CLOSE -> Menu.closeMenu(plugin, player, true, true)
            ActionType.OPEN_GUI_MENU, ActionType.OPEN_MENU -> {
                val temporaryExecutable = executable.replace("\\s+".toRegex(), " ").replace("  ", " ")
                val executableParts = temporaryExecutable.split(" ", limit = 2)

                if (executableParts.isEmpty()) {
                    plugin.debug(DebugLevel.HIGHEST, Level.WARNING, "Could not find and open menu $executable")
                    return
                }

                val menuName = executableParts[0]

                val menuToOpen = Menu.getMenuByName(menuName)

                if (menuToOpen == null) {
                    plugin.debug(DebugLevel.HIGHEST, Level.WARNING, "Could not find and open menu $executable")
                    return
                }

                val menuArgumentNames = menuToOpen.options.arguments

                val passedArgumentValues = if (executableParts.size > 1) executableParts[1].split(" ") else null

                if (menuArgumentNames.isEmpty()) {
                    if (!passedArgumentValues.isNullOrEmpty()) {
                        plugin.debug(
                            DebugLevel.HIGHEST,
                            Level.WARNING,
                            "Arguments were given for menu $menuName in action [openguimenu] or [openmenu], but the menu does not support arguments!"
                        )
                    }

                    menuToOpen.openMenu(player, holder?.typedArgs, holder?.placeholderPlayer)
                    return
                }

                if (passedArgumentValues.isNullOrEmpty()) {
                    // Replicate old behavior: If no arguments are given, open the menu with the arguments from the current menu
                    menuToOpen.openMenu(player, holder?.typedArgs, holder?.placeholderPlayer)
                    return
                }

                if (passedArgumentValues.size < menuArgumentNames.size) {
                    plugin.debug(
                        DebugLevel.HIGHEST,
                        Level.WARNING,
                        "Not enough arguments given for menu $menuName when opening using the [openguimenu] or [openmenu] action!"
                    )
                    return
                }

                val argumentsMap = mutableMapOf<String, String>()
                if (holder?.typedArgs != null) {
                    // Pass the arguments from the current menu to the new menu. If the new menu has arguments with the
                    // same name, they will be overwritten
                    argumentsMap.putAll(holder.typedArgs!!)
                }

                for (index in 0 ..< menuArgumentNames.size) {
                    val argumentName = menuArgumentNames[index]

                    if (passedArgumentValues.size <= index) {
                        // This should never be the case!
                        plugin.debug(
                            DebugLevel.HIGHEST,
                            Level.WARNING,
                            "Not enough arguments given for menu $menuName when opening using the [openguimenu] or [openmenu] action!"
                        )
                        break
                    }

                    if (menuArgumentNames.size == index + 1) {
                        // If this is the last argument, get all remaining values and join them
                        val lastArgumentValue = passedArgumentValues.subList(index, passedArgumentValues.size).joinToString(" ")
                        argumentsMap[argumentName] = lastArgumentValue
                        break
                    }

                    argumentsMap[argumentName] = passedArgumentValues[index]
                }


                menuToOpen.openMenu(player, argumentsMap, holder?.placeholderPlayer)
            }

            ActionType.CONNECT -> plugin.connect(player, executable)
            ActionType.JSON_MESSAGE -> AdventureUtils.sendJson(plugin, player, executable)
            ActionType.JSON_BROADCAST, ActionType.BROADCAST_JSON -> plugin
                .audiences()
                .all()
                .sendMessage(AdventureUtils.fromJson(executable))

            ActionType.REFRESH -> {
                if (holder == null) {
                    plugin.debug(
                        DebugLevel.MEDIUM,
                        Level.WARNING,
                        player.name + " does not have menu open! Nothing to refresh!"
                    )
                    return
                }

                holder.refreshMenu()
            }

            ActionType.TAKE_MONEY -> {
                if (plugin.vault == null || !plugin.vault!!.hooked()) {
                    plugin.debug(DebugLevel.HIGHEST, Level.WARNING, "Vault not hooked! Cannot take money!")
                    return
                }

                try {
                    plugin.vault!!.takeMoney(player, executable.toDouble())
                } catch (_: NumberFormatException) {
                    plugin.debug(
                        DebugLevel.HIGHEST,
                        Level.WARNING,
                        "Amount for take money action: $executable, is not a valid number!"
                    )
                }
            }

            ActionType.GIVE_MONEY -> {
                if (plugin.vault == null || !plugin.vault!!.hooked()) {
                    plugin.debug(DebugLevel.HIGHEST, Level.WARNING, "Vault not hooked! Cannot give money!")
                    return
                }

                try {
                    plugin.vault!!.giveMoney(player, executable.toDouble())
                } catch (_: NumberFormatException) {
                    plugin.debug(
                        DebugLevel.HIGHEST,
                        Level.WARNING,
                        "Amount for give money action: $executable, is not a valid number!"
                    )
                }
            }

            ActionType.TAKE_EXP, ActionType.GIVE_EXP -> {
                val lowerCaseExecutable = executable.lowercase()

                try {
                    if (lowerCaseExecutable.replace("l", "").toInt() <= 0) return

                    if (actionType == ActionType.TAKE_EXP) {
                        ExpUtils.setExp(player, "-$lowerCaseExecutable")
                        return
                    }

                    ExpUtils.setExp(player, lowerCaseExecutable)
                    return
                } catch (_: NumberFormatException) {
                    if (actionType == ActionType.TAKE_EXP) {
                        plugin.debug(
                            DebugLevel.HIGHEST,
                            Level.WARNING,
                            "Amount for take exp action: $executable, is not a valid number!"
                        )
                        return
                    }

                    plugin.debug(
                        DebugLevel.HIGHEST,
                        Level.WARNING,
                        "Amount for give exp action: $executable, is not a valid number!"
                    )
                    return
                }
            }

            ActionType.GIVE_PERM -> {
                if (plugin.vault == null || !plugin.vault!!.hooked()) {
                    plugin.debug(
                        DebugLevel.HIGHEST,
                        Level.WARNING,
                        "Vault not hooked! Cannot give permission: $executable!"
                    )
                    return
                }

                plugin.vault!!.givePermission(player, executable)
            }

            ActionType.TAKE_PERM -> {
                if (plugin.vault == null || !plugin.vault!!.hooked()) {
                    plugin.debug(
                        DebugLevel.HIGHEST,
                        Level.WARNING,
                        "Vault not hooked! Cannot take permission: $executable!"
                    )
                    return
                }

                plugin.vault!!.takePermission(player, executable)
            }

            ActionType.BROADCAST_SOUND, ActionType.BROADCAST_WORLD_SOUND, ActionType.PLAY_SOUND -> {
                val sound: Sound
                var volume = 1f
                var pitch = 1f

                if (!executable.contains(" ")) {
                    try {
                        sound = SoundUtils.getSound(executable.uppercase())
                    } catch (e: Exception) {
                        plugin.printStacktrace("Sound name given for sound action: $executable, is not a valid sound!", e)
                        return
                    }
                } else {
                    val parts = executable.split(" ", limit = 3)

                    try {
                        sound = SoundUtils.getSound(parts[0].uppercase())
                    } catch (e: Exception) {
                        plugin.printStacktrace("Sound name given for sound action: " + parts[0] + ", is not a valid sound!", e)
                        return
                    }

                    if (parts.size == 3) {
                        try {
                            pitch = parts[2].toFloat()
                        } catch (exception: NumberFormatException) {
                            plugin.debug(
                                DebugLevel.HIGHEST,
                                Level.WARNING,
                                "Pitch given for sound action: " + parts[2] + ", is not a valid number!"
                            )

                            plugin.printStacktrace(
                                "Pitch given for sound action: " + parts[2] + ", is not a valid number!",
                                exception
                            )
                        }
                    }


                    try {
                        volume = parts[1].toFloat()
                    } catch (exception: NumberFormatException) {
                        plugin.debug(
                            DebugLevel.HIGHEST,
                            Level.WARNING,
                            "Volume given for sound action: " + parts[1] + ", is not a valid number!"
                        )

                        plugin.printStacktrace(
                            "Volume given for sound action: " + parts[1] + ", is not a valid number!",
                            exception
                        )
                    }
                }

                when (actionType) {
                    ActionType.BROADCAST_SOUND -> for (broadcastTarget in Bukkit.getOnlinePlayers()) {
                        broadcastTarget.playSound(broadcastTarget.location, sound, volume, pitch)
                    }

                    ActionType.BROADCAST_WORLD_SOUND -> for (broadcastTarget in player.world.players) {
                        broadcastTarget.playSound(broadcastTarget.location, sound, volume, pitch)
                    }

                    ActionType.PLAY_SOUND -> player.playSound(player.location, sound, volume, pitch)
                    else -> {}
                }
            }

        }
    }
}