package com.extendedclip.deluxemenus.persistentmeta

enum class DataAction {
    SET, REMOVE, ADD, SUBTRACT, SWITCH;

    companion object {
        val BY_NAME: Map<String, DataAction> = entries.associateBy { it.name }

        /**
         * Get a [DataAction] by its name.
         *
         * @param name The name of the action type.
         * @return The [DataAction] or null if it does not exist.
         */
        fun getActionByName(name: String) = BY_NAME[name.uppercase()]
    }
}
