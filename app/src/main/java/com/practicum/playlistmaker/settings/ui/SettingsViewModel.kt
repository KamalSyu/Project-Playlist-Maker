package com.practicum.playlistmaker.settings.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.core.contract.GetThemeStateUseCaseContract
import com.practicum.playlistmaker.core.contract.SwitchThemeUseCaseContract
import com.practicum.playlistmaker.core.usecase.UseCaseCreator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val useCaseCreator: UseCaseCreator
) : ViewModel() {

    private val getThemeStateUseCase: GetThemeStateUseCaseContract =
        useCaseCreator.createGetThemeStateUseCase()
    private val switchThemeUseCase: SwitchThemeUseCaseContract =
        useCaseCreator.createSwitchThemeUseCase()

    private val _uiState = MutableLiveData<SettingsUiState>()
    val uiState: LiveData<SettingsUiState> = _uiState

    init {
        loadThemeState()
    }

    private fun loadThemeState() {
        viewModelScope.launch {
            val isDarkTheme = getThemeStateUseCase()
            _uiState.value = SettingsUiState.Loaded(isDarkTheme)
        }
    }

    fun onThemeSwitch(isDarkMode: Boolean) {
        viewModelScope.launch {
            switchThemeUseCase(isDarkMode)
            _uiState.value = SettingsUiState.Loaded(isDarkMode)
        }
    }
}
