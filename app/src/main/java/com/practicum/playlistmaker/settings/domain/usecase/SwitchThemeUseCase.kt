package com.practicum.playlistmaker.settings.domain.usecase

import com.practicum.playlistmaker.core.contract.SwitchThemeUseCaseContract
import com.practicum.playlistmaker.settings.domain.model.ThemeSettings
import com.practicum.playlistmaker.settings.domain.repository.SettingsRepository

/**
 * Use case для переключения темы приложения (тёмная/светлая).
 * Создаёт объект настроек темы с указанным флагом и сохраняет его через репозиторий.
 *
 * @param settingsRepository репозиторий для сохранения настроек темы
 */
class SwitchThemeUseCase (
    private val settingsRepository: SettingsRepository
) : SwitchThemeUseCaseContract {

    /**
     * Переключает тему приложения на указанный режим.
     *
     * @param isDarkMode флаг, определяющий режим темы:
     *   - true — включить тёмную тему;
     *   - false — включить светлую тему.
     */
    override fun invoke(isDarkMode: Boolean) {
        val settings = ThemeSettings(isDarkTheme = isDarkMode)
        settingsRepository.saveTheme(settings)
    }
}