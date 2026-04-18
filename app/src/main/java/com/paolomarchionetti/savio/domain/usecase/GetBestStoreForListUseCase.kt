package com.paolomarchionetti.savio.domain.usecase

import com.paolomarchionetti.savio.domain.model.*
import com.paolomarchionetti.savio.domain.repository.OfferRepository
import com.paolomarchionetti.savio.domain.repository.StoreRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * Motore di ranking principale.
 *
 * Formula:
 *   score = prezzo_stimato_carrello
 *         + PENALTY_COVERAGE  × (100 - copertura%)   ← penalizza liste parzialmente coperte
 *         + PENALTY_EQUIVALENCE × num_equivalenze     ← penalizza match non esatti
 *         - BONUS_FRESHNESS  × giorni_freschezza      ← premia dati recenti
 *
 * Vincolo MVP:
 *   - Store con copertura < MIN_COVERAGE_PERCENT non vengono proposti come prima scelta
 *   - Restituisce max MAX_RESULTS store ordinati per score crescente
 */
class GetBestStoreForListUseCase @Inject constructor(
    private val storeRepository: StoreRepository,
    private val offerRepository: OfferRepository
) {

    companion object {
        private const val MIN_COVERAGE_PERCENT   = 40
        private const val MAX_RESULTS            = 3
        private const val PENALTY_COVERAGE       = 0.5    // €/punto% mancante
        private const val PENALTY_EQUIVALENCE    = 0.2    // € per ogni match non esatto
        private const val BONUS_FRESHNESS        = 0.05   // € per ogni giorno di freschezza (max 14gg)
    }

    suspend operator fun invoke(
        list: ShoppingList
    ): List<BasketEstimate> {

        val today  = LocalDate.now()
        val stores = storeRepository.getStoresByArea(list.areaId)
        val catalog = offerRepository.getCatalog()

        // Normalizza ogni item della lista verso una categoria prodotto
        val normalizedItems: Map<ShoppingListItem, ProductCategory?> = list.items.associateWith { item ->
            item.categoryId?.let { id -> catalog.find { it.id == id } }
                ?: offerRepository.findCategoryForQuery(item.name)
        }

        val estimates = stores.mapNotNull { store ->
            buildEstimate(store, list.items, normalizedItems, today)
        }

        // Filtra store sotto la soglia minima di copertura
        val validEstimates = estimates.filter { it.coveragePercent >= MIN_COVERAGE_PERCENT }
            .ifEmpty { estimates }   // Se nessuno supera la soglia, mostra tutti con warning

        // Calcola score e ordina
        return validEstimates
            .sortedBy { it.computeScore() }
            .take(MAX_RESULTS)
            .mapIndexed { index, estimate -> estimate.copy(rankingPosition = index + 1) }
    }

    private suspend fun buildEstimate(
        store: Store,
        items: List<ShoppingListItem>,
        normalizedItems: Map<ShoppingListItem, ProductCategory?>,
        today: LocalDate
    ): BasketEstimate? {

        val offers = offerRepository.getOffersForStore(store.id)
            .filter { it.validTo >= today }   // Solo offerte ancora valide

        val coveredItems   = mutableListOf<CoveredItem>()
        val uncoveredItems = mutableListOf<ShoppingListItem>()

        for (item in items) {
            val category = normalizedItems[item]
            val matchingOffer = category?.let {
                offers.filter { o -> o.productCategoryId == it.id }
                      .minByOrNull { o -> o.priceEur }   // prendi l'offerta più bassa
            }

            if (matchingOffer != null) {
                coveredItems.add(CoveredItem(
                    item          = item,
                    offer         = matchingOffer,
                    isExactMatch  = matchingOffer.productName.contains(item.name, ignoreCase = true)
                ))
            } else {
                uncoveredItems.add(item)
            }
        }

        val totalItems      = items.size
        val coveragePercent = if (totalItems == 0) 0
                              else (coveredItems.size * 100) / totalItems

        val estimatedTotal  = coveredItems.sumOf { it.offer.priceEur * it.item.quantity }

        // Confidence: se tutti i dati sono HIGH → HIGH, se almeno uno LOW → LOW
        val confidence = when {
            coveredItems.all   { it.offer.confidenceLevel == ConfidenceLevel.HIGH   } -> ConfidenceLevel.HIGH
            coveredItems.any   { it.offer.confidenceLevel == ConfidenceLevel.LOW    } -> ConfidenceLevel.LOW
            else                                                                       -> ConfidenceLevel.MEDIUM
        }

        val mostRecentDate = coveredItems.maxOfOrNull { it.offer.validFrom } ?: today

        return BasketEstimate(
            store            = store,
            estimatedTotalEur = estimatedTotal,
            coveredItems     = coveredItems,
            uncoveredItems   = uncoveredItems,
            coveragePercent  = coveragePercent,
            confidence       = confidence,
            dataAsOf         = mostRecentDate,
            rankingPosition  = 0   // Assegnato dopo il sort
        )
    }

    /** Punteggio composito per il ranking. Più basso = migliore. */
    private fun BasketEstimate.computeScore(): Double {
        val freshnessBonus = minOf(
            (LocalDate.now().toEpochDay() - dataAsOf.toEpochDay()).toDouble(),
            14.0
        ) * BONUS_FRESHNESS

        val penaltyEquivalence = coveredItems.count { !it.isExactMatch } * PENALTY_EQUIVALENCE
        val penaltyCoverage    = (100 - coveragePercent) * PENALTY_COVERAGE

        return estimatedTotalEur + penaltyCoverage + penaltyEquivalence - freshnessBonus
    }
}
