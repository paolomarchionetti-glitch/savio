package com.paolomarchionetti.savio.core.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Typography Savio.
 * Font system: usa il font di sistema (sans-serif) per semplicità nel MVP.
 * Sostituire con Google Fonts (es. DM Sans + Fraunces) in una versione successiva
 * aggiungendo i file .ttf in res/font/ e collegandoli qui.
 */

// Placeholder: font system Android. Da sostituire con font custom.
val SavioFontFamily = FontFamily.Default

val SavioTypography = Typography(

    // Titolo principale schermata (es. "La tua lista")
    headlineLarge = TextStyle(
        fontFamily  = SavioFontFamily,
        fontWeight  = FontWeight.Bold,
        fontSize    = 28.sp,
        lineHeight  = 34.sp,
        letterSpacing = (-0.5).sp
    ),

    // Titolo card negozio (es. "Esselunga — Via Roma")
    headlineMedium = TextStyle(
        fontFamily  = SavioFontFamily,
        fontWeight  = FontWeight.SemiBold,
        fontSize    = 22.sp,
        lineHeight  = 28.sp,
        letterSpacing = (-0.3).sp
    ),

    // Prezzo stimato carrello (es. "€ 34,80")
    headlineSmall = TextStyle(
        fontFamily  = SavioFontFamily,
        fontWeight  = FontWeight.Bold,
        fontSize    = 18.sp,
        lineHeight  = 24.sp
    ),

    // Label bottoni, tab
    titleMedium = TextStyle(
        fontFamily  = SavioFontFamily,
        fontWeight  = FontWeight.SemiBold,
        fontSize    = 15.sp,
        lineHeight  = 20.sp,
        letterSpacing = 0.1.sp
    ),

    // Testo corpo principale
    bodyLarge = TextStyle(
        fontFamily  = SavioFontFamily,
        fontWeight  = FontWeight.Normal,
        fontSize    = 16.sp,
        lineHeight  = 24.sp
    ),

    bodyMedium = TextStyle(
        fontFamily  = SavioFontFamily,
        fontWeight  = FontWeight.Normal,
        fontSize    = 14.sp,
        lineHeight  = 20.sp
    ),

    // Note, disclaimer, data aggiornamento
    bodySmall = TextStyle(
        fontFamily  = SavioFontFamily,
        fontWeight  = FontWeight.Normal,
        fontSize    = 12.sp,
        lineHeight  = 16.sp,
        color       = SavioNeutral600
    ),

    // Badge, chip, label piccoli
    labelSmall = TextStyle(
        fontFamily  = SavioFontFamily,
        fontWeight  = FontWeight.Medium,
        fontSize    = 11.sp,
        lineHeight  = 14.sp,
        letterSpacing = 0.3.sp
    )
)
