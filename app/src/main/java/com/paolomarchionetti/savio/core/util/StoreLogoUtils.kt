package com.paolomarchionetti.savio.core.util

import androidx.annotation.DrawableRes
import com.paolomarchionetti.savio.R

/**
 * Restituisce il DrawableRes del logo per una catena di supermercati.
 * Usato in StoreRankingCard (ResultsScreen) e StoreDetailScreen.
 * Ritorna null se la catena non ha un logo — la UI mostra le iniziali come fallback.
 */
@DrawableRes
fun chainToLogoRes(chain: String): Int? = when (chain.lowercase().trim()) {
    "esselunga"  -> R.drawable.ic_store_esselunga
    "conad"      -> R.drawable.ic_store_conad
    "coop"       -> R.drawable.ic_store_coop
    "lidl"       -> R.drawable.ic_store_lidl
    "eurospin"   -> R.drawable.ic_store_eurospin
    "carrefour"  -> R.drawable.ic_store_carrefour
    "md"         -> R.drawable.ic_store_md
    else         -> null
}

/** Colore accent del brand — usato per badge e bordi nelle card. */
fun chainToColor(chain: String): Long = when (chain.lowercase().trim()) {
    "esselunga"  -> 0xFF1B6B58
    "conad"      -> 0xFFE3000F
    "coop"       -> 0xFF005CA9
    "lidl"       -> 0xFF0050AA
    "eurospin"   -> 0xFFF47920
    "carrefour"  -> 0xFF1E3A8A
    "md"         -> 0xFFD61A1A
    else         -> 0xFF888888
}