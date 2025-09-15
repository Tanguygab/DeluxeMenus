package com.extendedclip.deluxemenus.command.subcommand

import com.extendedclip.deluxemenus.DeluxeMenus
import com.extendedclip.deluxemenus.persistentmeta.DataAction
import com.extendedclip.deluxemenus.persistentmeta.DataAction.Companion.getActionByName
import com.extendedclip.deluxemenus.persistentmeta.DataType
import com.extendedclip.deluxemenus.persistentmeta.PersistentMetaHandler
import com.extendedclip.deluxemenus.utils.Messages
import com.extendedclip.deluxemenus.utils.PaginationUtils
import com.extendedclip.deluxemenus.utils.StringUtils
import com.extendedclip.deluxemenus.utils.VersionHelper
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.TreeMap

class MetaCommand(plugin: DeluxeMenus) : SubCommand(plugin, "meta") {

    override fun execute(sender: CommandSender, arguments: List<String>) {
        if (!sender.hasPermission(META_COMMAND)) {
            plugin.sms(sender, Messages.NO_PERMISSION)
            return
        }

        if (!VersionHelper.IS_PDC_VERSION || plugin.persistentMetaHandler == null) {
            plugin.sms(sender, Messages.META_NOT_SUPPORTED)
            return
        }

        if (arguments.size < 2) {
            sendWrongUsageMessage(sender, null)
            return
        }

        val target = Bukkit.getPlayerExact(arguments[0])

        if (target == null) {
            plugin.sms(
                sender,
                Messages.PLAYER_IS_NOT_ONLINE.message
                    .replaceText(PLAYER_REPLACER_BUILDER.replacement(arguments[0]).build())
            )
            return
        }

        val actionName = StringUtils.replacePlaceholders(arguments[1], target)
        val action = getActionByName(actionName)

        if (action == null) {
            if (actionName.equals("list", ignoreCase = true)) {
                handleListMeta(sender, target, arguments.subList(2, arguments.size))
                return
            }

            if (actionName.equals("show", ignoreCase = true)) {
                handleShowMeta(sender, target, arguments.subList(2, arguments.size))
                return
            }

            sendWrongUsageMessage(sender, null)
            return
        }

        if (arguments.size < 3) {
            sendWrongUsageMessage(sender, action)
            return
        }

        val keyName = StringUtils.replacePlaceholders(arguments[2], target)
        val namespacedKey = plugin.persistentMetaHandler!!.getKey(keyName)
        if (namespacedKey == null) {
            plugin.sms(
                sender,
                Messages.META_KEY_INVALID.message.replaceText(KEY_REPLACER_BUILDER.replacement(keyName).build())
            )
            return
        }

        val context = mutableMapOf<ContextKeys, String>()
        context[ContextKeys.KEY_NAME] = keyName

        if (action == DataAction.SWITCH) {
            handleSwitchMeta(sender, target, namespacedKey, context)
            return
        }

        if (arguments.size < 4) {
            sendWrongUsageMessage(sender, action)
            return
        }

        val typeName = StringUtils.replacePlaceholders(arguments[3], target).uppercase()
        val type = DataType.getSupportedTypeByName(typeName)
        if (type == null) {
            plugin.sms(
                sender,
                Messages.META_TYPE_UNSUPPORTED.message
                    .replaceText(TYPE_REPLACER_BUILDER.replacement(typeName).build())
            )
            return
        }

        context[ContextKeys.TYPE_NAME] = typeName

        if (action == DataAction.REMOVE) {
            handleRemoveMeta(sender, target, namespacedKey, type, context)
            return
        }

        if (arguments.size < 5) {
            sendWrongUsageMessage(sender, action)
            return
        }

        val value = StringUtils.replacePlaceholders(arguments.subList(4, arguments.size).joinToString(" "), target)

        @Suppress("UNCHECKED_CAST")
        when (action) {
            DataAction.SET -> handleSetMeta(sender, target, namespacedKey, type as DataType<*, Any>, value, context)
            DataAction.ADD -> handleAddMeta(sender, target, namespacedKey, type, value, context)
            DataAction.SUBTRACT -> handleSubtractMeta(sender, target, namespacedKey, type, value, context)
            else -> sendWrongUsageMessage(sender, action)
        }
    }

