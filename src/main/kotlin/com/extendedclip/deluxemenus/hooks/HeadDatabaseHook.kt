package com.extendedclip.deluxemenus.hooks

import com.extendedclip.deluxemenus.DeluxeMenus
import me.arcaniax.hdb.api.HeadDatabaseAPI
import org.bukkit.inventory.ItemStack

class HeadDatabaseHook(private val plugin: DeluxeMenus) : ItemHook {
    override val prefix = "hdb-"

    private val api = HeadDatabaseAPI()

    override fun getItem(vararg arguments: String): ItemStack {
        if (arguments.isEmpty()) plugin.head.clone()

        var item: ItemStack? = null
        try {
            item = api.getItemHead(arguments[0])
        } catch (e: NullPointerException) {
            plugin.printStacktrace("Something went wrong while trying to get head database head: " + arguments[0], e)
        }

        return item ?: plugin.head.clone()
    }

    override fun itemMatchesIdentifiers(item: ItemStack, vararg arguments: String): Boolean {
        return arguments.isNotEmpty() && arguments[0].equals(api.getItemID(item), ignoreCase = true)
    }
}
