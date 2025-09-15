package com.extendedclip.deluxemenus.utils

import com.extendedclip.deluxemenus.persistentmeta.DataType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration

enum class Messages(val message: Component) {
    PLUGIN_TITLE(
        Component.empty()
            .append(Component.text("Deluxe", NamedTextColor.GOLD, TextDecoration.BOLD))
            .append(Component.text("Menus", NamedTextColor.YELLOW))
    ),

    PLUGIN_VERSION(
        PLUGIN_TITLE.message
            .append(Component.space())
            .append(Component.text("version", NamedTextColor.WHITE))
            .append(Component.space())
            .append(Component.text("<version>", NamedTextColor.YELLOW))
            .append(Component.newline())
            .append(Component.text("Created by", NamedTextColor.GRAY))
            .append(Component.space())
            .append(Component.text("<authors>", NamedTextColor.WHITE))
    ),

    HELP(
        PLUGIN_TITLE.message
            .append(Component.space())
            .append(Component.text("help", NamedTextColor.WHITE))
            .append(Component.newline())
            .append(Component.text(">", NamedTextColor.AQUA))
            .append(Component.space().append(Component.space()))
            .append(Component.text("/dm open <menu-name> [player]", NamedTextColor.WHITE))
            .append(Component.newline())
            .append(Component.text(">", NamedTextColor.AQUA))
            .append(Component.space().append(Component.space()))
            .append(Component.text("/dm list [page/all]", NamedTextColor.WHITE))
            .append(Component.newline())
            .append(Component.text(">", NamedTextColor.AQUA))
            .append(Component.space().append(Component.space()))
            .append(Component.text("/dm dump <menu-name/config>", NamedTextColor.WHITE))
            .append(Component.newline())
            .append(Component.text(">", NamedTextColor.AQUA))
            .append(Component.space().append(Component.space()))
            .append(Component.text("/dm reload [menu-name]", NamedTextColor.WHITE))
    ),

    HELP_OP(
        PLUGIN_TITLE.message
            .append(Component.space())
            .append(Component.text("help", NamedTextColor.WHITE))
            .append(Component.newline())
            .append(Component.text(">", NamedTextColor.AQUA))
            .append(Component.space().append(Component.space()))
            .append(Component.text("/dm open <menu-name> [player]", NamedTextColor.WHITE))
            .append(Component.newline())
            .append(Component.text(">", NamedTextColor.AQUA))
            .append(Component.space().append(Component.space()))
            .append(Component.text("/dm list [page/all]", NamedTextColor.WHITE))
            .append(Component.newline())
            .append(Component.text(">", NamedTextColor.AQUA))
            .append(Component.space().append(Component.space()))
            .append(Component.text("/dm execute <player> <action>", NamedTextColor.WHITE))
            .append(Component.newline())
            .append(Component.text(">", NamedTextColor.AQUA))
            .append(Component.space().append(Component.space()))
            .append(Component.text("/dm dump <menu-name/config>", NamedTextColor.WHITE))
            .append(Component.newline())
            .append(Component.text(">", NamedTextColor.AQUA))
            .append(Component.space().append(Component.space()))
            .append(Component.text("/dm meta <player> <set/remove/add/subtract/list/show>", NamedTextColor.WHITE))
            .append(Component.newline())
            .append(Component.text(">", NamedTextColor.AQUA))
            .append(Component.space().append(Component.space()))
            .append(Component.text("/dm reload [menu-name]", NamedTextColor.WHITE))
    ),

    NO_PERMISSION(Component.text("You don't have permission to do that!", NamedTextColor.RED)),
    NO_PERMISSION_PLAYER_ARGUMENT(
        Component.text(
            "You don't have permission to use the argument -p:<player>!",
            NamedTextColor.RED
        )
    ),
    WRONG_USAGE_BASE(
        Component.empty()
            .append(Component.text("Incorrect Usage!", NamedTextColor.RED))
            .append(Component.space())
            .append(Component.text("Use"))
            .append(Component.space())
    ),
    WRONG_USAGE(
        WRONG_USAGE_BASE.message
            .append(Component.text("/dm help", NamedTextColor.GRAY))
    ),
    WRONG_USAGE_EXECUTE_COMMAND(
        WRONG_USAGE_BASE.message
            .append(Component.text("/dm execute <player> <action>", NamedTextColor.GRAY))
    ),
    WRONG_USAGE_DUMP_COMMAND(
        WRONG_USAGE_BASE.message
            .append(Component.text("/dm dump <menu-name/config>", NamedTextColor.GRAY))
    ),

