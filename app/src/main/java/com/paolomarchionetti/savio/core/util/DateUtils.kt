package com.paolomarchionetti.savio.core.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtils {
    private val IT_FORMATTER = DateTimeFormatter.ofPattern("d MMM", Locale.ITALIAN)

    fun formatShort(date: LocalDate): String = date.format(IT_FORMATTER)

    fun formatDataAsOf(date: LocalDate): String = "Dati al ${formatShort(date)}"

    fun isExpired(date: LocalDate): Boolean = date.isBefore(LocalDate.now())
}