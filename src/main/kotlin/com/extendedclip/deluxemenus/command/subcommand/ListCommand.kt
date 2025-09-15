package com.extendedclip.deluxemenus.command.subcommand

import com.extendedclip.deluxemenus.DeluxeMenus
import com.extendedclip.deluxemenus.menu.Menu
import com.extendedclip.deluxemenus.utils.Messages
import com.extendedclip.deluxemenus.utils.PaginationUtils
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import java.util.TreeMap
import kotlin.math.ceil

class ListCommand(plugin: DeluxeMenus) : SubCommand(plugin, "list") {

    override fun execute(sender: CommandSender, arguments: List<String>) {
        if (!sender.hasPermission(LIST_PERMISSION)) {
            plugin.sms(sender, Messages.NO_PERMISSION)
            return
        }

        if (!arguments.isEmpty() && arguments[0].equals("all", ignoreCase = true)) {
            val menus = Menu.getAllMenus()
            if (menus.isEmpty()) {
                plugin.sms(
                    sender,
                    Messages.MENUS_LOADED.message
                        .replaceText(AMOUNT_REPLACER_BUILDER.replacement("There are no").build())
                )
                return
            }

            sendSimpleMenuList(sender, Menu.getAllMenus())
            return
        }

        if (Menu.getAllMenuNames().isEmpty()) {
            plugin.sms(
                sender,
                Messages.MENUS_LOADED.message.replaceText(AMOUNT_REPLACER_BUILDER.replacement("There are no").build())
            )
            return
        }

        val menus: MutableMap<String, MutableList<Menu>> = Menu.getPathSortedMenus()
        val configMenus = menus.remove("config")

        sendPaginatedMenuList(
            sender,
            menus,
            configMenus ?: mutableListOf(),
            arguments
        )
    }

    override fun onTabComplete(sender: CommandSender, arguments: List<String>): List<String>? {
        if (!sender.hasPermission(LIST_PERMISSION)) return null
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

        val menusCount: Int = Menu.getAllMenuNames().size
        val menusPerPage = plugin.generalConfig.menusListPageSize
        val pagesCount = ceil(menusCount.toDouble() / menusPerPage).toInt()

        val completions = (1..<pagesCount).map { it.toString() }.toMutableList()
        completions.add("all")

        if (arguments[1].isEmpty()) return completions

        return completions.filter { it.startsWith(arguments[1], ignoreCase = true) }
    }

    private fun sendSimpleMenuList(sender: CommandSender, menus: List<Menu>) {
        val list = Component.text()
        list.append(
            Component.text("The following " + menus.size + " menus are loaded on the server:", NamedTextColor.GOLD)
                .append(Component.newline())
        )

        if (sender is ConsoleCommandSender) {
            list.append(Component.newline())

            val menusList = menus.stream().map { menu: Menu ->
                val menuCommand = getMenuDisplayCommand(menu)
                if (menuCommand == null)
                    Component.text(menu.options.name, NamedTextColor.DARK_AQUA)
                        .append(Component.text(" - ", NamedTextColor.GRAY))
                        .append(Component.text("No menu command", NamedTextColor.RED))
                else
                    Component.text(menu.options.name, NamedTextColor.DARK_AQUA)
                        .append(Component.text(" - ", NamedTextColor.GRAY))
                        .append(Component.text(menuCommand, NamedTextColor.GREEN))
            }.collect(Component.toComponent(Component.text(" | ", NamedTextColor.WHITE)))

            plugin.sms(sender, list.append(menusList).build())
            return
        }

        list.append(Component.text("**Hover menu name for more info**", NamedTextColor.GRAY))
            .append(Component.newline())
            .append(Component.newline())

        val menusList = menus.stream().map { menu: Menu ->
            val menuCommand = getMenuDisplayCommand(menu)
            if (menuCommand == null)
                Component.text(
                    menu.options.name,
                    NamedTextColor.DARK_AQUA
                ).hoverEvent(HoverEvent.showText(Component.text("No open command", NamedTextColor.GOLD)))
            else
                Component.text(menu.options.name, NamedTextColor.DARK_AQUA).hoverEvent(
                    HoverEvent.showText(
                        Component.text("Open Command: ", NamedTextColor.GOLD)
                            .append(Component.text(menuCommand, NamedTextColor.YELLOW)))
                ).clickEvent(ClickEvent.suggestCommand(menuCommand))
        }.collect(Component.toComponent(Component.text(", ", NamedTextColor.WHITE)))

        list.append(menusList)
        plugin.sms(sender, list.build())
    }

