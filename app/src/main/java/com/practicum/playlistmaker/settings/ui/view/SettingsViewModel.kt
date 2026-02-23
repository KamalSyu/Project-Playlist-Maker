package com.practicum.playlistmaker.settings.ui.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.core.contract.GetThemeStateUseCaseContract
import com.practicum.playlistmaker.core.contract.SwitchThemeUseCaseContract
import com.practicum.playlistmaker.core.usecase.UseCaseCreator
import com.practicum.playlistmaker.settings.ui.SettingsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel для экрана настроек приложения.
 * Управляет состоянием UI, взаимодействует с Use Cases для получения и сохранения настроек темы.
 *
 * @param useCaseCreator фабрика для создания Use Cases бизнес‑логики
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val useCaseCreator: UseCaseCreator
) : ViewModel() {

    // Use Cases для работы с настройками темы
    private val getThemeStateUseCase: GetThemeStateUseCaseContract =
        useCaseCreator.createGetThemeStateUseCase()

    private val switchThemeUseCase: SwitchThemeUseCaseContract =
        useCaseCreator.createSwitchThemeUseCase()

    // Приватное изменяемое состояние UI
    private val _uiState = MutableLiveData<SettingsUiState>()

    /**
     * Публичное неизменяемое состояние UI для наблюдения из Activity.
     * Позволяет Activity реагировать на изменения состояния экрана настроек.
     */
    val uiState: LiveData<SettingsUiState> = _uiState

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
}