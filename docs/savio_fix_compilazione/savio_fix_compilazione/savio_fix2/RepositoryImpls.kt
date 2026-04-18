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
        dao.getListWithItemsById(id)?.toDomain()

    override suspend fun saveList(list: ShoppingList) {
        val entity = list.toEntity()
        val items  = list.items.mapIndexed { idx, item -> item.toEntity(list.id, idx) }
        dao.saveListWithItems(entity, items)
        // FIX CRITICO: marca sempre questa lista come attiva.
        // MVP = flusso a lista singola: l'ultima lista salvata è sempre quella attiva.
        dao.clearActiveList()
        dao.setActiveList(list.id)
    }

    override suspend fun deleteList(id: String) = dao.deleteList(id)

    override suspend fun getActiveList(): ShoppingList? {
        val entity = dao.getActiveList() ?: return null
        return getListById(entity.id)   // ora restituisce la lista CON i suoi item
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

        // Fallback: carica da assets e persisti nel DB
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

    private var catalogCache: List<ProductCategory>? = null

    override suspend fun getOffersForStore(storeId: String): List<Offer> {
        val fromDb = dao.getOffersForStore(storeId)
        if (fromDb.isNotEmpty()) return fromDb.map { it.toDomain() }

        val fromAssets = assetDataSource.loadOffers()
            .filter { it.storeId == storeId }
        if (fromAssets.isNotEmpty()) {
            dao.insertAll(fromAssets.map { it.toEntity() })
        }
        return fromAssets.map { it.toDomain() }
    }

    override suspend fun getOffersForCategory(categoryId: String, areaId: String): List<Offer> =
        dao.getOffersForCategoryInArea(categoryId, areaId).map { it.toDomain() }

    override suspend fun getCatalog(): List<ProductCategory> =
        catalogCache ?: assetDataSource.loadCatalog()
            .map { it.toDomain() }
            .also { catalogCache = it }

    override suspend fun findCategoryForQuery(query: String): ProductCategory? {
        val catalog = getCatalog()
        val q = query.lowercase().trim()
        catalog.find { it.name.lowercase() == q }?.let { return it }
        catalog.find { it.name.lowercase().contains(q) }?.let { return it }
        return catalog.find { cat -> cat.aliases.any { alias -> alias.lowercase().contains(q) } }
    }

    override suspend fun syncRemoteData(): Result<Unit> = Result.success(Unit)

    suspend fun cleanExpiredOffers() {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        dao.deleteExpired(today)
    }
}