    private fun sendPaginatedMenuList(
        sender: CommandSender, menus: Map<String, List<Menu>>,
        configMenus: List<Menu>, args: List<String>
    ) {
        val menusPerPage = plugin.generalConfig.menusListPageSize
        val totalMenusCount = configMenus.size + menus.values.sumOf { it.size }
        val pagesCount = PaginationUtils.getPagesCount(menusPerPage, totalMenusCount)

        val page = PaginationUtils.parsePage(
            menusPerPage,
            totalMenusCount,
            pagesCount,
            if (args.isEmpty()) null else args[0]
        )

        val paginatedMenus = getPaginatedMenus(
            menus,
            configMenus.stream().collect(
                { TreeMap() },
                { map: TreeMap<String, Menu>, menu: Menu -> map[menu.options.name] = menu },
                { obj: TreeMap<String, Menu>, map: TreeMap<String, Menu> -> obj.putAll(map) }),
            page,
            menusPerPage
        )

        val pageMenusCount = paginatedMenus.values.sumOf { it.size }
        val pageMenusTree = convertMenusToTree(paginatedMenus)

        val list = Component.text()
        list.append(
            Component.text("Page $page/$pagesCount - $pageMenusCount menus:", NamedTextColor.GOLD)
                .append(Component.newline())
        )

        if (sender is ConsoleCommandSender) {
            val menuList = createMenuListForConsole(pageMenusTree, 0)

            list.append(Component.newline()).append(menuList).append(Component.newline())
                .append(Component.text("Use /dm list <page> to view more menus", NamedTextColor.GRAY))
            plugin.sms(sender, list.build())
            return
        }

        list.append(Component.text("**Hover menu name for more info**", NamedTextColor.GRAY))
        list.append(Component.newline()).append(Component.newline())

        val menuList = createMenuListForPlayer(pageMenusTree, 0)

        list.append(menuList)

        if (page !in pagesCount..1) {
            list.append(Component.newline())

            if (page > 1) {
                list.append(
                    Component.text("<< Previous", NamedTextColor.GOLD)
                        .hoverEvent(
                            HoverEvent.showText(
                                Component.text("Click to go to the previous page", NamedTextColor.GRAY)
                                    .append(Component.newline())
                                    .append(Component.newline())
                                    .append(Component.text("Executes: /dm list " + (page - 1), NamedTextColor.GRAY))
                            )
                        )
                        .clickEvent(ClickEvent.runCommand("/dm list " + (page - 1)))
                )
                if (page < pagesCount) {
                    list.append(Component.text(" | ", NamedTextColor.GREEN))
                }
            }

            if (page < pagesCount) {
                list.append(
                    Component.text("Next >>", NamedTextColor.GOLD)
                        .hoverEvent(
                            HoverEvent.showText(
                                Component.text("Click to go to the next page", NamedTextColor.GRAY)
                                    .append(Component.newline())
                                    .append(Component.newline())
                                    .append(Component.text("Executes: /dm list " + (page + 1), NamedTextColor.GRAY))
                            )
                        )
                        .clickEvent(ClickEvent.runCommand("/dm list " + (page + 1)))
                )
            }
        }

        plugin.sms(sender, list.build())
    }

    private fun getPaginatedMenus(
        menus: Map<String, List<Menu>>,
        configMenus: Map<String, Menu>,
        page: Int,
        pageSize: Int
    ): Map<String, List<Menu>> {
        val paginatedMenus = mutableMapOf<String, MutableList<Menu>>()
        val start = (page - 1) * pageSize
        val end = start + pageSize

        var count = 0
        var i = 0
        for (entry in configMenus.entries) {
            if (count >= pageSize || i >= end) {
                break
            }

            if (i < start) {
                i++
                continue
            }

            paginatedMenus.computeIfAbsent("config") { mutableListOf() }.add(entry.value)
            count++
            i++
        }

        for (entry in menus.entries) {
            if (count >= pageSize || i >= end) break

            for (menu in entry.value) {
                if (count >= pageSize || i >= end) break

                if (i < start) {
                    i++
                    continue
                }

                paginatedMenus.computeIfAbsent(entry.key) { mutableListOf() }.add(menu)
                count++
                i++
            }
        }

        return paginatedMenus
    }

