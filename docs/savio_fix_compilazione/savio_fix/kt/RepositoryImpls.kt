package com.paolomarchionetti.savio.data.repository

import com.paolomarchionetti.savio.data.assets.AssetDataSource
import com.paolomarchionetti.savio.data.local.dao.OfferDao
import com.paolomarchionetti.savio.data.local.dao.ShoppingListDao
import com.paolomarchionetti.savio.data.local.dao.StoreDao
import com.paolomarchionetti.savio.data.mapper.*
import com.paolomarchionetti.savio.domain.model.*
import com.paolomarchionetti.savio.domain.repository.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

// ── ShoppingListRepositoryImpl ────────────────────────────────────────────────

@Singleton
class ShoppingListRepositoryImpl @Inject constructor(
    private val dao: ShoppingListDao
) : ShoppingListRepository {

    override fun getAllLists(): Flow<List<ShoppingList>> =
        dao.getAllLists().map { list -> list.map { it.toDomain() } }

    override suspend fun getListById(id: String): ShoppingList? =
        // FIX: usa getListWithItemsById invece di getListById
        // così restituiamo la lista CON i suoi item (non più emptyList)
        dao.getListWithItemsById(id)?.toDomain()

    override suspend fun saveList(list: ShoppingList) {
        val entity = list.toEntity()
        val items  = list.items.mapIndexed { idx, item -> item.toEntity(list.id, idx) }
        dao.saveListWithItems(entity, items)
    }

    override suspend fun deleteList(id: String) = dao.deleteList(id)

    override suspend fun getActiveList(): ShoppingList? {
        // 1. Trova l'entity con isActive = true
        val entity = dao.getActiveList() ?: return null
        // 2. Carica la lista COMPLETA con i suoi item tramite getListById (ora corretto)
        return getListById(entity.id)
    }

    override suspend fun setActiveList(id: String) {
        dao.clearActiveList()
        dao.setActiveList(id)
    }
}

// ── StoreRepositoryImpl ───────────────────────────────────────────────────────

@Singleton
class StoreRepositoryImpl @Inject constructor(
    private val dao: StoreDao,
    private val assetDataSource: AssetDataSource
) : StoreRepository {

    override suspend fun getStoresByArea(areaId: String): List<Store> {
        val fromDb = dao.getStoresByArea(areaId)
        if (fromDb.isNotEmpty()) return fromDb.map { it.toDomain() }

        // Fallback: carica da assets e persisti nel DB per i prossimi accessi
        val fromAssets = assetDataSource.loadStores()
            .filter { it.areaId == areaId }
        if (fromAssets.isNotEmpty()) {
            dao.insertAll(fromAssets.map { it.toEntity() })
        }
        return fromAssets.map { it.toDomain() }
    }

    override suspend fun getStoreById(id: String): Store? =
        dao.getStoreById(id)?.toDomain()

    override suspend fun getAllAreas(): List<Area> =
        assetDataSource.loadAreas().map { it.toDomain() }
}

// ── OfferRepositoryImpl ───────────────────────────────────────────────────────

@Singleton
class OfferRepositoryImpl @Inject constructor(
    private val dao: OfferDao,
    private val assetDataSource: AssetDataSource
) : OfferRepository {

    // Cache in-memory del catalogo prodotti (immutabile per sessione)
    private var catalogCache: List<ProductCategory>? = null

    override suspend fun getOffersForStore(storeId: String): List<Offer> {
        val fromDb = dao.getOffersForStore(storeId)
        if (fromDb.isNotEmpty()) return fromDb.map { it.toDomain() }

        // Fallback: seed da assets
        val fromAssets = assetDataSource.loadOffers()
            .filter { it.storeId == storeId }
        if (fromAssets.isNotEmpty()) {
            dao.insertAll(fromAssets.map { it.toEntity() })
        }
        return fromAssets.map { it.toDomain() }
    }

    override suspend fun getOffersForCategory(categoryId: String, areaId: String): List<Offer> =
        dao.getOffersForCategoryInArea(categoryId, areaId).map { it.toDomain() }

    override suspend fun getCatalog(): List<ProductCategory> {
        return catalogCache ?: assetDataSource.loadCatalog()
            .map { it.toDomain() }
            .also { catalogCache = it }
    }

    override suspend fun findCategoryForQuery(query: String): ProductCategory? {
        val catalog = getCatalog()
        val q = query.lowercase().trim()
        // 1) Match esatto sul nome categoria
        catalog.find { it.name.lowercase() == q }?.let { return it }
        // 2) Il nome categoria contiene la query
        catalog.find { it.name.lowercase().contains(q) }?.let { return it }
        // 3) Un alias contiene la query
        return catalog.find { cat -> cat.aliases.any { alias -> alias.lowercase().contains(q) } }
    }

    override suspend fun syncRemoteData(): Result<Unit> {
        // MVP: sync remoto non ancora implementato.
        // v1.1: scarica JSON da BuildConfig.DATA_BASE_URL via Retrofit
        return Result.success(Unit)
    }

    /** Elimina offerte scadute — da chiamare all'avvio */
    suspend fun cleanExpiredOffers() {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        dao.deleteExpired(today)
    }
}