    WRONG_USAGE_OPEN_COMMAND(
        WRONG_USAGE_BASE.message
            .append(Component.text("/dm open <menu-name> [player]", NamedTextColor.GRAY))
    ),
    WRONG_USAGE_REFRESH_COMMAND(
        WRONG_USAGE_BASE.message
            .append(Component.text("/dm refresh <menu-name>", NamedTextColor.GRAY))
    ),
    PLAYER_IS_NOT_ONLINE(
        Component.empty()
            .append(Component.text("Player:", NamedTextColor.RED))
            .append(Component.space())
            .append(Component.text("<player>", NamedTextColor.WHITE))
            .append(Component.space())
            .append(Component.text("is not online!", NamedTextColor.RED))
    ),
    PLAYER_IS_EXEMPT(
        Component.text("<player>", NamedTextColor.WHITE)
            .append(Component.space())
            .append(Component.text("is exempt from placeholder target arguments.", NamedTextColor.GRAY))
    ),

    MUST_SPECIFY_PLAYER(Component.text("You must specify a player to open a menu for!", NamedTextColor.RED)),
    WRONG_ACTION_TYPE(Component.text("Action type specified does not exist!", NamedTextColor.RED)),
    CHANCE_FAIL(
        Component.text(
            "The chance for this action determined the action should not execute!",
            NamedTextColor.RED
        )
    ),

    ACTION_TO_BE_EXECUTED(
        Component.text("Action set to be executed in", NamedTextColor.GREEN)
            .append(Component.space())
            .append(Component.text("<amount>"))
            .append(Component.space())
            .append(Component.text("ticks."))
    ),
    ACTION_EXECUTED_FOR(
        Component.text("Action executed for player:", NamedTextColor.GREEN)
            .append(Component.space())
            .append(Component.text("<player>"))
    ),

    RELOAD_FAIL(Component.text("Errors detected in config.yml. Failed to reload.", NamedTextColor.RED)),
    RELOAD_SUCCESS(
        PLUGIN_TITLE.message
            .append(Component.space())
            .append(Component.text("successfully reloaded!", NamedTextColor.GREEN))
    ),

    INVALID_MENU(
        Component.text("Could not find menu:", NamedTextColor.RED)
            .append(Component.space())
            .append(Component.text("<menu>", NamedTextColor.GOLD))
            .append(Component.text(".", NamedTextColor.RED))
    ),
    MENU_RELOADED(
        Component.text("<menu>", NamedTextColor.GOLD)
            .append(Component.space())
            .append(Component.text("menu successfully reloaded!", NamedTextColor.GREEN))
    ),
    MENU_NOT_RELOADED(
        Component.text("<menu>", NamedTextColor.GOLD)
            .append(Component.space())
            .append(Component.text("menu could not be reloaded!", NamedTextColor.RED))
    ),
    MENU_LOADED(Component.text("<amount> menu loaded...", NamedTextColor.YELLOW)),
    MENUS_LOADED(Component.text("<amount> menus loaded...", NamedTextColor.YELLOW)),
    MENU_REFRESHED(
        Component.text("<menu>", NamedTextColor.GOLD)
            .append(Component.space())
            .append(Component.text("menu refreshed for <amount> players...", NamedTextColor.YELLOW))
    ),

    DUMP_FAILED(Component.text("Failed to create and post dump!", NamedTextColor.RED)),

    DUMP_SUCCESS(Component.text("Dump created successfully! Find it at: ", NamedTextColor.GREEN)),

    UPDATE_AVAILABLE(
        Component.text("An update for", NamedTextColor.GREEN)
            .append(Component.space())
            .append(PLUGIN_TITLE.message)
            .append(Component.space())
            .append(Component.text("is available. Version", NamedTextColor.GREEN))
            .append(Component.space())
            .append(Component.text("<latest-version>", NamedTextColor.WHITE))
            .append(Component.text(", You are running", NamedTextColor.GREEN))
            .append(Component.space())
            .append(Component.text("<current-version>", NamedTextColor.WHITE))
            .append(Component.newline())
            .append(Component.text("Download the latest version at:", NamedTextColor.GREEN))
            .append(Component.space())
            .append(Component.text("https://www.spigotmc.org/resources/deluxemenus.11734/", NamedTextColor.WHITE))

    ),