    override fun onTabComplete(sender: CommandSender, arguments: List<String>): List<String>? {
        if (!sender.hasPermission(META_COMMAND) || !VersionHelper.IS_PDC_VERSION || plugin.persistentMetaHandler == null) {
            return null
        }

        if (arguments.isEmpty()) return listOf(name)
        if (arguments.size > 5) return null

        if (arguments.size == 1) {
            val firstArgument = arguments[0].lowercase()
            if (firstArgument.isEmpty() || name.startsWith(firstArgument)) return listOf(name)
            return null
        }

        val firstArgument = arguments[0].lowercase()
        if (name != firstArgument) return null

        return when (arguments.size) {
            2 -> getPlayerNameCompletion(arguments[1])
            3 -> {
                val thirdArgument = arguments[2].lowercase()
                if (thirdArgument.isEmpty()) return SUB_COMMANDS

                SUB_COMMANDS.filter { it.startsWith(thirdArgument) }
            }
            4 -> {
                val action = arguments[2].lowercase()
                if (action != "list") return null

                val fourthArgument = arguments[3]

                if (fourthArgument.isEmpty()) return DataType.getSupportedTypeNames().toList()

                DataType.getSupportedTypeNames().filter { it.startsWith(fourthArgument.uppercase()) }
            }
            5 -> {
                val actionName = arguments[2].lowercase()
                val action = getActionByName(actionName)

                if (actionName.equals("list", ignoreCase = true) || action == DataAction.SWITCH) {
                    return null
                }

                val fifthArgument = arguments[4]
                if (fifthArgument.isEmpty()) return DataType.getSupportedTypeNames().toList()

                DataType.getSupportedTypeNames().filter { it.startsWith(fifthArgument.uppercase()) }
            }
            else -> null
        }
    }

    private fun handleListMeta(
        sender: CommandSender,
        target: Player,
        arguments: List<String>
    ) {
        if (arguments.isEmpty()) {
            plugin.sms(sender, Messages.WRONG_USAGE_META_LIST_COMMAND)
            return
        }

        val typeName = arguments[0].uppercase()
        val type = DataType.getSupportedTypeByName(typeName)
        if (type == null) {
            plugin.sms(
                sender,
                Messages.META_TYPE_UNSUPPORTED.message
                    .replaceText(TYPE_REPLACER_BUILDER.replacement(typeName).build())
            )
            return
        }

        val metas = TreeMap<String, Any>(String.CASE_INSENSITIVE_ORDER)
        metas.putAll(plugin.persistentMetaHandler!!.getMetaValues(target, type))

        if (metas.isEmpty()) {
            plugin.sms(
                sender, Messages.NO_META_VALUES.message
                    .replaceText(TYPE_REPLACER_BUILDER.replacement(typeName).build())
                    .replaceText(PLAYER_REPLACER_BUILDER.replacement(target.name).build())
            )
            return
        }

        val itemsPerPage = plugin.generalConfig.metasListPageSize
        val itemsCount = metas.size
        val pagesCount = PaginationUtils.getPagesCount(itemsPerPage, itemsCount)

        val page = PaginationUtils.parsePage(
            itemsPerPage,
            itemsCount,
            pagesCount,
            if (arguments.size < 2) null else arguments[1]
        )

        val pageItems = mutableMapOf<String, Any>()

        val start = (page - 1) * itemsPerPage
        val end = start + itemsPerPage

        var index = 0
        for (entry in metas.entries) {
            if (index >= end) {
                break
            }

            if (index < start) {
                index++
                continue
            }

            pageItems[entry.key] = entry.value
            index++
        }

        val pageItemsCount = pageItems.size

        val list = Component.text()
            .append(
                Component.text(
                    "Page $page/$pagesCount - $pageItemsCount pairs:",
                    NamedTextColor.GOLD
                )
            )
            .append(Component.newline())
            .append(Component.newline())
            .append(Component.text("Key (String) - Value ($typeName)", NamedTextColor.GRAY))
            .append(Component.newline())

        val pairsList = pageItems.entries.stream()
            .map {
                Component.text(it.key, NamedTextColor.DARK_AQUA)
                    .append(Component.text(" - ", NamedTextColor.GRAY))
                    .append(Component.text(it.value.toString(), NamedTextColor.GREEN))
                    .append(Component.newline())
            }
            .collect(Component.toComponent())

        list.append(Component.newline())
            .append(pairsList)
            .append(Component.newline())
            .append(
                Component.text(
                    "Use /dm meta list $typeName <page> to view more values of this type",
                    NamedTextColor.GRAY
                )
            )
        plugin.sms(sender, list.build())
    }

