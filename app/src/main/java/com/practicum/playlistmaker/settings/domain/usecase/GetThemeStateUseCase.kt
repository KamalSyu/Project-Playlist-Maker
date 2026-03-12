package com.practicum.playlistmaker.settings.domain.usecase

import com.practicum.playlistmaker.core.contract.GetThemeStateUseCaseContract
import com.practicum.playlistmaker.settings.domain.repository.SettingsRepository

/**
 * Use case для получения текущего состояния темы приложения (тёмная/светлая).
 * Делегирует запрос к репозиторию настроек и извлекает флаг тёмной темы.
 *
 * @param settingsRepository репозиторий для доступа к настройкам темы
 */
class GetThemeStateUseCase (
    private val settingsRepository: SettingsRepository
) : GetThemeStateUseCaseContract {

    /**
     * Выполняет получение текущего состояния темы.
     *
     * @return true, если включена тёмная тема; false — если светлая
     */
    override operator fun invoke(): Boolean {
        return settingsRepository.getThemeSettings().isDarkTheme
    }
}