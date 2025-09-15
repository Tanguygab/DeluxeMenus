package com.extendedclip.deluxemenus.menu

import com.extendedclip.deluxemenus.DeluxeMenus
import com.extendedclip.deluxemenus.events.DeluxeMenusOpenMenuEvent
import com.extendedclip.deluxemenus.events.DeluxeMenusPreOpenMenuEvent
import com.extendedclip.deluxemenus.menu.command.RegistrableMenuCommand
import com.extendedclip.deluxemenus.menu.options.MenuOptions
import com.extendedclip.deluxemenus.requirement.RequirementList
import com.extendedclip.deluxemenus.utils.DebugLevel
import com.extendedclip.deluxemenus.utils.StringUtils
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import java.util.TreeMap
import java.util.UUID
import java.util.logging.Level

class Menu(
    private val plugin: DeluxeMenus,
    val options: MenuOptions,
    val items: Map<Int, TreeMap<Int, MenuItem>>,
    // menu path starting from the plugin directory
    private val path: String
) {

    private var command: RegistrableMenuCommand? = null

    init {

        if (options.registerCommands) {
            command = RegistrableMenuCommand(plugin, this)
            command!!.register()
        }

        menus[options.name] = this
    }

    private fun unregisterCommand() {
        command?.unregister()

        // WARNING! A reference to the command is stored by CraftBukkit for their `/help` command. There is currently
        // no way to remove this reference!
        command = null
    }

    private fun hasOpenBypassPerm(viewer: Player): Boolean {
        return viewer.hasPermission("deluxemenus.openrequirement.bypass." + options.name)
                || viewer.hasPermission("deluxemenus.openrequirement.bypass.*")
    }

    private fun handleOpenRequirements(holder: MenuHolder): Boolean {
        if (options.openRequirements == null) return true

        val openRequirements: RequirementList = options.openRequirements

        if (options.enableBypassPerm && hasOpenBypassPerm(holder.viewer)) return true

        if (!openRequirements.evaluate(holder)) {
            openRequirements.denyHandler?.onClick(holder)
            return false
        }
        return true
    }

    private fun handleArgRequirements(holder: MenuHolder): Boolean {
        for (rl in options.argumentRequirements) {
            if (!rl.evaluate(holder)) {
                rl.denyHandler?.onClick(holder)
                return false
            }
        }
        return true
    }

    @JvmOverloads
    fun openMenu(viewer: Player, args: MutableMap<String, String>? = null, placeholderPlayer: Player? = null) {
        if (items.isEmpty()) return

        val preOpenEvent = DeluxeMenusPreOpenMenuEvent(viewer)
        Bukkit.getPluginManager().callEvent(preOpenEvent)

        if (preOpenEvent.isCancelled()) return

        val holder = MenuHolder(plugin, viewer)
        if (placeholderPlayer != null) holder.placeholderPlayer = placeholderPlayer

        holder.typedArgs = args
        holder.parsePlaceholdersInArguments = options.parsePlaceholdersInArguments
        holder.parsePlaceholdersAfterArguments = options.parsePlaceholdersAfterArguments

        if (!handleArgRequirements(holder)) return
        if (!handleOpenRequirements(holder)) return

        plugin.server.scheduler.runTaskAsynchronously(plugin, Runnable {
            val activeItems = mutableSetOf<MenuItem>()
            for (entry in items.entries) {
                for (item in entry.value.values) {
                    val slot: Int = item.options.slot

                    if (slot >= options.size) {
                        plugin.debug(
                            DebugLevel.HIGHEST,
                            Level.WARNING,
                            "Item set to slot " + slot + " for menu: " + options.name + " exceeds the inventory size!",
                            "This item will not be added to the menu!"
                        )
                        continue
                    }

                    if (item.options.viewRequirements != null) {
                        if (item.options.viewRequirements.evaluate(holder)) {
                            activeItems.add(item)
                            break
                        }
                    } else {
                        activeItems.add(item)
                        break
                    }
                }
            }

            if (activeItems.isEmpty()) return@Runnable

            holder.menuName = options.name
            holder.activeItems = activeItems

            options.openHandler?.onClick(holder)

            val title = StringUtils.color(holder.setPlaceholdersAndArguments(options.title))

            val inventory: Inventory = if (options.type !== InventoryType.CHEST)
                Bukkit.createInventory(holder, options.type, title)
            else Bukkit.createInventory(holder, options.size, title)

            holder.inv = inventory

            var update = false

            for (item in activeItems) {
                var iStack = item.getItemStack(holder) ?: continue

                iStack = plugin.menuItemMarker!!.mark(iStack)

                val slot = item.options.slot

                if (slot >= options.size) {
                    plugin.debug(
                        DebugLevel.HIGHEST,
                        Level.WARNING,
                        "Item set to slot " + slot + " for menu: " + options.name + " exceeds the inventory size!",
                        "This item will not be added to the menu!"
                    )
                    continue
                }

                if (item.options.updatePlaceholders) update = true

                inventory.setItem(item.options.slot, iStack)
            }

            val updatePlaceholders = update

            Bukkit.getScheduler().runTask(plugin, Runnable {
                if (options.refresh) {
                    holder.startRefreshTask()
                }
                if (isInMenu(holder.viewer)) {
                    closeMenu(plugin, holder.viewer, false)
                }

                viewer.openInventory(inventory)
                menuHolders.add(holder)
                if (updatePlaceholders) {
                    holder.startUpdatePlaceholdersTask()
                }
            })
            Bukkit.getScheduler().runTask(plugin, Runnable {
                val openEvent = DeluxeMenusOpenMenuEvent(viewer, holder)
                Bukkit.getPluginManager().callEvent(openEvent)
            })
        })
    }

    fun refreshForAll() = menuHolders
        .filter { options.name.equals(it.menuName, ignoreCase = true) }
        .forEach { it.refreshMenu() }

    fun getMenuCommandUsed(command: String) = options.commands.find { it.equals(command, ignoreCase = true) }

    fun getActiveViewers() = menuHolders.count { options.name.equals(it.menuName, ignoreCase = true) }

    companion object {
        private val menus = mutableMapOf<String, Menu>()
        private val menuHolders = mutableSetOf<MenuHolder>()
        private val lastOpenedMenus = mutableMapOf<UUID, Menu?>()

        fun unload(plugin: DeluxeMenus, name: String) {
            for (p in Bukkit.getOnlinePlayers()) {
                if (isInMenu(p, name)) {
                    closeMenu(plugin, p, true)
                }
            }

            val menu = getMenuByName(name) ?: return

            menu.unregisterCommand()
            menus.remove(name)
        }

        fun unload(plugin: DeluxeMenus) {
            for (p in Bukkit.getOnlinePlayers()) {
                if (isInMenu(p)) {
                    closeMenu(plugin, p, true)
                }
            }
            for (menu in getAllMenus()) {
                menu.unregisterCommand()
            }
            menus.clear()
            menuHolders.clear()
            lastOpenedMenus.clear()
        }

        fun unloadForShutdown(plugin: DeluxeMenus) {
            for (player in Bukkit.getOnlinePlayers()) {
                if (isInMenu(player)) {
                    closeMenuForShutdown(plugin, player)
                }
            }
            menus.clear()
        }

        fun getLoadedMenuSize() = menus.size

        fun getAllMenuNames() = menus.keys

        fun getAllMenus() = menus.values.toList()

        // Menus need to be stored in a list because config.yml can contain multiple menus.
        fun getPathSortedMenus() = menus
            .values
            .stream()
            .map { it.path to it}
            .collect(
                 { TreeMap() },
                 { tree: TreeMap<String, MutableList<Menu>>, entry: Pair<String, Menu> ->
                    val list = tree.computeIfAbsent(entry.first) { ArrayList() }
                    list.add(entry.second)
                     tree[entry.first] = list
                 },
                 { tree1: TreeMap<String, MutableList<Menu>>, tree2: TreeMap<String, MutableList<Menu>> ->
                    for (entry in tree2.entries) {
                        val list = tree1.computeIfAbsent(entry.key) { ArrayList() }
                        list.addAll(entry.value)
                        tree1[entry.key] = list
                    }
                }
            )

        fun getMenuByName(name: String) = menus.entries.find { it.key.equals(name, ignoreCase = true) }?.value

        fun getMenuByCommand(command: String) = menus.values.find { it.getMenuCommandUsed(command) != null }

        fun isMenuCommand(command: String) = getMenuByCommand(command) != null

        fun isInMenu(player: Player) = menuHolders.any { it.getViewerName() == player.name }

        fun isInMenu(player: Player, menu: String) = menuHolders.any { it.menuName == menu && it.getViewerName() == player.name }

        fun getMenuHolder(player: Player) = menuHolders.find { it.getViewerName() == player.name }

        fun getOpenMenu(player: Player) = getMenuHolder(player)?.getMenu()

        fun getLastMenu(player: Player) = lastOpenedMenus[player.uniqueId]

        fun cleanInventory(plugin: DeluxeMenus, player: Player) {
            for (itemStack in player.inventory.contents) {
                if (itemStack == null) continue
                if (!plugin.menuItemMarker!!.isMarked(itemStack)) continue

                plugin.debug(
                    DebugLevel.LOWEST,
                    Level.INFO,
                    "Found a DeluxeMenus item in a player's inventory. Removing it."
                )
                player.inventory.remove(itemStack)
            }
            @Suppress("UnstableApiUsage")
            player.updateInventory()
        }

        @JvmOverloads
        fun closeMenu(plugin: DeluxeMenus, player: Player, close: Boolean, executeCloseActions: Boolean = false) {
            val holder = getMenuHolder(player) ?: return

            holder.stopPlaceholderUpdate()
            holder.stopRefreshTask()

            if (executeCloseActions) holder.getMenu()?.options?.closeHandler?.onClick(holder)

            if (close) {
                plugin.server.scheduler.runTask(plugin, Runnable {
                    player.closeInventory()
                    cleanInventory(plugin, player)
                })
            }
            menuHolders.remove(holder)
            lastOpenedMenus[player.uniqueId] = holder.getMenu()
        }

        fun closeMenuForShutdown(plugin: DeluxeMenus, player: Player) {
            getMenuHolder(player)?.stopPlaceholderUpdate()
            player.closeInventory()
            cleanInventory(plugin, player)
        }
    }
}
