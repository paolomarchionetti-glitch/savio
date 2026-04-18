package com.paolomarchionetti.savio.data.local.dao

import androidx.room.*
import com.paolomarchionetti.savio.data.local.entity.*
import kotlinx.coroutines.flow.Flow

// ── ShoppingListDao ───────────────────────────────────────────────────────────

@Dao
interface ShoppingListDao {

    @Transaction
    @Query("SELECT * FROM shopping_lists ORDER BY updatedAt DESC")
    fun getAllLists(): Flow<List<ShoppingListWithItems>>

    // FIX: restituisce la lista CON i suoi item (usato da getActiveList nel repository)
    @Transaction
    @Query("SELECT * FROM shopping_lists WHERE id = :id LIMIT 1")
    suspend fun getListWithItemsById(id: String): ShoppingListWithItems?

    // Versione senza item — usata solo quando servono metadati lista (non items)
    @Query("SELECT * FROM shopping_lists WHERE id = :id LIMIT 1")
    suspend fun getListById(id: String): ShoppingListEntity?

    @Query("SELECT * FROM shopping_lists WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveList(): ShoppingListEntity?

    @Query("UPDATE shopping_lists SET isActive = 0")
    suspend fun clearActiveList()

    @Query("UPDATE shopping_lists SET isActive = 1 WHERE id = :id")
    suspend fun setActiveList(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(list: ShoppingListEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<ShoppingListItemEntity>)

    @Query("DELETE FROM shopping_lists WHERE id = :id")
    suspend fun deleteList(id: String)

    @Query("DELETE FROM shopping_list_items WHERE listId = :listId")
    suspend fun deleteItemsForList(listId: String)

    @Transaction
    suspend fun saveListWithItems(list: ShoppingListEntity, items: List<ShoppingListItemEntity>) {
        insertList(list)
        deleteItemsForList(list.id)
        if (items.isNotEmpty()) insertItems(items)
    }
}

/** Relazione 1:N — Room carica lista + items in un'unica query JOIN */
data class ShoppingListWithItems(
    @Embedded val list: ShoppingListEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "listId"
    )
    val items: List<ShoppingListItemEntity>
)

// ── StoreDao ──────────────────────────────────────────────────────────────────

@Dao
interface StoreDao {

    @Query("SELECT * FROM stores WHERE areaId = :areaId")
    suspend fun getStoresByArea(areaId: String): List<StoreEntity>

    @Query("SELECT * FROM stores WHERE id = :id LIMIT 1")
    suspend fun getStoreById(id: String): StoreEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stores: List<StoreEntity>)

    @Query("DELETE FROM stores")
    suspend fun clearAll()
}

// ── OfferDao ──────────────────────────────────────────────────────────────────

@Dao
interface OfferDao {

    @Query("SELECT * FROM offers WHERE storeId = :storeId")
    suspend fun getOffersForStore(storeId: String): List<OfferEntity>

    @Query("""
        SELECT o.* FROM offers o
        INNER JOIN stores s ON o.storeId = s.id
        WHERE o.productCategoryId = :categoryId
          AND s.areaId = :areaId
    """)
    suspend fun getOffersForCategoryInArea(categoryId: String, areaId: String): List<OfferEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(offers: List<OfferEntity>)

    @Query("DELETE FROM offers WHERE validTo < :isoDate")
    suspend fun deleteExpired(isoDate: String)

    @Query("DELETE FROM offers")
    suspend fun clearAll()
}
