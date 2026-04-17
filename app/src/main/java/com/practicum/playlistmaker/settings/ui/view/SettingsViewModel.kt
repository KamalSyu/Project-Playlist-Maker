package com.practicum.playlistmaker.settings.ui.view

import android.content.Intent
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.core.contract.GetThemeStateUseCaseContract
import com.practicum.playlistmaker.core.contract.SendSupportEmailUseCaseContract
import com.practicum.playlistmaker.core.contract.ShareAppUseCaseContract
import com.practicum.playlistmaker.core.contract.SwitchThemeUseCaseContract
import com.practicum.playlistmaker.settings.ui.SettingsUiState
import kotlinx.coroutines.launch

class SettingsViewModel (
    private val getThemeStateUseCase: GetThemeStateUseCaseContract,
    private val switchThemeUseCase: SwitchThemeUseCaseContract,
    private val shareAppUseCase: ShareAppUseCaseContract,
    private val sendSupportEmailUseCase: SendSupportEmailUseCaseContract
    ) : ViewModel() {

    private val _uiState = MutableLiveData<SettingsUiState>()

    val uiState: LiveData<SettingsUiState> = _uiState

    private val _shareApp = MutableLiveData<Intent>()
    val shareApp: LiveData<Intent> = _shareApp

    private val _sendEmail = MutableLiveData<Intent>()
    val sendEmail: LiveData<Intent> = _sendEmail

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

    fun onShareRequested() {
        val shareText = shareAppUseCase()
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        _shareApp.value = intent
    }

    fun onEmailRequested() {
        val emailData = sendSupportEmailUseCase()
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:".toUri()
            putExtra(Intent.EXTRA_EMAIL, arrayOf(emailData.email))
            putExtra(Intent.EXTRA_SUBJECT, emailData.subject)
            putExtra(Intent.EXTRA_TEXT, emailData.body)
        }
        _sendEmail.value = intent
    }
}