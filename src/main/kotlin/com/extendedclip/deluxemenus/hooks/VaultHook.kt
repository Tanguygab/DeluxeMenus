package com.extendedclip.deluxemenus.hooks

import net.milkbowl.vault.economy.Economy
import net.milkbowl.vault.permission.Permission
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class VaultHook {
    private val economy: Economy?
    private val permission: Permission?

    init {
        val rspEconomy = Bukkit.getServicesManager().getRegistration(Economy::class.java)
        val rspPermissions = Bukkit.getServicesManager().getRegistration(Permission::class.java)

        economy = rspEconomy?.getProvider()
        permission = rspPermissions?.getProvider()
    }

    /**
     * Checks if the Economy and Permission hooks are enabled.
     *
     * @return true if both hooks are enabled, false otherwise.
     */
    fun hooked() = economy != null && permission != null

    /**
     * Checks if the player has the amount in their account.
     *
     * @param player the player to check.
     * @param amount the amount to check for.
     * @return true if the economy hook is enabled and player has the amount, false otherwise.
     */
    fun hasEnough(player: Player, amount: Double): Boolean {
        return economy != null && economy.has(player, amount)
    }

    /**
     * Takes the amount from the player's account.
     * <br></br>
     * This will do nothing if the economy hook is disabled. You should check [.hooked] before calling this.
     *
     * @param player the player to take from.
     * @param amount the amount to take.
     */
    fun takeMoney(player: Player, amount: Double) {
        economy?.withdrawPlayer(player, amount)
    }

    /**
     * Gives the player the amount.
     * <br></br>
     * This will do nothing if the economy hook is disabled. You should check [.hooked] before calling this.
     *
     * @param player the player to give to.
     * @param amount the amount to give.
     */
    fun giveMoney(player: Player, amount: Double) {
        economy?.depositPlayer(player, amount)
    }

    /**
     * Checks if the player has the permission.
     *
     * @param player the player to check.
     * @param permissionNode the permission to check for.
     * @return true if the permission hook is enabled and player has the permission, false otherwise.
     */
    fun hasPermission(player: Player, permissionNode: String): Boolean {
        return this.permission != null && this.permission.has(player, permissionNode)
    }

    /**
     * Take the permission from the player.
     * <br></br>
     * This will do nothing if the permission hook is disabled. You should check [.hooked] before calling this.
     *
     * @param player the player to take from.
     * @param permissionNode the permission to take.
     */
    fun takePermission(player: Player, permissionNode: String) {
        permission?.playerRemove(null, player, permissionNode)
    }

    /**
     * Give the player the permission.
     * <br></br>
     * This will do nothing if the permission hook is disabled. You should check [.hooked] before calling this.
     *
     * @param player the player to give to.
     * @param permissionNode the permission to give.
     */
    fun givePermission(player: Player, permissionNode: String) {
        permission?.playerAdd(null, player, permissionNode)
    }
}