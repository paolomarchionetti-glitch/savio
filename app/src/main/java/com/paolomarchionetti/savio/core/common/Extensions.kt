package com.paolomarchionetti.savio.core.common

fun String.normalizeForSearch(): String =
    this.lowercase().trim()
        .replace("à", "a").replace("è", "e").replace("é", "e")
        .replace("ì", "i").replace("ò", "o").replace("ù", "u")

fun Double.toEurString(): String = "€ %.2f".format(this).replace(".", ",")


