package com.extendedclip.deluxemenus.config

import com.extendedclip.deluxemenus.DeluxeMenus
import com.extendedclip.deluxemenus.action.ActionType
import com.extendedclip.deluxemenus.action.ClickAction
import com.extendedclip.deluxemenus.action.ClickActionTask
import com.extendedclip.deluxemenus.action.ClickHandler
import com.extendedclip.deluxemenus.hooks.ItemHook
import com.extendedclip.deluxemenus.menu.Menu
import com.extendedclip.deluxemenus.menu.MenuHolder
import com.extendedclip.deluxemenus.menu.MenuItem
import com.extendedclip.deluxemenus.menu.options.CustomModelDataComponent
import com.extendedclip.deluxemenus.menu.options.LoreAppendMode
import com.extendedclip.deluxemenus.menu.options.MenuItemOptions
import com.extendedclip.deluxemenus.menu.options.MenuOptions
import com.extendedclip.deluxemenus.requirement.*
import com.extendedclip.deluxemenus.requirement.wrappers.ItemWrapper
import com.extendedclip.deluxemenus.utils.Constants
import com.extendedclip.deluxemenus.utils.DebugLevel
import com.extendedclip.deluxemenus.utils.ItemUtils.isPlaceholderOption
import com.extendedclip.deluxemenus.utils.LocationUtils
import com.extendedclip.deluxemenus.utils.VersionHelper
import com.extendedclip.deluxemenus.utils.VersionHelper.VALID_INVENTORY_TYPES
import org.bukkit.DyeColor
import org.bukkit.Material
import org.bukkit.block.banner.Pattern
import org.bukkit.block.banner.PatternType
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemFlag
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.TreeMap
import java.util.logging.Level

class DeluxeMenusConfig(private val plugin: DeluxeMenus) {
    private val separator = File.separator
    val menuDirector: File
    private val exampleMenus = listOf("basics_menu", "advanced_menu", "requirements_menu") // more example menus here

    init {
        VALID_MATERIAL_PREFIXES.addAll(plugin.itemHooks.values.map(ItemHook::prefix))

        menuDirector = File(plugin.dataFolder.toString() + separator + "gui_menus")
        try {
            if (menuDirector.mkdirs()) {
                plugin.debug(
                    DebugLevel.HIGH,
                    Level.INFO,
                    "Individual menus directory did not exist.",
                    "Created directory: plugins" + separator + "DeluxeMenus" + separator + "gui_menus"
                )
            }
        } catch (_: SecurityException) {
            plugin.debug(
                DebugLevel.HIGHEST,
                Level.WARNING,
                "Something went wrong while creating directory: plugins" + separator + "DeluxeMenus" + separator + "gui_menus"
            )
        }
    }

    private fun getStringListFromConfig(config: FileConfiguration, path: String): List<String> {
        if (!config.contains(path)) return listOf()

        return if (config.isList(path)) config.getStringList(path)
        else listOf(config.getString(path, ""))
    }

    fun loadDefConfig(): Boolean {
        if (checkConfig(null, "config.yml", true) == null) {
            return false
        }

        plugin.getConfig().apply {
            options().setHeader(listOf(
                "DeluxeMenus " + plugin.description.version + " main configuration file",
                "",
                "A full wiki on how to use this plugin can be found at:",
                "https://wiki.helpch.at/helpchat-plugins/deluxemenus",
                ""
            ))
            addDefault("debug", "LOW")
            addDefault("check_updates", true)
            addDefault("use_admin_commands_in_menus_list", false)
            addDefault("menus_list_page_size", 10)
            options().copyDefaults(true)
            if (!contains("gui_menus")) {
                createMenuExamples(this)
            } else {
                plugin.saveConfig()
                plugin.reloadConfig()
            }
        }
        return true
    }

    private fun createMenuExamples(c: FileConfiguration) {
        for (name in exampleMenus) {
            val menuFile = File(menuDirector.path, "$name.yml")
            try {
                menuFile.createNewFile()
            } catch (e: IOException) {
                plugin.printStacktrace("Failed to create example menus!", e)
                continue
            }
            saveResourceToFile("$name.yml", menuFile)
            c.set("gui_menus.$name.file", menuFile.getName())
        }
        plugin.saveConfig()
        plugin.reloadConfig()
    }

    private fun saveResourceToFile(resource: String, file: File): Boolean {
        try {
            val stream = plugin.getResource(resource)
            val buffer = ByteArray(stream!!.available())
            stream.read(buffer)
            val os: OutputStream = FileOutputStream(file)
            os.write(buffer)
            return true
        } catch (e: Exception) {
            plugin.printStacktrace("Failed to update file: $resource", e)
            plugin.debug(
                DebugLevel.HIGHEST,
                Level.SEVERE,
                "Failed to save default settings for:" + file.getName() + " from resource:" + resource
            )
        }
        return false
    }

    fun checkConfig(folder: String?, fileName: String, create: Boolean): FileConfiguration? {
        val directory = if (folder != null)
            File(plugin.dataFolder.toString() + separator + folder + separator)
        else File(plugin.dataFolder.toString() + separator)

        try { if (!directory.exists()) return null }
        catch (_: SecurityException) { return null }

        val configFile = File(directory.path, fileName)
        if (create) {
            try {
                configFile.createNewFile()
            } catch (e: IOException) {
                plugin.printStacktrace("Failed to create file: $fileName", e)
                return null
            }
        }

        return if (!configFile.exists()) null
        else checkConfig(configFile)
    }

    private fun checkConfig(f: File): FileConfiguration? {
        val config: FileConfiguration = YamlConfiguration()

        try {
            config.load(f)
            return config
        } catch (e: Exception) {
            plugin.debug(DebugLevel.HIGHEST, Level.SEVERE, "Could not read file: " + f.getName())

            plugin.printStacktrace("Could not read file: " + f.getName(), e)
            return null
        }
    }

    fun loadGUIMenu(menu: String): Boolean {
        if (checkConfig(null, "config.yml", false) == null) return false

        val c = plugin.getConfig()
        if (!c.contains("gui_menus")) return false
        if (!c.isConfigurationSection("gui_menus")) return false

        val keys = c.getConfigurationSection("gui_menus")!!.getKeys(false)
        if (keys.isEmpty()) return false
        if (!keys.contains(menu)) return false

        if (c.contains("gui_menus.$menu.file")) {
            loadMenuFromFile(menu)
        } else {
            loadMenu(c, menu, true, "config")
        }

        return true
    }

