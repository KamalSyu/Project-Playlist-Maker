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

/**
 * ViewModel для экрана настроек приложения.
 * Управляет состоянием UI, взаимодействует с Use Cases для получения и сохранения настроек темы.
 */
class SettingsViewModel (
    private val getThemeStateUseCase: GetThemeStateUseCaseContract,
    private val switchThemeUseCase: SwitchThemeUseCaseContract,
    private val shareAppUseCase: ShareAppUseCaseContract,
    private val sendSupportEmailUseCase: SendSupportEmailUseCaseContract
    ) : ViewModel() {

    // Приватное изменяемое состояние UI
    private val _uiState = MutableLiveData<SettingsUiState>()

    /**
     * Публичное неизменяемое состояние UI для наблюдения из Activity.
     * Позволяет Activity реагировать на изменения состояния экрана настроек.
     */
    val uiState: LiveData<SettingsUiState> = _uiState

    // События для UI
    private val _shareApp = MutableLiveData<Intent>()
    val shareApp: LiveData<Intent> = _shareApp

    private val _sendEmail = MutableLiveData<Intent>()
    val sendEmail: LiveData<Intent> = _sendEmail
    /**
     * Инициализация ViewModel: при создании автоматически загружает текущее состояние темы.
     */
    init {
        loadThemeState()
    }

    /**
     * Загружает текущее состояние темы из хранилища.
     * Последовательность действий:
     * 1. Устанавливает состояние UI как Loading (опционально — в текущей реализации не используется).
     * 2. Асинхронно вызывает Use Case для получения текущего состояния темы.
     * 3. Обновляет состояние UI до Loaded с полученным значением isDarkTheme.
     *
     * Это гарантирует, что UI отображает актуальные настройки сразу после открытия экрана.
     */
    private fun loadThemeState() {
        viewModelScope.launch {
            // Получаем текущее состояние темы через Use Case
            val isDarkTheme = getThemeStateUseCase()
            // Обновляем состояние UI — тема успешно загружена
            _uiState.value = SettingsUiState.Loaded(isDarkTheme)
        }
    }

    /**
     * Обрабатывает переключение темы пользователем.
     * Последовательность действий:
     * 1. Асинхронно сохраняет новое состояние темы через SwitchThemeUseCase.
     * 2. Немедленно обновляет состояние UI до Loaded с новым значением isDarkMode.
     *
     * @param isDarkMode флаг, указывающий на режим темы:
     *   - true — включить тёмную тему;
     *   - false — включить светлую тему.
     *
     * Обеспечивает мгновенную обратную связь для пользователя: переключатель темы
     * визуально обновляется сразу, даже если сохранение в хранилище ещё не завершено.
     */
    fun onThemeSwitch(isDarkMode: Boolean) {
        viewModelScope.launch {
            // Сохраняем новое состояние темы в хранилище
            switchThemeUseCase(isDarkMode)
            // Немедленно обновляем состояние UI для отображения изменений
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