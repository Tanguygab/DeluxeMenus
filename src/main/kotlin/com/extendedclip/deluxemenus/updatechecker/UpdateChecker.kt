package com.extendedclip.deluxemenus.updatechecker

import com.extendedclip.deluxemenus.DeluxeMenus
import com.extendedclip.deluxemenus.listener.Listener
import com.extendedclip.deluxemenus.utils.DebugLevel
import com.extendedclip.deluxemenus.utils.Messages
import net.kyori.adventure.text.TextReplacementConfig
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import java.util.logging.Level
import java.util.regex.Pattern

class UpdateChecker(plugin: DeluxeMenus) : Listener(plugin) {
    val resourceId: Int = 11734
    var latestVersion: String? = null
        private set
    var updateAvailable = false

    init {
        plugin.server.scheduler.runTaskAsynchronously(plugin, Runnable {
            if (check()) plugin.server.scheduler.runTask(plugin, ::register)
        })
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player
        if (!player.isOp || !updateAvailable) return

        plugin.sms(player, Messages.UPDATE_AVAILABLE.message
            .replaceText(CURRENT_VERSION_REPLACER_BUILDER.replacement(plugin.description.version).build())
            .replaceText(LATEST_VERSION_REPLACER_BUILDER.replacement(latestVersion!!).build())
        )
    }

    private fun getSpigotVersion(): String? {
        try {
            val connection = URI.create("https://api.spigotmc.org/legacy/update.php?resource=$resourceId").toURL().openConnection() as HttpURLConnection
            connection.setDoOutput(true)
            connection.setRequestMethod("GET")
            return BufferedReader(InputStreamReader(connection.getInputStream())).readLine()
        } catch (_: Exception) {
            plugin.debug(DebugLevel.HIGH, Level.INFO, "Failed to check for update on spigot!")
        }
        return null
    }

    fun check(): Boolean {
        val version = getSpigotVersion() ?: return false

        if (checkHigher(plugin.description.version, version)) {
            latestVersion = version
            updateAvailable = true
            return true
        }

        latestVersion = plugin.description.version
        updateAvailable = false
        return false
    }

    private fun checkHigher(currentVersion: String, newVersion: String) = toReadable(currentVersion) < toReadable(newVersion)

    private fun toReadable(version: String): String {
        val split = Pattern
            .compile(".", Pattern.LITERAL)
            .split(version.replace("v", ""))

        val versionBuilder = StringBuilder()
        for (s in split) {
            versionBuilder.append(String.format("%4s", s))
        }

        return versionBuilder.toString()
    }

    companion object {
        private val LATEST_VERSION_REPLACER_BUILDER = TextReplacementConfig.builder().matchLiteral("<latest-version>")
        private val CURRENT_VERSION_REPLACER_BUILDER = TextReplacementConfig.builder().matchLiteral("<current-version>")
    }
}
