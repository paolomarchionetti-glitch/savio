package com.paolomarchionetti.savio.data.assets

import android.content.Context
import com.paolomarchionetti.savio.data.assets.dto.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Sorgente dati locale: legge i JSON seed dalla cartella assets/.
 * Questi file vengono aggiornati senza dover rilasciare un nuovo APK
 * tramite il meccanismo di sync remoto in RemoteDataSource.
 *
 * Ordine di priorità dati:
 * 1. File aggiornati scaricati in cache (RemoteDataSource)
 * 2. File seed nel bundle dell'app (questo AssetDataSource)
 */
@Singleton
class AssetDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val json = Json {
        ignoreUnknownKeys   = true   // tollerante verso versioni future del JSON
        isLenient           = true
        coerceInputValues   = true
    }

    fun loadCatalog(): List<ProductCategoryDto> =
        loadJson("catalog_v1.json")

    fun loadStores(): List<StoreDto> =
        loadJson("stores_pilot_v1.json")

    fun loadOffers(): List<OfferDto> =
        loadJson("offers_current.json")

    fun loadEquivalences(): List<EquivalenceDto> =
        loadJson("equivalences_v1.json")

    fun loadAreas(): List<AreaDto> =
        loadJson("areas_v1.json")

    // ── Privato ───────────────────────────────────────────────────────────────

    private inline fun <reified T> loadJson(fileName: String): List<T> {
        return try {
            val raw = context.assets.open(fileName).bufferedReader().readText()
            json.decodeFromString<List<T>>(raw)
        } catch (e: Exception) {
            // Seed mancante o corrotto: fallback a lista vuota
            // In produzione aggiungere Crashlytics.recordException(e)
            emptyList()
        }
    }
}
