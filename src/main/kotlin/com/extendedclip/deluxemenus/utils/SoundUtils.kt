package com.extendedclip.deluxemenus.utils

import org.bukkit.Sound

object SoundUtils {
    fun getSound(name: String): Sound {
        try {
            // As of Minecraft 1.21.3, the org.bukkit.Sound class type changed from Enum to Interface.
            // This fixes java.lang.IncompatibleClassChangeError when trying to use versions prior to 1.21.3.
            val valueOfMethod = Class.forName("org.bukkit.Sound").getMethod("valueOf", String::class.java)
            return valueOfMethod.invoke(null, name) as Sound
        } catch (_: Exception) {
            // Use the Sound#valueOf method if Reflection fails.
            @Suppress("DEPRECATION")
            return Sound.valueOf(name)
        }
    }
}
