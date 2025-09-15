package com.extendedclip.deluxemenus.requirement.wrappers

import com.extendedclip.deluxemenus.menu.options.CustomModelDataComponent

class ItemWrapper {
    var material: String? = null
    var name: String? = null
    var lore: String? = null
    var loreList: MutableList<String>? = null

    var data: Short = 0
    var hasData = false
    var customModelDataComponent = CustomModelDataComponent()
    var customData: Int = 0
    var amount: Int = 1

    var isStrict: Boolean = false
    var checkArmor = false
    var checkOffHand = false
    var checkNameContains = false
    var checkNameIgnoreCase = false
    var checkLoreContains = false
    var checkLoreIgnoreCase = false
}