    fun loadGUIMenus(): Int {
        if (checkConfig(null, "config.yml", false) == null) return 0

        val c = plugin.getConfig()
        if (!c.contains("gui_menus")) return 0
        if (!c.isConfigurationSection("gui_menus")) return 0

        val keys = c.getConfigurationSection("gui_menus")!!.getKeys(false)
        if (keys.isEmpty()) return 0

        for (key in keys) {
            if (c.contains("gui_menus.$key.file")) {
                loadMenuFromFile(key)
            } else {
                loadMenu(c, key, true, "config")
            }
        }
        return Menu.getLoadedMenuSize()
    }

    fun loadMenuFromFile(menuName: String): Boolean {
        val fileName = plugin.getConfig().getString("gui_menus.$menuName.file")!!

        if (!fileName.endsWith(".yml")) {
            plugin.debug(
                DebugLevel.HIGHEST,
                Level.SEVERE,
                "Filename specified for menu: $menuName is not a .yml file!",
                "Make sure that the file name to load this menu from is specified as a .yml file!",
                "Skipping loading of menu: $menuName"
            )
            return false
        }

        val f = File(menuDirector.path, fileName)

        if (!f.exists()) {
            plugin.debug(DebugLevel.HIGHEST, Level.INFO, f.getName() + " does not exist!")

            try {
                val folder = f.getParentFile()
                if (!folder.exists()) folder.mkdirs()

                f.createNewFile()
                if (!saveResourceToFile("default_menu.yml", f)) {
                    plugin.debug(
                        DebugLevel.HIGHEST,
                        Level.WARNING,
                        "Failed to create a default menu file for menu: $menuName",
                        "Skipping loading menu: $menuName"
                    )
                    return false
                }
                plugin.debug(
                    DebugLevel.HIGHEST,
                    Level.INFO,
                    f.getName() + " created! Add your menu options to this file and use /dm reload to load it!"
                )
            } catch (_: IOException) {
                plugin.debug(
                    DebugLevel.HIGHEST,
                    Level.SEVERE,
                    "Could not create menu file: plugins" + separator + "DeluxeMenus" + separator + "gui_menus" + separator + fileName
                )
                return false
            }
        }

        val cfg = checkConfig(f)

        if (cfg == null) {
            plugin.debug(
                DebugLevel.HIGHEST,
                Level.WARNING,
                "Menu: $menuName in file: $fileName not loaded."
            )
            return false
        }

        if (cfg.getKeys(false).isEmpty()) {
            plugin.debug(
                DebugLevel.HIGH,
                Level.INFO,
                "Menu config: " + f.getName() + " is empty! Creating default config example..."
            )
            saveResourceToFile("default_menu.yml", f)
            return false
        }

        val guiMenusPath = menuDirector.toPath()
        val menuPath = f.toPath()
        val relativePath = guiMenusPath.relativize(menuPath)

        loadMenu(cfg, menuName, false, relativePath.toString())
        return Menu.getMenuByName(menuName) != null
    }

