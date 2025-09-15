package com.extendedclip.deluxemenus.utils

enum class DebugLevel(val priority: Int, private val level: String) {
    LOWEST(0, "LOWEST"),
    LOW(1, "LOW"),
    MEDIUM(2, "MEDIUM"),
    HIGH(3, "HIGH"),
    HIGHEST(4, "HIGHEST");

    companion object {
        private val LEVELS = entries.associateBy { it.level }

        fun getByName(name: String) = LEVELS[name.lowercase()]
    }
}
