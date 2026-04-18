package com.paolomarchionetti.savio.feature.storedetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paolomarchionetti.savio.core.navigation.AppDestination
import com.paolomarchionetti.savio.domain.model.Store
import com.paolomarchionetti.savio.domain.repository.StoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoreDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val storeRepository: StoreRepository
) : ViewModel() {

    private val storeId: String = checkNotNull(
        savedStateHandle[AppDestination.StoreDetail.ARG_STORE_ID]
    )

    private val _store = MutableStateFlow<Store?>(null)
    val store: StateFlow<Store?> = _store

    init {
        viewModelScope.launch {
            _store.value = storeRepository.getStoreById(storeId)
        }
    }
}
