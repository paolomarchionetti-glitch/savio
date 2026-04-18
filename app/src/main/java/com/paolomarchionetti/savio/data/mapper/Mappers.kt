package com.paolomarchionetti.savio.data.mapper

import com.paolomarchionetti.savio.data.assets.dto.*
import com.paolomarchionetti.savio.data.local.dao.ShoppingListWithItems
import com.paolomarchionetti.savio.data.local.entity.*
import com.paolomarchionetti.savio.domain.model.*
import java.time.LocalDate

// ── DTO → Domain ──────────────────────────────────────────────────────────────

fun AreaDto.toDomain() = Area(
    id          = id,
    displayName = displayName,
    cap         = cap,
    city        = city
)

fun StoreDto.toDomain() = Store(
    id          = id,
    name        = name,
    branch      = branch,
    chain       = chain,
    areaId      = areaId,
    address     = address,
    mapsUrl     = mapsUrl,
    leafletUrl  = leafletUrl
)

fun ProductCategoryDto.toDomain() = ProductCategory(
    id      = id,
    name    = name,
    aliases = aliases
)

fun OfferDto.toDomain() = Offer(
    id                  = id,
    storeId             = storeId,
    productCategoryId   = productCategoryId,
    productName         = productName,
    brand               = brand,
    priceEur            = priceEur,
    pricePerUnit        = pricePerUnit,
    requiresFidelityCard = requiresFidelityCard,
    validFrom           = LocalDate.parse(validFrom),
    validTo             = LocalDate.parse(validTo),
    sourceType          = runCatching { OfferSourceType.valueOf(sourceType) }
                            .getOrDefault(OfferSourceType.LEAFLET),
    confidenceLevel     = runCatching { ConfidenceLevel.valueOf(confidenceLevel) }
                            .getOrDefault(ConfidenceLevel.MEDIUM)
)

// ── DTO → Entity ──────────────────────────────────────────────────────────────

fun StoreDto.toEntity() = StoreEntity(
    id          = id,
    name        = name,
    branch      = branch,
    chain       = chain,
    areaId      = areaId,
    address     = address,
    mapsUrl     = mapsUrl,
    leafletUrl  = leafletUrl
)

fun OfferDto.toEntity() = OfferEntity(
    id                   = id,
    storeId              = storeId,
    productCategoryId    = productCategoryId,
    productName          = productName,
    brand                = brand,
    priceEur             = priceEur,
    pricePerUnit         = pricePerUnit,
    requiresFidelityCard = requiresFidelityCard,
    validFrom            = validFrom,
    validTo              = validTo,
    sourceType           = sourceType,
    confidenceLevel      = confidenceLevel
)

// ── Entity → Domain ───────────────────────────────────────────────────────────

fun StoreEntity.toDomain() = Store(
    id          = id,
    name        = name,
    branch      = branch,
    chain       = chain,
    areaId      = areaId,
    address     = address,
    mapsUrl     = mapsUrl,
    leafletUrl  = leafletUrl
)

fun OfferEntity.toDomain() = Offer(
    id                   = id,
    storeId              = storeId,
    productCategoryId    = productCategoryId,
    productName          = productName,
    brand                = brand,
    priceEur             = priceEur,
    pricePerUnit         = pricePerUnit,
    requiresFidelityCard = requiresFidelityCard,
    validFrom            = LocalDate.parse(validFrom),
    validTo              = LocalDate.parse(validTo),
    sourceType           = runCatching { OfferSourceType.valueOf(sourceType) }
                             .getOrDefault(OfferSourceType.LEAFLET),
    confidenceLevel      = runCatching { ConfidenceLevel.valueOf(confidenceLevel) }
                             .getOrDefault(ConfidenceLevel.MEDIUM)
)

fun ShoppingListWithItems.toDomain() = ShoppingList(
    id        = list.id,
    name      = list.name,
    areaId    = list.areaId,
    createdAt = list.createdAt,
    updatedAt = list.updatedAt,
    items     = items.map { it.toDomain() }
)

fun ShoppingListItemEntity.toDomain() = ShoppingListItem(
    id              = id,
    name            = name,
    categoryId      = categoryId,
    quantity        = quantity,
    brandPreference = brandPreference,
    isChecked       = isChecked
)

// ── Domain → Entity ───────────────────────────────────────────────────────────

fun ShoppingList.toEntity() = ShoppingListEntity(
    id        = id,
    name      = name,
    areaId    = areaId,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun ShoppingListItem.toEntity(listId: String, sortOrder: Int = 0) = ShoppingListItemEntity(
    id              = id,
    listId          = listId,
    name            = name,
    categoryId      = categoryId,
    quantity        = quantity,
    brandPreference = brandPreference,
    isChecked       = isChecked,
    sortOrder       = sortOrder
)
