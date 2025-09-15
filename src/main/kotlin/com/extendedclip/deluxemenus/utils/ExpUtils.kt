package com.extendedclip.deluxemenus.utils

import org.bukkit.entity.Player
import kotlin.math.roundToInt

/**
 * This is a utility class for handling and calculating experience points and levels.
 */
object ExpUtils {
    /**
     * Set the player's experience to the given value.
     * @param target The player to set the experience of.
     * @param stringAmount The amount of experience to set. Can end with an 'l' to set the experience levels.
     * @throws NumberFormatException If the stringAmount is not a valid number.
     */
    @Throws(NumberFormatException::class)
    fun setExp(
        target: Player,
        stringAmount: String
    ) {
        var amount: Long
        val lowerCase = stringAmount.lowercase()

        if (stringAmount.contains("l")) {
            val neededLevel = lowerCase.replace("l".toRegex(), "").toInt() + target.level
            amount = (getExpToLevel(neededLevel) + (getTotalExperience(target) - getExpToLevel(target.level))).toLong()
            setTotalExperience(target, 0)
        } else {
            amount = lowerCase.toLong()
        }
        amount += getTotalExperience(target).toLong()

        if (amount > Int.MAX_VALUE) amount = Int.MAX_VALUE.toLong()
        if (amount < 0L) amount = 0L
        setTotalExperience(target, amount.toInt())
    }

    /**
     * Set the player's experience to the given value.
     * <br></br>
     * This method updates both the record total experience and displayed total experience.
     *
     * @param player The player to set the total experience for.
     * @param exp The amount of experience to set.
     * @throws IllegalArgumentException If the exp amount is less than 0.
     */
    @Throws(IllegalArgumentException::class)
    fun setTotalExperience(
        player: Player,
        exp: Int
    ) {
        require(exp >= 0) { "Experience is negative!" }

        player.exp = 0f
        player.level = 0
        player.totalExperience = 0

        var amount = exp
        while (amount > 0) {
            val expToLevel = getExpAtLevel(player)
            amount -= expToLevel
            if (amount >= 0) {
                player.giveExp(expToLevel)
            } else {
                amount += expToLevel
                player.giveExp(amount)
                amount = 0
            }
        }
    }

    /**
     * Get the amount of experience required to reach the given level.
     * @param player The player to get the experience for.
     * @return The amount of experience required to reach the given level.
     */
    private fun getExpAtLevel(player: Player) = getExpAtLevel(player.level)

    /**
     * Get the amount of experience points required to reach the next level from the given level.
     * @param level The level to calculate the required experience points to level up for.
     * @return The amount of experience required to reach the next level from the given one.
     */
    fun getExpAtLevel(level: Int): Int {
        if (level <= 15) return (2 * level) + 7
        if (level <= 30) return (5 * level) - 38
        return (9 * level) - 158
    }

    /**
     * Translates the given amount of levels to the amount of experience points required to get to this level from 0.
     * @param level The amount of levels to translate.
     * @return The amount of experience points required to reach the given level from 0.
     */
    fun getExpToLevel(level: Int): Int {
        var currentLevel = 0
        var exp = 0

        while (currentLevel < level) {
            exp += getExpAtLevel(currentLevel)
            currentLevel++
        }
        return if (exp < 0) Int.MAX_VALUE else exp
    }

    /**
     * Get the total amount of experience points that the given player has right now.
     * @param player The player to get the experience for.
     * @return The total amount of experience points that the given player has.
     */
    fun getTotalExperience(player: Player): Int {
        var exp = (getExpAtLevel(player) * player.exp).roundToInt()
        var currentLevel = player.level

        while (currentLevel > 0) {
            currentLevel--
            exp += getExpAtLevel(currentLevel)
        }
        return if (exp < 0) Int.MAX_VALUE else exp
    }
}
