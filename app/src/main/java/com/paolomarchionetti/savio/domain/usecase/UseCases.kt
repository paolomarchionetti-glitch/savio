package com.paolomarchionetti.savio.domain.usecase

import com.paolomarchionetti.savio.domain.model.*
import com.paolomarchionetti.savio.domain.repository.*
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

// ── Gestione lista spesa ──────────────────────────────────────────────────────

class GetAllShoppingListsUseCase @Inject constructor(
    private val repo: ShoppingListRepository
) {
    operator fun invoke(): Flow<List<ShoppingList>> = repo.getAllLists()
}

class SaveShoppingListUseCase @Inject constructor(
    private val repo: ShoppingListRepository
) {
    suspend operator fun invoke(list: ShoppingList) = repo.saveList(list)
}

class DeleteShoppingListUseCase @Inject constructor(
    private val repo: ShoppingListRepository
) {
    suspend operator fun invoke(listId: String) = repo.deleteList(listId)
}

class GetActiveShoppingListUseCase @Inject constructor(
    private val repo: ShoppingListRepository
) {
    suspend operator fun invoke(): ShoppingList? = repo.getActiveList()
}

// ── Gestione area ─────────────────────────────────────────────────────────────

class GetAllAreasUseCase @Inject constructor(
    private val repo: StoreRepository
) {
    suspend operator fun invoke(): List<Area> = repo.getAllAreas()
}

class GetStoresInAreaUseCase @Inject constructor(
    private val repo: StoreRepository
) {
    suspend operator fun invoke(areaId: String): List<Store> =
        repo.getStoresByArea(areaId)
}

// ── Catalogo e ricerca prodotti ───────────────────────────────────────────────

class SearchProductCategoryUseCase @Inject constructor(
    private val repo: OfferRepository
) {
    /**
     * Suggerisce categorie prodotto mentre l'utente digita.
     * Prima cerca match esatto nel nome, poi negli alias.
     */
    suspend operator fun invoke(query: String): List<ProductCategory> {
        if (query.length < 2) return emptyList()
        val catalog = repo.getCatalog()
        val q = query.lowercase().trim()
        return catalog.filter { cat ->
            cat.name.lowercase().contains(q) ||
            cat.aliases.any { alias -> alias.lowercase().contains(q) }
        }.take(8)
    }
}

// ── Aggiornamento dati remoti ─────────────────────────────────────────────────

class SyncRemoteDataUseCase @Inject constructor(
    private val repo: OfferRepository
) {
    /** Scarica JSON aggiornati dal CDN. Silenzioso in caso di errore. */
    suspend operator fun invoke(): Result<Unit> = repo.syncRemoteData()
}

// ── Utility: creazione nuova lista ───────────────────────────────────────────

fun createNewShoppingList(name: String, areaId: String): ShoppingList =
    ShoppingList(
        id        = UUID.randomUUID().toString(),
        name      = name,
        items     = emptyList(),
        areaId    = areaId
    )

fun createShoppingListItem(name: String, categoryId: String? = null): ShoppingListItem =
    ShoppingListItem(
        id         = UUID.randomUUID().toString(),
        name       = name,
        categoryId = categoryId
    )
