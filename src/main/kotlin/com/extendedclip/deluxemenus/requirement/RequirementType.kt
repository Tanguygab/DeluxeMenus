package com.extendedclip.deluxemenus.requirement

enum class RequirementType(val identifiers: List<String>, val description: String, val options: List<String>) {
    HAS_META(
        listOf("has meta", "meta"), "Checks if a player has a certain metadata value",
        listOf("key", "value")
    ),
    DOES_NOT_HAVE_META(
        listOf("!has meta", "!meta"),
        "Checks if a player does not have a certain metadata value", listOf("key", "value")
    ),
    IS_NEAR(
        listOf("is near", "near"),
        "Checks if a player is within a certain distance of a specific location",
        listOf("location", "distance")
    ),
    IS_NOT_NEAR(
        listOf("!is near", "!near"),
        "Checks if a player is not within a certain distance of a specific location",
        listOf("location", "distance")
    ),
    JAVASCRIPT(
        listOf("javascript", "js"),
        "Evaluates a javascript expression that must return true or false",
        listOf("expression")
    ),
    HAS_ITEM(
        listOf("has item", "item", "hasitem"), "Checks if a player has a specific item",
        listOf("material", "amount", "data", "name", "lore")
    ),
    DOES_NOT_HAVE_ITEM(
        listOf("!has item", "!item", "!hasitem", "does not have item"),
        "Checks if a player does not have specific item",
        listOf("material", "amount", "data", "name", "lore")
    ),
    HAS_MONEY(
        listOf("has money", "hasmoney", "money"),
        "Checks if a player has enough money (Vault required)",
        listOf("amount", "placeholder")
    ),
    DOES_NOT_HAVE_MONEY(
        listOf("!has money", "!hasmoney", "!money"),
        "Checks if a player does not have enough money (Vault required)",
        listOf("amount", "placeholder")
    ),
    HAS_EXP(
        listOf("has exp", "hasexp", "exp"),
        "Checks if a player has enough exp",
        listOf("amount")
    ),
    DOES_NOT_HAVE_EXP(
        listOf("!has exp", "!hasexp", "!exp"),
        "Checks if a player has enough exp",
        listOf("amount")
    ),
    HAS_PERMISSION(
        listOf("has permission", "has perm", "haspermission", "hasperm", "perm"),
        "Checks if a player has a specific permission", listOf("permission")
    ),
    DOES_NOT_HAVE_PERMISSION(
        listOf("!has permission", "!has perm", "!haspermission", "!hasperm", "!perm"),
        "Checks if a player does not have a specific permission",
        listOf("permission")
    ),
    HAS_PERMISSIONS(
        listOf("has permissions", "has perms", "haspermissions", "hasperms", "perms"),
        "Checks if a player has a set amount of permissions", listOf("permissions")
    ),
    DOES_NOT_HAVE_PERMISSIONS(
        listOf("!has permissions", "!has perms", "!haspermissions", "!hasperms", "!perms"),
        "Checks if a player does not have a set amount of permission",
        listOf("permissions", "minimum")
    ),
    STRING_CONTAINS(
        listOf("string contains", "stringcontains", "contains"),
        "Checks if a string contains another string", listOf("input", "output")
    ),
    STRING_DOES_NOT_CONTAIN(
        listOf("!string contains", "!stringcontains", "!contains"),
        "Checks if a string does not contain another string", listOf("input", "output")
    ),
    STRING_EQUALS(
        listOf("string equals", "stringequals", "equals"),
        "Checks if a string equals another string", listOf("input", "output")
    ),
    STRING_DOES_NOT_EQUAL(
        listOf("!string equals", "!stringequals", "!equals"),
        "Checks if a string does not equal another string", listOf("input", "output")
    ),
    STRING_EQUALS_IGNORECASE(
        listOf("stringequalsignorecase", "string equals ignorecase", "equalsignorecase"),
        "Checks if a string equals another string ignoring case", listOf("input", "output")
    ),
    STRING_DOES_NOT_EQUAL_IGNORECASE(
        listOf("!stringequalsignorecase", "!string equals ignorecase", "!equalsignorecase"),
        "Checks if a string does not equal another string ignoring case",
        listOf("input", "output")
    ),
    GREATER_THAN(
        listOf(">", "greater than", "greaterthan"),
        "Checks if a number is greater than another number", listOf("input", "output")
    ),
    GREATER_THAN_EQUAL_TO(
        listOf(">=", "greater than or equal to", "greaterthanorequalto"),
        "Checks if a number is greater than or equal to another number",
        listOf("input", "output")
    ),
    EQUAL_TO(
        listOf("==", "equal to", "equalto"),
        "Checks if a number is equal to another number", listOf("input", "output")
    ),
    NOT_EQUAL_TO(
        listOf("!=", "not equal to", "notequalto"),
        "Checks if a number is not equal to another number", listOf("input", "output")
    ),
    LESS_THAN_EQUAL_TO(
        listOf("<=", "less than or equal to", "lessthanorequalto"),
        "Checks if a number is less than or equal to another number",
        listOf("input", "output")
    ),
    LESS_THAN(
        listOf("<", "less than", "lessthan"),
        "Checks if a number is less than another number", listOf("input", "output")
    ),
    REGEX_MATCHES(
        listOf("regex matches", "regex"),
        "Checks if a placeholder parsed string matches a regex pattern",
        listOf("input", "regex")
    ),
    REGEX_DOES_NOT_MATCH(
        listOf("!regex matches", "!regex"),
        "Checks if a placeholder parsed string does not match a regex pattern",
        listOf("input", "regex")
    ),
    STRING_LENGTH(
        listOf("string length"),
        "Checks if the given string's length is between the provided minimum and (optionally) maximum.",
        listOf("input", "min", "max")
    ),
    IS_OBJECT(
        listOf("is object"),
        "Checks if the given string can be parsed as a given Java object.",
        listOf("input", "object")
    );

    companion object {
        fun getType(s: String) = entries.find { s.lowercase() in it.identifiers }
    }
}
