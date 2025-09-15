package com.extendedclip.deluxemenus

import com.extendedclip.deluxemenus.cache.SimpleCache
import com.extendedclip.deluxemenus.command.DeluxeMenusCommand
import com.extendedclip.deluxemenus.config.DeluxeMenusConfig
import com.extendedclip.deluxemenus.config.GeneralConfig
import com.extendedclip.deluxemenus.dupe.DupeFixer
import com.extendedclip.deluxemenus.dupe.MenuItemMarker
import com.extendedclip.deluxemenus.hooks.*
import com.extendedclip.deluxemenus.listener.PlayerListener
import com.extendedclip.deluxemenus.menu.Menu
import com.extendedclip.deluxemenus.menu.options.HeadType
import com.extendedclip.deluxemenus.nbt.NbtProvider.isAvailable
import com.extendedclip.deluxemenus.persistentmeta.PersistentMetaHandler
import com.extendedclip.deluxemenus.placeholder.Expansion
import com.extendedclip.deluxemenus.updatechecker.UpdateChecker
import com.extendedclip.deluxemenus.utils.DebugLevel
import com.extendedclip.deluxemenus.utils.Messages
import com.extendedclip.deluxemenus.utils.VersionHelper
import com.google.common.io.ByteStreams
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.Component
import org.bstats.bukkit.Metrics
import org.bstats.charts.AdvancedPie
import org.bstats.charts.SingleLineChart
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Level

class DeluxeMenus : JavaPlugin() {
    var persistentMetaHandler: PersistentMetaHandler? = null
        private set
    var menuItemMarker: MenuItemMarker? = null
        private set

    private var audiences: BukkitAudiences? = null

    var vault: VaultHook? = null
        private set

    var head: ItemStack = ItemStack(Material.DIRT, 1)
    var itemHooks = mutableMapOf<String, ItemHook>()

    val generalConfig: GeneralConfig = GeneralConfig(this)
    private var menuConfig: DeluxeMenusConfig? = null

    val configuration: DeluxeMenusConfig
        get() = menuConfig!!

    override fun onLoad() {
        if (isAvailable) {
            debug(DebugLevel.HIGHEST, Level.INFO, "NMS hook has been setup successfully!")
            return
        }

        debug(
            DebugLevel.HIGHEST,
            Level.WARNING,
            "Could not setup a NMS hook for your server version! The following Item options will not work: nbt_int, nbt_ints, nbt_string and nbt_strings."
        )
    }

    override fun onEnable() {
        generalConfig.load()

        if (!hookIntoPlaceholderAPI()) {
            server.pluginManager.disablePlugin(this)
            return
        }

        persistentMetaHandler = PersistentMetaHandler(this)
        menuItemMarker = MenuItemMarker(this)
        DupeFixer(this, menuItemMarker!!).register()

        audiences = BukkitAudiences.create(this)

        hookIntoVault()
        setUpItemHooks()

        menuConfig = DeluxeMenusConfig(this)
        if (menuConfig!!.loadDefConfig()) {
            debug(DebugLevel.HIGHEST, Level.INFO, menuConfig!!.loadGUIMenus().toString() + " GUI menus loaded!")
        } else {
            debug(
                DebugLevel.HIGHEST,
                Level.WARNING,
                "Failed to load from config.yml. Use /dm reload after fixing your errors."
            )
        }

        PlayerListener(this).register()
        if (!DeluxeMenusCommand(this).register()) {
            debug(DebugLevel.HIGHEST, Level.SEVERE, "Could not register the DeluxeMenus command!")
        }
        Expansion(this).register()

        setUpBungeeCordMessaging()
        setUpUpdateChecker()
        setUpMetrics()
    }

    override fun onDisable() {
        server.messenger.unregisterOutgoingPluginChannel(this, "BungeeCord")

        server.scheduler.cancelTasks(this)

        if (audiences != null) {
            audiences!!.close()
            audiences = null
        }

        Menu.unloadForShutdown(this)

        itemHooks.clear()

        HandlerList.unregisterAll(this)
    }

    fun getItemHook(id: String?) = itemHooks[id]

    fun shouldPrintStackTrace() = generalConfig.debugLevel.priority <= STACKTRACE_PRINT_LEVEL.priority

    fun printStacktrace(message: String?, throwable: Throwable?) {
        if (!shouldPrintStackTrace()) return

        logger.log(Level.SEVERE, message, throwable)
    }

    fun connect(p: Player, server: String) {
        val out = ByteStreams.newDataOutput()

        try {
            out.writeUTF("Connect")
            out.writeUTF(server)
        } catch (e: Exception) {
            debug(
                DebugLevel.HIGHEST,
                Level.SEVERE,
                "There was a problem attempting to send " + p.name + " to server " + server + "!"
            )

            printStacktrace("There was a problem attempting to send " + p.name + " to server " + server + "!", e)
        }

        p.sendPluginMessage(this, "BungeeCord", out.toByteArray())
    }
    fun sms(s: CommandSender, msg: Component) = audiences().sender(s).sendMessage(msg)

    fun sms(s: CommandSender, msg: Messages) = audiences().sender(s).sendMessage(msg.message)

    fun debug(messageDebugLevel: DebugLevel, level: Level, vararg messages: String) {
        debug(generalConfig.debugLevel, messageDebugLevel, level, *messages)
    }

    fun debug(generalDebugLevel: DebugLevel, messageDebugLevel: DebugLevel, level: Level, vararg messages: String) {
        if (generalDebugLevel.priority > messageDebugLevel.priority) return
        logger.log(level, messages.joinToString(System.lineSeparator()))
    }

