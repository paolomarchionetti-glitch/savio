package com.paolomarchionetti.savio.domain.model

import java.time.LocalDate

// ── Area geografica ───────────────────────────────────────────────────────────
data class Area(
    val id: String,
    val displayName: String,   // Es: "Bologna - Centro"
    val cap: String,           // Es: "40121"
    val city: String           // Es: "Bologna"
)

// ── Supermercato ──────────────────────────────────────────────────────────────
data class Store(
    val id: String,
    val name: String,          // Es: "Esselunga"
    val branch: String,        // Es: "Via Indipendenza, 3"
    val chain: String,         // Es: "esselunga" — usato per logo e link
    val areaId: String,
    val address: String,
    val mapsUrl: String?,      // Link Google Maps
    val leafletUrl: String?    // Link volantino ufficiale
)

// ── Categoria prodotto ────────────────────────────────────────────────────────
data class ProductCategory(
    val id: String,
    val name: String,          // Es: "Acqua naturale 1.5L"
    val aliases: List<String>  // Es: ["acqua naturale", "acqua lt 1.5", "acqua bottiglia grande"]
)

// ── Offerta ───────────────────────────────────────────────────────────────────
data class Offer(
    val id: String,
    val storeId: String,
    val productCategoryId: String,
    val productName: String,       // Nome come appare sull'offerta
    val brand: String?,
    val priceEur: Double,          // Prezzo in euro
    val pricePerUnit: Double?,     // Prezzo per unità (es. €/kg) — opzionale
    val requiresFidelityCard: Boolean,
    val validFrom: LocalDate,
    val validTo: LocalDate,
    val sourceType: OfferSourceType,
    val confidenceLevel: ConfidenceLevel
)

enum class OfferSourceType {
    LEAFLET,            // Volantino digitale ufficiale
    USER_CONTRIBUTION,  // Contributo manuale utente
    PARTNER_FEED,       // Feed diretto retailer (futuro)
    ISTAT_CALIBRATION   // Solo per calibrazione, non mostrato come prezzo preciso
}

enum class ConfidenceLevel { HIGH, MEDIUM, LOW }

// ── Elemento lista spesa ──────────────────────────────────────────────────────
data class ShoppingListItem(
    val id: String,
    val name: String,          // Come digitato dall'utente
    val categoryId: String?,   // null = non ancora normalizzato
    val quantity: Int = 1,
    val brandPreference: String? = null,  // null = qualsiasi marca
    val isChecked: Boolean = false
)

// ── Lista spesa ───────────────────────────────────────────────────────────────
data class ShoppingList(
    val id: String,
    val name: String,
    val items: List<ShoppingListItem>,
    val areaId: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// ── Stima carrello per un negozio ─────────────────────────────────────────────
data class BasketEstimate(
    val store: Store,
    val estimatedTotalEur: Double,
    val coveredItems: List<CoveredItem>,
    val uncoveredItems: List<ShoppingListItem>,
    val coveragePercent: Int,     // 0-100
    val confidence: ConfidenceLevel,
    val dataAsOf: LocalDate,
    val rankingPosition: Int      // 1 = consigliato, 2-3 = alternative
)

data class CoveredItem(
    val item: ShoppingListItem,
    val offer: Offer,
    val isExactMatch: Boolean     // true = SKU esatto, false = equivalenza categoria
)
