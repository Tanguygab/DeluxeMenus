package com.extendedclip.deluxemenus.persistentmeta

import com.extendedclip.deluxemenus.DeluxeMenus
import com.extendedclip.deluxemenus.utils.DebugLevel
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import java.util.logging.Level

class PersistentMetaHandler(private val plugin: DeluxeMenus) {
    /**
     * Check if a player has a meta value in their [org.bukkit.persistence.PersistentDataContainer].
     * It will check all supported types. See [DataType.supportedTypes].
     *
     * @param player The player to check.
     * @param key    The key of the meta value.
     * @return True if the player has the meta value, false if not.
     */
    fun hasMetaValue(player: Player, key: NamespacedKey) = DataType.supportedTypes.any { hasMetaValue(player, key, it) }

    /**
     * Check if a player has a meta value in their [org.bukkit.persistence.PersistentDataContainer].
     *
     * @param player The player to check.
     * @param key    The key of the meta value.
     * @param type   The type of the meta value.
     * @return True if the player has the meta value, false if not.
     */
    fun hasMetaValue(
        player: Player,
        key: NamespacedKey,
        type: DataType<*, *>
    ) = player.persistentDataContainer.has(key, type.pDType) && type.isSupported(player.persistentDataContainer.get(key, type.pDType))

    /**
     * Get a meta value from a player's [org.bukkit.persistence.PersistentDataContainer].
     * If the meta value is not found, null is returned.
     *
     * @param player The player to get the meta value from.
     * @param key    The key of the meta value.
     * @param type   The type of the meta value.
     * @return The meta value or null if no meta value was found.
     */
    fun <T> getMetaValue(
        player: Player,
        key: NamespacedKey,
        type: DataType<*, T>
    ): T? {
        if (!player.persistentDataContainer.has(key, type.pDType)) return null

        val value = player.persistentDataContainer.get(key, type.pDType)
        return if (value != null && type.isSupported(value)) value else null
    }

    /**
     * Get a meta value from a player's [org.bukkit.persistence.PersistentDataContainer].
     * If the meta value is not found, the default value is returned.
     *
     * @param player       The player to get the meta value from.
     * @param key          The key of the meta value.
     * @param type         The type of the meta value.
     * @param defaultValue The default value to return if no meta value was found.
     * @return The meta value or the default value if no meta value was found.
     */
    fun <T> getMetaValueOrDefault(
        player: Player,
        key: NamespacedKey,
        type: DataType<*, T>,
        defaultValue: T
    ) = getMetaValue(player, key, type) ?: defaultValue

    /**
     * Get a list of all meta values of the given type from a player's [org.bukkit.persistence.PersistentDataContainer].
     *
     * @param player The player to get the meta values from.
     * @param type   The type of the meta values.
     * @return A map of all meta values.
     */
    fun <T> getMetaValues(
        player: Player,
        type: DataType<*, T>
    ) = player.persistentDataContainer.keys
        .filter { player.persistentDataContainer.has(it, type.pDType) }
        .map { it.toString() to player.persistentDataContainer.get(it, type.pDType) }
        .filter { it.second != null }
        .map { it.first to it.second!! }
        .filter { type.isSupported(it.second) }
        .toMap()

    /**
     * Set a meta value in a player's [org.bukkit.persistence.PersistentDataContainer].
     * If the meta value already exists, it will be overwritten.
     *
     * @param player The player to set the meta value for.
     * @param key    The key of the meta value.
     * @param type   The type of the meta value.
     * @param value  The value to set.
     * @return The result of the operation.
     */
    fun setMetaValue(
        player: Player,
        key: NamespacedKey,
        type: DataType<*, Any>,
        value: Any
    ): OperationResult {
        if (!type.isSupported(value)) return OperationResult.NEW_VALUE_IS_DIFFERENT_TYPE

        if (player.persistentDataContainer.has(key)
            && (!player.persistentDataContainer.has(key, type.pDType) || !type.isSupported(player.persistentDataContainer.get(key, type.pDType)))
        ) return OperationResult.EXISTENT_VALUE_IS_DIFFERENT_TYPE

        player.persistentDataContainer.set(key, type.pDType, value)
        return OperationResult.SUCCESS
    }

    /**
     * Remove a meta value from a player's [org.bukkit.persistence.PersistentDataContainer].
     *
     * @param player The player to remove the meta value from.
     * @param key    The key of the meta value.
     * @param type  The type of the meta value.
     * @return The result of the operation.
     */
    fun removeMetaValue(
        player: Player,
        key: NamespacedKey,
        type: DataType<*, *>
    ): OperationResult {
        if (player.persistentDataContainer.has(key)
            && (!player.persistentDataContainer.has(key, type.pDType) || !type.isSupported(player.persistentDataContainer.get(key, type.pDType)))
        ) return OperationResult.EXISTENT_VALUE_IS_DIFFERENT_TYPE

        if (!player.persistentDataContainer.has(key, type.pDType)) return OperationResult.VALUE_NOT_FOUND

        player.persistentDataContainer.remove(key)
        return OperationResult.SUCCESS
    }

