package com.paolomarchionetti.savio.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "savio_prefs")

/**
 * Gestisce le preferenze utente tramite DataStore.
 * Nessun dato sensibile qui — solo configurazione UX locale.
 */
@Singleton
class UserPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val ONBOARDING_DONE  = booleanPreferencesKey("onboarding_done")
        val SELECTED_AREA_ID = stringPreferencesKey("selected_area_id")
        val ACTIVE_LIST_ID   = stringPreferencesKey("active_list_id")
        val LAST_SYNC_EPOCH  = longPreferencesKey("last_sync_epoch")
        val DARK_MODE_FORCED = booleanPreferencesKey("dark_mode_forced")
    }

    // ── Lettura ───────────────────────────────────────────────────────────────

    val isOnboardingDone: Flow<Boolean> = context.dataStore.data
        .catchIO()
        .map { it[Keys.ONBOARDING_DONE] ?: false }

    val selectedAreaId: Flow<String?> = context.dataStore.data
        .catchIO()
        .map { it[Keys.SELECTED_AREA_ID] }

    val activeListId: Flow<String?> = context.dataStore.data
        .catchIO()
        .map { it[Keys.ACTIVE_LIST_ID] }

    val lastSyncEpoch: Flow<Long> = context.dataStore.data
        .catchIO()
        .map { it[Keys.LAST_SYNC_EPOCH] ?: 0L }

    // ── Scrittura ─────────────────────────────────────────────────────────────

    suspend fun setOnboardingDone() {
        context.dataStore.edit { it[Keys.ONBOARDING_DONE] = true }
    }

    suspend fun setSelectedArea(areaId: String) {
        context.dataStore.edit { it[Keys.SELECTED_AREA_ID] = areaId }
    }

    suspend fun setActiveList(listId: String) {
        context.dataStore.edit { it[Keys.ACTIVE_LIST_ID] = listId }
    }

    suspend fun updateLastSync() {
        context.dataStore.edit { it[Keys.LAST_SYNC_EPOCH] = System.currentTimeMillis() }
    }

    // ── Util ──────────────────────────────────────────────────────────────────

    /** Gestisce IOException senza crashare l'app — ritorna Preferences vuote */
    private fun Flow<Preferences>.catchIO() = catch { e ->
        if (e is IOException) emit(emptyPreferences()) else throw e
    }
}