    fun audiences(): BukkitAudiences {
        checkNotNull(audiences) { "Tried to access Adventure when the plugin was disabled!" }
        return audiences!!
    }

    fun clearCaches() {
        itemHooks.values
            .filter { it is SimpleCache }
            .map { it as SimpleCache }
            .forEach { it.clearCache() }
    }

    fun reload() = generalConfig.reload()

    private fun hookIntoPlaceholderAPI(): Boolean {
        val canHook = server.pluginManager.getPlugin("PlaceholderAPI") != null
        if (!canHook) {
            debug(
                DebugLevel.HIGHEST,
                Level.SEVERE,
                "Could not hook into PlaceholderAPI!",
                "DeluxeMenus will now disable!"
            )
            return false
        }

        debug(DebugLevel.HIGHEST, Level.INFO, "Successfully hooked into PlaceholderAPI!")
        return true
    }

    private fun hookIntoVault() {
        if (!server.pluginManager.isPluginEnabled("Vault")) return
        vault = VaultHook()

        if (vault!!.hooked()) {
            debug(DebugLevel.HIGHEST, Level.INFO, "Successfully hooked into Vault!")
            return
        }

        debug(
            DebugLevel.HIGHEST, Level.WARNING, "Could not hook into Vault!",
            "DeluxeMenus will continue to work but some features (such as the 'has money' requirement) may not be available."
        )
    }

    private fun setUpItemHooks() {
        if (!VersionHelper.IS_ITEM_LEGACY) {
            head = ItemStack(Material.PLAYER_HEAD, 1)
        } else {
            @Suppress("DEPRECATION")
            head = ItemStack(Material.valueOf("SKULL_ITEM"), 1, 3)
        }

        val namedHeadHook = NamedHeadHook(this)
        namedHeadHook.register()
        itemHooks[HeadType.NAMED.hookName] = namedHeadHook
        itemHooks[HeadType.BASE64.hookName] = BaseHeadHook(this)
        itemHooks[HeadType.TEXTURE.hookName] = TextureHeadHook(this)

        if (server.pluginManager.isPluginEnabled("HeadDatabase")) {
            try {
                Class.forName("me.arcaniax.hdb.api.HeadDatabaseAPI")
                itemHooks[HeadType.HDB.hookName] = HeadDatabaseHook(this)
            } catch (_: ClassNotFoundException) {
                // We are looking for this specific class because we've had issues with other plugins being named HeadDatabase
                // in the past
            }
        }

        if (server.pluginManager.isPluginEnabled("ItemsAdder")) itemHooks["itemsadder"] = ItemsAdderHook()
        if (server.pluginManager.isPluginEnabled("Nexo")) itemHooks["nexo"] = NexoHook()
        if (server.pluginManager.isPluginEnabled("Oraxen")) itemHooks["oraxen"] = OraxenHook()
        if (server.pluginManager.isPluginEnabled("MMOItems")) itemHooks["mmoitems"] = MMOItemsHook(this)
        if (server.pluginManager.isPluginEnabled("ExecutableItems")) itemHooks["executableitems"] = ExecutableItemsHook()
        if (server.pluginManager.isPluginEnabled("ExecutableBlocks")) itemHooks["executableblocks"] = ExecutableBlocksHook()
        if (server.pluginManager.isPluginEnabled("SimpleItemGenerator")) itemHooks["simpleitemgenerator"] = SimpleItemGeneratorHook(this)
    }

    private fun setUpBungeeCordMessaging() {
        server.messenger.registerOutgoingPluginChannel(this, "BungeeCord")
    }

    private fun setUpUpdateChecker() {
        if (!generalConfig.checkForUpdates) return

        val updateChecker = UpdateChecker(this)
        updateChecker.register()

        if (updateChecker.updateAvailable) {
            debug(
                DebugLevel.HIGHEST,
                Level.INFO,
                "An update for DeluxeMenus (DeluxeMenus v" + updateChecker.latestVersion + ")",
                "is available at https://www.spigotmc.org/resources/deluxemenus.11734/"
            )
            return
        }

        debug(DebugLevel.HIGHEST, Level.INFO, "You are running the latest version of DeluxeMenus!")
    }

    private fun setUpMetrics() {
        val metrics = Metrics(this, 445)
        metrics.addCustomChart(SingleLineChart("menus") { Menu.getLoadedMenuSize() })

        metrics.addCustomChart(AdvancedPie("inventory_types") {
            Menu.getAllMenus()
                .map { it.options }
                .map { it.type }
                .groupingBy { it.name }
                .fold(0) { i,_ -> i+1}
        })

        // added for 1.21 usage
        metrics.addCustomChart(AdvancedPie("nbt_usage") {
            val options = Menu.getAllMenus()
                .map { it.items }
                .flatMap { items ->
                    items.values
                        .map { it.values }
                        .flatMap { it }
                }
                .map { it.options }
            
            mutableMapOf<String, Int>().apply {
                put("Byte", options.count { it.nbtByte != null })
                put("Bytes", options.count { !it.nbtBytes.isEmpty() })
                put("Short", options.count { it.nbtShort != null })
                put("Shorts", options.count { !it.nbtShorts.isEmpty() })
                put("Int", options.count { it.nbtInt != null })
                put("Ints", options.count { !it.nbtInts.isEmpty() })
                put("String", options.count { it.nbtString != null })
                put("Strings", options.count { !it.nbtStrings.isEmpty() })
                put("Model Data", options.count { it.customModelData != null })
            }
        })
    }

    companion object {
        val MATERIALS: Map<String, Material> = Material.entries.associateBy { it.name }


        private val STACKTRACE_PRINT_LEVEL = DebugLevel.MEDIUM
    }
}