    fun loadMenu(c: FileConfiguration, key: String, mainConfig: Boolean, path: String) {
        if (mainConfig) {
            plugin.debug(
                DebugLevel.HIGHEST,
                Level.WARNING,
                "Menu: $key does not have a file specified in config.yml! Creating menus in the config.yml file is deprecated and will be removed in a future version! Please migrate your menus to individual files in the gui_menus directory! For more information see: https://wiki.helpch.at/clips-plugins/deluxemenus/external-menus"
            )
        }

        var pre = "gui_menus.$key."

        if (!mainConfig) pre = ""

        if (!c.contains(pre + "menu_title")) {
            plugin.debug(
                DebugLevel.HIGHEST,
                Level.SEVERE,
                "Menu title for menu: $key is not present!",
                "Skipping menu: $key"
            )
            return
        }

        val title = if (c.isString(pre + "menu_title"))
            c.getString(pre + "menu_title")
        else if (c.isList(pre + "menu_title"))
            c.getStringList(pre + "menu_title")[0]
        else null

        if (title.isNullOrEmpty()) {
            plugin.debug(
                DebugLevel.HIGHEST,
                Level.SEVERE,
                "Menu title for menu: $key is invalid!",
                "Skipping menu: $key"
            )
            return
        }


        var type = InventoryType.CHEST

        if (c.contains(pre + "inventory_type")) {
            try {
                val inventoryType = InventoryType.valueOf(c.getString(pre + "inventory_type")!!.uppercase())
                type = if (!VALID_INVENTORY_TYPES.contains(inventoryType)) InventoryType.CHEST else inventoryType
            } catch (_: Exception) {
                plugin.debug(
                    DebugLevel.HIGHEST,
                    Level.WARNING,
                    "Inventory type for menu: $key is invalid!",
                    "Valid Inventory types: " + VALID_INVENTORY_TYPES.toTypedArray().contentToString(),
                    "Defaulting to CHEST inventory type."
                )
            }
        }

        val openCommands = mutableListOf<String>()

        if (c.contains(pre + "open_command")) {
            if (c.isString(pre + "open_command") && !c.getString(pre + "open_command")!!.isEmpty()) {
                val cmd = c.getString(pre + "open_command")

                if (cmd == null) {
                    plugin.debug(
                        DebugLevel.HIGHEST,
                        Level.SEVERE,
                        "open_command specified for menu: $key is null!",
                        "Skipping menu: $key"
                    )
                    return
                }

                if (Menu.isMenuCommand(cmd)) {
                    plugin.debug(
                        DebugLevel.HIGHEST,
                        Level.SEVERE,
                        "open_command specified for menu: $key already exists for another menu!",
                        "Skipping menu: $key"
                    )
                    return
                }

                openCommands.add(cmd.lowercase())
            } else if (c.isList(pre + "open_command") && !c.getStringList(pre + "open_command").isEmpty()) {
                val cmds = c.getStringList(pre + "open_command")

                for (cmd in cmds) {
                    if (Menu.isMenuCommand(cmd)) {
                        plugin.debug(
                            DebugLevel.HIGHEST,
                            Level.WARNING,
                            "command: $cmd specified for menu: $key already exists for another menu!",
                            "Skipping command: $cmd in menu: $key"
                        )
                    } else {
                        openCommands.add(cmd.lowercase())
                    }
                }
            }
        }

        val argumentNames = mutableListOf<String?>()
        val argumentRequirements = mutableListOf<RequirementList?>()

        if (c.contains(pre + "args")) {
            // New requirements parsing
            if (c.isConfigurationSection(pre + "args")) {
                val mapList = c.getConfigurationSection(pre + "args")!!.getKeys(false)
                debug("found args")
                for (arg in mapList) {
                    debug("arg: $arg")
                    // If it has requirements, add them
                    if (c.contains(pre + "args." + arg + ".requirements")) {
                        debug("arg has requirements: $arg")
                        argumentRequirements.add(getRequirements(c, pre + "args." + arg))
                    }
                    // Always add the arg itself
                    argumentNames.add(arg)
                }
                // Old list parsing
            } else if (c.isList(pre + "args")) {
                argumentNames.addAll(c.getStringList(pre + "args"))
                // Old singular item parsing
            } else if (c.isString(pre + "args")) {
                argumentNames.add(c.getString(pre + "args"))
            }
        }

        var size = 54
        if (type == InventoryType.CHEST) {
            if (!c.contains(pre + "size")) {
                plugin.debug(
                    DebugLevel.HIGHEST,
                    Level.INFO,
                    "Menu size for menu: $key is not present!",
                    "Using default size of 54"
                )
            } else {
                size = c.getInt(pre + "size")

                if ((size + 1) % 9 == 0) size++

                if ((size - 1) % 9 == 0) size--

                if (size < 9) {
                    plugin.debug(
                        DebugLevel.HIGHEST,
                        Level.INFO,
                        "Menu size for menu: $key is lower than 9",
                        "Defaulting to 9."
                    )
                    size = 9
                }

                if (size > 54) {
                    plugin.debug(
                        DebugLevel.HIGHEST,
                        Level.WARNING,
                        "Menu size for menu: $key is higher than 54",
                        "Defaulting to 54."
                    )
                    size = 54
                }

                if (size % 9 != 0) {
                    plugin.debug(
                        DebugLevel.HIGHEST,
                        Level.WARNING,
                        "Menu size for menu: $key is not a multiple of 9",
                        "Defaulting to 54."
                    )
                    size = 54
                }
            }
        } else {
            size = type.defaultSize
            plugin.debug(
                DebugLevel.LOWEST,
                Level.INFO,
                "TYPE IS: " + type + ". Setting size to:" + type.defaultSize
            )
        }

        val updateInterval = c.getInt(pre + "update_interval", 10)
        val refreshInterval = c.getInt(pre + "refresh_interval", 10)

        val items = loadMenuItems(c, key, mainConfig)

        if (items == null || items.isEmpty()) {
            plugin.debug(
                DebugLevel.HIGHEST,
                Level.SEVERE,
                "Failed to load menu items for menu: $key",
                "Skipping menu: $key"
            )
            return
        }

        val options = MenuOptions(
            name = key,
            title = title,
            type = type,
            size = size,

            commands = openCommands,
            registerCommands = openCommands.isNotEmpty() && c.getBoolean(pre + "register_command", false),

            arguments = argumentNames.filterNotNull(),
            argumentRequirements = argumentRequirements.filterNotNull(),
            argumentsUsageMessage = c.getString(pre + "args_usage_message", null),

            openRequirements = getRequirements(c, pre + "open_requirement"),
            openHandler = getClickHandler(c, pre + "open_commands"),
            closeHandler = getClickHandler(c, pre + "close_commands"),

            updateInterval = if (updateInterval > 0) updateInterval else 10,
            refreshInterval = if (refreshInterval > 0) refreshInterval else 10,
            refresh = c.getBoolean(pre + "refresh", false),

            parsePlaceholdersInArguments = c.getBoolean(pre + "arguments_support_placeholders", false),
            parsePlaceholdersAfterArguments = c.getBoolean(pre + "parse_placeholders_after_arguments", false),
            enableBypassPerm = c.getBoolean(pre + "enable_open_requirements_bypass_permissions", false)
        )
        // Don't need to register the menu since it's done in the constructor
        Menu(plugin, options, items, path)
    }

