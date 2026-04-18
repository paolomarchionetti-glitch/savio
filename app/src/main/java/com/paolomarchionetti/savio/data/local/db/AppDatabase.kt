package com.paolomarchionetti.savio.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.paolomarchionetti.savio.data.local.dao.OfferDao
import com.paolomarchionetti.savio.data.local.dao.ShoppingListDao
import com.paolomarchionetti.savio.data.local.dao.StoreDao
import com.paolomarchionetti.savio.data.local.entity.OfferEntity
import com.paolomarchionetti.savio.data.local.entity.ShoppingListEntity
import com.paolomarchionetti.savio.data.local.entity.ShoppingListItemEntity
import com.paolomarchionetti.savio.data.local.entity.StoreEntity

@Database(
    entities = [
        ShoppingListEntity::class,
        ShoppingListItemEntity::class,
        StoreEntity::class,
        OfferEntity::class
    ],
    version  = 1,
    exportSchema = true    // true: genera JSON schema per migration history
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun shoppingListDao(): ShoppingListDao
    abstract fun storeDao(): StoreDao
    abstract fun offerDao(): OfferDao

    companion object {
        const val DATABASE_NAME = "savio_db"
    }
}
