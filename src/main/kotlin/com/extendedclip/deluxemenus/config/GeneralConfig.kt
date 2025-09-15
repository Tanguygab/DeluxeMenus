package com.extendedclip.deluxemenus.config

import com.extendedclip.deluxemenus.DeluxeMenus
import com.extendedclip.deluxemenus.utils.DebugLevel

class GeneralConfig(private val plugin: DeluxeMenus) {
    var checkForUpdates = true
    private val defaultDebugLevel = DebugLevel.LOW
    var debugLevel = defaultDebugLevel
    var useAdminCommandsInMenusList = false
    var menusListPageSize = 10
    var metasListPageSize = 15

    fun load() {
        plugin.config.apply {
            addDefault("check_updates", checkForUpdates)
            addDefault("debug", debugLevel.name)
            addDefault("use_admin_commands_in_menus_list", false)
            addDefault("menus_list_page_size", menusListPageSize)
            addDefault("metas_list_page_size", metasListPageSize)

            checkForUpdates = getBoolean("check_updates")
            debugLevel = loadDebugLevel()
            useAdminCommandsInMenusList = getBoolean("use_admin_commands_in_menus_list")
            menusListPageSize = getInt("menus_list_page_size", 10)
            metasListPageSize = getInt("metas_list_page_size", 15)
        }
    }

    fun reload() {
        plugin.reloadConfig()
        load()
    }

    private fun loadDebugLevel(): DebugLevel {
        var configDebugLevel = plugin.config.getString("debug", "HIGHEST")!!

        if (configDebugLevel.equals("true", ignoreCase = true)) {
            configDebugLevel = "LOWEST"
            plugin.config.set("debug", "LOWEST")
        }

        if (configDebugLevel.equals("false", ignoreCase = true)) {
            configDebugLevel = "HIGHEST"
            plugin.config.set("debug", "HIGHEST")
        }

        return DebugLevel.getByName(configDebugLevel) ?: defaultDebugLevel
    }

}
