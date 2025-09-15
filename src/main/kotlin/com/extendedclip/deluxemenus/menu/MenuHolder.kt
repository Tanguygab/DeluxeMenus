package com.extendedclip.deluxemenus.menu

import com.extendedclip.deluxemenus.DeluxeMenus
import com.extendedclip.deluxemenus.utils.StringUtils
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask

class MenuHolder : InventoryHolder {
    val plugin: DeluxeMenus
    val viewer: Player

    var placeholderPlayer: Player? = null
    var menuName: String? = null
    var activeItems: Set<MenuItem>? = null
    var inv: Inventory? = null

    private var updateTask: BukkitTask? = null
    private var refreshTask: BukkitTask? = null

    var isUpdating: Boolean = false
    var parsePlaceholdersInArguments = false
    var parsePlaceholdersAfterArguments = false
    var typedArgs: MutableMap<String, String>? = null

    constructor(plugin: DeluxeMenus, viewer: Player) {
        this.plugin = plugin
        this.viewer = viewer
    }

    override fun getInventory() = inv!!

    fun getViewerName() = viewer.name

    fun getItem(slot: Int) = activeItems?.find { it.options.slot == slot }

    fun getMenu() = Menu.getMenuByName(menuName!!)

    fun setPlaceholdersAndArguments(string: String): String {
        return if (parsePlaceholdersAfterArguments)  setPlaceholders(setArguments(string))
        else setArguments(setPlaceholders(string))
    }

    fun setPlaceholders(string: String): String {
        val player = if (placeholderPlayer != null) placeholderPlayer else viewer
        return if (player == null) string
        else StringUtils.replacePlaceholders(string, player)
    }

    fun setArguments(string: String): String {
        val player = if (placeholderPlayer != null) placeholderPlayer else viewer

        return StringUtils.replaceArguments(
            string,
            typedArgs,
            player,
            parsePlaceholdersInArguments
        )
    }

    fun refreshMenu() {
        val menu = getMenu() ?: return

        if (menu.items.isEmpty()) return

        isUpdating = true
        val inventory = inv!!

        plugin.server.scheduler.runTaskAsynchronously(plugin, Runnable {
            val active = mutableSetOf<MenuItem>()
            for (i in 0..<inventory.size) {
                val e = menu.items[i]

                if (e == null) {
                    inventory.setItem(i, null)
                    continue
                }

                var m = false
                for (item in e.values) {
                    if (item.options.viewRequirements != null) {
                        if (item.options.viewRequirements.evaluate(this)) {
                            m = true
                            active.add(item)
                            break
                        }
                    } else {
                        m = true
                        active.add(item)
                        break
                    }
                }

                if (!m) {
                    inventory.setItem(i, null)
                }
            }

            if (active.isEmpty()) {
                Menu.closeMenu(plugin, viewer, true)
            }
            plugin.server.scheduler.runTask(plugin, Runnable {
                var update = false
                for (item in active) {
                    val iStack = item.getItemStack(this)

                    val slot: Int = item.options.slot

                    if (slot >= menu.options.size) {
                        continue
                    }

                    if (item.options.updatePlaceholders) {
                        update = true
                    }

                    inventory.setItem(item.options.slot, iStack)
                }

                activeItems = active

                if (update && updateTask == null) {
                    startUpdatePlaceholdersTask()
                } else if (!update && updateTask != null) {
                    stopPlaceholderUpdate()
                }
                isUpdating = false
            })
        })
    }

    fun stopPlaceholderUpdate() {
        try { updateTask?.cancel() }
        catch (_: Exception) {}
        updateTask = null
    }

    fun stopRefreshTask() {
        try { refreshTask?.cancel() }
        catch (_: Exception) {}
        refreshTask = null
    }

    fun startRefreshTask() {
        if (refreshTask != null) stopRefreshTask()

        refreshTask = object : BukkitRunnable() {
            override fun run() = refreshMenu()
        }.runTaskTimerAsynchronously(plugin, 20L,20L * (Menu.getMenuByName(menuName!!)?.options?.refreshInterval ?: 10))
    }

    fun startUpdatePlaceholdersTask() {
        if (updateTask != null) {
            stopPlaceholderUpdate()
        }

        updateTask = object : BukkitRunnable() {
            override fun run() {
                if (isUpdating) return

                val items = activeItems ?: return

                for (item in items) {
                    if (item.options.updatePlaceholders) {
                        val i = inv!!.getItem(item.options.slot) ?: continue

                        var amt = i.amount

                        if (item.options.dynamicAmount != null) {
                            try {
                                amt = setPlaceholdersAndArguments(item.options.dynamicAmount).toInt()
                                if (amt <= 0) {
                                    amt = 1
                                }
                            } catch (e: Exception) {
                                plugin.printStacktrace(
                                    "Something went wrong while updating item in slot " + item.options.slot +
                                            ". Invalid dynamic amount: " + setPlaceholdersAndArguments(
                                        item.options.dynamicAmount
                                    ),
                                    e
                                )
                            }
                        }

                        val meta = i.itemMeta!!

                        if (item.options.displayNameHasPlaceholders && item.options.displayName != null) {
                            meta.setDisplayName(StringUtils.color(setPlaceholdersAndArguments(item.options.displayName)))
                        }

                        if (item.options.loreHasPlaceholders) {
                            meta.lore = item.getMenuItemLore(this@MenuHolder, item.options.lore)
                        }

                        i.itemMeta = meta
                        i.amount = amt
                    }
                }
            }
        }.runTaskTimerAsynchronously(plugin, 20L, 20L * (Menu.getMenuByName(menuName!!)?.options?.updateInterval ?: 10))
    }
}
