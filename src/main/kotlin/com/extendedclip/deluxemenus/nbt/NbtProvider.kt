package com.extendedclip.deluxemenus.nbt

import com.extendedclip.deluxemenus.utils.VersionHelper
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.lang.reflect.Constructor
import java.lang.reflect.Method

object NbtProvider {
    var isAvailable = false
        private set

    private lateinit var getStringMethod: Method
    private lateinit var setStringMethod: Method
    private lateinit var setBooleanMethod: Method
    private lateinit var setByteMethod: Method
    private lateinit var setShortMethod: Method
    private lateinit var setIntMethod: Method
    private lateinit var removeTagMethod: Method
    private lateinit var hasTagMethod: Method
    private lateinit var getTagMethod: Method
    private lateinit var setTagMethod: Method
    private lateinit var containsMethod: Method
    private lateinit var asNMSCopyMethod: Method
    private lateinit var asBukkitCopyMethod: Method

    private lateinit var nbtCompoundConstructor: Constructor<*>

    init {
        try {
            val compoundClass = VersionHelper.getNMSClass("nbt", "NBTTagCompound")
            val itemStackClass = VersionHelper.getNMSClass("world.item", "ItemStack")
            val inventoryClass = VersionHelper.getCraftClass("inventory.CraftItemStack")
            
            containsMethod = compoundClass.getMethod(VersionConstants.CONTAINS_METHOD_NAME, String::class.java)
            getStringMethod = compoundClass.getMethod(VersionConstants.GET_STRING_METHOD_NAME, String::class.java)
            setStringMethod =
                compoundClass.getMethod(VersionConstants.SET_STRING_METHOD_NAME, String::class.java, String::class.java)
            setBooleanMethod = compoundClass.getMethod(
                VersionConstants.SET_BOOLEAN_METHOD_NAME,
                String::class.java,
                Boolean::class.javaPrimitiveType
            )
            setByteMethod = compoundClass.getMethod(
                VersionConstants.SET_BYTE_METHOD_NAME,
                String::class.java,
                Byte::class.javaPrimitiveType
            )
            setShortMethod = compoundClass.getMethod(
                VersionConstants.SET_SHORT_METHOD_NAME,
                String::class.java,
                Short::class.javaPrimitiveType
            )
            setIntMethod = compoundClass.getMethod(
                VersionConstants.SET_INTEGER_METHOD_NAME,
                String::class.java,
                Int::class.javaPrimitiveType
            )
            removeTagMethod = compoundClass.getMethod(VersionConstants.REMOVE_TAG_METHOD_NAME, String::class.java)
            hasTagMethod = itemStackClass.getMethod(VersionConstants.HAS_TAG_METHOD_NAME)
            getTagMethod = itemStackClass.getMethod(VersionConstants.GET_TAG_METHOD_NAME)
            setTagMethod = itemStackClass.getMethod(VersionConstants.SET_TAG_METHOD_NAME, compoundClass)
            nbtCompoundConstructor = compoundClass.getDeclaredConstructor()

            asNMSCopyMethod = inventoryClass.getMethod("asNMSCopy", ItemStack::class.java)
            asBukkitCopyMethod = inventoryClass.getMethod("asBukkitCopy", itemStackClass)

            isAvailable = true
        } catch (_: Exception) {
            isAvailable = false
        }
    }

    /**
     * Sets an NBT tag to the an [ItemStack].
     *
     * @param itemStack The current [ItemStack] to be set.
     * @param setValue  The function to set the NBT value.
     * @return An [ItemStack] that has NBT set.
     */
    fun setField(itemStack: ItemStack?, method: Method, key: String, value: Any): ItemStack? {
        if (itemStack == null) return null
        if (itemStack.type == Material.AIR) return itemStack

        val nmsItemStack = asNMSCopy(itemStack)
        val itemCompound = if (hasTag(nmsItemStack)) getTag(nmsItemStack) else newNBTTagCompound()

        try { method.invoke(itemCompound, key, value) }
        catch (_: Exception) {}
        setTag(nmsItemStack, itemCompound)

        return asBukkitCopy(nmsItemStack)
    }

    fun setString(itemStack: ItemStack?, key: String, value: String) = setField(itemStack, setStringMethod, key, value)
    fun setBoolean(itemStack: ItemStack?, key: String, value: Boolean) = setField(itemStack, setBooleanMethod, key, value)
    fun setByte(itemStack: ItemStack?, key: String, value: Byte) = setField(itemStack, setByteMethod, key, value)
    fun setShort(itemStack: ItemStack?, key: String, value: Short) = setField(itemStack, setShortMethod, key, value)
    fun setInt(itemStack: ItemStack?, key: String, value: Int) = setField(itemStack, setIntMethod, key, value)

    fun removeKey(itemStack: ItemStack?, key: String): ItemStack? {
        if (itemStack == null) return null
        if (itemStack.type == Material.AIR) return null

        val nmsItemStack = asNMSCopy(itemStack)
        if (!hasTag(nmsItemStack)) return itemStack
        val itemCompound = getTag(nmsItemStack)

        removeTag(itemCompound, key)
        setTag(nmsItemStack, itemCompound)

        return asBukkitCopy(nmsItemStack)
    }

