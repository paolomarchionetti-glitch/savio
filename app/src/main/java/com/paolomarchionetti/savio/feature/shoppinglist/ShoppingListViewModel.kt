package com.paolomarchionetti.savio.feature.shoppinglist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paolomarchionetti.savio.data.local.datastore.UserPreferencesDataStore
import com.paolomarchionetti.savio.domain.model.ShoppingList
import com.paolomarchionetti.savio.domain.model.ShoppingListItem
import com.paolomarchionetti.savio.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ShoppingListUiState(
    val items: List<ShoppingListItem>  = emptyList(),
    val inputText: String              = "",
    val suggestions: List<String>      = emptyList(),
    val currentListId: String?         = null,
    val areaId: String?                = null
)

@HiltViewModel
class ShoppingListViewModel @Inject constructor(
    private val getActiveList: GetActiveShoppingListUseCase,
    private val saveList: SaveShoppingListUseCase,
    private val searchCategory: SearchProductCategoryUseCase,
    private val prefs: UserPreferencesDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShoppingListUiState())
    val uiState: StateFlow<ShoppingListUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var currentList: ShoppingList? = null

    init {
        viewModelScope.launch {
            // Carica area selezionata
            prefs.selectedAreaId.collect { areaId ->
                _uiState.update { it.copy(areaId = areaId) }
                loadActiveList(areaId)
            }
        }
    }

    private suspend fun loadActiveList(areaId: String?) {
        val list = getActiveList() ?: run {
            // Nessuna lista attiva: crea una lista vuota default
            val newList = createNewShoppingList("Spesa", areaId ?: "bologna_centro")
            saveList(newList)
            newList
        }
        currentList = list
        _uiState.update { it.copy(items = list.items, currentListId = list.id) }
    }

    fun onInputChanged(text: String) {
        _uiState.update { it.copy(inputText = text, suggestions = emptyList()) }

        // Debounce ricerca categorie
        searchJob?.cancel()
        if (text.length >= 2) {
            searchJob = viewModelScope.launch {
                delay(300)
                val results = searchCategory(text)
                _uiState.update { it.copy(suggestions = results.map { cat -> cat.name }.take(4)) }
            }
        }
    }

    fun addItem() {
        val name = _uiState.value.inputText.trim()
        if (name.isBlank()) return
        addItemByName(name)
    }

    fun addItemFromSuggestion(suggestion: String) {
        addItemByName(suggestion)
    }

    private fun addItemByName(name: String) {
        val newItem = createShoppingListItem(name)
        val updatedItems = _uiState.value.items + newItem
        _uiState.update { it.copy(items = updatedItems, inputText = "", suggestions = emptyList()) }
        persistList(updatedItems)
    }

    fun removeItem(itemId: String) {
        val updatedItems = _uiState.value.items.filter { it.id != itemId }
        _uiState.update { it.copy(items = updatedItems) }
        persistList(updatedItems)
    }

    private fun persistList(items: List<ShoppingListItem>) {
        val list = currentList ?: return
        val updated = list.copy(items = items, updatedAt = System.currentTimeMillis())
        currentList = updated
        viewModelScope.launch { saveList(updated) }
    }
}
