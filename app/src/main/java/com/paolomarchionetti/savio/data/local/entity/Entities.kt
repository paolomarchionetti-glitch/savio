package com.paolomarchionetti.savio.data.local.entity

import androidx.room.*

@Entity(tableName = "shopping_lists")
data class ShoppingListEntity(
    @PrimaryKey val id: String,
    val name: String,
    val areaId: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isActive: Boolean = false
)

@Entity(
    tableName = "shopping_list_items",
    foreignKeys = [ForeignKey(
        entity        = ShoppingListEntity::class,
        parentColumns = ["id"],
        childColumns  = ["listId"],
        onDelete      = ForeignKey.CASCADE
    )],
    indices = [Index("listId")]
)
data class ShoppingListItemEntity(
    @PrimaryKey val id: String,
    val listId: String,
    val name: String,
    val categoryId: String?,
    val quantity: Int,
    val brandPreference: String?,
    val isChecked: Boolean,
    val sortOrder: Int = 0
)

@Entity(tableName = "stores")
data class StoreEntity(
    @PrimaryKey val id: String,
    val name: String,
    val branch: String,
    val chain: String,
    val areaId: String,
    val address: String,
    val mapsUrl: String?,
    val leafletUrl: String?
)

@Entity(tableName = "offers")
data class OfferEntity(
    @PrimaryKey val id: String,
    val storeId: String,
    val productCategoryId: String,
    val productName: String,
    val brand: String?,
    val priceEur: Double,
    val pricePerUnit: Double?,
    val requiresFidelityCard: Boolean,
    val validFrom: String,    // ISO 8601: "2026-04-15"
    val validTo: String,      // ISO 8601: "2026-04-22"
    val sourceType: String,   // OfferSourceType.name()
    val confidenceLevel: String  // ConfidenceLevel.name()
)