    private fun loadMenuItems(
        c: FileConfiguration,
        name: String,
        mainConfig: Boolean
    ): Map<Int, TreeMap<Int, MenuItem>>? {
        val itemsPath = if (mainConfig) "gui_menus.$name.items" else "items"

        if (!c.isConfigurationSection(itemsPath)) return null

        val itemKeys = c.getConfigurationSection(itemsPath)!!.getKeys(false)
        if (itemKeys.isEmpty()) return null

        val menuItems = mutableMapOf<Int, TreeMap<Int, MenuItem>>()

        for (key in itemKeys) {
            val currentPath = "$itemsPath.$key."

            if (!c.contains(currentPath + "material")) {
                plugin.debug(
                    DebugLevel.HIGHEST,
                    Level.WARNING,
                    "Material for item: $key in menu: $name is not present!",
                    "Skipping item: $key"
                )
                continue
            }

            val material = c.getString(currentPath + "material")
            val lowercaseMaterial = material!!.lowercase()
            if (!isValidMaterial(lowercaseMaterial)) {
                plugin.debug(
                    DebugLevel.HIGHEST,
                    Level.WARNING,
                    "Material for item: $key in menu: $name is not valid!",
                    "Skipping item: $key"
                )
                continue
            }

            checkForDeprecatedItemOptions(c.getConfigurationSection(currentPath)!!, name)

            var customModelDataComponent: CustomModelDataComponent? = null
            if (c.contains(currentPath + "model_data_component") && c.isConfigurationSection(currentPath + "model_data_component")) {
                val modelDataComponent = c.getConfigurationSection(currentPath + "model_data_component")
                if (modelDataComponent != null) {
                    customModelDataComponent = CustomModelDataComponent(
                        colors = modelDataComponent.getStringList("colors"),
                        flags = modelDataComponent.getStringList("flags"),
                        floats = modelDataComponent.getStringList("floats"),
                        strings = modelDataComponent.getStringList("strings")
                    )
                }
            }

            // Lore Append Mode
            var loreAppendMode: LoreAppendMode? = null
            if (c.contains(currentPath + "lore_append_mode")) {
                val loreAppendModeStr = c.getString(currentPath + "lore_append_mode", "OVERRIDE")!!.uppercase()
                loreAppendMode = try { LoreAppendMode.valueOf(loreAppendModeStr) }
                catch (_: Exception) {
                    plugin.debug(
                        DebugLevel.HIGHEST,
                        Level.WARNING,
                        "Lore append mode: $loreAppendModeStr for item: $key in menu: $name is not a valid lore append mode!"
                    )
                    LoreAppendMode.OVERRIDE // Defaults to override in case of invalid append mode
                }
            }

            // item flags
            val itemFlags = mutableSetOf<ItemFlag>()
            if (c.contains(currentPath + "item_flags")) {

                for (flagAsString in getStringListFromConfig(c, currentPath + "item_flags")) {
                    val flag = ItemFlag.entries.find { it.name == flagAsString.uppercase() }

                    if (flag == null) {
                        plugin.debug(
                            DebugLevel.HIGHEST,
                            Level.WARNING,
                            "Item flag: $flagAsString for item: $key in menu: $name is not a valid item flag!"
                        )
                        continue
                    }

                    itemFlags.add(flag)
                }
            }
            if (c.getBoolean(currentPath + "hide_attributes")) itemFlags.add(ItemFlag.HIDE_ATTRIBUTES)
            if (c.getBoolean(currentPath + "hide_unbreakable")) itemFlags.add(ItemFlag.HIDE_UNBREAKABLE)
            if (c.getBoolean(currentPath + "hide_enchantments")) itemFlags.add(ItemFlag.HIDE_ENCHANTS)

            val bannerMeta = mutableListOf<Pattern>()
            if (c.contains(currentPath + "banner_meta") && c.isList(currentPath + "banner_meta")) {
                for (e in c.getStringList(currentPath + "banner_meta")) {
                    if (!e.contains(";")) {
                        plugin.debug(
                            DebugLevel.HIGHEST,
                            Level.WARNING,
                            "Banner Meta for item: $key, meta entry: $e is invalid! Skipping this entry!"
                        )
                        continue
                    }

                    val metaParts = e.split(";", limit = 2)

                    if (metaParts.size != 2) {
                        plugin.debug(
                            DebugLevel.HIGHEST,
                            Level.WARNING,
                            "Banner Meta for item: $key, meta entry: $e is invalid! Skipping this entry!"
                        )
                        continue
                    }

                    val color: DyeColor
                    val type: PatternType

                    try {
                        color = DyeColor.valueOf(metaParts[0].uppercase())
                        type = PatternType.valueOf(metaParts[1].uppercase())
                    } catch (exception: IllegalArgumentException) {
                        plugin.debug(
                            DebugLevel.HIGHEST,
                            Level.WARNING,
                            "Banner Meta for item: $key, meta entry: $e is invalid! Skipping this entry!"
                        )

                        plugin.printStacktrace(
                            "Banner Meta for item: $key, meta entry: $e is invalid! Skipping this entry!",
                            exception
                        )
                        continue
                    }

                    bannerMeta.add(Pattern(color, type))
                }
            }

            val potionEffects = mutableListOf<PotionEffect>()
            if (c.contains(currentPath + "potion_effects") && c.isList(currentPath + "potion_effects")) {
                for (e in c.getStringList(currentPath + "potion_effects")) {
                    try {
                        if (!e.contains(";")) {
                            plugin.debug(
                                DebugLevel.HIGHEST,
                                Level.WARNING,
                                "Potion Meta for item: $key, meta entry: $e is invalid! Skipping this entry!"
                            )
                            continue
                        }

                        val metaParts = e.split(";", limit = 3)

                        if (metaParts.size != 3) {
                            plugin.debug(
                                DebugLevel.HIGHEST,
                                Level.WARNING,
                                "Potion Meta for item: $key, meta entry: $e is invalid! Skipping this entry!"
                            )
                            continue
                        }

                        val type = PotionEffectType.getByName(metaParts[0])
                        val duration = metaParts[1].toInt()
                        val amplifier = metaParts[2].toInt()

                        if (type == null) {
                            plugin.debug(
                                DebugLevel.HIGHEST,
                                Level.WARNING,
                                "Potion Meta for item: $key, meta entry: $e is invalid! Skipping this entry!"
                            )
                            continue
                        }

                        potionEffects.add(type.createEffect(duration, amplifier))
                    } catch (_: Exception) {
                        plugin.debug(
                            DebugLevel.HIGHEST,
                            Level.WARNING,
                            "Potion Meta for item: $key, meta entry: $e is invalid! Skipping this entry!"
                        )
                    }
                }
            }

            val slots = mutableListOf<Int>()

            if (c.contains(currentPath + "slots") && c.isList(currentPath + "slots")) {
                val confSlots = c.getStringList(currentPath + "slots")
                for (slot in confSlots) {
                    val values = slot.split("-", limit = 2)
                    if (values.size == 2) {
                        for (i in values[0].toInt()..values[1].toInt()) {
                            slots.add(i)
                        }
                    } else slots.add(slot.toInt())
                }
            } else slots.add(c.getInt(currentPath + "slot", 0))



            val displayName = c.getString(currentPath + "display_name")
            val lore = c.getStringList(currentPath + "lore")

            val options = MenuItemOptions(
                material = material,
                headType = null,
                slot = c.getInt(currentPath + "slot", 0),
                priority = c.getInt(currentPath + "priority", 1),
                updatePlaceholders = c.getBoolean(currentPath + "update", false),

                displayName = displayName,
                displayNameHasPlaceholders = displayName != null && containsPlaceholders(displayName),

                hasLore = c.contains(currentPath + "lore"),
                lore = lore,
                loreHasPlaceholders = lore.any { containsPlaceholders(it) },
                loreAppendMode = loreAppendMode,

                amount = c.getInt(currentPath + "amount", -1),
                dynamicAmount = c.getString(currentPath + "dynamic_amount", null),

                damage = getDamageOption(c, currentPath, key, name),
                enchantments = getEnchantmentsOption(c, currentPath, key, name),

                itemFlags = itemFlags,
                unbreakable = c.getBoolean(currentPath + "unbreakable", false),
                hideTooltip = c.getString(currentPath + "hide_tooltip", null),
                tooltipStyle = c.getString(currentPath + "tooltip_style", null),
                enchantmentGlintOverride = c.getString(currentPath + "enchantment_glint_override", null),
                rarity = c.getString(currentPath + "rarity", null),

                itemModel = c.getString(currentPath + "item_model", null),
                customModelData = c.getString(currentPath + "model_data", null),
                customModelDataComponent = customModelDataComponent,

                trimMaterial = c.getString(currentPath + "trim_material", null),
                trimPattern = c.getString(currentPath + "trim_pattern", null),

                bannerMeta = bannerMeta,
                baseColor = c.getString(currentPath + "base_color")?.uppercase()?.let { DyeColor.valueOf(it) },

                potionEffects = potionEffects,
                rgb = c.getString(currentPath + "rgb", null),

                lightLevel = c.getString(currentPath + "light_level", null),

                nbtString = c.getString(currentPath + "nbt_string", null),
                nbtStrings = c.getStringList(currentPath + "nbt_strings"),

                nbtByte = c.getString(currentPath + "nbt_byte", null),
                nbtBytes = c.getStringList(currentPath + "nbt_bytes"),

                nbtShort = c.getString(currentPath + "nbt_short", null),
                nbtShorts = c.getStringList(currentPath + "nbt_shorts"),

                nbtInt = c.getString(currentPath + "nbt_int", null),
                nbtInts = c.getStringList(currentPath + "nbt_ints"),

                viewRequirements = getRequirements(c, currentPath + "view_requirement"),

                clickHandler = getClickHandler(c, currentPath + "click_commands"),
                clickRequirements = getRequirements(c, currentPath + "click_requirement"),

                leftClickHandler = getClickHandler(c, currentPath + "left_click_commands"),
                leftClickRequirements = getRequirements(c, currentPath + "left_click_requirement"),

                rightClickHandler = getClickHandler(c, currentPath + "right_click_commands"),
                rightClickRequirements = getRequirements(c, currentPath + "right_click_requirement"),

                shiftLeftClickHandler = getClickHandler(c, currentPath + "shift_left_click_commands"),
                shiftLeftClickRequirements = getRequirements(c, currentPath + "shift_left_click_requirement"),

                shiftRightClickHandler = getClickHandler(c, currentPath + "shift_right_click_commands"),
                shiftRightClickRequirements = getRequirements(c, currentPath + "shift_right_click_requirement"),

                middleClickHandler = getClickHandler(c, currentPath + "middle_click_commands"),
                middleClickRequirements = getRequirements(c, currentPath + "middle_click_requirement"),
            )
            val menuItem = MenuItem(plugin, options)

            for (slot in slots) {
                val slotPriorityMap: TreeMap<Int, MenuItem>
                if (!menuItems.containsKey(slot) || menuItems[slot] == null) {
                    slotPriorityMap = TreeMap<Int, MenuItem>()
                    menuItems[slot] = slotPriorityMap
                } else {
                    slotPriorityMap = menuItems[slot]!!
                }
                slotPriorityMap[menuItem.options.priority] = MenuItem(plugin, menuItem.options.copy(slot = slot))
            }
        }

        return menuItems
    }