    /**
     * Remove a meta value from a player's [org.bukkit.persistence.PersistentDataContainer].
     *
     * @param player The player to remove the meta value from.
     * @param key    The key of the meta value.
     * @return The result of the operation.
     */
    fun removeMetaValue(
        player: Player,
        key: NamespacedKey
    ): OperationResult {
        if (!player.persistentDataContainer.has(key)) return OperationResult.VALUE_NOT_FOUND

        player.persistentDataContainer.remove(key)
        return OperationResult.SUCCESS
    }


    /**
     * Switch a meta value in a player's [org.bukkit.persistence.PersistentDataContainer].
     *
     * The value must be a boolean.
     * If the meta value does not exist, it will be created and set to true.
     * If the meta value is not a boolean, it will not be changed.
     *
     * @param player The player to switch the meta value for.
     * @param key    The key of the meta value.
     * @return The result of the operation.
     */
    fun switchMetaValue(
        player: Player,
        key: NamespacedKey
    ): OperationResult {
        if (player.persistentDataContainer.has(key) && !player.persistentDataContainer.has(key, DataType.BOOLEAN.pDType)
        ) return OperationResult.EXISTENT_VALUE_IS_DIFFERENT_TYPE

        val currentValue = player.persistentDataContainer.getOrDefault(key, DataType.BOOLEAN.pDType, "false")
        if (!DataType.BOOLEAN.isSupported(currentValue)) return OperationResult.EXISTENT_VALUE_IS_DIFFERENT_TYPE

        player.persistentDataContainer.set(
            key,
            DataType.BOOLEAN.pDType,
            if (currentValue.equals("true", ignoreCase = true)) "false" else "true"
        )
        return OperationResult.SUCCESS
    }

    /**
     * Perform addition on a meta value in a player's [org.bukkit.persistence.PersistentDataContainer].
     *
     * The value must be a number.
     * If the meta value does not exist, it will be created with the given value.
     * If the meta value is not a number, it will not be changed.
     *
     * @param player The player to add the meta value for.
     * @param key    The key of the meta value.
     * @param type   The type of the meta value.
     * @param value  The value to add.
     * @return The result of the operation.
     */
    fun addMetaValue(
        player: Player,
        key: NamespacedKey,
        type: DataType<*, *>,
        value: Number
    ): OperationResult {
        if (type != DataType.DOUBLE && type != DataType.LONG && type != DataType.INTEGER) return OperationResult.INVALID_TYPE

        if (player.persistentDataContainer.has(key)
            && (!player.persistentDataContainer.has(key, type.pDType) || !type.isSupported(player.persistentDataContainer.get(key, type.pDType)))
        ) return OperationResult.EXISTENT_VALUE_IS_DIFFERENT_TYPE

        val currentValue = player.persistentDataContainer.get(key, type.pDType)

        when (type) {
            DataType.DOUBLE -> {
                val newValue = (currentValue ?: 0.0).toDouble() - value.toDouble()
                player.persistentDataContainer.set(key, type.pDType, newValue)
            }
            DataType.INTEGER, DataType.LONG -> {
                val newValue = (currentValue ?: 0).toLong() + value.toLong()
                player.persistentDataContainer.set(key, type.pDType, newValue)
            }
        }
        return OperationResult.SUCCESS
    }

    /**
     * Perform subtraction on a meta value in a player's [org.bukkit.persistence.PersistentDataContainer].
     *
     * The value must be a number.
     * If the meta value does not exist, it will be created with the given value.
     * If the meta value is not a number, it will not be changed.
     *
     * @param player The player to subtract the meta value for.
     * @param key    The key of the meta value.
     * @param type   The type of the meta value.
     * @param value  The value to subtract.
     * @return The result of the operation.
     */
    fun subtractMetaValue(
        player: Player,
        key: NamespacedKey,
        type: DataType<*, *>,
        value: Number
    ): OperationResult {
        if (type != DataType.DOUBLE && type != DataType.LONG && type != DataType.INTEGER) return OperationResult.INVALID_TYPE

        if (player.persistentDataContainer.has(key)
            && (!player.persistentDataContainer.has(key, type.pDType) || !type.isSupported(player.persistentDataContainer.get(key, type.pDType)))
        ) return OperationResult.EXISTENT_VALUE_IS_DIFFERENT_TYPE

        val currentValue = player.persistentDataContainer.get(key, type.pDType)

        when (type) {
            DataType.DOUBLE -> {
                val newValue = (currentValue ?: 0.0).toDouble() - value.toDouble()
                player.persistentDataContainer.set(key, type.pDType, newValue)
            }
            DataType.INTEGER, DataType.LONG -> {
                val newValue = (currentValue ?: 0).toLong() - value.toLong()
                player.persistentDataContainer.set(key, type.pDType, newValue)
            }
        }
        return OperationResult.SUCCESS
    }