    private fun handleShowMeta(
        sender: CommandSender,
        target: Player,
        arguments: List<String>
    ) {
        if (arguments.size < 2) {
            plugin.sms(sender, Messages.WRONG_USAGE_META_SHOW_COMMAND)
            return
        }

        val keyName = arguments[0]
        val pmh = plugin.persistentMetaHandler!!
        val namespacedKey = pmh.getKey(keyName)
        if (namespacedKey == null) {
            plugin.sms(
                sender,
                Messages.META_KEY_INVALID.message.replaceText(KEY_REPLACER_BUILDER.replacement(keyName).build())
            )
            return
        }

        val typeName = arguments[1].uppercase()
        val type = DataType.getSupportedTypeByName(typeName)
        if (type == null) {
            plugin.sms(
                sender,
                Messages.META_TYPE_UNSUPPORTED.message
                    .replaceText(TYPE_REPLACER_BUILDER.replacement(typeName).build())
            )
            return
        }

        val value: Any? = pmh.getMetaValue(target, namespacedKey, type)

        val message = if (value == null) Messages.NO_META_VALUE.message
        else Messages.META_VALUE_FOUND.message.replaceText(VALUE_REPLACER_BUILDER.replacement(value.toString()).build())

        plugin.sms(sender, message
            .replaceText(KEY_REPLACER_BUILDER.replacement(keyName).build())
            .replaceText(TYPE_REPLACER_BUILDER.replacement(typeName).build())
            .replaceText(PLAYER_REPLACER_BUILDER.replacement(target.name).build())
        )
    }

    private fun handleSetMeta(
        sender: CommandSender, target: Player,
        namespacedKey: NamespacedKey,
        type: DataType<*, Any>,
        value: String,
        context: Map<ContextKeys, String>
    ) {
        val pmh = plugin.persistentMetaHandler!!
        val parsedValue = pmh.parseValueByType(type, value)
        if (parsedValue == null) {
            plugin.sms(
                sender, Messages.META_VALUE_TYPE_MISMATCH.message
                    .replaceText(VALUE_REPLACER_BUILDER.replacement(value).build())
                    .replaceText(
                        TYPE_REPLACER_BUILDER.replacement(
                            context.getOrDefault(
                                ContextKeys.TYPE_NAME,
                                type.getComplexType().getSimpleName()
                            )
                        ).build()
                    )
            )
            return
        }

        val result = pmh.setMetaValue(target, namespacedKey, type, parsedValue)
        plugin.sms(sender, when (result) {
            PersistentMetaHandler.OperationResult.SUCCESS -> Messages
                .META_VALUE_SET
                .message
                .replaceText(
                    KEY_REPLACER_BUILDER.replacement(
                        context.getOrDefault(
                            ContextKeys.KEY_NAME,
                            namespacedKey.toString()
                        )
                    ).build()
                )
                .replaceText(
                    TYPE_REPLACER_BUILDER.replacement(
                        context.getOrDefault(
                            ContextKeys.TYPE_NAME,
                            type.getComplexType().getSimpleName()
                        )
                    ).build()
                )
                .replaceText(VALUE_REPLACER_BUILDER.replacement(parsedValue.toString()).build())
                .replaceText(PLAYER_REPLACER_BUILDER.replacement(target.name).build())

            PersistentMetaHandler.OperationResult.NEW_VALUE_IS_DIFFERENT_TYPE -> Messages
                .META_VALUE_TYPE_MISMATCH
                .message
                .replaceText(VALUE_REPLACER_BUILDER.replacement(parsedValue.toString()).build())
                .replaceText(
                    TYPE_REPLACER_BUILDER.replacement(
                        context.getOrDefault(
                            ContextKeys.TYPE_NAME,
                            type.getComplexType().getSimpleName()
                        )
                    ).build()
                )

            PersistentMetaHandler.OperationResult.EXISTENT_VALUE_IS_DIFFERENT_TYPE -> Messages
                .META_EXISTENT_VALUE_WRONG_TYPE
                .message
                .replaceText(
                    KEY_REPLACER_BUILDER.replacement(
                        context.getOrDefault(
                            ContextKeys.KEY_NAME,
                            namespacedKey.toString()
                        )
                    ).build()
                )

            else -> Messages.WRONG_USAGE_META_SET_COMMAND.message
        })
    }