    private fun getRequirements(c: FileConfiguration, path: String): RequirementList? {
        if (!c.contains("$path.requirements")) return null
        debug("requirement path: $path")

        val requirements = mutableListOf<Requirement>()


        debug("found requirements list")

        for (key in c.getConfigurationSection("$path.requirements")!!.getKeys(false)) {
            debug("requirement: $key from requirements list")
            val rPath = "$path.requirements.$key"
            if (!c.contains("$rPath.type")) {
                plugin.debug(
                    DebugLevel.HIGHEST,
                    Level.WARNING,
                    "No type set for requirement: $key for path: $rPath"
                )
                continue
            }

            val stringType = c.getString("$rPath.type")
            val type = RequirementType.getType(stringType!!)

            if (type == null) {
                plugin.debug(
                    DebugLevel.HIGHEST,
                    Level.WARNING,
                    "Requirement type '$stringType' at path '$rPath' is not valid!"
                )
                continue
            }

            debug("Requirement type: " + type.name)

            var req: Requirement? = null

            val invert: Boolean
            when (type) {
                RequirementType.HAS_ITEM, RequirementType.DOES_NOT_HAVE_ITEM -> {
                    val wrapper = ItemWrapper()
                    if (c.contains("$rPath.material")) {
                        val materialName = c.getString("$rPath.material")
                        try {
                            if (!containsPlaceholders(materialName!!) && plugin.itemHooks.values.find { materialName.lowercase().startsWith(it.prefix) } == null)
                                Material.valueOf(materialName.uppercase())
                        } catch (_: Exception) {
                            plugin.debug(
                                DebugLevel.HIGHEST,
                                Level.WARNING,
                                "has item requirement at path: $rPath does not specify a valid Material name!"
                            )
                            break
                        }
                        wrapper.material = materialName
                    } else {
                        plugin.debug(
                            DebugLevel.HIGHEST,
                            Level.WARNING,
                            "has item requirement at path: $rPath does not contain a material: entry!"
                        )
                        break
                    }

                    wrapper.apply {
                        amount = c.getInt("$rPath.amount", 1)
                        hasData = c.contains("$rPath.data") && c.isInt("$rPath.data")
                        data = c.getInt("$rPath.data", 0).toShort()

                        if (c.isString("$rPath.name")) {
                            name = c.getString("$rPath.name")
                        }
                        if (c.isString("$rPath.lore")) {
                            lore = c.getString("$rPath.lore")
                        }
                        if (c.isList("$rPath.lore")) {
                            loreList = c.getStringList("$rPath.lore")
                        }

                        isStrict = c.getBoolean("$rPath.strict")

                        checkArmor = c.getBoolean("$rPath.armor")
                        checkOffHand = c.getBoolean("$rPath.offhand")

                        // TODO: Remove support for the old options in v1.14.0
                        customData = if (c.contains("$rPath.model_data") && c.isInt("$rPath.model_data"))
                            c.getInt("$rPath.model_data")
                        else c.getInt("$rPath.modeldata", 0)
                    }


                    if (c.contains("$rPath.model_data_component") && c.isConfigurationSection("$rPath.model_data_component")) {
                        val modelDataComponent = c.getConfigurationSection("$rPath.model_data_component")
                        if (modelDataComponent != null) {
                            wrapper.customModelDataComponent = CustomModelDataComponent(
                                colors = modelDataComponent.getStringList("colors"),
                                flags = modelDataComponent.getStringList("flags"),
                                floats = modelDataComponent.getStringList("floats"),
                                strings = modelDataComponent.getStringList("strings")
                            )
                        }
                    }

                    if (c.contains("$rPath.name_contains")) {
                        wrapper.checkNameContains = c.getBoolean("$rPath.name_contains")
                    } else {
                        wrapper.checkNameContains = c.getBoolean("$rPath.name-contains")
                    }

                    if (c.contains("$rPath.name_ignorecase")) {
                        wrapper.checkNameContains = c.getBoolean("$rPath.name_ignorecase")
                    } else {
                        wrapper.checkNameContains = c.getBoolean("$rPath.name-ignorecase")
                    }

                    if (c.contains("$rPath.lore_contains")) {
                        wrapper.checkLoreContains = c.getBoolean("$rPath.lore_contains")
                    } else {
                        wrapper.checkLoreContains = c.getBoolean("$rPath.lore-contains")
                    }

                    if (c.contains("$rPath.lore_ignorecase")) {
                        wrapper.checkLoreContains = c.getBoolean("$rPath.lore_ignorecase")
                    } else {
                        wrapper.checkLoreContains = c.getBoolean("$rPath.lore-ignorecase")
                    }

                    invert = type == RequirementType.DOES_NOT_HAVE_ITEM
                    req = HasItemRequirement(plugin, wrapper, invert)
                }

                RequirementType.HAS_PERMISSION, RequirementType.DOES_NOT_HAVE_PERMISSION -> if (c.contains("$rPath.permission")) {
                    invert = type == RequirementType.DOES_NOT_HAVE_PERMISSION
                    req = HasPermissionRequirement(c.getString("$rPath.permission")!!, invert)
                } else {
                    plugin.debug(
                        DebugLevel.HIGHEST,
                        Level.WARNING,
                        "Has Permission requirement at path: $rPath does not contain a permission: entry"
                    )
                }

                RequirementType.HAS_PERMISSIONS, RequirementType.DOES_NOT_HAVE_PERMISSIONS -> if (c.contains("$rPath.permissions")) {
                    invert = type == RequirementType.DOES_NOT_HAVE_PERMISSIONS
                    var minimum = -1
                    if (c.contains("$rPath.minimum") && (c.getInt("$rPath.minimum").also { minimum = it }) < 1) {
                        plugin.debug(
                            DebugLevel.HIGHEST,
                            Level.WARNING,
                            "Has Permissions requirement at path: $rPath has a minimum lower than 1. All permissions will be checked"
                        )
                        minimum = -1
                    }
                    val permissions = c.getStringList("$rPath.permissions")
                    if (permissions.isEmpty()) {
                        plugin.debug(
                            DebugLevel.HIGHEST,
                            Level.WARNING,
                            "Has Permissions requirement at path: $rPath has no permissions to check. Ignoring..."
                        )
                        break
                    } else if (minimum > permissions.size) {
                        plugin.debug(
                            DebugLevel.HIGHEST,
                            Level.WARNING,
                            "Has Permissions requirement at path: " + rPath + " has a minimum higher than the amount of permissions. Using " + permissions.size + " instead"
                        )
                        minimum = permissions.size
                    }
                    req = HasPermissionsRequirement(permissions, minimum, invert)
                } else {
                    plugin.debug(
                        DebugLevel.HIGHEST,
                        Level.WARNING,
                        "Has Permissions requirement at path: $rPath does not contain permissions: entry"
                    )
                }

                RequirementType.JAVASCRIPT -> if (c.contains("$rPath.expression")) {
                    req = JavascriptRequirement(plugin, c.getString("$rPath.expression")!!)
                } else {
                    plugin.debug(
                        DebugLevel.HIGHEST,
                        Level.WARNING,
                        "Javascript requirement at path: $rPath does not contain an expression: entry"
                    )
                }

                RequirementType.EQUAL_TO, RequirementType.NOT_EQUAL_TO,
                RequirementType.GREATER_THAN, RequirementType.GREATER_THAN_EQUAL_TO,
                RequirementType.LESS_THAN, RequirementType.LESS_THAN_EQUAL_TO,
                RequirementType.STRING_CONTAINS, RequirementType.STRING_DOES_NOT_CONTAIN,
                RequirementType.STRING_EQUALS, RequirementType.STRING_DOES_NOT_EQUAL,
                RequirementType.STRING_EQUALS_IGNORECASE, RequirementType.STRING_DOES_NOT_EQUAL_IGNORECASE -> if (c.contains("$rPath.input") && c.contains("$rPath.output")) {
                    req = InputResultRequirement(type, c.getString("$rPath.input")!!, c.getString("$rPath.output")!!)
                } else {
                    plugin.debug(
                        DebugLevel.HIGHEST,
                        Level.WARNING,
                        "Requirement at path: $rPath does not contain the input: and/or the output: entries"
                    )
                }

                RequirementType.HAS_MONEY, RequirementType.DOES_NOT_HAVE_MONEY -> if (c.contains("$rPath.amount") || c.contains("$rPath.placeholder")) {
                    invert = type == RequirementType.DOES_NOT_HAVE_MONEY
                    req = HasMoneyRequirement(
                        plugin,
                        c.getDouble("$rPath.amount"),
                        invert,
                        c.getString("$rPath.placeholder", null)
                    )
                } else {
                    plugin.debug(
                        DebugLevel.HIGHEST,
                        Level.WARNING,
                        "Has Money requirement at path: $rPath does not contain an amount: entry"
                    )
                }

                RequirementType.HAS_EXP, RequirementType.DOES_NOT_HAVE_EXP -> if (c.contains("$rPath.amount")) {
                    if (!containsPlaceholders(c.getString("$rPath.amount")!!) && !c.isInt("$rPath.amount")) {
                        plugin.debug(
                            DebugLevel.HIGHEST,
                            Level.WARNING,
                            "Value at path: $rPath.amount is not a placeholder or a number"
                        )
                        break
                    }
                    invert = type == RequirementType.DOES_NOT_HAVE_EXP
                    req = HasExpRequirement(
                        plugin,
                        c.getString("$rPath.amount")!!,
                        invert,
                        c.getBoolean("$rPath.level")
                    )
                } else {
                    plugin.debug(
                        DebugLevel.HIGHEST,
                        Level.WARNING,
                        "Has Exp requirement at path: $rPath does not contain an amount: entry"
                    )
                }

                RequirementType.REGEX_MATCHES, RequirementType.REGEX_DOES_NOT_MATCH -> if (c.contains("$rPath.input") && c.contains("$rPath.regex")) {
                    val p = java.util.regex.Pattern.compile(c.getString("$rPath.regex")!!)
                    invert = type == RequirementType.REGEX_DOES_NOT_MATCH
                    req = RegexMatchesRequirement(p, c.getString("$rPath.input")!!, invert)
                } else {
                    plugin.debug(
                        DebugLevel.HIGHEST,
                        Level.WARNING,
                        "Regex requirement at path: $rPath does not contain a input: or regex: entry"
                    )
                }

                RequirementType.IS_NEAR, RequirementType.IS_NOT_NEAR -> if (c.contains("$rPath.location") && c.contains("$rPath.distance")) {
                    invert = type == RequirementType.IS_NOT_NEAR
                    val loc = LocationUtils.deserializeLocation(c.getString("$rPath.location")!!)
                    if (loc == null) {
                        plugin.debug(
                            DebugLevel.HIGHEST,
                            Level.WARNING,
                            "requirement at path: $rPath has an invalid location. Valid Format is: <world>,<x>,<y>,<z>"
                        )
                    }
                    req = IsNearRequirement(loc!!, c.getInt("$rPath.distance"), invert)
                } else {
                    plugin.debug(
                        DebugLevel.HIGHEST,
                        Level.WARNING,
                        "Is Near requirement at path: $rPath does not contain a location: or distance: entry"
                    )
                }

                RequirementType.HAS_META, RequirementType.DOES_NOT_HAVE_META -> {
                    if (!VersionHelper.IS_PDC_VERSION) {
                        plugin.debug(
                            DebugLevel.HIGHEST,
                            Level.WARNING,
                            "Has Meta requirement is not available for your server version!"
                        )
                        break
                    }
                    if (c.contains("$rPath.key") && c.contains("$rPath.meta_type") && c.contains("$rPath.value")) {
                        val metaKey = c.getString("$rPath.key")
                        invert = type == RequirementType.DOES_NOT_HAVE_META
                        req = HasMetaRequirement(
                            plugin,
                            metaKey!!,
                            c.getString("$rPath.meta_type")!!.uppercase(),
                            c.getString("$rPath.value")!!,
                            invert
                        )
                    } else {
                        plugin.debug(
                            DebugLevel.HIGHEST,
                            Level.WARNING,
                            "Has Meta requirement at path: $rPath does not contain the key:, meta_type: and/or value: entries!"
                        )
                    }
                }

                RequirementType.STRING_LENGTH -> if (c.contains("$rPath.input") && (c.contains("$rPath.min") || c.contains("$rPath.max"))) {
                    val min = c.getInt("$rPath.min", 0)
                    var max: Int? = null
                    if (c.contains("$rPath.max")) {
                        max = c.getInt("$rPath.max")
                    }
                    req = StringLengthRequirement(c.getString("$rPath.input")!!, min, max)
                } else {
                    plugin.debug(
                        DebugLevel.HIGHEST,
                        Level.WARNING,
                        "String length requirement at path: $rPath does not contain an input: or one of (min: or max:)"
                    )
                }

                RequirementType.IS_OBJECT -> if (c.contains("$rPath.input") && c.contains("$rPath.object")) {
                    req = IsObjectRequirement(c.getString("$rPath.input")!!, c.getString("$rPath.object")!!)
                } else {
                    plugin.debug(
                        DebugLevel.HIGHEST,
                        Level.WARNING,
                        "String length requirement at path: $rPath does not contain an input: or object:"
                    )
                }
            }

            if (req != null) {
                if (c.contains("$rPath.success_commands")) {
                    debug("Requirement has success commands")
                    req.successHandler = getClickHandler(c, "$rPath.success_commands")
                }
                if (c.contains("$rPath.deny_commands")) {
                    debug("Requirement has deny commands")
                    req.denyHandler = getClickHandler(c, "$rPath.deny_commands")
                }
                req.isOptional = c.getBoolean("$rPath.optional")
                requirements.add(req)
            }
        }

        if (requirements.isEmpty()) return null

        val list = RequirementList(requirements)

        if (c.contains("$path.deny_commands")) {
            debug("global deny handler found")
            list.denyHandler = getClickHandler(c, "$path.deny_commands")
        }

        list.stopAtSuccess = c.getBoolean("$path.stop_at_success")

        list.minimumRequirements = if (c.contains("$path.minimum_requirements"))
             c.getInt("$path.minimum_requirements")
        else requirements.count { !it.isOptional }

        return list
    }

