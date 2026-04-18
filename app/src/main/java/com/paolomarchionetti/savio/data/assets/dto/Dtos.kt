package com.paolomarchionetti.savio.data.assets.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AreaDto(
    val id: String,
    @SerialName("display_name") val displayName: String,
    val cap: String,
    val city: String
)

@Serializable
data class StoreDto(
    val id: String,
    val name: String,
    val branch: String,
    val chain: String,
    @SerialName("area_id")    val areaId: String,
    val address: String,
    @SerialName("maps_url")   val mapsUrl: String?    = null,
    @SerialName("leaflet_url") val leafletUrl: String? = null
)

@Serializable
data class ProductCategoryDto(
    val id: String,
    val name: String,
    val aliases: List<String> = emptyList()
)

@Serializable
data class OfferDto(
    val id: String,
    @SerialName("store_id")            val storeId: String,
    @SerialName("product_category_id") val productCategoryId: String,
    @SerialName("product_name")        val productName: String,
    val brand: String?                 = null,
    @SerialName("price_eur")           val priceEur: Double,
    @SerialName("price_per_unit")      val pricePerUnit: Double?  = null,
    @SerialName("requires_fidelity")   val requiresFidelityCard: Boolean = false,
    @SerialName("valid_from")          val validFrom: String,   // "2026-04-14"
    @SerialName("valid_to")            val validTo: String,     // "2026-04-22"
    @SerialName("source_type")         val sourceType: String   = "LEAFLET",
    @SerialName("confidence_level")    val confidenceLevel: String = "HIGH"
)

@Serializable
data class EquivalenceDto(
    @SerialName("category_id")  val categoryId: String,
    val aliases: List<String>
)
