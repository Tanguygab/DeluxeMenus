package com.extendedclip.deluxemenus.utils

import com.extendedclip.deluxemenus.DeluxeMenus
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.profile.PlayerProfile
import java.lang.reflect.Field
import java.net.MalformedURLException
import java.net.URI
import java.util.Base64
import java.util.UUID

object SkullUtils {
    private val GSON = Gson()

    /**
     * Helper method to get the encoded bytes for a full MC Texture
     *
     * @param url the url of the texture
     * @return fully encoded texture url
     */
    fun getEncoded(url: String) = String(Base64.getEncoder().encode("{textures:{SKIN:{url:\"https://textures.minecraft.net/texture/$url\"}}}".toByteArray()))

    /**
     * Get the skull from a base64 encoded texture url
     *
     * @param base64Url base64 encoded url to use
     * @return skull
     */
    fun getSkullByBase64EncodedTextureUrl(plugin: DeluxeMenus, base64Url: String): ItemStack {
        val head = plugin.head.clone()
        if (base64Url.isEmpty()) {
            return head
        }

        val headMeta = head.itemMeta as SkullMeta? ?: return head

        if (VersionHelper.HAS_PLAYER_PROFILES) {
            val profile = getPlayerProfile(plugin, base64Url)
            headMeta.ownerProfile = profile
            head.itemMeta = headMeta
            return head
        }

        val profile = getGameProfile(base64Url)
        val profileField: Field
        try {
            profileField = headMeta.javaClass.getDeclaredField("profile")
            profileField.setAccessible(true)
            profileField.set(headMeta, profile)
        } catch (e: Exception) {
            plugin.printStacktrace("Failed to get head item from base64 texture url", e)
        }
        head.itemMeta = headMeta
        return head
    }

    fun getTextureFromSkull(plugin: DeluxeMenus, item: ItemStack): String? {
        if (item.itemMeta !is SkullMeta) return null
        val meta = item.itemMeta as SkullMeta

        if (VersionHelper.HAS_PLAYER_PROFILES) {
            val profile = meta.ownerProfile ?: return null
            val url = profile.textures.skin ?: return null
            return url.toString().substring("https://textures.minecraft.net/texture/".length - 1)
        }

        val profile: GameProfile
        try {
            val profileField = meta.javaClass.getDeclaredField("profile")
            profileField.setAccessible(true)
            profile = profileField.get(meta) as GameProfile
        } catch (e: NoSuchFieldException) {
            plugin.printStacktrace("Failed to get base64 texture url from head item", e)
            return null
        }

        for (property in profile.properties.get("textures")) {
            if (property.name == "textures") {
                return decodeSkinUrl(property.value)
            }
        }
        return null
    }


    /**
     * Get the skull from a player name
     *
     * @param playerName the player name to use
     * @return skull
     */
    @Suppress("DEPRECATION")
    fun getSkullByName(plugin: DeluxeMenus, playerName: String): ItemStack {
        val head = plugin.head.clone()
        if (playerName.isEmpty()) return head

        val headMeta = head.itemMeta as SkullMeta? ?: return head

        val offlinePlayer = Bukkit.getOfflinePlayer(playerName)

        if (VersionHelper.HAS_PLAYER_PROFILES && offlinePlayer.playerProfile.textures.isEmpty) {
            // updates the Player Profile and populates textures for offline players - for some reason this doesn't populate when getting the Profile first time
            headMeta.ownerProfile = offlinePlayer.playerProfile.update().join()
        } else if (!VersionHelper.IS_SKULL_OWNER_LEGACY) {
            headMeta.owningPlayer = offlinePlayer
        } else {
            headMeta.owner = offlinePlayer.name
        }

        head.itemMeta = headMeta
        return head
    }

    fun getSkullOwner(skull: ItemStack?): String? {
        if (skull == null || skull.itemMeta !is SkullMeta) return null
        val meta = skull.itemMeta as SkullMeta

        if (!VersionHelper.IS_SKULL_OWNER_LEGACY) {
            if (meta.owningPlayer == null) return null
            return meta.owningPlayer!!.name
        }

        @Suppress("DEPRECATION")
        return meta.owner
    }

    /**
     * Create a game profile object
     *
     * @param base64Url the base64 encoded texture url to use
     * @return game profile
     */
    private fun getGameProfile(base64Url: String): GameProfile {
        val profile = GameProfile(UUID.randomUUID(), "")
        profile.properties.put("textures", Property("textures", base64Url))
        return profile
    }

    /**
     * Create a player profile object
     * Player profile was introduced in 1.18.1+
     *
     * @param base64Url the base64 encoded texture URL to use
     * @return player profile
     */
    private fun getPlayerProfile(plugin: DeluxeMenus, base64Url: String): PlayerProfile {
        val profile = Bukkit.createPlayerProfile(UUID.randomUUID())

        val decodedBase64 = decodeSkinUrl(base64Url) ?: return profile

        val textures = profile.textures

        try {
            textures.skin = URI.create(decodedBase64).toURL()
        } catch (e: MalformedURLException) {
            plugin.printStacktrace("Something went horribly wrong trying to create basehead URL", e)
        }

        profile.setTextures(textures)
        return profile
    }

    /**
     * Decode a base64 string and extract the url of the skin. Example:
     * <br></br>
     * - Base64: `eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGNlYjE3MDhkNTQwNGVmMzI2MTAzZTdiNjA1NTljOTE3OGYzZGNlNzI5MDA3YWM5YTBiNDk4YmRlYmU0NjEwNyJ9fX0=`
     * <br></br>
     * - JSON: `{"textures":{"SKIN":{"url":"http://textures.minecraft.net/texture/dceb1708d5404ef326103e7b60559c9178f3dce729007ac9a0b498bdebe46107"}}}`
     * <br></br>
     * - Result: `http://textures.minecraft.net/texture/dceb1708d5404ef326103e7b60559c9178f3dce729007ac9a0b498bdebe46107`
     * <br></br>
     * Credit: [iGabyTM](https://github.com/TriumphTeam/triumph-gui/pull/104/files#diff-ef6f3ffdac8e5f722e2e9121be8003b26d087c2d7871ca43d31b65c7565b0c1fR92)
     *
     * @param base64Texture the texture
     * @return the url of the texture if found, otherwise `null`
     */
    fun decodeSkinUrl(base64Texture: String): String? {
        val decoded = String(Base64.getDecoder().decode(base64Texture))
        val obj = GSON.fromJson(decoded, JsonObject::class.java)

        val textures = obj.get("textures") ?: return null
        val skin = textures.getAsJsonObject().get("SKIN") ?: return null
        val url = skin.getAsJsonObject().get("url")
        return url?.asString
    }
}