    private fun getClickHandler(c: FileConfiguration, configPath: String): ClickHandler? {
        val commands = c.getStringList(configPath)

        if (commands.isEmpty()) return null

        val actions = mutableListOf<ClickAction>()

        for (command in commands) {
            if (command.isNullOrEmpty()) continue
            var command = command.trim()

            val type = ActionType.getByStart(command) ?: continue
            command = command.replaceFirst(type.identifier, "")

            val action = ClickAction(type, command)

            val d = DELAY_MATCHER.matcher(command)
            if (d.find()) {
                action.delay = d.group(1)
                command = command.replaceFirst(d.group(), "")
            }

            val ch = CHANCE_MATCHER.matcher(command)
            if (ch.find()) {
                action.chance = ch.group(1)
                command = command.replaceFirst(ch.group(), "")
            }

            action.executable = command
            actions.add(action)
        }

        var handler: ClickHandler? = null

        if (!actions.isEmpty()) {
            handler = object : ClickHandler {
                override fun onClick(holder: MenuHolder) {
                    for (action in actions) {
                        if (!action.checkChance(holder)) continue

                        val actionTask = ClickActionTask(
                            plugin,
                            holder.viewer.uniqueId,
                            action.type,
                            action.executable,
                            holder.typedArgs,
                            holder.parsePlaceholdersInArguments,
                            holder.parsePlaceholdersAfterArguments
                        )

                        if (action.hasDelay()) {
                            actionTask.runTaskLater(plugin, action.getDelay(holder))
                            continue
                        }

                        actionTask.runTask(plugin)
                    }
                }
            }
        }

        return handler
    }

