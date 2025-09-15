package com.extendedclip.deluxemenus.requirement

import com.extendedclip.deluxemenus.action.ClickHandler
import com.extendedclip.deluxemenus.menu.MenuHolder

abstract class Requirement {
    var isOptional: Boolean = false
    var successHandler: ClickHandler? = null
    var denyHandler: ClickHandler? = null

    abstract fun evaluate(holder: MenuHolder): Boolean
}
