package com.paolomarchionetti.savio.core.util

object PriceFormatter {
    fun format(price: Double): String = "€ %.2f".format(price).replace(".", ",")
    fun formatDiff(diff: Double): String {
        val sign = if (diff >= 0) "+" else ""
        return "$sign%.2f€".format(diff).replace(".", ",")
    }
}