    private fun handleRemoveMeta(
        sender: CommandSender, target: Player,
        namespacedKey: NamespacedKey,
        type: DataType<*, *>,
        context: Map<ContextKeys, String>
    ) {
        val result = plugin.persistentMetaHandler!!.removeMetaValue(target, namespacedKey, type)
        plugin.sms(sender, when (result) {
            PersistentMetaHandler.OperationResult.SUCCESS -> Messages
                .META_VALUE_REMOVED
                .message
                .replaceText(
                    KEY_REPLACER_BUILDER.replacement(
                        context.getOrDefault(
                            ContextKeys.KEY_NAME,
                            namespacedKey.toString()
                        )
                    ).build()
                )
                .replaceText(
                    TYPE_REPLACER_BUILDER.replacement(
                        context.getOrDefault(
                            ContextKeys.TYPE_NAME,
                            type.getComplexType().getSimpleName()
                        )
                    ).build()
                )
                .replaceText(PLAYER_REPLACER_BUILDER.replacement(target.name).build())

            PersistentMetaHandler.OperationResult.EXISTENT_VALUE_IS_DIFFERENT_TYPE -> Messages
                .META_EXISTENT_VALUE_WRONG_TYPE
                .message
                .replaceText(
                    KEY_REPLACER_BUILDER.replacement(
                        context.getOrDefault(
                            ContextKeys.KEY_NAME,
                            namespacedKey.toString()
                        )
                    ).build()
                )

            PersistentMetaHandler.OperationResult.VALUE_NOT_FOUND -> Messages
                .NO_META_VALUE
                .message
                .replaceText(
                    KEY_REPLACER_BUILDER.replacement(
                        context.getOrDefault(
                            ContextKeys.KEY_NAME,
                            namespacedKey.toString()
                        )
                    ).build()
                )
                .replaceText(
                    TYPE_REPLACER_BUILDER.replacement(
                        context.getOrDefault(
                            ContextKeys.TYPE_NAME,
                            type.getComplexType().getSimpleName()
                        )
                    ).build()
                )
                .replaceText(PLAYER_REPLACER_BUILDER.replacement(target.name).build())

            else -> Messages.WRONG_USAGE_META_REMOVE_COMMAND.message
        })
    }

    private fun handleSwitchMeta(
        sender: CommandSender, target: Player,
        namespacedKey: NamespacedKey, context: Map<ContextKeys, String>
    ) {
        val pmh = plugin.persistentMetaHandler!!
        val result = pmh.switchMetaValue(target, namespacedKey)

        plugin.sms(sender, when (result) {
            PersistentMetaHandler.OperationResult.SUCCESS -> {
                val newValue = pmh.getMetaValue(target, namespacedKey, DataType.BOOLEAN)

                Messages.META_VALUE_SWITCHED.message
                    .replaceText(
                        KEY_REPLACER_BUILDER.replacement(
                            context.getOrDefault(
                                ContextKeys.KEY_NAME,
                                namespacedKey.toString()
                            )
                        ).build()
                    )
                    .replaceText(NEW_VALUE_REPLACER_BUILDER.replacement(newValue.toString()).build())
                    .replaceText(PLAYER_REPLACER_BUILDER.replacement(target.name).build())
            }

            PersistentMetaHandler.OperationResult.EXISTENT_VALUE_IS_DIFFERENT_TYPE -> Messages
                .META_EXISTENT_VALUE_WRONG_TYPE
                .message
                .replaceText(
                    KEY_REPLACER_BUILDER.replacement(
                        context.getOrDefault(
                            ContextKeys.KEY_NAME,
                            namespacedKey.toString()
                        )
                    ).build()
                )

            else -> Messages.WRONG_USAGE_META_SWITCH_COMMAND.message
        })
    }

    private fun handleAddMeta(
        sender: CommandSender, target: Player,
        namespacedKey: NamespacedKey,
        type: DataType<*, *>,
        value: String,
        context: Map<ContextKeys, String>
    ) {
        val pmh = plugin.persistentMetaHandler!!
        val parsedValue = pmh.parseValueByType(type, value)

        if (parsedValue !is Number) {
            plugin.sms(
                sender, Messages.META_ADD_TYPE_MISMATCH.message
                    .replaceText(VALUE_REPLACER_BUILDER.replacement(value).build())
            )
            return
        }

        val result = pmh.addMetaValue(target, namespacedKey, type, parsedValue)

        plugin.sms(sender, when (result) {
            PersistentMetaHandler.OperationResult.SUCCESS -> {
                val newValue = pmh.getMetaValue(target, namespacedKey, type)

                Messages.META_VALUE_ADDED.message
                    .replaceText(
                        KEY_REPLACER_BUILDER.replacement(
                            context.getOrDefault(
                                ContextKeys.KEY_NAME,
                                namespacedKey.toString()
                            )
                        ).build()
                    )
                    .replaceText(
                        TYPE_REPLACER_BUILDER.replacement(
                            context.getOrDefault(
                                ContextKeys.TYPE_NAME,
                                type.getComplexType().getSimpleName()
                            )
                        ).build()
                    )
                    .replaceText(VALUE_REPLACER_BUILDER.replacement(parsedValue.toString()).build())
                    .replaceText(NEW_VALUE_REPLACER_BUILDER.replacement(newValue.toString()).build())
                    .replaceText(PLAYER_REPLACER_BUILDER.replacement(target.name).build())
            }

            PersistentMetaHandler.OperationResult.INVALID_TYPE -> Messages
                .META_ADD_TYPE_MISMATCH
                .message
                .replaceText(VALUE_REPLACER_BUILDER.replacement(parsedValue.toString()).build())

            PersistentMetaHandler.OperationResult.EXISTENT_VALUE_IS_DIFFERENT_TYPE -> Messages
                .META_EXISTENT_VALUE_WRONG_TYPE
                .message
                .replaceText(
                    KEY_REPLACER_BUILDER.replacement(
                        context.getOrDefault(
                            ContextKeys.KEY_NAME,
                            namespacedKey.toString()
                        )
                    ).build()
                )

            else -> Messages.WRONG_USAGE_META_ADD_COMMAND.message
        })
    }

