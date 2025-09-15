package com.extendedclip.deluxemenus.persistentmeta

import org.bukkit.persistence.PersistentDataType

class DataType<P, C> private constructor(
    val name: String,
    val pDType: PersistentDataType<P, C>,
    private val checker: (Any) -> Boolean
) {
    fun getComplexType() = pDType.getComplexType()

    fun getPrimitiveType() = pDType.getPrimitiveType()

    fun isSupported(value: Any?) = getComplexType().isInstance(value) && checker(value!!)

    companion object {
        val DOUBLE = DataType<Double, Double>("DOUBLE", PersistentDataType.DOUBLE) { true }
        val INTEGER = DataType<Long, Long>("INTEGER", PersistentDataType.LONG) { true }
        val LONG = DataType<Long, Long>("LONG", PersistentDataType.LONG) { true }
        val STRING = DataType<String, String>("STRING", PersistentDataType.STRING) { true }
        val BOOLEAN = DataType<String, String>("BOOLEAN", PersistentDataType.STRING) {
            "true".equals(it as String, ignoreCase = true) || "false".equals(it, ignoreCase = true)
        }

        /**
         * List of all supported types.
         *
         * @return A list of all supported types.
         */
        val supportedTypes = listOf(DOUBLE, INTEGER, LONG, STRING, BOOLEAN)

        /**
         * Helper method to parse a string into a [DataType].
         *
         * @param name The name of the type.
         * @return The type, or null if type does not exist or is not supported.
         */
        fun getSupportedTypeByName(name: String) = supportedTypes.find { it.name.equals(name, ignoreCase = true) }

        /**
         * Get a list of all supported type names.
         *
         * @return A set of all supported type names.
         */
        fun getSupportedTypeNames() = supportedTypes.map { it.name }.toSet()
    }
}
