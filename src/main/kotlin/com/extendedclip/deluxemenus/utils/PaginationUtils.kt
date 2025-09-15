package com.extendedclip.deluxemenus.utils

import kotlin.math.ceil

object PaginationUtils {
    /**
     * Loose parsing of a page number. If the provided argument is not a number or is less than 1, 1 is returned.
     * If the provided page number is greater than the maximum number of pages, the maximum number of pages is returned.
     * @param itemsPerPage The number of items per page
     * @param itemsCount The total number of items
     * @param pages The maximum number of pages
     * @param argument The argument to parse as a page number
     * @return The parsed page number
     */
    fun parsePage(
        itemsPerPage: Int, itemsCount: Int, pages: Int?,
        argument: String?
    ): Int {
        if (itemsCount <= itemsPerPage || argument == null) return 1

        val page = argument.toIntOrNull()
        if (page == null || page < 1) return 1

        val maxPages = pages ?: getPagesCount(itemsPerPage, itemsCount)
        return if (page > maxPages) maxPages else page
    }

    fun getPagesCount(itemsPerPage: Int, itemsCount: Int) = ceil(itemsCount.toDouble() / itemsPerPage).toInt()
}
