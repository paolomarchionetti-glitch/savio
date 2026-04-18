package com.paolomarchionetti.savio.core.navigation

/**
 * Definizione centralizzata di tutte le destinazioni di navigazione.
 * Uso di sealed class per type safety e autocompletamento.
 */
sealed class AppDestination(val route: String) {

    // ── Onboarding ────────────────────────────────────────────────────────────
    data object Onboarding : AppDestination("onboarding")

    // ── Selezione area ────────────────────────────────────────────────────────
    data object AreaSelection : AppDestination("area_selection")

    // ── Lista spesa ───────────────────────────────────────────────────────────
    data object ShoppingList : AppDestination("shopping_list")

    // ── Risultati confronto ───────────────────────────────────────────────────
    data object Results : AppDestination("results")

    // ── Dettaglio supermercato ────────────────────────────────────────────────
    data object StoreDetail : AppDestination("store_detail/{storeId}") {
        fun createRoute(storeId: String) = "store_detail/$storeId"
        const val ARG_STORE_ID = "storeId"
    }

    // ── Impostazioni ─────────────────────────────────────────────────────────
    data object Settings : AppDestination("settings")
}
