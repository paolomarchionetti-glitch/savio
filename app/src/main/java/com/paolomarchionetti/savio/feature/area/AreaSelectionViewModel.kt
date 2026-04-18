package com.paolomarchionetti.savio.feature.area

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paolomarchionetti.savio.data.local.datastore.UserPreferencesDataStore
import com.paolomarchionetti.savio.domain.model.Area
import com.paolomarchionetti.savio.domain.usecase.GetAllAreasUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AreaSelectionUiState(
    val allAreas: List<Area>    = emptyList(),
    val filteredAreas: List<Area> = emptyList(),
    val searchQuery: String     = "",
    val selectedAreaId: String? = null,
    val isLoading: Boolean      = true
)

@HiltViewModel
class AreaSelectionViewModel @Inject constructor(
    private val getAllAreasUseCase: GetAllAreasUseCase,
    private val prefs: UserPreferencesDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(AreaSelectionUiState())
    val uiState: StateFlow<AreaSelectionUiState> = _uiState.asStateFlow()

    init {
        loadAreas()
    }

    private fun loadAreas() {
        viewModelScope.launch {
            val areas = getAllAreasUseCase()
            _uiState.update {
                it.copy(
                    allAreas      = areas,
                    filteredAreas = areas,
                    isLoading     = false
                )
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        val filtered = if (query.isBlank()) {
            _uiState.value.allAreas
        } else {
            _uiState.value.allAreas.filter { area ->
                area.displayName.contains(query, ignoreCase = true) ||
                area.cap.contains(query) ||
                area.city.contains(query, ignoreCase = true)
            }
        }
        _uiState.update { it.copy(searchQuery = query, filteredAreas = filtered) }
    }

    fun selectArea(area: Area) {
        _uiState.update { it.copy(selectedAreaId = area.id) }
    }

    fun confirmArea() {
        val areaId = _uiState.value.selectedAreaId ?: return
        viewModelScope.launch {
            prefs.setSelectedArea(areaId)
        }
    }
}
