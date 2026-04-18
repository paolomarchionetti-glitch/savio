package com.paolomarchionetti.savio.di

import android.content.Context
import androidx.room.Room
import com.paolomarchionetti.savio.data.local.dao.OfferDao
import com.paolomarchionetti.savio.data.local.dao.ShoppingListDao
import com.paolomarchionetti.savio.data.local.dao.StoreDao
import com.paolomarchionetti.savio.data.local.db.AppDatabase
import com.paolomarchionetti.savio.data.repository.*
import com.paolomarchionetti.savio.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// ── Database Module ───────────────────────────────────────────────────────────

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
        .fallbackToDestructiveMigration()  // MVP: ok distruggere il DB in caso di migration
        // Produzione: sostituire con migration esplicite
        .build()

    @Provides fun provideShoppingListDao(db: AppDatabase): ShoppingListDao = db.shoppingListDao()
    @Provides fun provideStoreDao(db: AppDatabase): StoreDao               = db.storeDao()
    @Provides fun provideOfferDao(db: AppDatabase): OfferDao               = db.offerDao()
}

// ── Repository Module ─────────────────────────────────────────────────────────

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindShoppingListRepository(
        impl: ShoppingListRepositoryImpl
    ): ShoppingListRepository

    @Binds @Singleton
    abstract fun bindStoreRepository(
        impl: StoreRepositoryImpl
    ): StoreRepository

    @Binds @Singleton
    abstract fun bindOfferRepository(
        impl: OfferRepositoryImpl
    ): OfferRepository
}
