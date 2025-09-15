package com.extendedclip.deluxemenus.menu.options

data class CustomModelDataComponent(
    var colors: MutableList<String> = mutableListOf(),
    var flags: MutableList<String> = mutableListOf(),
    var floats: MutableList<String> = mutableListOf(),
    var strings: MutableList<String> = mutableListOf(),
)
