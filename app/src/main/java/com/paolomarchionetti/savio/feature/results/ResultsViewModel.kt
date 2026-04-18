package com.paolomarchionetti.savio.feature.results

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paolomarchionetti.savio.domain.model.BasketEstimate
import com.paolomarchionetti.savio.domain.usecase.GetActiveShoppingListUseCase
import com.paolomarchionetti.savio.domain.usecase.GetBestStoreForListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ResultsUiState(
    val estimates: List<BasketEstimate> = emptyList(),
    val isLoading: Boolean              = true,
    val error: String?                  = null
)

@HiltViewModel
class ResultsViewModel @Inject constructor(
    private val getActiveList: GetActiveShoppingListUseCase,
    private val getBestStore: GetBestStoreForListUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResultsUiState())
    val uiState: StateFlow<ResultsUiState> = _uiState.asStateFlow()

    init {
        runRanking()
    }

    private fun runRanking() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val list = getActiveList()
                if (list == null || list.items.isEmpty()) {
                    _uiState.update { it.copy(isLoading = false, estimates = emptyList()) }
                    return@launch
                }
                val estimates = getBestStore(list)
                _uiState.update { it.copy(isLoading = false, estimates = estimates) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = "Errore nel calcolo. Riprova.")
                }
            }
        }
    }

    fun retry() = runRanking()
}