    // Meta related messages
    WRONG_USAGE_META_COMMAND(
        WRONG_USAGE_BASE.message
            .append(Component.text("/dm meta <player> <set/remove/add/subtract/switch/list/show>", NamedTextColor.GRAY))
    ),

    WRONG_USAGE_META_LIST_COMMAND(
        WRONG_USAGE_BASE.message
            .append(Component.text("/dm meta <player> list <type> [page]", NamedTextColor.GRAY))
    ),
    WRONG_USAGE_META_SWITCH_COMMAND(
        WRONG_USAGE_BASE.message
            .append(Component.text("/dm meta <player> switch <key>", NamedTextColor.GRAY))
    ),
    WRONG_USAGE_META_SHOW_COMMAND(
        WRONG_USAGE_BASE.message
            .append(Component.text("/dm meta <player> show <key> <type>", NamedTextColor.GRAY))
    ),
    WRONG_USAGE_META_REMOVE_COMMAND(
        WRONG_USAGE_BASE.message
            .append(Component.text("/dm meta <player> remove <key> <type>", NamedTextColor.GRAY))
    ),
    WRONG_USAGE_META_SET_COMMAND(
        WRONG_USAGE_BASE.message
            .append(Component.text("/dm meta <player> set <key> <type> <value>", NamedTextColor.GRAY))
    ),
    WRONG_USAGE_META_ADD_COMMAND(
        WRONG_USAGE_BASE.message
            .append(Component.text("/dm meta <player> add <key> <type> <value>", NamedTextColor.GRAY))
    ),
    WRONG_USAGE_META_SUBTRACT_COMMAND(
        WRONG_USAGE_BASE.message
            .append(Component.text("/dm meta <player> subtract <key> <type> <value>", NamedTextColor.GRAY))
    ),

