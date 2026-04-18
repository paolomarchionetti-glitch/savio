package com.paolomarchionetti.savio.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paolomarchionetti.savio.data.local.datastore.UserPreferencesDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val prefs: UserPreferencesDataStore
) : ViewModel() {

    fun completeOnboarding() {
        viewModelScope.launch {
            prefs.setOnboardingDone()
        }
    }
}