    private fun checkForDeprecatedItemOptions(config: ConfigurationSection, menuName: String?) {
        fun oldItemFlagOptionCheck(option: String, itemFlag: ItemFlag) {
            if (config.contains(option)) plugin.debug(
                DebugLevel.HIGHEST,
                Level.WARNING,
                "Option '$option' of item '${config.name}' in menu '$menuName' is deprecated and will be removed in the future. Replace it with item_flags: [$itemFlag].",
            )
        }
        oldItemFlagOptionCheck("hide_attributes", ItemFlag.HIDE_ATTRIBUTES)
        oldItemFlagOptionCheck("hide_enchantments", ItemFlag.HIDE_ENCHANTS)
        oldItemFlagOptionCheck("hide_unbreakable", ItemFlag.HIDE_UNBREAKABLE)
    }

    fun debug(vararg messages: String) = plugin.debug(DebugLevel.LOWEST, Level.INFO, *messages)

    fun debugLevel(): DebugLevel {
        var stringLevel: String = plugin.getConfig().getString("debug", "HIGHEST")!!

        if (stringLevel.equals("true", ignoreCase = true)) {
            stringLevel = "LOWEST"
            plugin.getConfig().set("debug", "LOWEST")
        } else if (stringLevel.equals("false", ignoreCase = true)) {
            stringLevel = "HIGHEST"
            plugin.getConfig().set("debug", "HIGHEST")
        }

        return DebugLevel.getByName(stringLevel) ?: DebugLevel.LOW
    }