    @Suppress("UNCHECKED_CAST")
    private fun createMenuListForConsole(tree: Map<String, Any>, tabs: Int): Component {
        val list = Component.text()

        for (entry in tree.entries) {
            if (entry.value is MutableList<*>) {
                for (menu in entry.value as MutableList<Menu>) {
                    list.append(
                        Component.text("  ".repeat(tabs) + "- " + entry.key, NamedTextColor.DARK_AQUA)
                            .append(Component.text(" - ", NamedTextColor.GRAY))
                            .append(Component.text(menu.options.name, NamedTextColor.GREEN))
                            .append(Component.newline())
                    )
                }
            } else {
                list.append(Component.text("  ".repeat(tabs) + "|- " + entry.key, NamedTextColor.DARK_AQUA))
                    .append(Component.newline())
                list.append(createMenuListForConsole(entry.value as Map<String, Any>, tabs + 1))
            }
        }

        return list.build()
    }

    @Suppress("UNCHECKED_CAST")
    private fun createMenuListForPlayer(tree: Map<String, Any>, tabs: Int): Component {
        val list = Component.text()

        for (entry in tree.entries) {
            if (entry.value !is MutableList<*>) {
                list.append(Component.text("  ".repeat(tabs) + "|-" + entry.key, NamedTextColor.DARK_AQUA))
                    .append(Component.newline())
                list.append(createMenuListForPlayer(entry.value as Map<String, Any>, tabs + 1))
                continue
            }

            for (menu in entry.value as MutableList<Menu>) {
                val menuCommand = getMenuDisplayCommand(menu)

                list.append(
                    Component.text("  ".repeat(tabs) + "- " + entry.key, NamedTextColor.DARK_AQUA)
                        .append(Component.text(" - ", NamedTextColor.GRAY))
                        .append(Component.text(menu.options.name, NamedTextColor.GREEN))
                        .hoverEvent(
                            HoverEvent.showText(
                                if (menuCommand != null) Component.text("Open Command: ", NamedTextColor.GOLD)
                                    .append(Component.text(menuCommand, NamedTextColor.YELLOW)
                                ) else Component.text("No open command", NamedTextColor.GOLD)
                            )
                        )
                        .clickEvent(ClickEvent.suggestCommand((menuCommand ?: "No open command")))
                ).append(Component.newline())
            }
        }

        return list.build()
    }

    private fun convertMenusToTree(menus: Map<String, List<Menu>>): Map<String, Any> {
        val tree = mutableMapOf<String, Any>()

        for (entry in menus.entries) {
            val path = entry.key.split("/")
            addMenuToTreeRecursively(tree, path, entry.value, 0)
        }

        return tree
    }

    @Suppress("UNCHECKED_CAST")
    private fun addMenuToTreeRecursively(
        tree: MutableMap<String, Any>,
        path: List<String>,
        menus: List<Menu>,
        step: Int
    ) {
        if (step < 0 || step >= path.size) return

        if (step == path.size - 1) {
            if (!tree.containsKey(path[step])) {
                tree[path[step]] = ArrayList<Menu>(menus)
            } else {
                val list = tree[path[step]] as MutableList<Menu>
                list.addAll(menus)
            }
            return
        }

        val value = path[step]
        if (!tree.containsKey(value)) tree[value] = TreeMap<Any, Any>()

        addMenuToTreeRecursively(
            (tree[value] as MutableMap<String, Any>),
            path,
            menus,
            step + 1
        )
    }

    /**
     * Get the command that can be used to open this menu.
     * The response will be the first command in the list of commands for this menu.
     * If the config option to use admin commands in menus list is enabled, the admin "/dm open" command will be returned.
     * @return The command that can be used to open this menu.
     */
    fun getMenuDisplayCommand(menu: Menu): String? {
        return if (plugin.generalConfig.useAdminCommandsInMenusList) "/deluxemenus open " + menu.options.name
        else if (menu.options.commands.isEmpty()) null
        else "/" + menu.options.commands[0]
    }

    companion object {
        private const val LIST_PERMISSION = "deluxemenus.list"
    }
}
