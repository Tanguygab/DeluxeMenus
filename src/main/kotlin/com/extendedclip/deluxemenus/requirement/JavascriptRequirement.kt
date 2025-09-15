package com.extendedclip.deluxemenus.requirement

import com.extendedclip.deluxemenus.DeluxeMenus
import com.extendedclip.deluxemenus.menu.MenuHolder
import com.extendedclip.deluxemenus.utils.DebugLevel
import org.bukkit.plugin.ServicePriority
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory
import java.util.logging.Level
import javax.script.ScriptEngineFactory
import javax.script.ScriptEngineManager

class JavascriptRequirement(private val plugin: DeluxeMenus, private val expression: String) : Requirement() {
    private val factory: ScriptEngineFactory = NashornScriptEngineFactory()
    private val manager = plugin.server.servicesManager

    companion object {
        private var engine: ScriptEngineManager? = null
    }

    init {
        if (engine == null) {
            if (manager.isProvidedFor(ScriptEngineManager::class.java)) {
                val provider = manager.getRegistration(ScriptEngineManager::class.java)
                engine = provider!!.getProvider()
            } else {
                engine = ScriptEngineManager()
                manager.register(ScriptEngineManager::class.java, engine!!, plugin, ServicePriority.Highest)
            }
            engine!!.registerEngineName("JavaScript", factory)
            engine!!.put("BukkitServer", plugin.server)
        }
    }

    override fun evaluate(holder: MenuHolder): Boolean {
        val exp = holder.setPlaceholdersAndArguments(expression)
        try {
            engine!!.put("BukkitPlayer", holder.viewer)
            val result: Any? = engine!!.getEngineByName("JavaScript").eval(exp)

            if (result !is Boolean) {
                plugin.debug(
                    DebugLevel.HIGHEST,
                    Level.WARNING,
                    "Requirement javascript <$expression> is invalid and does not return a boolean!"
                )
                return false
            }

            return result
        } catch (e: Exception) {
            plugin.debug(DebugLevel.HIGHEST, Level.WARNING, "Error in requirement javascript syntax - $expression")
            plugin.printStacktrace("Error in requirement javascript syntax - $expression", e)
            return false
        }
    }
}
