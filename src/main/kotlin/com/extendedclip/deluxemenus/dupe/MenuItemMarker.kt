package com.extendedclip.deluxemenus.dupe

import com.extendedclip.deluxemenus.DeluxeMenus
import com.extendedclip.deluxemenus.dupe.marker.ItemMarker
import com.extendedclip.deluxemenus.dupe.marker.impl.NMSMenuItemMarker
import com.extendedclip.deluxemenus.dupe.marker.impl.PDCMenuItemMarker
import com.extendedclip.deluxemenus.dupe.marker.impl.UnavailableMenuItemMarker
import com.extendedclip.deluxemenus.nbt.NbtProvider
import com.extendedclip.deluxemenus.utils.VersionHelper
import org.bukkit.inventory.ItemStack
import java.util.regex.Pattern

/**
 * Mark inventory items created by DeluxeMenus to prevent duplication. Marked items will be removed from all inventories
 * except the one they were created in.
 */
class MenuItemMarker @JvmOverloads constructor(plugin: DeluxeMenus, mark: String = DEFAULT_MARK) : ItemMarker {
    private val marker: ItemMarker
    val mark: String = if (DEFAULT_MARK == mark || MARK_PATTERN.matcher(mark).matches()) mark else DEFAULT_MARK

    init {
        marker = when {
            SUPPORTS_PDC -> PDCMenuItemMarker(plugin, this.mark)
            SUPPORTS_NMS -> NMSMenuItemMarker(this.mark)
            else -> UnavailableMenuItemMarker()
        }
    }

    override fun mark(itemStack: ItemStack) = marker.mark(itemStack)

    override fun unmark(itemStack: ItemStack) = marker.unmark(itemStack)

    override fun isMarked(itemStack: ItemStack) = marker.isMarked(itemStack)

    companion object {
        private const val DEFAULT_MARK = "DM"
        private val MARK_PATTERN: Pattern = Pattern.compile("^[a-zA-Z0-9]+$")
        private val SUPPORTS_PDC = VersionHelper.IS_PDC_VERSION
        private val SUPPORTS_NMS = NbtProvider.isAvailable
    }
}