    /**
     * Gets the NBT tag based on a given key.
     *
     * @param itemStack The [ItemStack] to get from.
     * @param key       The key to look for.
     * @return The tag that was stored in the [ItemStack].
     */
    fun getString(itemStack: ItemStack?, key: String): String? {
        if (itemStack == null) return null
        if (itemStack.type == Material.AIR) return null

        val nmsItemStack = asNMSCopy(itemStack)
        val itemCompound = if (hasTag(nmsItemStack)) getTag(nmsItemStack) else newNBTTagCompound()

        return try { getStringMethod.invoke(itemCompound, key) as String? }
        catch (_: Exception) { null }
    }

    fun hasKey(itemStack: ItemStack?, key: String): Boolean {
        if (itemStack == null) return false

        val nmsItemStack = asNMSCopy(itemStack)
        val itemCompound = if (hasTag(nmsItemStack)) getTag(nmsItemStack) else newNBTTagCompound()

        return try { containsMethod.invoke(itemCompound, key) as Boolean }
        catch (_: Exception) { false }
    }

    /**
     * Mimics the nmsItemStack#hasTag method.
     *
     * @param nmsItemStack the NMS ItemStack to check from.
     * @return True or false depending on if it has tag or not.
     */
    private fun hasTag(nmsItemStack: Any?): Boolean {
        return try { hasTagMethod.invoke(nmsItemStack) as Boolean }
        catch (_: Exception) { false }
    }

    /**
     * Mimics the nmsItemStack#getTag method.
     *
     * @param nmsItemStack The NMS ItemStack to get from.
     * @return The tag compound.
     */
    fun getTag(nmsItemStack: Any?): Any? {
        return try { getTagMethod.invoke(nmsItemStack) }
        catch (_: Exception) { null }
    }

    /**
     * Mimics the nmsItemStack#setTag method.
     *
     * @param nmsItemStack the NMS ItemStack to set the tag to.
     * @param itemCompound The item compound to set.
     */
    private fun setTag(nmsItemStack: Any?, itemCompound: Any?) {
        try { setTagMethod.invoke(nmsItemStack, itemCompound) }
        catch (_: Exception) {}
    }

    /**
     * Mimics the nmsItemStack#removeTag method.
     *
     * @param nmsItemStack the NMS ItemStack to remove the tag from.
     * @param itemCompound The item compound to remove.
     */
    private fun removeTag(nmsItemStack: Any?, itemCompound: Any?) {
        try { removeTagMethod.invoke(nmsItemStack, itemCompound) }
        catch (_: Exception) {}
    }

    /**
     * Mimics the new NBTTagCompound instantiation.
     *
     * @return The new NBTTagCompound.
     */
    private fun newNBTTagCompound(): Any? {
        return try { nbtCompoundConstructor.newInstance() }
        catch (_: IllegalAccessException) { null }
    }

    /**
     * Mimics the CraftItemStack#asNMSCopy method.
     *
     * @param itemStack The ItemStack to make NMS copy.
     * @return An NMS copy of the ItemStack.
     */
    fun asNMSCopy(itemStack: ItemStack): Any? {
        return try { asNMSCopyMethod.invoke(null, itemStack) }
        catch (_: Exception) { null }
    }

    /**
     * Mimics the CraftItemStack#asBukkitCopy method.
     *
     * @param nmsItemStack The NMS ItemStack to turn into [ItemStack].
     * @return The new [ItemStack].
     */
    fun asBukkitCopy(nmsItemStack: Any?): ItemStack? {
        return try { asBukkitCopyMethod.invoke(null, nmsItemStack) as ItemStack? }
        catch (_: IllegalAccessException) { null }
    }

    private object VersionConstants {
        val CONTAINS_METHOD_NAME: String = getFieldName("hasKey", "e")
        val GET_STRING_METHOD_NAME: String = getFieldName("getString", "l")
        val SET_STRING_METHOD_NAME: String = getFieldName("setString", "a")
        val SET_BOOLEAN_METHOD_NAME: String = getFieldName("setBoolean", "a")
        val SET_BYTE_METHOD_NAME: String = getFieldName("setByte", "a")
        val SET_SHORT_METHOD_NAME: String = getFieldName("setShort", "a")
        val SET_INTEGER_METHOD_NAME: String = getFieldName("setInt", "a")
        val REMOVE_TAG_METHOD_NAME: String = getFieldName("remove", "r")
        val HAS_TAG_METHOD_NAME: String = hasTagMethodName()
        val GET_TAG_METHOD_NAME: String = getTagMethodName()
        val SET_TAG_METHOD_NAME: String = getFieldName("setTag", "c")

        fun getFieldName(name: String, obfuscated: String) = if (VersionHelper.HAS_OBFUSCATED_NAMES) obfuscated else name

        fun hasTagMethodName(): String {
            VersionHelper.CURRENT_VERSION.let { 
                return when {
                    it >= 1200 -> "u" // 1.20 variable change
                    it >= 1190 -> "t" // 1.19 variable change
                    it == 1182 -> "s" // 1.18.2 variable change
                    else -> getFieldName("hasTag", "r") // 1.18-1.18.1
                }
            }
        }

        fun getTagMethodName(): String {
            VersionHelper.CURRENT_VERSION.let {
                return when {
                    it >= 1200 -> "v" // 1.20 variable change
                    it >= 1190 -> "u" // 1.19 variable change
                    it == 1182 -> "t" // 1.18.2 variable change
                    else -> getFieldName("getTag", "s") // 1.18-1.18.1
                }
            }
        }
    }
}
