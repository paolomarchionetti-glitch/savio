package com.paolomarchionetti.savio.domain.repository

import com.paolomarchionetti.savio.domain.model.*
import kotlinx.coroutines.flow.Flow

interface ShoppingListRepository {
    fun getAllLists(): Flow<List<ShoppingList>>
    suspend fun getListById(id: String): ShoppingList?
    suspend fun saveList(list: ShoppingList)
    suspend fun deleteList(id: String)
    suspend fun getActiveList(): ShoppingList?
    suspend fun setActiveList(id: String)
}

interface StoreRepository {
    suspend fun getStoresByArea(areaId: String): List<Store>
    suspend fun getStoreById(id: String): Store?
    suspend fun getAllAreas(): List<Area>
}

interface OfferRepository {
    suspend fun getOffersForStore(storeId: String): List<Offer>
    suspend fun getOffersForCategory(categoryId: String, areaId: String): List<Offer>
    suspend fun getCatalog(): List<ProductCategory>
    suspend fun findCategoryForQuery(query: String): ProductCategory?
    /** Scarica aggiornamento JSON remoto se disponibile */
    suspend fun syncRemoteData(): Result<Unit>
}
