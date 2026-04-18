package com.paolomarchionetti.savio.core.designsystem

import androidx.compose.ui.graphics.Color

// ── Palette primaria — Verde petrolio (fiducia, risparmio, intelligenza) ──────
val SavioGreen900   = Color(0xFF0D3D2E)   // testo su chiaro / bottoni primari dark
val SavioGreen800   = Color(0xFF155740)
val SavioGreen700   = Color(0xFF1B6E50)   // colore primario principale
val SavioGreen600   = Color(0xFF228660)
val SavioGreen500   = Color(0xFF2A9D76)
val SavioGreen100   = Color(0xFFD1F0E4)   // sfondo badge copertura
val SavioGreen050   = Color(0xFFF0FBF5)   // sfondo card verde chiaro

// ── Palette secondaria — Ambra/Arancio (offerta, energia, azione) ─────────────
val SavioAmber600   = Color(0xFFD97706)   // offerte, badge promozione
val SavioAmber500   = Color(0xFFF59E0B)
val SavioAmber100   = Color(0xFFFEF3C7)
val SavioAmber050   = Color(0xFFFFFBEB)

// ── Neutri ───────────────────────────────────────────────────────────────────
val SavioNeutral950 = Color(0xFF0C0F0E)   // testo principale
val SavioNeutral800 = Color(0xFF2D3330)
val SavioNeutral600 = Color(0xFF5A6460)
val SavioNeutral400 = Color(0xFF8E9B96)
val SavioNeutral200 = Color(0xFFCDD5D1)
val SavioNeutral100 = Color(0xFFE8EDEB)
val SavioNeutral050 = Color(0xFFF4F7F5)   // sfondo app (bianco caldo leggermente verde)
val White           = Color(0xFFFFFFFF)

// ── Semantici ─────────────────────────────────────────────────────────────────
val SavioSuccess    = SavioGreen700
val SavioWarning    = SavioAmber600
val SavioError      = Color(0xFFD32F2F)
val SavioInfo       = Color(0xFF1565C0)

// ── Affidabilità dati — usati in ConfidenceBadge ─────────────────────────────
val ConfidenceHigh      = SavioGreen700
val ConfidenceMedium    = SavioAmber600
val ConfidenceLow       = Color(0xFF9E9E9E)