    META_NOT_SUPPORTED(Component.text("Meta is not supported on this server version!", NamedTextColor.RED)),
    META_TYPE_UNSUPPORTED(
        Component.text("Unsupported meta type ", NamedTextColor.RED)
            .append(Component.text("<type>", NamedTextColor.GOLD))
            .append(Component.text("! Supported values are: ", NamedTextColor.RED))
            .append(Component.text(DataType.getSupportedTypeNames().joinToString(", "), NamedTextColor.GOLD))
    ),
    META_KEY_INVALID(
        Component.text("An invalid meta key was provided: '", NamedTextColor.RED)
            .append(Component.text("<key>", NamedTextColor.GOLD))
            .append(Component.text("'!", NamedTextColor.RED))
    ),
    META_ADD_TYPE_MISMATCH(
        Component.text("Only NUMBERS can be added, and '", NamedTextColor.RED)
            .append(Component.text("<value>", NamedTextColor.GOLD))
            .append(Component.text("' is not a number!", NamedTextColor.RED))
    ),
    META_SUBTRACT_TYPE_MISMATCH(
        Component.text("Only NUMBERS can be subtracted, and '", NamedTextColor.RED)
            .append(Component.text("<value>", NamedTextColor.GOLD))
            .append(Component.text("' is not a number!", NamedTextColor.RED))
    ),
    META_VALUE_TYPE_MISMATCH(
        Component.text("Given value '", NamedTextColor.RED)
            .append(Component.text("<value>", NamedTextColor.GOLD))
            .append(Component.text("' does not match the given type '", NamedTextColor.RED))
            .append(Component.text("<type>", NamedTextColor.GOLD))
            .append(Component.text("'!", NamedTextColor.RED))
    ),
    META_EXISTENT_VALUE_WRONG_TYPE(
        Component.text("Given key '", NamedTextColor.RED)
            .append(Component.text("<key>", NamedTextColor.GOLD))
            .append(Component.text("' stores a value with a different type!", NamedTextColor.RED))
    ),
    NO_META_VALUE(
        Component.text("Could not find a meta value with key ", NamedTextColor.RED)
            .append(Component.text("<key>", NamedTextColor.GOLD))
            .append(Component.text(" and type ", NamedTextColor.RED))
            .append(Component.text("<type>", NamedTextColor.GOLD))
            .append(Component.text(" for ", NamedTextColor.RED))
            .append(Component.text("<player>", NamedTextColor.GOLD))
    ),
    NO_META_VALUES(
        Component.text("Could not find any meta values with type ", NamedTextColor.RED)
            .append(Component.text("<type>", NamedTextColor.GOLD))
            .append(Component.text(" for ", NamedTextColor.RED))
            .append(Component.text("<player>", NamedTextColor.GOLD))
    ),
    META_VALUE_FOUND(
        Component.text("Meta value with key ", NamedTextColor.GRAY)
            .append(Component.text("<key>", NamedTextColor.GREEN))
            .append(Component.text(" and type ", NamedTextColor.GRAY))
            .append(Component.text("<type>", NamedTextColor.GREEN))
            .append(Component.text(" for ", NamedTextColor.GRAY))
            .append(Component.text("<player>", NamedTextColor.GREEN))
            .append(Component.text(": ", NamedTextColor.GRAY))
            .append(Component.text("<value>", NamedTextColor.GREEN))
    ),
    META_VALUE_SET(
        Component.text("Meta value with key ", NamedTextColor.GRAY)
            .append(Component.text("<key>", NamedTextColor.GREEN))
            .append(Component.text(" and type ", NamedTextColor.GRAY))
            .append(Component.text("<type>", NamedTextColor.GREEN))
            .append(Component.text(" for ", NamedTextColor.GRAY))
            .append(Component.text("<player>", NamedTextColor.GREEN))
            .append(Component.text(" set to: ", NamedTextColor.GRAY))
            .append(Component.text("<value>", NamedTextColor.GREEN))
    ),
    META_VALUE_REMOVED(
        Component.text("Meta value with key ", NamedTextColor.GRAY)
            .append(Component.text("<key>", NamedTextColor.GREEN))
            .append(Component.text(" and type ", NamedTextColor.GRAY))
            .append(Component.text("<type>", NamedTextColor.GREEN))
            .append(Component.text(" for ", NamedTextColor.GRAY))
            .append(Component.text("<player>", NamedTextColor.GREEN))
            .append(Component.text(" removed.", NamedTextColor.GRAY))
    ),
    META_VALUE_ADDED(
        Component.text("Added ", NamedTextColor.GRAY)
            .append(Component.text("<value>", NamedTextColor.GREEN))
            .append(Component.text(" to the meta value with key ", NamedTextColor.GRAY))
            .append(Component.text("<key>", NamedTextColor.GREEN))
            .append(Component.text(" and type ", NamedTextColor.GRAY))
            .append(Component.text("<type>", NamedTextColor.GREEN))
            .append(Component.text(" for ", NamedTextColor.GRAY))
            .append(Component.text("<player>", NamedTextColor.GREEN))
            .append(Component.text(". New value: ", NamedTextColor.GRAY))
            .append(Component.text("<new-value>", NamedTextColor.GREEN))
    ),
    META_VALUE_SUBTRACTED(
        Component.text("Subtracted ", NamedTextColor.GRAY)
            .append(Component.text("<value>", NamedTextColor.GREEN))
            .append(Component.text(" from the meta value with key ", NamedTextColor.GRAY))
            .append(Component.text("<key>", NamedTextColor.GREEN))
            .append(Component.text(" and type ", NamedTextColor.GRAY))
            .append(Component.text("<type>", NamedTextColor.GREEN))
            .append(Component.text(" for ", NamedTextColor.GRAY))
            .append(Component.text("<player>", NamedTextColor.GREEN))
            .append(Component.text(". New value: ", NamedTextColor.GRAY))
            .append(Component.text("<new-value>", NamedTextColor.GREEN))
    ),
    META_VALUE_SWITCHED(
        Component.text("Meta value with key ", NamedTextColor.GRAY)
            .append(Component.text("<key>", NamedTextColor.GREEN))
            .append(Component.text(" for ", NamedTextColor.GRAY))
            .append(Component.text("<player>", NamedTextColor.GREEN))
            .append(Component.text(" switched to: ", NamedTextColor.GRAY))
            .append(Component.text("<new-value>", NamedTextColor.GREEN))
    )
}