    private fun handleSubtractMeta(
        sender: CommandSender, target: Player,
        namespacedKey: NamespacedKey,
        type: DataType<*, *>,
        value: String,
        context: Map<ContextKeys, String>
    ) {
        val pmh = plugin.persistentMetaHandler!!
        val parsedValue = pmh.parseValueByType(type, value)

        if (parsedValue !is Number) {
            plugin.sms(
                sender, Messages.META_SUBTRACT_TYPE_MISMATCH.message
                    .replaceText(VALUE_REPLACER_BUILDER.replacement(value).build())
            )
            return
        }

        val result = pmh.subtractMetaValue(target, namespacedKey, type, parsedValue)

        plugin.sms(sender, when (result) {
            PersistentMetaHandler.OperationResult.SUCCESS -> {
                val newValue = pmh.getMetaValue(target, namespacedKey, type)

                Messages.META_VALUE_SUBTRACTED.message
                    .replaceText(
                        KEY_REPLACER_BUILDER.replacement(
                            context.getOrDefault(
                                ContextKeys.KEY_NAME,
                                namespacedKey.toString()
                            )
                        ).build()
                    )
                    .replaceText(
                        TYPE_REPLACER_BUILDER.replacement(
                            context.getOrDefault(
                                ContextKeys.TYPE_NAME,
                                type.getComplexType().getSimpleName()
                            )
                        ).build()
                    )
                    .replaceText(VALUE_REPLACER_BUILDER.replacement(parsedValue.toString()).build())
                    .replaceText(NEW_VALUE_REPLACER_BUILDER.replacement(newValue.toString()).build())
                    .replaceText(PLAYER_REPLACER_BUILDER.replacement(target.name).build())
            }

            PersistentMetaHandler.OperationResult.INVALID_TYPE -> Messages
                .META_SUBTRACT_TYPE_MISMATCH
                .message
                .replaceText(VALUE_REPLACER_BUILDER.replacement(parsedValue.toString()).build())

            PersistentMetaHandler.OperationResult.EXISTENT_VALUE_IS_DIFFERENT_TYPE -> Messages
                .META_EXISTENT_VALUE_WRONG_TYPE
                .message
                .replaceText(
                    KEY_REPLACER_BUILDER.replacement(
                        context.getOrDefault(
                            ContextKeys.KEY_NAME,
                            namespacedKey.toString()
                        )
                    ).build()
                )
            else -> Messages.WRONG_USAGE_META_SUBTRACT_COMMAND.message
        })
    }

    private fun sendWrongUsageMessage(sender: CommandSender, action: DataAction?) {
        plugin.sms(sender, when (action) {
            DataAction.SET -> Messages.WRONG_USAGE_META_SET_COMMAND
            DataAction.REMOVE -> Messages.WRONG_USAGE_META_REMOVE_COMMAND
            DataAction.ADD -> Messages.WRONG_USAGE_META_ADD_COMMAND
            DataAction.SUBTRACT -> Messages.WRONG_USAGE_META_SUBTRACT_COMMAND
            DataAction.SWITCH -> Messages.WRONG_USAGE_META_SWITCH_COMMAND
            else -> Messages.WRONG_USAGE_META_COMMAND
        })
    }

    enum class ContextKeys {
        KEY_NAME,
        TYPE_NAME,
        VALUE,
    }

    companion object {
        private val SUB_COMMANDS = listOf("list", "show", "set", "remove", "add", "subtract", "switch")
        private const val META_COMMAND = "deluxemenus.meta"
    }
}
