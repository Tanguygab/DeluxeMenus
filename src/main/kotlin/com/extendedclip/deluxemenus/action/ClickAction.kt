package com.extendedclip.deluxemenus.action

import com.extendedclip.deluxemenus.menu.MenuHolder
import java.util.concurrent.ThreadLocalRandom

class ClickAction(
    /**
     * Set the [ActionType] of this action.
     *
     * @param type the type to set
     */
    var type: ActionType,
    /**
     * Set the executable of this action.
     *
     * @param executable the executable to set
     */
    var executable: String
) {
    /**
     * Get the [ActionType] of this action.
     *
     * @return the type
     */
    /**
     * Get the executable of this action.
     * If the delay is null or can't be parsed to a [Long], there will be no delay.
     *
     * @return the executable
     */
    var delay: String? = null
    /**
     * Get the unparsed chance of this action.
     *
     * @return the chance
     */
    /**
     * Set the chance of this action. If the chance is null or can't be parsed to a [Double], it will be considered
     * as 100%.
     *
     * @param chance the chance to set
     */
    var chance: String? = null

    /**
     * Checks if this action has a delay. This does not check if the delay is valid or not!
     *
     * @return true if there is a delay, false otherwise
     */
    fun hasDelay() = delay != null

    /**
     * Get the parsed delay of this action. If the delay is null or can't be parsed to a [Long], the delay will be 0.
     *
     * @param holder the holder to parse placeholders in the delay for.
     * @return the parsed delay
     */
    fun getDelay(holder: MenuHolder): Long {
        if (delay == null || delay!!.isEmpty()) return 0

        return holder.setPlaceholdersAndArguments(delay!!).toLongOrNull() ?: 0
    }

    /**
     * Parses the chance of this action and tries it. If [.getChance] is null this will return true but if it
     * can't be parsed to a [Double], this will return false.
     *
     * @param holder the holder to parse placeholders in the chance for.
     * @return true if the chance has passed, false otherwise
     */
    fun checkChance(holder: MenuHolder): Boolean {
        if (chance == null) return true

        val parsedChance = holder.setPlaceholdersAndArguments(chance!!).toDoubleOrNull() ?: return false
        if (parsedChance >= 100.0) return true

        // Generate a random number with a maximum of 2 decimals.
        val random = ThreadLocalRandom.current().nextInt(10000) / 100.0

        return random <= parsedChance
    }
}