    /**
     * Parse and execute a meta action from a string.
     * <br></br>The format is: &lt;action&gt; &lt;key&gt; &lt;type&gt; [value].
     * <br></br>Example: set points INTEGER 0
     *
     * @param player The player to execute the action for.
     * @param input  The action to execute.
     * @return The result of the operation.
     */
    fun parseAndExecuteMetaActionFromString(
        player: Player,
        input: String
    ): OperationResult {
        // <action> <key> [type] [value] - type is optional for switch action since it only toggles a boolean
        val args = input.split(" ", limit = 4)

        if (args.size < 2) return OperationResult.INVALID_SYNTAX

        val action = DataAction.getActionByName(args[0]) ?: return OperationResult.INVALID_SYNTAX
        val key = getKey(args[1]) ?: return OperationResult.INVALID_SYNTAX
        if (action == DataAction.SWITCH) return switchMetaValue(player, key)
        if (args.size < 3) return OperationResult.INVALID_SYNTAX

        val type = DataType.getSupportedTypeByName(args[2]) ?: return OperationResult.INVALID_SYNTAX

        val parsedValue = parseValueByType(type, if (args.size >= 4) args[3] else null)

        return when (action) {
            DataAction.SET -> {
                if (parsedValue == null) OperationResult.NEW_VALUE_IS_DIFFERENT_TYPE
                else setMetaValue(player, key, type as DataType<*, Any>, parsedValue)
            }
            DataAction.ADD -> {
                if (parsedValue !is Number) OperationResult.NEW_VALUE_IS_DIFFERENT_TYPE
                else addMetaValue(player, key, type, parsedValue)
            }
            DataAction.SUBTRACT -> {
                if (parsedValue !is Number) OperationResult.NEW_VALUE_IS_DIFFERENT_TYPE
                else subtractMetaValue(player, key, type, parsedValue)
            }
            DataAction.REMOVE -> return removeMetaValue(player, key, type)

            else -> OperationResult.INVALID_SYNTAX
        }

    }

    /**
     * Parse a string value into an object based on the type.
     *
     * @param type  The type to parse the value for.
     * @param value The value to parse.
     * @return The parsed value or null if the value is null or could not be parsed.
     */
    fun parseValueByType(
        type: DataType<*, *>,
        value: String?
    ): Any? {
        if (value == null) return null
        if (type == DataType.BOOLEAN) return value.lowercase().toBooleanStrictOrNull()
        if (type == DataType.STRING) return value
        if (type == DataType.DOUBLE) return value.toDoubleOrNull()
        if (type == DataType.LONG || type == DataType.INTEGER) value.toLong()
        return null
    }

    /**
     * Helper method to parse a string into a [NamespacedKey].
     * If the key contains a namespace, it will use that, otherwise it will use the plugin's namespace. If the key is
     * invalid, it will log a warning and return null.
     *
     * @param key The string to parse.
     * @return The [NamespacedKey] or null if the key could not be parsed.
     */
    fun getKey(key: String): NamespacedKey? {
        val namespacedKey: NamespacedKey

        try {
            if (key.contains(":")) {
                val split = key.split(":", limit = 2)
                @Suppress("UnstableApiUsage")
                namespacedKey = NamespacedKey(split[0], split[1])
            } else {
                namespacedKey = NamespacedKey(plugin, key)
            }
        } catch (e: IllegalArgumentException) {
            plugin.debug(
                DebugLevel.HIGHEST,
                Level.WARNING,
                "Failed to parse meta key with value: '" + key + "'. Reason: " + e.message
            )
            return null
        }

        return namespacedKey
    }

    enum class OperationResult {
        SUCCESS,  // Operation was successful
        INVALID_SYNTAX,  // Used when parsing an action from a string and the syntax is invalid
        INVALID_TYPE,  // Used when the type is not supported
        VALUE_NOT_FOUND,  // Used when no value was found with the specified key and/or type
        EXISTENT_VALUE_IS_DIFFERENT_TYPE,  // Used when the value already exists but is a different type
        NEW_VALUE_IS_DIFFERENT_TYPE // Used when the new value is of an unsupported type
    }
}
