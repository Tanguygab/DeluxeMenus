package com.extendedclip.deluxemenus.utils

import com.extendedclip.deluxemenus.DeluxeMenus
import com.google.common.io.CharStreams
import com.google.gson.Gson
import com.google.gson.JsonObject
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException
import java.util.logging.Level

object DumpUtils {
    const val URL = "https://paste.helpch.at/"
    private val gson = Gson()
    private val DATE_FORMAT = DateTimeFormatter
        .ofLocalizedDateTime(FormatStyle.LONG)
        .withLocale(Locale.getDefault())
        .withZone(ZoneId.of("UTC"))


    fun postDump(dump: String) = CompletableFuture.supplyAsync {
        try {
            (URI.create(URL + "documents").toURL().openConnection() as HttpURLConnection).apply {
                setRequestMethod("POST")
                setRequestProperty("Content-Type", "text/plain; charset=utf-8")
                setDoOutput(true)

                connect()

                getOutputStream().use { it.write(dump.toByteArray(StandardCharsets.UTF_8)) }
                getInputStream().use {
                    val json = CharStreams.toString(InputStreamReader(it, StandardCharsets.UTF_8))
                    return@supplyAsync gson.fromJson(json, JsonObject::class.java)["key"].asString
                }
            }
        } catch (e: IOException) {
            throw CompletionException(e)
        }
    }!!

    @Throws(RuntimeException::class)
    fun createDump(
        plugin: DeluxeMenus,
        name: String
    ): String {
        val builder = StringBuilder()

        builder.append("Generated On: ")
            .append(DATE_FORMAT.format(Instant.now()))
            .append(System.lineSeparator())
            .append(System.lineSeparator())

        builder.append("DeluxeMenus Version: ")
            .append(plugin.description.version)
            .append(System.lineSeparator())

        builder.append("Java Version: ")
            .append(System.getProperty("java.version"))
            .append(System.lineSeparator())

        builder.append("Server Info:")
            .append(plugin.server.bukkitVersion)
            .append('/')
            .append(plugin.server.version)
            .append(System.lineSeparator())
            .append(System.lineSeparator())

        if (name.equals("config", ignoreCase = true)) {
            if (createConfigDump(plugin, builder)) return builder.toString()
        } else if (createMenuDump(plugin, name, builder)) return builder.toString()

        throw RuntimeException("Something went wrong while creating the menu dump")
    }

    private fun createMenuDump(
        plugin: DeluxeMenus,
        menuName: String,
        builder: StringBuilder
    ): Boolean {
        builder.append("Menu Name: ")
            .append(menuName)
            .append(System.lineSeparator())

        val config = plugin.getConfig()
        val guiMenus = config.getConfigurationSection("gui_menus")

        if (guiMenus == null) {
            plugin.debug(
                DebugLevel.HIGHEST,
                Level.WARNING,
                "No gui_menus section found in config.yml!"
            )

            return false
        }

        val keys = guiMenus.getKeys(false)

        if (!keys.contains(menuName)) {
            plugin.debug(
                DebugLevel.HIGHEST,
                Level.WARNING,
                "File for the $menuName menu is not declared in config.yml!"
            )

            return false
        }

        val fileName = plugin.getConfig().getString("gui_menus.$menuName.file")

        if (fileName == null) {
            plugin.debug(
                DebugLevel.HIGHEST,
                Level.WARNING,
                "File for the $menuName menu is not declared in config.yml!"
            )

            return false
        }

        if (!fileName.endsWith(".yml")) {
            plugin.debug(
                DebugLevel.HIGHEST,
                Level.WARNING,
                "File for the $menuName menu is not declared in config.yml!"
            )

            return false
        }

        builder.append("Menu Path: ")
            .append(fileName)
            .append(System.lineSeparator())
            .append(System.lineSeparator())
            .append("---------------------------------------------")
            .append(System.lineSeparator())
            .append(System.lineSeparator())

        val menuFile = File(plugin.configuration.menuDirector, fileName)

        if (!menuFile.exists() || !menuFile.isFile()) {
            plugin.debug(
                DebugLevel.HIGHEST,
                Level.WARNING,
                "Could not find the " + fileName + " file in " +
                        plugin.configuration.menuDirector.path + " while creating the dump!"
            )

            return false
        }

        try {
            Files.readAllLines(menuFile.toPath(), StandardCharsets.UTF_8).forEach {
                builder.append(it).append(System.lineSeparator())
            }
        } catch (e: IOException) {
            plugin.debug(
                DebugLevel.HIGHEST,
                Level.WARNING,
                "Something went wrong while reading the the file: $fileName"
            )

            plugin.printStacktrace(
                "Something went wrong while reading the the file: $fileName",
                e
            )
            return false
        }

        return true
    }

    private fun createConfigDump(
        plugin: DeluxeMenus,
        builder: StringBuilder
    ): Boolean {
        val configFile = File(plugin.dataFolder, "config.yml")

        if (!configFile.exists() || !configFile.isFile()) {
            plugin.debug(
                DebugLevel.HIGHEST,
                Level.WARNING,
                "Could not find the " + configFile + " file in " + plugin.dataFolder.path + " while creating the dump!"
            )

            return false
        }

        builder.append("---------------------------------------------")
            .append(System.lineSeparator())
            .append(System.lineSeparator())

        try {
            Files.readAllLines(configFile.toPath(), StandardCharsets.UTF_8).forEach {
                builder.append(it).append(System.lineSeparator())
            }
        } catch (e: IOException) {
            plugin.debug(
                DebugLevel.HIGHEST,
                Level.WARNING,
                "Something went wrong while reading the the file: $configFile"
            )

            plugin.printStacktrace(
                "Something went wrong while reading the the file: $configFile",
                e
            )
            return false
        }

        return true
    }
}