    fun getEnchantmentsOption(
        c: FileConfiguration,
        currentPath: String,
        itemKey: String,
        menuName: String
    ): Map<Enchantment, Int> {
        if (!c.contains(currentPath + "enchantments")) return mapOf()

        val configEnchantments = c.getStringList(currentPath + "enchantments")
        val parsedEnchantments = mutableMapOf<Enchantment, Int>()

        for (configEnchantment in configEnchantments) {
            if (configEnchantment == null || !configEnchantment.contains(";")) {
                plugin.debug(
                    DebugLevel.HIGHEST,
                    Level.WARNING,
                    "Enchantment format '$configEnchantment' is incorrect for item $itemKey in GUI $menuName!",
                    "Correct format: - '<Enchantment name>;<level>"
                )
                continue
            }

            val parts = configEnchantment.split(";", limit = 2)
            if (parts.size != 2) {
                plugin.debug(
                    DebugLevel.HIGHEST,
                    Level.WARNING,
                    "Enchantment format '$configEnchantment' is incorrect for item $itemKey in GUI $menuName!",
                    "Correct format: - '<Enchantment name>;<level>"
                )
                continue
            }

            val enchantment = Enchantment.getByName(parts[0].trim().uppercase())
            if (enchantment == null) {
                plugin.debug(
                    DebugLevel.HIGHEST,
                    Level.WARNING,
                    "Enchantment '${parts[0].trim()}' for item $itemKey in menu $menuName is not a valid enchantment name!"
                )
                continue
            }

            var level = parts[1].toIntOrNull()
            if (level == null) {
                level = 1
                plugin.debug(
                    DebugLevel.HIGHEST,
                    Level.WARNING,
                    "Enchantment level '${parts[1].trim()}' is incorrect for item $itemKey in menu $menuName!"
                )
            }
            parsedEnchantments[enchantment] = level
        }
        return parsedEnchantments
    }

    fun getDamageOption(
        c: FileConfiguration,
        currentPath: String,
        itemKey: String,
        menuName: String,
    ): String? {
        var damageOptionIsPresent = false
        var damageValue: String? = null

        var key = "damage"
        if (c.contains(currentPath + key)) {
            damageOptionIsPresent = true
            damageValue = c.getString(currentPath + key, "")
        }

        key = "data"
        if (c.contains(currentPath + key)) {
            if (!damageOptionIsPresent) {
                plugin.debug(
                    DebugLevel.HIGHEST,
                    Level.WARNING,
                    "Found 'data' option for item: $itemKey in menu: $menuName. This option is deprecated and will be removed soon. Please use 'damage' instead."
                )
                damageValue = c.getString(currentPath + key, "")
            } else {
                plugin.debug(
                    DebugLevel.HIGHEST,
                    Level.WARNING,
                    "Found 'data' and 'damage' options for item: $itemKey in menu: $menuName. 'data' option is deprecated and will be ignored. Using 'damage' instead."
                )
            }
        }

        if (damageValue == null) return null

        if (damageOptionIsPresent) key = "damage"

        if (!isPlaceholderOption(damageValue) && damageValue.toIntOrNull() == null) {
            plugin.debug(
                DebugLevel.HIGHEST,
                Level.WARNING,
                "Found invalid value for '$key' option for item: $itemKey in menu: $menuName.",
                "The correct formats for '$key' are:",
                "  -> <number>",
                "  -> placeholder-<placeholder>",
                "Ignoring the invalid value."
            )
            return null
        }

        val parts = damageValue.split("-", limit = 2)
        if (parts.size >= 2 && !containsPlaceholders(parts[1])) {
            plugin.debug(
                DebugLevel.HIGHEST,
                Level.WARNING,
                "Could not find placeholder for '$key' option for item: $itemKey in menu: $menuName.",
                "Ignoring the invalid value."
            )
            return null
        }

        return if (parts.size == 1) parts[0] else parts[1]
    }

    companion object {
        val VALID_MATERIALS = Constants.PLAYER_ITEMS.toMutableList().apply { add(Constants.WATER_BOTTLE) }
        val VALID_MATERIAL_PREFIXES = mutableListOf(Constants.PLACEHOLDER_PREFIX, Constants.STACK_PREFIX)
        val DELAY_MATCHER = java.util.regex.Pattern.compile("<delay=([^<>]+)>", java.util.regex.Pattern.CASE_INSENSITIVE)!!
        val CHANCE_MATCHER = java.util.regex.Pattern.compile("<chance=([^<>]+)>", java.util.regex.Pattern.CASE_INSENSITIVE)!!
        val PLACEHOLDER_PATTERN = java.util.regex.Pattern.compile("%((?<identifier>[a-zA-Z0-9]+)_)(?<parameters>[^%]+)%")!!

        private fun isValidMaterial(material: String): Boolean {
            val lowercaseMaterial = material.lowercase()
            if (VALID_MATERIALS.contains(lowercaseMaterial)) return true

            if (Material.getMaterial(material.uppercase()) != null) return true

            for (prefix in VALID_MATERIAL_PREFIXES) {
                if (lowercaseMaterial.startsWith(prefix)) {
                    return lowercaseMaterial.split("-").size >= 2
                }
            }

            return false
        }

        fun containsPlaceholders(text: String) = PLACEHOLDER_PATTERN.matcher(text).find()
    }
}
