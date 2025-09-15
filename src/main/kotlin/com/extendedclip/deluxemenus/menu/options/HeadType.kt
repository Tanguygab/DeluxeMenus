package com.extendedclip.deluxemenus.menu.options

import com.extendedclip.deluxemenus.utils.Constants

enum class HeadType(val hookName: String, val prefix: String) {
    /**
     * @see com.extendedclip.deluxemenus.hooks.NamedHeadHook
     */
    NAMED(Constants.NAMED_HEAD_TYPE, Constants.NAMED_HEAD_PREFIX),

    /**
     * @see com.extendedclip.deluxemenus.hooks.TextureHeadHook
     */
    TEXTURE(Constants.TEXTURE_HEAD_TYPE, Constants.TEXTURE_HEAD_PREFIX),

    /**
     * @see com.extendedclip.deluxemenus.hooks.HeadDatabaseHook
     */
    HDB(Constants.HDB_HEAD_TYPE, Constants.HDB_HEAD_PREFIX),

    /**
     * @see com.extendedclip.deluxemenus.hooks.BaseHeadHook
     */
    BASE64(Constants.BASE64_HEAD_TYPE, Constants.BASE64_HEAD_PREFIX);

    companion object {

        fun parseHeadType(string: String) = entries.find { string.startsWith(it.prefix, ignoreCase = true) }

        fun getHeadType(string: String) = entries.find { string.equals(it.prefix, ignoreCase = true) }
    }
}
